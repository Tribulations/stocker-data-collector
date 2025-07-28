package stocker.database;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import stocker.model.Candlestick;
import stocker.util.PostgresTestContainerUtil;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@DisplayName("CandlestickDao Integration Tests - Date Overwrite Logic")
class CandlestickDaoIntegrationTest {
    @Container
    static PostgreSQLContainer<?> postgres = PostgresTestContainerUtil.POSTGRES
            .withDatabaseName("stockdb_test")
            .withUsername("test_user")
            .withPassword("test_password");

    private DatabaseManager databaseManager;
    private CandlestickDao candlestickDao;
    private static final double PRICE_DELTA = 0.01; // Tolerance for floating-point price comparisons

    @BeforeEach
    void setUp() {
        databaseManager = new DatabaseManager(PostgresTestContainerUtil.createConfig(
                "stockdb_test", "test_user", "test_password"
        ));
        databaseManager.initialize();
        candlestickDao = databaseManager.createCandlestickDao();
        candlestickDao.resetTable();
    }

    @AfterEach
    void tearDown() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    @Test
    @DisplayName("Same symbol and date with different times - last insert overwrites")
    void sameSymbolAndDateShouldOverwriteRegardlessOfTime() {
        String symbol = "BOL.ST";
        // 2025-07-26 12:00:00 GMT+02:00 (timestamp in seconds)
        long timestamp1 = 1753524000L;
        // 2025-07-26 17:30:00 GMT+02:00 (same day, different time)
        long timestamp2 = 1753543800L;
        Candlestick cs1 = new Candlestick(100.0, 110.0, 95.0, 112.0, 1000L, timestamp1);
        Candlestick cs2 = new Candlestick(200.0, 210.0, 195.0, 212.0, 2000L, timestamp2);

        // Insert first candlestick
        candlestickDao.addRows(symbol, List.of(cs1));
        List<Candlestick> rowsAfterFirstInsert = candlestickDao.getAllRowsByName(symbol);

        // Verify that only one row was inserted
        assertEquals(1, rowsAfterFirstInsert.size(), "Should have one row after first insert");
        // Verify that the inserted row has the correct data
        assertEquals(cs1.open(), rowsAfterFirstInsert.get(0).open(), PRICE_DELTA);
        assertEquals(cs1.timestamp(), rowsAfterFirstInsert.get(0).timestamp());

        // Insert second candlestick with same symbol and same date but different time and prices
        candlestickDao.addRows(symbol, List.of(cs2));
        List<Candlestick> rowsAfterSecondInsert = candlestickDao.getAllRowsByName(symbol);

        // Verify that only one row was inserted and it has the correct data
        assertEquals(1, rowsAfterSecondInsert.size(), "Should still have only one row after overwrite");
        assertEquals(cs2.open(), rowsAfterSecondInsert.get(0).open(), PRICE_DELTA, "Open price should be updated");
        assertEquals(cs2.timestamp(), rowsAfterSecondInsert.get(0).timestamp(), "Timestamp should be updated");
        assertEquals(cs2.close(), rowsAfterSecondInsert.get(0).close(), PRICE_DELTA, "Close price should be updated");
        assertEquals(cs2.high(), rowsAfterSecondInsert.get(0).high(), PRICE_DELTA, "High price should be updated");
        assertEquals(cs2.low(), rowsAfterSecondInsert.get(0).low(), PRICE_DELTA, "Low price should be updated");
        assertEquals(cs2.volume(), rowsAfterSecondInsert.get(0).volume(), PRICE_DELTA, "Volume should be updated");

        // Create a third candlestick with the same symbol and date but a time that is earlier than the second insert
        // 2025-07-26 09:00:00 GMT+02:00 (same day, different time)
        long timestamp3 = 1753513200L;
        Candlestick cs3 = new Candlestick(199.0, 209.0, 191.0, 213.0, 25000L, timestamp3);

        // Insert third candlestick
        candlestickDao.addRows(symbol, List.of(cs3));
        List<Candlestick> rowsAfterThirdInsert = candlestickDao.getAllRowsByName(symbol);

        // Verify that only one row was inserted and it has the correct data
        assertEquals(1, rowsAfterThirdInsert.size(), "Should still have only one row after overwrite");
        assertEquals(cs3.open(), rowsAfterThirdInsert.get(0).open(), PRICE_DELTA, "Open price should be updated");
        assertEquals(cs3.timestamp(), rowsAfterThirdInsert.get(0).timestamp(), "Timestamp should be updated");
        assertEquals(cs3.close(), rowsAfterThirdInsert.get(0).close(), PRICE_DELTA, "Close price should be updated");
        assertEquals(cs3.high(), rowsAfterThirdInsert.get(0).high(), PRICE_DELTA, "High price should be updated");
        assertEquals(cs3.low(), rowsAfterThirdInsert.get(0).low(), PRICE_DELTA, "Low price should be updated");
        assertEquals(cs3.volume(), rowsAfterThirdInsert.get(0).volume(), PRICE_DELTA, "Volume should be updated");
    }

    @Test
    @DisplayName("Same timestamp in seconds vs milliseconds are treated as different entries")
    void testTimestampPrecisionAffectsUniqueness() {
        String symbol = "BOL.ST";
        // 2025-07-26 09:00:00 GMT+02:00 (timestamp in seconds)
        long timestamp1 = 1753513200L;
        // 2025-07-26 09:00:00 GMT+02:00 (timestamp in milliseconds)
        long timestamp2 = 1753513200000L;

        Candlestick cs1 = new Candlestick(100.0, 110.0, 95.0, 112.0, 1000L, timestamp1);
        Candlestick cs2 = new Candlestick(200.0, 210.0, 195.0, 212.0, 2000L, timestamp2);

        candlestickDao.addRows(symbol, List.of(cs1));
        candlestickDao.addRows(symbol, List.of(cs2));

        List<Candlestick> rowsAfterInsert = candlestickDao.getAllRowsByName(symbol);
        assertEquals(2, rowsAfterInsert.size(), "Should have two rows after insert");
    }
}
