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
    public static final String DB_URL = "jdbc:postgresql://155.4.55.36:5432/test_db";
    public static final String DB_USERNAME = Authenticator.INSTANCE.getDbUsername();
    public static final String DB_PASSWORD = Authenticator.INSTANCE.getDbPassword();
    public static final String CANDLESTICK_TABLE = "test_schema.temp_1d_price";

    /** Database table and attribute name constants */
    public static final String TIME_STAMP_COLUMN = "time_stamp";
    public static final String OPEN_COLUMN = "open";
    public static final String CLOSE_COLUMN = "close";
    public static final String LOW_COLUMN = "low";
    public static final String HIGH_COLUMN = "high";
    public static final String VOLUME_COLUMN = "volume";
    public static final String INTERVAL_COLUMN = "interval";
}
