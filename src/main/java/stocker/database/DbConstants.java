package stocker.database;

import stocker.security.Authenticator;

/**
 * Storing constants used in classes interacting with the database.
 */
public final class DbConstants {

    public DbConstants() {
        throw new IllegalStateException("Utility class");
    }

    /** Database connection constants */
    public static final String DB_IP_ADDRESS = "213.164.214.9";
    public static final String DB_PORT = ":5432";
    public static final String DB_NAME = "test_db";
    public static final String DB_URL = "jdbc:postgresql://" + DB_IP_ADDRESS + DB_PORT + "/" + DB_NAME;
    public static final String DB_USERNAME = Authenticator.INSTANCE.getDbUsername();
    public static final String DB_PASSWORD = Authenticator.INSTANCE.getDbPassword();
    public static final String CANDLESTICK_TABLE = "test_schema.stock_prices1";

    /** Database table and attribute name constants */
    public static final String TIMESTAMP_COLUMN = "timestamp";
    public static final String OPEN_COLUMN = "open";
    public static final String CLOSE_COLUMN = "close";
    public static final String LOW_COLUMN = "low";
    public static final String HIGH_COLUMN = "high";
    public static final String VOLUME_COLUMN = "volume";
    public static final String INTERVAL_COLUMN = "interval";
    public static final String INSERT_CANDLESTICK_QUERY = "INSERT INTO " + CANDLESTICK_TABLE
            + " (timestamp, open, close, low, high, volume, symbol) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
}
