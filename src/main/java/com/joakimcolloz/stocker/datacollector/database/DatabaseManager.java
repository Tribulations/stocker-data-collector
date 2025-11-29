package com.joakimcolloz.stocker.datacollector.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseManager {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final MigrationManager migrationManager;

    /**
     * Initialize DatabaseManager with configuration
     */
    public DatabaseManager(DatabaseConfig config) {
        this.jdbcUrl = config.getJdbcUrl();
        this.username = config.getUsername();
        this.password = config.getPassword();
        this.migrationManager = new MigrationManager(jdbcUrl, username, password);
    }

    /**
     * Initialize DatabaseManager with direct connection details
     */
    public DatabaseManager(String jdbcUrl, String username, String password) {
        this.jdbcUrl = jdbcUrl;
        this.username = username;
        this.password = password;
        this.migrationManager = new MigrationManager(jdbcUrl, username, password);
    }

    /**
     * Get a new database connection
     */
    public Connection getConnection() throws SQLException {
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");

            // Create new connection each time
            return DriverManager.getConnection(jdbcUrl, username, password);

        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL driver not found", e);
        }
    }

    /**
     * Initialize database schema and run migrations
     */
    public void initialize() {
        try {
            logger.info("Initializing database...");

            // Test connection first
            try (Connection testConnection = getConnection()) {
                logger.info("Database connection successful!");
            }

            // Run all pending migrations
            migrationManager.migrate();

            logger.info("Database initialized successfully!");
            logger.info("Current database version: {}", migrationManager.getCurrentVersion());

        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database: " + e.getMessage(), e);
        }
    }

    /**
     * Create CandlestickDao instance
     */
    public CandlestickDao createCandlestickDao() {
        return new CandlestickDao(this);
    }

    /**
     * Get current database version
     */
    public String getCurrentVersion() {
        return migrationManager.getCurrentVersion();
    }

    /**
     * Check if database connection is healthy
     */
    public boolean isHealthy() {
        try (Connection connection = getConnection()) {
            return connection.isValid(5);
        } catch (SQLException e) {
            logger.error("Database health check failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Close database manager and release resources
     *
     */
    public void close() {
        logger.info("DatabaseManager closed");
    }

    public MigrationManager getMigrationManager() {
        return migrationManager;
    }
}
