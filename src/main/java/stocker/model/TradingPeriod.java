package stocker.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TradingPeriod {
    private static final Logger logger = LoggerFactory.getLogger(TradingPeriod.class);
    private final List<Candlestick> tradingPeriod;
    private final String RANGE;
    private final String INTERVAL;

    private final double maxOpen = 0.0;
    private final double maxClose = 0.0;
    private final double maxLow = 0.0;
    private final double maxHigh = 0.0;
    private final long maxVolume = 0;

    public TradingPeriod(final List<Candlestick> candlesticks, final String range, final String interval) {
        this.tradingPeriod = candlesticks;
        this.RANGE = range;
        this.INTERVAL = interval;
    }

    public void printTradingPeriod() {
        tradingPeriod.forEach(System.out::println);
    }

    /**
     * Returns the candlestick that has the highest volume in the trading period.
     * @return the Candlestick
     */
    public Candlestick getMaxVolumeCandlestick() {
        Candlestick maxVolumeCandlestick = null;
        long maxVolume = Long.MIN_VALUE;

        for (Candlestick candlestick : tradingPeriod) {
            long volume = candlestick.getVolume();
            if (volume > maxVolume) {
                maxVolume = volume;
                maxVolumeCandlestick = candlestick;
            }
        }

        return maxVolumeCandlestick;
    }

    public List<Candlestick> getCandlesticks() {
        return tradingPeriod;
    }

    public String getRANGE() {
        return RANGE;
    }

    public String getINTERVAL() {
        return INTERVAL;
    }

    public void removeLast() {
        Candlestick currentDayCandlestick = tradingPeriod.get(tradingPeriod.size() - 1);
        logger.info("skipCurrentDayPriceData is set to true");
        logger.info("Removing current day price data for trading session: {}",
                currentDayCandlestick.getHumanReadableDate());

        tradingPeriod.remove(currentDayCandlestick);
    }
}
