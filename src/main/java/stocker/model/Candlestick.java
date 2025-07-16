package stocker.model;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single candlestick (OHLCV) data point in a time series of stock market data.
 * The time interval (e.g., 1 minute, 5 minutes, 1 day) is not stored in individual candlesticks
 * but is managed by the containing {@link TradingPeriod}, which represents a collection of
 * candlesticks for a specific time range and interval.
 *
 * @param open the opening price for the time period
 * @param close the closing price for the time period
 * @param low the lowest price during the time period
 * @param high the highest price during the time period
 * @param volume the number of shares traded during the time period
 * @param timestamp the Unix timestamp (seconds since epoch) for the period start
 *
 * @author Joakim Colloz
 * @version 1.1
 * @since 1.0
 *
 * @see TradingPeriod
 * @see #getHumanReadableDate()
 */
public record Candlestick(double open, double close, double low, double high, long volume, long timestamp) {
    /**
     * Returns the candlestick's timestamp in a more readable way e.g. 2023-08-01 12:05:00.
     * @return the candlestick's timestamp with the format yyyy-MM-dd HH:mm:ss
     */
    public String getHumanReadableDate() {
        return DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss").format(
                        Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()));
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %s %s %s", getHumanReadableDate(), open, close, low, high, volume);
    }
}
