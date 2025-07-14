package stocker.model;

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

    /**
     * Default public construction initializing all member fields to 0.
     */
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

    /**
     * Public construction initializing all member fields.
     * @param open the candlestick's opening price
     * @param close the candlestick's closing price
     * @param low the candlestick's lowest price
     * @param high the candlestick's highest price
     * @param volume the total volume of candlestick
     * @param timestamp the candlestick timestamp
     * @param interval the interval of the candlestick e.g. 1 day, 1 min 15 min etc.
     */
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

    /**
     * Public accessor.
     * @return the candlestick's opening price
     */
    public double getOpen() {
        return open;
    }

    /**
     * Public accessor.
     * @return the candlestick's closing price
     */
    public double getClose() {
        return close;
    }

    /**
     * Public accessor.
     * @return the candlestick's lowest price
     */
    public double getLow() {
        return low;
    }

    /**
     * Public accessor.
     * @return the candlestick's highest price
     */
    public double getHigh() {
        return high;
    }

    /**
     * Public accessor.
     * @return the candlestick's total volume
     */
    public long getVolume() {
        return volume;
    }

    /**
     * Public accessor.
     * @return the candlestick's timestamp
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Public accessor.
     * @return the candlestick's interval 1 day, 15 min etc.
     */
    public String getInterval() {
        return interval;
    }

    /**
     * Returns the candlestick's timestamp in a more readable way e.g. 2023-08-01 12:05:00.
     * @return the candlestick's timestamp with the format yyyy-MM-dd HH:mm:ss
     */
    public String getHumanReadableDate() {
        return DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss").format(
                        Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()));
    }

//    public static String asHumanReadableDate(long timestamp) {
//        return DateTimeFormatter.ofPattern(
//                "yyyy-MM-dd HH:mm").format(
//                Instant.ofEpochSecond(timestamp).atZone(ZoneId.systemDefault()));
//    }


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
