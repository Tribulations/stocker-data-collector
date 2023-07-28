package stocker.stock;

import java.util.List;

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
