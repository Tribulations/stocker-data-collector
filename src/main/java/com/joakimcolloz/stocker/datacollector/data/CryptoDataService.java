package com.joakimcolloz.stocker.datacollector.data;

import com.joakimcolloz.stocker.datacollector.data.exception.DataFetchException;
import com.joakimcolloz.stocker.datacollector.data.fetchers.CoinbaseFetcher;
import com.joakimcolloz.stocker.datacollector.data.parsers.CoinbaseCandleParser;
import com.joakimcolloz.stocker.datacollector.data.parsers.CoinbaseProductParser;
import com.joakimcolloz.stocker.datacollector.data.validation.CryptoInputValidator;
import com.joakimcolloz.stocker.datacollector.database.CandlestickDao;
import com.joakimcolloz.stocker.datacollector.database.DatabaseManager;
import com.joakimcolloz.stocker.datacollector.model.Candlestick;
import com.joakimcolloz.stocker.datacollector.model.Granularity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Orchestrator for fetching and storing cryptocurrency candlestick data from Coinbase.
 * Mirrors the {@link StockDataService} pattern but tailored for the Coinbase API.
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public class CryptoDataService {
    private static final Logger logger = LoggerFactory.getLogger(CryptoDataService.class);

    private final CoinbaseFetcher fetcher;
    private final CoinbaseCandleParser candleParser;
    private final CoinbaseProductParser productParser;
    private final CryptoInputValidator validator;
    private final DatabaseManager databaseManager;

    private long delayInMs = 200;

    public CryptoDataService(CoinbaseFetcher fetcher,
                             CoinbaseCandleParser candleParser,
                             CoinbaseProductParser productParser,
                             DatabaseManager databaseManager) {
        this.fetcher = fetcher;
        this.candleParser = candleParser;
        this.productParser = productParser;
        this.databaseManager = databaseManager;
        this.validator = new CryptoInputValidator();
        logger.info("CryptoDataService initialized");
    }

    public CryptoDataService(DatabaseManager databaseManager) {
        this(new CoinbaseFetcher(),
             new CoinbaseCandleParser(),
             new CoinbaseProductParser(),
             databaseManager);
    }

    /**
     * Discovers all online trading pairs for the given quote currency.
     *
     * @param quoteCurrency the quote currency to filter by (e.g., "EUR")
     * @return list of product IDs (e.g., ["BTC-EUR", "ETH-EUR", ...])
     * @throws DataFetchException if fetching products fails
     */
    public List<String> discoverProducts(String quoteCurrency) throws DataFetchException {
        logger.info("Discovering {} products from Coinbase...", quoteCurrency);

        List<String> jsonPages = fetcher.fetchAllProducts();
        List<String> productIds = productParser.parseProductIds(jsonPages, quoteCurrency);

        logger.info("Discovered {} {} trading pairs", productIds.size(), quoteCurrency);
        return productIds;
    }

    /**
     * Fetches candle data for each product ID and inserts it into the database.
     * Processing continues on per-symbol failures (same isolation pattern as {@link StockDataService}).
     *
     * @param productIds  list of product IDs to process
     * @param granularity the candle granularity
     * @param startEpoch  start time in seconds since epoch
     * @param endEpoch    end time in seconds since epoch
     */
    public void addPriceDataToDb(List<String> productIds, Granularity granularity,
                                 long startEpoch, long endEpoch) {
        logger.info("Starting addPriceDataToDb with {} products, granularity: {}, range: [{} - {}]",
                productIds.size(), granularity, startEpoch, endEpoch);

        validator.validateProductIdList(productIds);
        validator.validateGranularity(granularity);
        validator.validateTimeRange(startEpoch, endEpoch);

        CandlestickDao candlestickDao = databaseManager.createCandlestickDao();
        int successCount = 0;
        int failureCount = 0;

        for (String productId : productIds) {
            logger.debug("Processing product: {}", productId);

            try {
                // Fetch all candle chunks
                List<String> jsonResponses = fetcher.fetchAllCandles(productId, startEpoch, endEpoch, granularity);

                // Parse and merge
                List<Candlestick> candlesticks = candleParser.parseAllCandles(jsonResponses);

                if (candlesticks.isEmpty()) {
                    logger.warn("No candlesticks parsed for product: {} - skipping", productId);
                    failureCount++;
                    continue;
                }

                logger.debug("Parsed {} candlesticks for product: {}", candlesticks.size(), productId);

                // Insert into database
                candlestickDao.addRows(productId, candlesticks);
                logger.info("Successfully added {} candlesticks for product: {}",
                        candlesticks.size(), productId);
                successCount++;

            } catch (DataFetchException e) {
                logger.error("Failed to fetch data for product {}: {}", productId, e.getMessage(), e);
                failureCount++;
            } catch (Exception e) {
                logger.error("Unexpected error processing product {}: {}", productId, e.getMessage(), e);
                failureCount++;
            }

            // Delay before next product
            try {
                Thread.sleep(delayInMs);
            } catch (InterruptedException e) {
                logger.warn("Thread interrupted while waiting to process next product", e);
                Thread.currentThread().interrupt();
            }
        }

        // Log summary
        logger.info("Completed processing {} products. Success: {}, Failure: {}",
                productIds.size(), successCount, failureCount);

        if (failureCount > 0) {
            logger.warn("Processing completed with {} failures out of {} total products ({}% success rate)",
                    failureCount, productIds.size(),
                    Math.round((double) successCount / productIds.size() * 100));
        } else {
            logger.info("All {} products processed successfully (100% success rate)", productIds.size());
        }
    }

    public void setDelayInMs(long delayInMs) {
        this.delayInMs = delayInMs;
    }

    public long getDelayInMs() {
        return delayInMs;
    }
}
