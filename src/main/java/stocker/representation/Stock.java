package stocker.representation;

import stocker.data.fetchers.wJson.YahooFinanceFetcher;
import stocker.data.parsers.YahooFinanceParser;
import stocker.support.StockAppLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class representing a stock and provides different functionalities to get info about a stock.
 *
 * @author Joakim Colloz
 * @version 1.0
 * @since 1.0
 */
public class Stock {
    private final String symbol;
    private final TradingPeriod tradingPeriod;
    // TODO maybe have a map of trading periods? The interval can be the key?
    private final Map<String, TradingPeriod> tradingPeriodMap = new HashMap<>();

    /**
     * Create a Stock by passing its name and a TradingPeriod object.
     * @param symbol the symbol/name of the stock
     * @param tradingPeriod the trading period this Stock should have
     */
    public Stock(String symbol, TradingPeriod tradingPeriod) {
        this.symbol = symbol;
        this.tradingPeriod = tradingPeriod; /* todo having a TradingPeriod class is weird??
         Should just be candlesticks? Or maybe not beacuase a trading period can be of the same period
         but with different intervals. So we might have two 6 month trading periods where one has a one
         day iterval and the other one hour interval. This way maybe we should store the interval as a
         member in TradingPeriod? */
        this.tradingPeriodMap.put(tradingPeriod.getINTERVAL(), tradingPeriod);
    }

    /**
     * Creates a Stock object with specified parameters.
     *
     * This constructor initializes a Stock object by fetching and parsing data from Yahoo Finance
     * based on the provided symbol, range, and interval.
     *
     * @param symbol The ticker symbol (name of the stock).
     * @param range The time range for the trading period (e.g., "1d", "1mo", "6mo", "1y").
     * @param interval The time interval between each candlestick (e.g., "1m", "5m", "15m", "1h", "1d").
     */
    public Stock(String symbol, final String range, final String interval) {
        this.symbol = symbol;
        final YahooFinanceParser yahooFinanceParser = new YahooFinanceParser(
                YahooFinanceFetcher.INSTANCE.fetchData(symbol, range, interval));
        yahooFinanceParser.parse();
        this.tradingPeriod = yahooFinanceParser.getTradingPeriod();
    }

    /**
     * Creates a Stock object with specified parameters.
     *
     * This constructor initializes a Stock object by fetching and parsing data from Yahoo Finance
     * based on the provided symbol, range, and interval. It also offers an option to exclude the
     * current day's price data.
     *
     * @param symbol The ticker symbol (name of the stock).
     * @param range The time range for the trading period (e.g., "1d", "1mo", "6mo", "1y").
     * @param interval The time interval between each candlestick (e.g., "1m", "5m", "15m", "1h", "1d").
     * @param skipCurrentDayPriceData If true, excludes the current day's price data from the results.
     *                                This is recommended when fetching data during an ongoing trading day
     *                                to prevent duplicate entries for the current session.
     */
    public Stock(String symbol, final String range, final String interval, final boolean skipCurrentDayPriceData) {
        this.symbol = symbol;
        final YahooFinanceParser yahooFinanceParser = new YahooFinanceParser(
                YahooFinanceFetcher.INSTANCE.fetchData(symbol, range, interval));
        yahooFinanceParser.parse();

        if (skipCurrentDayPriceData) {
            removeLatestCandlestick(yahooFinanceParser.getTradingPeriod().getCandlesticks());
        }
        this.tradingPeriod = yahooFinanceParser.getTradingPeriod();
    }

    private static void removeLatestCandlestick(List<Candlestick> candlesticks) {
        Candlestick currentDayCandlestick = candlesticks.get(candlesticks.size() - 1);
        StockAppLogger.INSTANCE.logInfo("Inside Stock constructor");
        StockAppLogger.INSTANCE.logInfo("skipCurrentDayPriceData is set to true");
        StockAppLogger.INSTANCE.logInfo(String.format(
                "About to remove price data/candletick for trading session: %s",
                currentDayCandlestick.getHumanReadableDate()));
        candlesticks.remove(currentDayCandlestick);
    }

    public String getSymbol() {
        return symbol;
    }

    public TradingPeriod getTradingPeriod() {
        return tradingPeriod;
    }

    // valid ranges
    public class Range {
        public static final String ONE_DAY = "1d";
        public static final String ONE_WEEK = "1wk";
        public static final String FIVE_DAY = "5d";
        public static final String ONE_MONTH = "1mo";
        public static final String THREE_MONTHS = "3mo";
        public static final String SIX_MONTHS = "6mo";
        public static final String ONE_YEAR = "1y";
        public static final String TWO_YEAR = "2y";
        public static final String FIVE_YEARS = "5y";
        public static final String TEN_YEARS = "10y";
        public static final String YTD = "ytd";
        public static final String MAX = "max";
    }

    // valid intervals
    public class Interval {
        public static final String ONE_MIN = "1m";
        public static final String FIVE_MIN = "5m";
        public static final String FIFTEEN_MIN = "15m";
        public static final String ONE_HOUR = "1h";
        public static final String ONE_WEEK = "1wk";
        public static final String ONE_DAY = "1d";
        public static final String FIVE_DAY = "5d";
        public static final String ONE_MONTH = "1mo";
        public static final String THREE_MONTHS = "3mo";
        public static final String SIX_MONTHS = "6mo";
        public static final String ONE_YEAR = "1y";
        public static final String TWO_YEAR = "2y";
        public static final String FIVE_YEARS = "5y";
        public static final String TEN_YEARS = "10y";
        public static final String YTD = "ytd";
        public static final String MAX = "max";
    }
}
