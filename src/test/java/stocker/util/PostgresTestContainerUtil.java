package stocker.util;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import stocker.database.DatabaseConfig;

/**
 * Utility class for managing a reusable PostgreSQLContainer for tests.
 */
@Testcontainers
public class PostgresTestContainerUtil {
    @Container
    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:13-alpine")
            .withReuse(false); // Fresh container for each test run

    /**
     * Creates a DatabaseConfig for the running PostgreSQLContainer with the given parameters.
     * Also updates the container's database name, username, and password.
     */
    public static DatabaseConfig createConfig(String dbName, String username, String password) {
        POSTGRES.withDatabaseName(dbName)
                .withUsername(username)
                .withPassword(password);
        return new DatabaseConfig(
                POSTGRES.getHost(),
                POSTGRES.getFirstMappedPort().toString(),
                dbName,
                username,
                password
        );
    }

    private PostgresTestContainerUtil() {
        // Prevent instantiation
    }
}
