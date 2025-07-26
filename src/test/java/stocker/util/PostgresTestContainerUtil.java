package stocker.util;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Utility class for managing a reusable PostgreSQLContainer for tests.
 */
@Testcontainers
public class PostgresTestContainerUtil {
    @Container
    public static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:13-alpine")
            .withReuse(false); // Fresh container for each test run

    private PostgresTestContainerUtil() {
        // Prevent instantiation
    }
}
