package com.joakimcolloz.stocker.datacollector.model;

import com.google.gson.JsonParseException;
import com.joakimcolloz.stocker.datacollector.data.exception.DataFetchException;
import com.joakimcolloz.stocker.datacollector.data.fetchers.YahooFinanceFetcher;
import com.joakimcolloz.stocker.datacollector.data.parsers.YahooFinanceParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger logger = LoggerFactory.getLogger(Stock.class);
    private final String symbol;
    private final TradingPeriod tradingPeriod;
    private final Map<String, TradingPeriod> tradingPeriodMap = new HashMap<>();

    /**
     * Create a Stock by passing its name and a TradingPeriod object.
     * @param symbol the symbol/name of the stock
     * @param tradingPeriod the trading period this Stock should have
     */
    public Stock(String symbol, TradingPeriod tradingPeriod) {
        this.symbol = symbol;
        this.tradingPeriod = tradingPeriod;
        this.tradingPeriodMap.put(tradingPeriod.interval(), tradingPeriod); // TODO: Is this map needed?
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
    public Stock(String symbol, final String range, final String interval) throws DataFetchException, JsonParseException {
        this.symbol = symbol;

        final String json = YahooFinanceFetcher.INSTANCE.fetchData(symbol, range, interval);
        try (YahooFinanceParser yahooFinanceParser = new YahooFinanceParser(json)) {
            yahooFinanceParser.parse();
            this.tradingPeriod = yahooFinanceParser.getTradingPeriod();
        }
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
     */
    public Stock(String symbol, final String range, final String interval, final boolean skipCurrentDayPriceData)
            throws DataFetchException {
        this.symbol = symbol;

        final String json = YahooFinanceFetcher.INSTANCE.fetchData(symbol, range, interval);
        try (YahooFinanceParser yahooFinanceParser = new YahooFinanceParser(json)) {
            yahooFinanceParser.parse();

            if (skipCurrentDayPriceData) {
                removeLatestCandlestick(yahooFinanceParser.getTradingPeriod().candlesticks());
            }

            this.tradingPeriod = yahooFinanceParser.getTradingPeriod();
        }
    }

    private static void removeLatestCandlestick(List<Candlestick> candlesticks) {
        Candlestick currentDayCandlestick = candlesticks.get(candlesticks.size() - 1);
        logger.info("Inside Stock constructor");
        logger.info("skipCurrentDayPriceData is set to true");
        logger.info("Removing current day price data for trading session: {}",
                currentDayCandlestick.getHumanReadableDate());
        candlesticks.remove(currentDayCandlestick);
    }

    public String getSymbol() {
        return symbol;
    }

    public TradingPeriod getTradingPeriod() {
        return tradingPeriod;
    }
}
