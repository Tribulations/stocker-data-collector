package stocker.data;

import stocker.data.fetchers.YahooFinanceFetcher;
import stocker.data.parsers.YahooFinanceParser;
import stocker.database.CandlestickDao;
import stocker.representation.TradingPeriod;

import java.util.List;

/**
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public class MainDataFetcher {
    private static final String MARKET_SUFFIX_SWE = ".ST";

    public MainDataFetcher() {
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
