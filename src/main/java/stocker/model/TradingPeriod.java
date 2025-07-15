package stocker.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Class representing a trading period i.e., a list of candlesticks which all have the same interval and range.
 *
 * @author Joakim Colloz
 * @version 1.0
 * @since 1.0
 */
public class TradingPeriod {
    private static final Logger logger = LoggerFactory.getLogger(TradingPeriod.class);
    private final List<Candlestick> tradingPeriod;
    private final String range;
    private final String interval;

    public TradingPeriod(final List<Candlestick> candlesticks, final String range, final String interval) {
        this.tradingPeriod = candlesticks;
        this.range = range;
        this.interval = interval;
    }

    public void printTradingPeriod() {
        tradingPeriod.forEach(System.out::println);
    }

    public List<Candlestick> getCandlesticks() {
        return tradingPeriod;
    }

    public String getRange() {
        return range;
    }

    public String getInterval() {
        return interval;
    }

    public void removeLast() {
        Candlestick currentDayCandlestick = tradingPeriod.get(tradingPeriod.size() - 1);
        logger.info("skipCurrentDayPriceData is set to true");
        logger.info("Removing current day price data for trading session: {}",
                currentDayCandlestick.getHumanReadableDate());

        tradingPeriod.remove(currentDayCandlestick);
    }
}
