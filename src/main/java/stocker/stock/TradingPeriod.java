package stocker.stock;

import stocker.stock.Candlestick;

import java.util.List;

/**
 * TODO or this class is maybe usable someway?
 * TODO this class is maybe unnecessary?
 * TODO this class should maybe be refatored to an indicator class?
 * Like a class that takes a list of candlesticks and then those some operation
 * e.g. calculating the max values maxHigh maxLow etc.?
 */
public class TradingPeriod {
    private final List<Candlestick> tradingPeriod;

    // store the max values. lazy init? reassign when these values is needed the first time.
    private final double maxOpen = 0.0;
    private final double maxClose = 0.0;
    private final double maxLow = 0.0;
    private final double maxHigh = 0.0;
    private final long maxVolume = 0;

    /**
     * init member field {@link #tradingPeriod}
     * @param candlesticks the candlesticks in the trading period
     */
    public TradingPeriod(final List<Candlestick> candlesticks) {
        this.tradingPeriod = candlesticks;
    }

    public void printTradingPeriod() {
        tradingPeriod.forEach(System.out::println);
    }

    /**
     * returns the candlestick that has the highest volume in the trading period.
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
}
