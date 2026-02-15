package com.joakimcolloz.stocker.datacollector;

import com.joakimcolloz.stocker.datacollector.data.CryptoDataService;
import com.joakimcolloz.stocker.datacollector.data.IntradayStockDataService;
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
        } else if (args.length > 0 && "EodhdIntraday".equals(args[0])) {
            runIntradayStockCollection(args, databaseManager);
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

    private static void runIntradayStockCollection(String[] args, DatabaseManager databaseManager) {
        String resourceFile = "largecap.txt";
        long days = 120;
        Long fromEpoch = null;
        Long toEpoch = null;
        int bars = 120;
        long maxDaysPerRequest = 600;
        long delayMs = 200;
        Range range = null;
        boolean barsSet = false;
        boolean maxDaysSet = false;

        if (args.length > 1 && !args[1].startsWith("--")) {
            resourceFile = args[1];
        }

        for (String arg : args) {
            if (arg == null) {
                continue;
            }
            if (arg.startsWith("--file=")) {
                resourceFile = arg.substring("--file=".length());
            } else if (arg.startsWith("--days=")) {
                days = Long.parseLong(arg.substring("--days=".length()));
            } else if (arg.startsWith("--from=")) {
                fromEpoch = Long.parseLong(arg.substring("--from=".length()));
            } else if (arg.startsWith("--to=")) {
                toEpoch = Long.parseLong(arg.substring("--to=".length()));
            } else if (arg.startsWith("--bars=")) {
                bars = Integer.parseInt(arg.substring("--bars=".length()));
                barsSet = true;
            } else if (arg.startsWith("--maxdays=")) {
                maxDaysPerRequest = Long.parseLong(arg.substring("--maxdays=".length()));
                maxDaysSet = true;
            } else if (arg.startsWith("--range=")) {
                range = parseRange(arg.substring("--range=".length()));
            } else if (arg.startsWith("--delay=")) {
                delayMs = Long.parseLong(arg.substring("--delay=".length()));
            }
        }

        ArrayList<String> stockList;
        try {
            stockList = StockReader.readStockNamesFromResource(resourceFile);
        } catch (IOException e) {
            logger.error("Failed to read stock names from resource: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        IntradayStockDataService intradayService = new IntradayStockDataService(databaseManager);
        intradayService.setDelayInMs(delayMs);

        if (fromEpoch != null && toEpoch != null) {
            if (barsSet && !maxDaysSet) {
                intradayService.addIntradayPriceDataToDbChunked(stockList, Interval.FIVE_MINUTES, fromEpoch, toEpoch, bars);
            } else {
                intradayService.addIntradayPriceDataToDbChunkedMaxDays(stockList, Interval.FIVE_MINUTES, fromEpoch, toEpoch, maxDaysPerRequest);
            }
        } else if (range != null) {
            intradayService.addIntradayPriceDataToDbRange(stockList, Interval.FIVE_MINUTES, range, maxDaysPerRequest);
        } else {
            if (barsSet && !maxDaysSet) {
                intradayService.addIntradayPriceDataToDbChunkedLastDays(stockList, Interval.FIVE_MINUTES, days, bars);
            } else {
                intradayService.addIntradayPriceDataToDbChunkedLastDaysMaxDays(stockList, Interval.FIVE_MINUTES, days, maxDaysPerRequest);
            }
        }
    }

    private static Range parseRange(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Range cannot be null");
        }

        String normalized = value.trim().toLowerCase();
        return switch (normalized) {
            case "1d" -> Range.ONE_DAY;
            case "5d" -> Range.FIVE_DAY;
            case "1wk" -> Range.ONE_WEEK;
            case "1mo" -> Range.ONE_MONTH;
            case "3mo" -> Range.THREE_MONTHS;
            case "6mo" -> Range.SIX_MONTHS;
            case "1y" -> Range.ONE_YEAR;
            case "2y" -> Range.TWO_YEAR;
            case "5y" -> Range.FIVE_YEARS;
            case "10y" -> Range.TEN_YEARS;
            case "ytd" -> Range.YTD;
            case "max" -> Range.MAX;
            default -> throw new IllegalArgumentException("Invalid range: " + value);
        };
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
