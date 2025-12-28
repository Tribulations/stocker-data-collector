package com.joakimcolloz.stocker.datacollector.database;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * Simple Database configuration class.
 */
public class DatabaseConfig {
    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .systemProperties() // Check system env as fallback TODO: CHECK THIS: doesn't seem to work as expected
            .load();

    private final String jdbcUrl;
    private final String username;
    private final String password;
    private final String host;
    private final String port;
    private final String databaseName;
    private final String schema;

    /**
     * Default constructor loading configuration from .env
     */
    public DatabaseConfig() {
        this.host = dotenv.get("DB_IP_ADDRESS", "localhost");
        this.port = dotenv.get("DB_PORT", "5432");
        this.databaseName = dotenv.get("DB_NAME", "stockdb_prod");
        this.username = dotenv.get("DB_USERNAME", "prod_user");
        this.password = dotenv.get("DB_PASSWORD", "prod_password");
        this.schema = dotenv.get("DB_SCHEMA", "stock_prices_schema");
        this.jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;
    }

    /**
     * Constructor with direct values used by Testcontainers and custom configurations
     */
    public DatabaseConfig(String host, String port, String databaseName, String username, String password) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
        this.schema = "stock_prices_schema"; // consistent schema across environments
        this.jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;
    }

    // Getters
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public String getSchema() {
        return schema;
    }

    @Override
    public String toString() {
        return String.format("DatabaseConfig{host='%s', port='%s', database='%s', username='%s', schema='%s'}",
                host, port, databaseName, username, schema);
    }
}
