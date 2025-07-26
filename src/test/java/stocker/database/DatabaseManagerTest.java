package stocker.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import stocker.util.PostgresTestContainerUtil;

/**
 * Integration tests using Testcontainers.
 * Each test run gets a fresh PostgreSQL container.
 * These tests currently runs as unit tests but will probably be updated to run as integration tests in the future.
 */
@Testcontainers
@DisplayName("DatabaseManager Integration Tests")
class DatabaseManagerTest {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseManagerTest.class);

    @Container
    static PostgreSQLContainer<?> postgres = PostgresTestContainerUtil.POSTGRES
            .withDatabaseName("stockdb_test")
            .withUsername("test_user")
            .withPassword("test_password");

    private DatabaseManager databaseManager;
    private DatabaseConfig testConfig;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up DatabaseManager test with container: {}:{}",
                postgres.getHost(), postgres.getFirstMappedPort());

        // Create config using container connection details
        testConfig = PostgresTestContainerUtil.createConfig(
            "stockdb_test", "test_user", "test_password"
        );

        databaseManager = new DatabaseManager(testConfig);
    }

    @AfterEach
    void tearDown() {
        try {
            if (databaseManager != null) {
                databaseManager.close();
            }
        } catch (Exception e) {
            logger.warn("Error during test cleanup: {}", e.getMessage());
        }
    }

    @Test
    @DisplayName("Should create database schema and table from scratch")
    void shouldCreateDatabaseFromScratch() throws SQLException {
        // Act - Initialize database (fresh container, so everything is new)
        databaseManager.initialize();

        // Assert - Verify critical components exist
        verifySchemaExists();
        verifyTableExists();
        verifyDatabaseIsHealthy();

        // Verify migration tracking works
        String currentVersion = databaseManager.getCurrentVersion();
        assertNotNull(currentVersion, "Database version should not be null");
        assertNotEquals("0", currentVersion, "Database should have migrated beyond version 0");
        assertNotEquals("Unknown", currentVersion, "Database version should be determinable");

        logger.debug("Database initialized successfully with version: {}", currentVersion);
    }

    @Test
    @DisplayName("Should handle re-running migrations safely")
    void shouldHandleRerunningMigrations() throws SQLException {
        // Arrange
        databaseManager.initialize();
        String firstVersion = databaseManager.getCurrentVersion();

        // Act
        databaseManager.initialize();
        String secondVersion = databaseManager.getCurrentVersion();

        // Assert - Should be safe to re-run
        assertEquals(firstVersion, secondVersion,
                "Migration version should be stable across re-runs");
        verifyDatabaseIsHealthy();
    }

    @Test
    @DisplayName("Should create functional database for DAO operations")
    void shouldCreateFunctionalDatabase() throws SQLException {
        // Arrange & Act
        databaseManager.initialize();
        CandlestickDao dao = databaseManager.createCandlestickDao();

        // Assert - Should be able to perform basic DAO operations
        assertDoesNotThrow(dao::resetTable,
                "DAO operations should work on initialized database");
        assertTrue(databaseManager.isHealthy(), "Database should remain healthy after DAO operations");
    }

    @Test
    @DisplayName("Should verify that the PostgreSQL container behaves like a real database")
    void shouldVerifyPostgresContainerBehavesLikeRealDatabase() throws SQLException {
        databaseManager.initialize();

        try (Connection conn = databaseManager.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            String productName = metaData.getDatabaseProductName();
            String version = metaData.getDatabaseProductVersion();

            assertEquals("PostgreSQL", productName, "Should be using PostgreSQL");
            assertTrue(version.startsWith("13."),
                    "Should be PostgreSQL 13.x, got: " + version);

            logger.debug("Verified container database: {} {}", productName, version);
        }
    }

    private void verifySchemaExists() throws SQLException {
        try (Connection conn = databaseManager.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            boolean schemaFound = false;
            try (ResultSet schemas = metaData.getSchemas()) {
                while (schemas.next()) {
                    String schemaName = schemas.getString("TABLE_SCHEM");
                    if ("stock_prices_schema".equals(schemaName)) {
                        schemaFound = true;
                        break;
                    }
                }
            }

            assertTrue(schemaFound, "stock_prices_schema should exist after migration");
        }
    }

    private void verifyTableExists() throws SQLException {
        try (Connection conn = databaseManager.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();

            boolean tableFound = false;
            try (ResultSet tables = metaData.getTables(null, "stock_prices_schema",
                    "stock_prices_1day", null)) {
                if (tables.next()) {
                    tableFound = true;
                    String tableName = tables.getString("TABLE_NAME");
                    assertEquals("stock_prices_1day", tableName,
                            "Table name should match expected value");
                }
            }

            assertTrue(tableFound, "stock_prices_1day table should exist after migration");
        }
    }

    private void verifyDatabaseIsHealthy() throws SQLException {
        assertTrue(databaseManager.isHealthy(), "Database should be healthy after migration");

        try (Connection conn = databaseManager.getConnection()) {
            assertNotNull(conn, "Connection should not be null");
            assertFalse(conn.isClosed(), "Connection should be open");
            assertTrue(conn.isValid(5), "Connection should be valid");
        }
    }
}
