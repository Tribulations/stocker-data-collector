package com.joakimcolloz.stocker.datacollector.database;

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
    public static final String CLOSE_COLUMN = "close";
    public static final String HIGH_COLUMN = "high";
    public static final String LOW_COLUMN = "low";
    public static final String OPEN_COLUMN = "open";
    public static final String VOLUME_COLUMN = "volume";
    public static final String TIMESTAMP_COLUMN = "timestamp";

    /** SQL query constants */
    public static final String INSERT_CANDLESTICK_QUERY = "INSERT INTO " + CANDLESTICK_TABLE
            + " (timestamp, open, high, low, close, volume, symbol) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?)";
    public static final String SELECT_ALL_QUERY = "SELECT * FROM " + CANDLESTICK_TABLE;
    public static final String SELECT_BY_SYMBOL_QUERY = "SELECT * FROM " + CANDLESTICK_TABLE + " WHERE symbol = ?";
    public static final String RESET_TABLE_QUERY = "TRUNCATE TABLE " + CANDLESTICK_TABLE;
}
