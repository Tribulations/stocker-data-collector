package stocker.datafetchers.wJson;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * a stock candlestick.
 */
public class Candlestick {
    private double open;
    private double close;
    private double low;
    private double high;
    private long volume;
    private long timestamp;

    public Candlestick() {
        this.open = 0;
        this.close = 0;
        this.low = 0;
        this.high = 0;
        this.volume = 0;
        this.timestamp = 0;
    }
    public Candlestick(double open, double close, double low, double high, long volume, long timestamp) {
        this.open = open;
        this.close = close;
        this.low = low;
        this.high = high;
        this.volume = volume;
        this.timestamp = timestamp;
    }

    public double getOpen() {
        return open;
    }

    public double getClose() {
        return close;
    }

    public double getLow() {
        return low;
    }

    public double getHigh() {
        return high;
    }

    public long getVolume() {
        return volume;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getHumanReadableDate() {
        return DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm").format(
                        Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()));
    }

    public static String asHumanReadableDate(long timestamp) {
        return DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm").format(
                Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()));
    }


    public void setOpen(double open) {
        this.open = open;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public void setVolume(long volume) {
        this.volume = volume;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %s %s %s", getHumanReadableDate(), open, close, low, high, volume);
    }
}
