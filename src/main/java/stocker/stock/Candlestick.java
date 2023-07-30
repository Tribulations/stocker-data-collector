package stocker.stock;

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
    // todo we have to keep track of the candlesticks interval e.g. 1 min, 15 min, 1 day etc. because we use it when
    //  adding data to the database so we can set the timestamp/date correctly
    private String interval;

    public Candlestick() {
        this.open = 0;
        this.close = 0;
        this.low = 0;
        this.high = 0;
        this.volume = 0;
        this.timestamp = 0;
        this.interval = "";
    }
//    public Candlestick(final double open, final double close, final double low, final double high,
//                       final long volume, final long timestamp) {
//        this.open = open;
//        this.close = close;
//        this.low = low;
//        this.high = high;
//        this.volume = volume;
//        this.timestamp = timestamp;
//    }

    public Candlestick(final double open, final double close, final double low, final double high,
                       final long volume, final long timestamp, final String interval) {
        this.open = open;
        this.close = close;
        this.low = low;
        this.high = high;
        this.volume = volume;
        this.timestamp = timestamp;
        this.interval = interval;
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

    public String getInterval() {
        return interval;
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

    public void setInterval(String interval) {
        this.interval = interval;
    }

    @Override
    public String toString() {
        return String.format("%s %s %s %s %s %s", getHumanReadableDate(), open, close, low, high, volume);
    }
}
