package com.joakimcolloz.stocker.datacollector.data;

import com.joakimcolloz.stocker.datacollector.data.exception.DataFetchException;
import com.joakimcolloz.stocker.datacollector.data.fetchers.BaseDataFetcher;
import com.joakimcolloz.stocker.datacollector.data.parsers.BaseParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.joakimcolloz.stocker.datacollector.data.validation.DataFetcherInputValidator;
import com.joakimcolloz.stocker.datacollector.database.CandlestickDao;
import com.joakimcolloz.stocker.datacollector.database.DatabaseConfig;
import com.joakimcolloz.stocker.datacollector.database.DatabaseManager;
import com.joakimcolloz.stocker.datacollector.model.Interval;
import com.joakimcolloz.stocker.datacollector.model.Range;
import com.joakimcolloz.stocker.datacollector.model.TradingPeriod;

import java.util.List;
import java.util.function.Supplier;

/**
 * Changelog:
 *  1.0 - Used YahooFinance
 *  1.1 - Used FinanceBird
 *  1.2 - Added support for different data fetchers and parsers
 *  1.3 - Added delay between fetching data for each stock symbol
 * @author Joakim Colloz
 * @version 1.3
 */
public class StockDataService {
    private static final Logger logger = LoggerFactory.getLogger(StockDataService.class);
    private static final String MARKET_SUFFIX_SWE = ".ST";

    private final DataFetcherInputValidator validator;
    private final DatabaseManager databaseManager;
    private final Supplier<BaseParser> baseParser;
    private final BaseDataFetcher fetcher;

    private long DELAY_IN_MS = 100;

    public StockDataService(Supplier<BaseParser> baseParser, BaseDataFetcher fetcher) {
        this.baseParser = baseParser;
        this.fetcher = fetcher;
        this.validator = new DataFetcherInputValidator();
        this.databaseManager = new DatabaseManager(new DatabaseConfig());
        logger.info("StockDataService initialized with default validator and database manager.");
        logInitialization(baseParser, fetcher, validator, databaseManager);
    }

    public StockDataService(
            Supplier<BaseParser> baseParser,
            BaseDataFetcher fetcher,
            DataFetcherInputValidator validator,
            DatabaseManager databaseManager)
    {
        this.baseParser = baseParser;
        this.fetcher = fetcher;
        this.validator = validator;
        this.databaseManager = databaseManager;
        logger.info("StockDataService initialized");
        logInitialization(baseParser, fetcher, validator, databaseManager);
    }

