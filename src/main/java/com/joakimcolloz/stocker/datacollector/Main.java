package com.joakimcolloz.stocker.datacollector;

import com.joakimcolloz.stocker.datacollector.data.CryptoDataService;
import com.joakimcolloz.stocker.datacollector.data.StockDataService;
import com.joakimcolloz.stocker.datacollector.data.exception.DataFetchException;
import com.joakimcolloz.stocker.datacollector.data.fetchers.FinanceBirdFetcher;
import com.joakimcolloz.stocker.datacollector.data.fetchers.YahooFinanceFetcher;
import com.joakimcolloz.stocker.datacollector.data.parsers.FinanceBirdParser;
import com.joakimcolloz.stocker.datacollector.data.parsers.YahooFinanceParser;
import com.joakimcolloz.stocker.datacollector.database.DatabaseConfig;
import com.joakimcolloz.stocker.datacollector.database.DatabaseManager;
import com.joakimcolloz.stocker.datacollector.model.Granularity;
import com.joakimcolloz.stocker.datacollector.model.Interval;
import com.joakimcolloz.stocker.datacollector.model.Range;
import com.joakimcolloz.stocker.datacollector.utils.StockReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Main class of the program.
 *
 * @author Joakim Colloz
 * @version 1.1
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String... args) {
        final DatabaseManager databaseManager = new DatabaseManager(new DatabaseConfig());
        databaseManager.initialize();

        if (args.length > 0 && "Coinbase".equals(args[0])) {
            runCryptoCollection(databaseManager);
        } else {
            runStockCollection(args, databaseManager);
        }
    }

    private static void runStockCollection(String[] args, DatabaseManager databaseManager) {
        ArrayList<String> stockList;

        final StockDataService stockDataService = createStockDataService(args);
        stockDataService.setDelayInMs(200);

        try {
            stockList = StockReader.readStockNamesFromResource("largecap.txt");
        } catch (IOException e) {
            logger.error("Failed to read stock names from resource: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        stockDataService.addPriceDataToDb(stockList, Range.THREE_MONTHS, Interval.ONE_DAY);
    }

    private static void runCryptoCollection(DatabaseManager databaseManager) {
        logger.info("Starting Coinbase cryptocurrency data collection");

        CryptoDataService cryptoDataService = new CryptoDataService(databaseManager);
        cryptoDataService.setDelayInMs(200);

        try {
            List<String> productIds = cryptoDataService.discoverProducts("EUR");
            logger.info("Discovered {} EUR trading pairs", productIds.size());

            long endEpoch = Instant.now().getEpochSecond();
            long startEpoch = Instant.now().minus(365, ChronoUnit.DAYS).getEpochSecond();

            cryptoDataService.addPriceDataToDb(productIds, Granularity.ONE_DAY, startEpoch, endEpoch);
        } catch (DataFetchException e) {
            logger.error("Failed to collect cryptocurrency data: {}", e.getMessage(), e);
            throw new RuntimeException("Cryptocurrency data collection failed", e);
        }
    }

    private static StockDataService createStockDataService(String... args) {
        if (args.length == 0) {
            logger.info("Using FinanceBird as default to fetch data");
            return new StockDataService(
                    FinanceBirdParser::new,
                    new FinanceBirdFetcher()
            );
        } else {
            final String apiToUse = args[0];
            switch (apiToUse) {
                case "FinanceBird" -> {
                    logger.info("Using FinanceBird to fetch data");
                    return new StockDataService(
                        FinanceBirdParser::new,
                        new FinanceBirdFetcher()
                    );
                }
                case "YahooFinance" -> {
                    logger.info("Using YahooFinance to fetch data");
                    return new StockDataService(
                        YahooFinanceParser::new,
                        new YahooFinanceFetcher()
                    );
                }
                default -> throw new IllegalArgumentException("Invalid API: " + apiToUse);
            }
        }
    }
}
