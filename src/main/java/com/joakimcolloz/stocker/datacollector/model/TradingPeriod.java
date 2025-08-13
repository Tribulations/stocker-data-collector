package com.joakimcolloz.stocker.datacollector.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Record representing a trading period i.e., a list of {@link Candlestick} which all have the same interval and range.
 *
 * @author Joakim Colloz
 * @version 1.0
 * @since 1.0
 */
public record TradingPeriod(List<Candlestick> candlesticks, String range, String interval) {
    private static final Logger logger = LoggerFactory.getLogger(TradingPeriod.class);

    public void printTradingPeriod() {
        candlesticks.forEach(System.out::println);
    }

    public void removeLast() {
        Candlestick currentDayCandlestick = candlesticks.get(candlesticks.size() - 1);
        logger.info("skipCurrentDayPriceData is set to true");
        logger.info("Removing current day price data for trading session: {}",
                currentDayCandlestick.getHumanReadableDate());

        candlesticks.remove(currentDayCandlestick);
    }
}
