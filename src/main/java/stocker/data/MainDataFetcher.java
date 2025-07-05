package stocker.data;

import stocker.data.fetchers.wJson.YahooFinanceFetcher;
import stocker.data.parsers.YahooFinanceParser;
import stocker.database.CandlestickDao;
import stocker.representation.Candlestick;
import stocker.representation.Stock;
import stocker.representation.TradingPeriod;
import stocker.support.StockAppLogger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static stocker.data.fetchers.wJson.JsonConstants.*;

/**
 * Class used to fetch data for many days for all stocks and add to db.
 * The method {@link #init()} has to be explicitly called in order to be
 * able to use the methods of this class.
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public class MainDataFetcher {
    private static final String MARKET_SUFFIX_SWE = ".ST";

    private final List<String> stockSymbols = new ArrayList<>();
    private List<String> fileNames;

    /**
     * Public constructor calling internal method {@link #initStockSymbolNames()} to initialize member field
     * {@link #stockSymbols} storing the names/symbols of the stocks for which price data should be retrieved.
     */
    public MainDataFetcher() {
    }

    /**
     * Initializes the necessary file paths and stock symbol names.
     * <p>
     * This method sets up a list of file paths for different stock lists and
     * initializes stock symbol names. It should be called before any other methods
     * to ensure that the required resources are properly configured.
     * <p>
     * For better practice, consider calling this method directly from the constructor.
     * However, it is currently designed to be called explicitly during testing or development.
     * </p>
     *
     * @see #initStockSymbolNames()
     */
    public void init() {
        fileNames = Arrays.asList("src/main/resources/LargeCap.txt", // TODO use constants
                "src/main/resources/MidCap.txt", "src/main/resources/SmallCap.txt");
        initStockSymbolNames();
    }

    /**
     * TODO maybe we should remove the -PREF, -A stocks etc. directly when we scrape these names and just save the needed symbol names?
     * TODO As for now we have to remove the unneeded stock names everytime in this method which is unnecessary!
     */
    private void initStockSymbolNames() {
        // read file/s containing stock symbols
        // add only the wanted ones i.e. we don't want pref, a or c stock etc. to list.
        for (String fileName : fileNames) {
            try {
                FileReader fileReader = new FileReader(fileName); // todo close file readers!
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                while (bufferedReader.ready()) {
                    final String[] readLine = bufferedReader.readLine().split(",");
                    final String symbol = readLine[2];
                    boolean addSymbol = !(symbol.contains("-PREF") || symbol.contains("-A") || symbol.contains("-D"));
                    if (addSymbol) {
                        stockSymbols.add(symbol);
                    }
                }
            } catch (IOException e) {
                StockAppLogger.INSTANCE.logInfo(e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println(stockSymbols.size()); // debug
    }

    /**
     * Not used in version 1.0
     */
    private void addLatest1dPriceDataToDb() {
        List<Stock> stocks = new ArrayList<>();
        CandlestickDao candlestickDao = new CandlestickDao();
        final String marketSuffix = ".ST";// TODO use constant

        for (String symbol : stockSymbols) {
            Stock stock = new Stock(symbol + marketSuffix, ONE_DAY, ONE_DAY);
            stocks.add(stock); // todo maybe only use a Stock as parameter to addRow()/s
            candlestickDao.addRows(stock.getSymbol(), stock.getTradingPeriod().getCandlesticks());
        }
    }

    /**
     * This method is not used in version 1.0
     *
     * @param range the range of the price data
     */
    private void addMultipleOlder1dPriceDataToDb(final String range) {
        CandlestickDao candlestickDao = new CandlestickDao();
        final String marketSuffix = ".ST";// TODO use constant

        for (String symbol : stockSymbols) {
            Stock stock = new Stock(symbol + marketSuffix, range, ONE_DAY);
            candlestickDao.addMultipleOlderRows(stock.getSymbol(), stock.getTradingPeriod().getCandlesticks());
        }
    }

    /**
     * Adds historical stock data to the database for multiple stocks. This method is intended
     * for initially populating a stock price table with historical price data, excluding the
     * current trading day if the stock market is open.
     *
     * The method processes the stock symbols specified by the parameter `stockSymbols`.
     * Only stocks traded on the Swedish stock market (i.e., stocks with the market suffix .ST)
     * are considered.
     *
     * @param stockSymbols a list of stock symbols to be added to the database
     */
    public void addHistoricalStockDataToDb(List<String> stockSymbols) {
        CandlestickDao candlestickDao = new CandlestickDao();
        final String marketSuffix = ".ST";

        for (String symbol : stockSymbols) {
            Stock stock = new Stock(
                    symbol + marketSuffix, Stock.Range.ONE_MONTH, Stock.Interval.ONE_DAY, true);
            candlestickDao.addRows(stock.getSymbol(), stock.getTradingPeriod().getCandlesticks());
        }
    }

    /**
     * Adds stock data for the current trading day to the database for the specified stocks.
     * <p>
     * This method retrieves stock data for the current trading day for each stock symbol provided in the
     * {@code stockSymbols} list and saves the data to the database. Only stocks with the Swedish market suffix
     * (i.e., ".ST") are processed.
     * </p>
     * <p>
     * This method uses the {@link CandlestickDao} to handle database operations.
     * </p>
     *
     * @param stockSymbols a list of stock symbols for which to add current day's data to the database
     */
    public void addCurrentDaysStockDataToDb(List<String> stockSymbols) {
        CandlestickDao candlestickDao = new CandlestickDao();
        final String marketSuffix = ".ST";

        for (String symbol : stockSymbols) {
            Stock stock = new Stock(
                    symbol + marketSuffix, Stock.Range.ONE_DAY, Stock.Interval.ONE_DAY);
            candlestickDao.addRows(stock.getSymbol(), stock.getTradingPeriod().getCandlesticks());
        }
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
     */
    public void addPriceDataToDb(List<String> stockSymbols, Range range, Interval interval) {
        CandlestickDao candlestickDao = new CandlestickDao();
        final String marketSuffix = ".ST";
        final boolean skipCurrentDayPriceData = range != Range.ONE_DAY;

        for (String symbol : stockSymbols) {
            // Fetch data
            final String json = YahooFinanceFetcher.INSTANCE.fetchData(symbol + marketSuffix, range.toString(), interval.toString());

            // Parse data
            final YahooFinanceParser yahooFinanceParser = new YahooFinanceParser(json);
            yahooFinanceParser.parse();

            // Get parsed data as TradingPeriod
            TradingPeriod tradingPeriod = yahooFinanceParser.getTradingPeriod();

            if (skipCurrentDayPriceData) {
                tradingPeriod.removeLast();
            }

            // Add data to database
            candlestickDao.addRows(symbol + marketSuffix, tradingPeriod.getCandlesticks());
        }
    }

    /**
     * Adds stock data to the database for the specified stocks, covering the range from the current trading day
     * back to the specified historical period.
     * <p>
     * This method retrieves stock data for each stock symbol provided in the {@code stockSymbols} list. The data
     * includes price information from the current trading day extending back to the historical range specified.
     * The range defines the total number of price data points to fetch, such as data for a whole month or ten years,
     * while the interval specifies the duration of each distinct candlestick or price data point (e.g., daily, hourly).
     * </p>
     * <p>
     * Use {@link Stock.Range#ONE_DAY} or a similar predefined enum for specifying the range.
     * Use {@link Stock.Interval#ONE_DAY} or a similar predefined enum for specifying the interval.
     * This method utilizes the {@link CandlestickDao} to handle database operations.
     * </p>
     *
     * @param stockSymbols a list of stock symbols for which to add the historical data to the database
     * @param range the total historical range of data to fetch, represented by an enum value such as a whole month or ten years
     * @param interval the duration of each distinct candlestick or price data point, represented by an enum value such as daily or hourly
     * @param marketSuffix the market suffix for filtering stock symbols, e.g., ".ST" for the Swedish market
     */
    public void addStockDataToDb(List<String> stockSymbols, String range, String interval, String marketSuffix) {
        CandlestickDao candlestickDao = new CandlestickDao();

        for (String symbol : stockSymbols) {
            Stock stock = new Stock(
                    symbol + marketSuffix, range, interval);
            candlestickDao.addRows(stock.getSymbol(), stock.getTradingPeriod().getCandlesticks());
        }
    }

    public static void main(String... args) {
        MainDataFetcher mainDataFetcher = new MainDataFetcher();
        mainDataFetcher.addHistoricalStockDataToDb(List.of("AAK", "ABB"));
//        mainDataFetcher.addCurrentDaysStockDataToDb(List.of("AAK", "ABB"));
    }

    // valid ranges
    public enum Range {
        ONE_DAY,
        FIVE_DAY,
        ONE_WEEK,
        ONE_MONTH,
        THREE_MONTHS,
        SIX_MONTHS,
        ONE_YEAR,
        TWO_YEAR,
        FIVE_YEARS,
        TEN_YEARS,
        YTD,
        MAX;


        @Override
        public String toString() {
            return switch (this) {
                case ONE_DAY -> "1d";
                case FIVE_DAY -> "5d";
                case ONE_WEEK -> "1wk";
                case ONE_MONTH -> "1mo";
                case THREE_MONTHS -> "3mo";
                case SIX_MONTHS -> "6mo";
                case ONE_YEAR -> "1y";
                case TWO_YEAR -> "2y";
                case FIVE_YEARS -> "5y";
                case TEN_YEARS -> "10y";
                case YTD -> "ytd";
                case MAX -> "max";
            };
        }
    }

    // valid intervals
    public enum Interval {
        ONE_MINUTE,
        FIVE_MINUTES,
        FIFTEEN_MINUTES,
        ONE_HOUR,
        ONE_WEEK,
        ONE_DAY,
        FIVE_DAYS,
        ONE_MONTH,
        THREE_MONTHS,
        SIX_MONTHS,
        ONE_YEAR,
        TWO_YEAR,
        FIVE_YEARS,
        TEN_YEARS,
        YTD,
        MAX;

        @Override
        public String toString() {
            return switch (this) {
                case ONE_MINUTE -> "1m";
                case FIVE_MINUTES -> "5m";
                case FIFTEEN_MINUTES -> "15m";
                case ONE_HOUR -> "1h";
                case ONE_WEEK -> "1wk";
                case ONE_DAY -> "1d";
                case FIVE_DAYS -> "5d";
                case ONE_MONTH -> "1mo";
                case THREE_MONTHS -> "3mo";
                case SIX_MONTHS -> "6mo";
                case ONE_YEAR -> "1y";
                case TWO_YEAR -> "2y";
                case FIVE_YEARS -> "5y";
                case TEN_YEARS -> "10y";
                case YTD -> "ytd";
                case MAX -> "max";
            };
        }
    }
}
