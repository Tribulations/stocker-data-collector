package stocker.database;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Storing constants used in classes interacting with the database.
 */
public final class DbConstants {
    
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .systemProperties() // Check system env as fallback
            .load();
    
    public DbConstants() {
        throw new IllegalStateException("Utility class");
    }

    /** Database connection constants */
    public static final String DB_IP_ADDRESS = dotenv.get("DB_IP_ADDRESS");
    public static final String DB_PORT = ":" + dotenv.get("DB_PORT");
    public static final String DB_NAME = dotenv.get("DB_NAME");
    public static final String DB_URL = "jdbc:postgresql://" + DB_IP_ADDRESS + DB_PORT + "/" + DB_NAME;
    public static final String DB_USERNAME = dotenv.get("DB_USERNAME");
    public static final String DB_PASSWORD = dotenv.get("DB_PASSWORD");
    public static final String CANDLESTICK_TABLE = dotenv.get("DB_SCHEMA") + "." +
                                                  dotenv.get("DB_TABLE");

    /** Database table and attribute name constants */
    public static final String TIMESTAMP_COLUMN = "timestamp";
    public static final String OPEN_COLUMN = "open";
    public static final String CLOSE_COLUMN = "close";
    public static final String LOW_COLUMN = "low";
    public static final String HIGH_COLUMN = "high";
    public static final String VOLUME_COLUMN = "volume";
    public static final String INTERVAL_COLUMN = "interval"; // TODO remove as not used?

    /** SQL query constants */
    public static final String INSERT_CANDLESTICK_QUERY = "INSERT INTO " + CANDLESTICK_TABLE
            + " (timestamp, open, close, low, high, volume, symbol) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    public static final String SELECT_ALL_QUERY = "SELECT * FROM " + CANDLESTICK_TABLE;
    public static final String SELECT_BY_SYMBOL_QUERY = "SELECT * FROM " + CANDLESTICK_TABLE + " WHERE symbol = ?";
    public static final String INSERT_ON_CONFLICT_UPDATE_QUERY = "INSERT INTO " + CANDLESTICK_TABLE
            + " (timestamp, open, close, low, high, volume, symbol, interval) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT (time_stamp, symbol) DO UPDATE SET "
            + "(open, close, low, high, volume) = (excluded.open, excluded.close, excluded.low, excluded.high, excluded.volume)";
    public static final String INSERT_ON_CONFLICT_DO_NOTHING_QUERY = "INSERT INTO " + CANDLESTICK_TABLE
            + " (timestamp, open, close, low, high, volume, symbol, interval) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING";
    public static final String RESET_TABLE_QUERY = "TRUNCATE TABLE " + CANDLESTICK_TABLE;
}
