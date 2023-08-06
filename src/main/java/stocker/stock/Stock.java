package stocker.stock;

import stocker.data.fetchers.wJson.YahooFinanceFetcher;
import stocker.data.parsers.YahooFinanceParser;

import java.util.HashMap;
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
     * Create a Stock by only passing its name and the requested range,
     * and interval the price data/TradingPeriod
     * should contain.
     * @param symbol the symbol/name of the stock
     * @param range the range of the TradingPeriod e.g. 1d, 1mo, 6mo 1y etc.
     * @param interval the interval of each candlestick e.g. 1m, 5m, 15m, 1h, 1d etc.
     */
    public Stock(String symbol, final String range, final String interval) {
        this.symbol = symbol;
//        this.tradingPeriod = StockDataParser.INSTANCE.parseStockData(YahooFinanceFetcher.INSTANCE.fetchData(symbol,
//                range, interval)).getTradingPeriod();
        final YahooFinanceParser yahooFinanceParser = new YahooFinanceParser(
                YahooFinanceFetcher.INSTANCE.fetchData(symbol, range, interval));
        yahooFinanceParser.parse();
        this.tradingPeriod = yahooFinanceParser.getTradingPeriod();
    }

    public String getSymbol() {
        return symbol;
    }

    public TradingPeriod getTradingPeriod() {
        return tradingPeriod;
    }
}
