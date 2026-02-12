package com.joakimcolloz.stocker.datacollector.model;

/**
 * Enum representing Coinbase API candlestick granularities.
 * Each value maps to the number of seconds per candle.
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public enum Granularity {
    ONE_MINUTE(60),
    FIVE_MINUTES(300),
    FIFTEEN_MINUTES(900),
    ONE_HOUR(3600),
    SIX_HOURS(21600),
    ONE_DAY(86400);

    private final int seconds;

    Granularity(int seconds) {
        this.seconds = seconds;
    }

    public int getSeconds() {
        return seconds;
    }

    /**
     * Maps an existing {@link Interval} to the corresponding Coinbase granularity.
     *
     * @param interval the interval to convert
     * @return the matching Granularity
     * @throws IllegalArgumentException if the interval has no Coinbase equivalent
     */
    public static Granularity fromInterval(Interval interval) {
        return switch (interval) {
            case ONE_MINUTE -> ONE_MINUTE;
            case FIVE_MINUTES -> FIVE_MINUTES;
            case FIFTEEN_MINUTES -> FIFTEEN_MINUTES;
            case ONE_HOUR -> ONE_HOUR;
            case ONE_DAY -> ONE_DAY;
            default -> throw new IllegalArgumentException(
                    "No Coinbase granularity mapping for interval: " + interval);
        };
    }
}
