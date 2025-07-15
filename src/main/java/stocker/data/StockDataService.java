package stocker.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stocker.data.fetchers.YahooFinanceFetcher;
import stocker.data.parsers.YahooFinanceParser;
import stocker.data.validation.DataFetcherInputValidator;
import stocker.database.CandlestickDao;
import stocker.model.Interval;
import stocker.model.Range;
import stocker.model.TradingPeriod;

import java.util.List;

/**
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public class StockDataService {
    private static final Logger logger = LoggerFactory.getLogger(StockDataService.class);
    private static final String MARKET_SUFFIX_SWE = ".ST";

    private final DataFetcherInputValidator validator;

    public StockDataService() {
        this.validator = new DataFetcherInputValidator();
        logger.debug("MainDataFetcher initialized");
    }

    /**
     * Retrieves and inserts price data into the database for each stock symbol in the given {@code stockSymbols} list.
     * The latest trading day is only included if the range is set to {@link Range#ONE_DAY}.
     * <p>
     * Only stock symbols that belong to the Swedish market (i.e., symbols ending with ".ST") are processed.
     * </p>
     * <p>
     * This method uses {@link YahooFinanceFetcher} to retrieve the data and {@link YahooFinanceParser} to parse it.
     * </p>
     *
     * @param stockSymbols the list of stock symbols to process
     * @param range        the price date range to fetch and insert data for. See {@link Range} for options
     * @param interval     the interval between data points. See {@link Interval} for options
     * @throws IllegalArgumentException if stockSymbols is null, or range is null, or interval is null
     */
    public void addPriceDataToDb(List<String> stockSymbols, Range range, Interval interval) {
        logger.info("Starting addPriceDataToDb with {} symbols, range: {}, interval: {}",
                stockSymbols != null ? stockSymbols.size() : 0, range, interval);

        // Validate input parameters using validator
        try {
            validator.validateStockSymbolsList(stockSymbols);
            logger.debug("Input validation completed successfully");
        } catch (IllegalArgumentException e) {
            logger.error("Input validation failed: {}", e.getMessage());
            throw e;
        }

        CandlestickDao candlestickDao = new CandlestickDao();
        final boolean skipCurrentDayPriceData = range != Range.ONE_DAY;
        int successCount = 0;
        int failureCount = 0;

        logger.info("Starting to fetch and process data for {} stock symbols with range {} and interval {}",
                stockSymbols.size(), range, interval);
        logger.debug("Skip current day data: {}", skipCurrentDayPriceData);

        for (String symbol : stockSymbols) {
            logger.debug("Starting processing for symbol: {}", symbol);
            try {
                String fullSymbol = symbol + MARKET_SUFFIX_SWE;
                logger.debug("Processing symbol: {} (full: {})", symbol, fullSymbol);

                // Fetch data
                logger.debug("Fetching data for symbol: {}", fullSymbol);
                final String json = YahooFinanceFetcher.INSTANCE.fetchData(
                        fullSymbol, range.toString(), interval.toString());

                logger.debug("Received {} characters of JSON data for symbol: {}",
                        json.length(), fullSymbol);

                // Parse data
                logger.debug("Parsing JSON data for symbol: {}", fullSymbol);
                final YahooFinanceParser yahooFinanceParser = new YahooFinanceParser(json);
                try {
                    yahooFinanceParser.parse();
                    logger.debug("JSON parsing completed for symbol: {}", fullSymbol);
                } catch (Exception e) {
                    logger.error("Failed to parse JSON data for symbol {}: {}", fullSymbol, e.getMessage(), e);
                    failureCount++;
                    continue;
                }

                // Get parsed data as TradingPeriod
                TradingPeriod tradingPeriod = yahooFinanceParser.getTradingPeriod();
                if (tradingPeriod == null || tradingPeriod.candlesticks() == null
                        || tradingPeriod.candlesticks().isEmpty()) {
                    logger.warn("No candlesticks available for symbol: {} - trading period is null or empty", fullSymbol);
                    failureCount++;
                    continue;
                }

                int originalCandlestickCount = tradingPeriod.candlesticks().size();
                logger.debug("Retrieved {} candlesticks for symbol: {}", originalCandlestickCount, fullSymbol);

                // Remove the last day's data if needed
                if (skipCurrentDayPriceData && !tradingPeriod.candlesticks().isEmpty()) {
                    tradingPeriod.removeLast();
                    logger.debug("Removed last candlestick (current day) for symbol: {} - {} candlesticks remaining",
                            fullSymbol, tradingPeriod.candlesticks().size());
                }

                if (tradingPeriod.candlesticks().isEmpty()) {
                    logger.warn("No candlesticks left after processing for symbol: {} - skipping database insertion", fullSymbol);
                    failureCount++;
                    continue;
                }

                // Add data to database
                try {
                    logger.debug("Inserting {} candlesticks into database for symbol: {}",
                            tradingPeriod.candlesticks().size(), fullSymbol);
                    candlestickDao.addRows(fullSymbol, tradingPeriod.candlesticks());
                    logger.info("Successfully added {} candlesticks for symbol: {}",
                            tradingPeriod.candlesticks().size(), fullSymbol);
                    successCount++;
                } catch (Exception e) {
                    logger.error("Error adding candlesticks to database for symbol {}: {}",
                            fullSymbol, e.getMessage(), e);
                    failureCount++;
                }
            } catch (IllegalArgumentException e) {
                logger.error("Validation error for symbol {}: {}", symbol, e.getMessage());
                failureCount++;
            } catch (Exception e) {
                logger.error("Unexpected error processing symbol {}: {}", symbol, e.getMessage(), e);
                failureCount++;
            }
            logger.debug("Completed processing for symbol: {} (success: {})",
                    symbol, successCount > (successCount + failureCount - stockSymbols.size() + 1) ? "true" : "false");
        }

        // Log summary of operation
        logger.info("Completed processing {} stock symbols. Success: {}, Failure: {}",
                stockSymbols.size(), successCount, failureCount);

        if (failureCount > 0) {
            logger.warn("Processing completed with {} failures out of {} total symbols ({}% success rate)",
                    failureCount, stockSymbols.size(),
                    Math.round((double) successCount / stockSymbols.size() * 100));
        } else {
            logger.info("All {} symbols processed successfully (100% success rate)", stockSymbols.size());
        }
    }
}