    /**
     * Retrieves and inserts price data into the database for each stock symbol in the given {@code stockSymbols} list.
     * The latest trading day is only included if the range is set to {@link Range#ONE_DAY}.
     * <p>
     * Only stock symbols that belong to the Swedish market (i.e., symbols ending with ".ST") are processed.
     * </p>
     * <p>
     * The data is fetched using the {@link BaseDataFetcher} and parsed using the {@link BaseParser}.
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

        validate(stockSymbols);

        CandlestickDao candlestickDao = databaseManager.createCandlestickDao();
        int successCount = 0;
        int failureCount = 0;

        logger.info("Starting to fetch and process data for {} stock symbols with range {} and interval {}",
                stockSymbols.size(), range, interval);

        for (String symbol : stockSymbols) {
            logger.debug("Starting processing for symbol: {}", symbol);
            try {
                String fullSymbol = symbol + MARKET_SUFFIX_SWE;
                logger.debug("Processing symbol: {} (full: {})", symbol, fullSymbol);

                // Fetch data
                final String jsonResponse = fetchData(range, interval, fullSymbol);

                // Parse data
                TradingPeriod tradingPeriod;
                ParsingResult parsingResult = parseResponse(jsonResponse, fullSymbol, failureCount);
                if (parsingResult.parsedSuccessfully) {
                    tradingPeriod = parsingResult.tradingPeriod;
                    failureCount = parsingResult.failureCount;
                } else {
                    continue;
                }

                // Validate trading period
                if (isInvalidTradingPeriod(tradingPeriod)) {
                    logger.warn("No candlesticks available for symbol: {} - trading period is null or empty", fullSymbol);
                    failureCount++;
                    continue;
                }

                // Log number of candlesticks retrieved
                int originalCandlestickCount = tradingPeriod.candlesticks().size();
                logger.debug("Retrieved {} candlesticks for symbol: {}", originalCandlestickCount, fullSymbol);

                if (tradingPeriod.candlesticks().isEmpty()) {
                    logger.warn("No candlesticks left after processing for symbol: {} - skipping database insertion", fullSymbol);
                    failureCount++;
                    continue;
                }

                // Add data to database
                try {
                    insertToDatabase(tradingPeriod, fullSymbol, candlestickDao);
                    successCount++;
                } catch (Exception e) { // TODO; Should be more specific here and catch RuntimeException instead so we do not catch programming errors such as NullPointerException
                    // TODO: Here we catch expected database failures (validation, connection issues)
                    logger.error("Error adding candlesticks to database for symbol {}: {}", fullSymbol, e.getMessage(), e);
                    failureCount++;
                }

            } catch (IllegalArgumentException e) {
                logger.error("Validation error for symbol {}: {}", symbol, e.getMessage());
                failureCount++;
            } catch (Exception e) {
                // TODO: and here we should catch RuntimeException again as these are unexpected runtime errors and these might be bugs!
                logger.error("Unexpected error processing symbol {}: {}", symbol, e.getMessage(), e);
                failureCount++;
            }

            logger.debug("Completed processing for symbol: {} (success: {})",
                    symbol, successCount > (successCount + failureCount - stockSymbols.size() + 1) ? "true" : "false");

            // Delay before fetching data for the next stock symbol
            try {
                Thread.sleep(DELAY_IN_MS);
            } catch (InterruptedException e) {
                logger.warn("Thread interrupted while waiting to fetch next symbol", e);
            }
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

    public long getDelayInMs() {
        return DELAY_IN_MS;
    }

    public void setDelayInMs(long DELAY_IN_MS) {
        this.DELAY_IN_MS = DELAY_IN_MS;
    }

    private static void insertToDatabase(TradingPeriod tradingPeriod, String fullSymbol, CandlestickDao candlestickDao) {
        logger.debug("Inserting {} candlesticks into database for symbol: {}",
                tradingPeriod.candlesticks().size(), fullSymbol);
        candlestickDao.addRows(fullSymbol, tradingPeriod.candlesticks());
        logger.info("Successfully added {} candlesticks for symbol: {}",
                tradingPeriod.candlesticks().size(), fullSymbol);
    }

    private boolean isInvalidTradingPeriod(TradingPeriod tradingPeriod) {
        return tradingPeriod == null
                || tradingPeriod.candlesticks() == null
                || tradingPeriod.candlesticks().isEmpty();
    }

    private String fetchData(Range range, Interval interval, String fullSymbol) throws DataFetchException {
        logger.debug("Fetching data for symbol: {}", fullSymbol);
        final String json = fetcher.fetchData(
                fullSymbol, range.toString(), interval.toString());

        logger.debug("Received {} characters of JSON data for symbol: {}",
                json.length(), fullSymbol);
        return json;
    }

    private void validate(List<String> stockSymbols) {
        try {
            validator.validateStockSymbolsList(stockSymbols);
            logger.debug("Input validation completed successfully");
        } catch (IllegalArgumentException e) {
            logger.error("Input validation failed: {}", e.getMessage());
            throw e;
        }
    }

    private void logInitialization(Supplier<BaseParser> baseParser, BaseDataFetcher fetcher, DataFetcherInputValidator validator, DatabaseManager databaseManager) {
        logger.info("Parser: {}", baseParser);
        logger.info("Fetcher: {}", fetcher);
        logger.info("Validator: {}", validator);
        logger.info("DatabaseManager: {}", databaseManager);
    }

    private ParsingResult parseResponse(String jsonResponse, String fullSymbol, int failureCount) {
        TradingPeriod tradingPeriod = null;
        boolean parsedSuccessfully = false;

        logger.info("Parsing JSON data for symbol: {}", fullSymbol);
        try (BaseParser parser = baseParser.get()) {
            parser.setJsonString(jsonResponse);
            parser.parse();
            logger.info("JSON parsing completed for symbol: {}", fullSymbol);
            tradingPeriod = parser.getTradingPeriod();
            parsedSuccessfully = true;
        } catch (Exception e) { // TODO should catch JsonParseException | IOException  instead?
            // TODO Here we catch specific parsing errors/expected business failures
            logger.error("Failed to parse JSON data for symbol {}: {}", fullSymbol, e.getMessage(), e);
            failureCount++;
        }

        return new ParsingResult(tradingPeriod, parsedSuccessfully, failureCount);
    }

    private record ParsingResult(TradingPeriod tradingPeriod, boolean parsedSuccessfully, int failureCount) {}
}
