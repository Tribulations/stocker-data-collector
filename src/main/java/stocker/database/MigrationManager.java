package stocker.database;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

/**
 * Simple migration manager for handling database schema initialization and migrations.
 * Focuses on core functionality: running migrations and getting current version.
 * Uses application schema instead of public schema.
 */
public class MigrationManager {
    private static final Logger logger = LoggerFactory.getLogger(MigrationManager.class);
    private static final String APP_SCHEMA = "stock_prices_schema";
    private final Flyway flyway;

    /**
     * Constructor with DataSource
     */
    public MigrationManager(DataSource dataSource) {
        this.flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)                // Handle existing databases
                .schemas(APP_SCHEMA)                    // Manage only our schema
                .defaultSchema(APP_SCHEMA)
                .table("flyway_schema_history")         // History table in our schema
                .createSchemas(true)                    // Flyway takes care of schema creation, not a migration script
                .validateOnMigrate(true)                // Validate migrations
                .cleanDisabled(true)                    // Prevent accidental data loss
                .load();

        logger.debug("MigrationManager initialized with DataSource for schema: {}", APP_SCHEMA);
    }

    /**
     * Constructor with connection details
     */
    public MigrationManager(String jdbcUrl, String username, String password) {
        this.flyway = Flyway.configure()
                .dataSource(jdbcUrl, username, password)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)  // Handle existing databases

                .schemas(APP_SCHEMA)                    // Manage only our schema
                .defaultSchema(APP_SCHEMA)
                .table("flyway_schema_history")         // History table in our schema
                .createSchemas(true)                    // Flyway takes care of schema creation, not a migration script
                .validateOnMigrate(true)                // Validate migrations
                .cleanDisabled(true)                    // Prevent accidental data loss
                .load();

        logger.debug("MigrationManager initialized with JDBC connection: {} for schema: {}", jdbcUrl, APP_SCHEMA);
    }

    /**
     * Run all pending migrations
     */
    public void migrate() {
        try {
            logger.info("Running database migrations in schema: {}", APP_SCHEMA);
            logger.info("Flyway history table: {}.flyway_schema_history", APP_SCHEMA);

            int migrationsExecuted = flyway.migrate().migrationsExecuted;
            logger.info("Executed {} migration(s) in schema: {}", migrationsExecuted, APP_SCHEMA);
        } catch (Exception e) {
            logger.error("Migration failed in schema {}: {}", APP_SCHEMA, e.getMessage(), e);
            throw new RuntimeException("Migration failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get current database version
     */
    public String getCurrentVersion() {
        try {
            var current = flyway.info().current();
            String version = current != null ? current.getVersion().toString() : "0";
            logger.debug("Current database version in schema {}: {}", APP_SCHEMA, version);
            return version;
        } catch (Exception e) {
            logger.warn("Could not determine current database version in schema {}: {}", APP_SCHEMA, e.getMessage());
            return "Unknown";
        }
    }
}
