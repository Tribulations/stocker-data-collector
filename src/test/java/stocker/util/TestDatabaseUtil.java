package stocker.util;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import stocker.database.DatabaseConfig;

/**
 * Utility class for managing a reusable PostgreSQLContainer for tests.
 */
@Testcontainers
public class TestDatabaseUtil {

    public static PostgreSQLContainer<?> createContainer(String dbName, String username, String password) {
        return new PostgreSQLContainer<>("postgres:13-alpine")  // ✅ NEW instance each time
                .withDatabaseName(dbName)
                .withUsername(username)
                .withPassword(password)
                .withReuse(false); // Fresh container for each test run
    }

    /**
     * Creates a DatabaseConfig for the running PostgreSQLContainer with the given parameters.
     * Also updates the container's database name, username, and password.
     */
    public static DatabaseConfig createConfig(PostgreSQLContainer<?> postgreSQLContainer) {

        return new DatabaseConfig(
                postgreSQLContainer.getHost(),
                postgreSQLContainer.getFirstMappedPort().toString(),
                postgreSQLContainer.getDatabaseName(),
                postgreSQLContainer.getUsername(),
                postgreSQLContainer.getPassword());
    }

    private TestDatabaseUtil() {
        // Prevent instantiation
    }
}
