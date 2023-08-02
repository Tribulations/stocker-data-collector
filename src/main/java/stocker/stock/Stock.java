package stocker.stock;

import stocker.datafetchers.wJson.StockDataFetcher;
import stocker.datafetchers.wJson.StockDataParser;

/**
 * class representing a stock and provides different functionalities to get info about a stock.
 *
 * @author Joakim Colloz
 * @version 1.0
 * @since 1.0
 */
public class Stock {
    private final String symbol;
    private final TradingPeriod tradingPeriod;

    /**
     * create a Stock by passing its name and a TradingPeriod object.
     * @param symbol the symbol/name of the stock
     * @param tradingPeriod the trading period this Stock should have
     */
    public Stock(String symbol, TradingPeriod tradingPeriod) {
        this.symbol = symbol;
        this.tradingPeriod = tradingPeriod; // todo having a TradingPeriod class is weird?? Should just be candlesticks?
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
        this.tradingPeriod = StockDataParser.INSTANCE.parseStockData(StockDataFetcher.INSTANCE.fetchStockData(symbol,
                range, interval)).getTradingPeriod();
    }

    public String getSymbol() {
        return symbol;
    }

    public TradingPeriod getTradingPeriod() {
        return tradingPeriod;
    }
}
