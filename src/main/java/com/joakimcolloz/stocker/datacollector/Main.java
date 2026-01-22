package com.joakimcolloz.stocker.datacollector;

import com.joakimcolloz.stocker.datacollector.data.StockDataService;
import com.joakimcolloz.stocker.datacollector.data.fetchers.FinanceBirdFetcher;
import com.joakimcolloz.stocker.datacollector.data.fetchers.YahooFinanceFetcher;
import com.joakimcolloz.stocker.datacollector.data.parsers.FinanceBirdParser;
import com.joakimcolloz.stocker.datacollector.data.parsers.YahooFinanceParser;
import com.joakimcolloz.stocker.datacollector.database.DatabaseConfig;
import com.joakimcolloz.stocker.datacollector.database.DatabaseManager;
import com.joakimcolloz.stocker.datacollector.model.Interval;
import com.joakimcolloz.stocker.datacollector.model.Range;
import com.joakimcolloz.stocker.datacollector.utils.StockReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;

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
