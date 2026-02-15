package com.joakimcolloz.stocker.datacollector.database;

import com.joakimcolloz.stocker.datacollector.model.Candlestick;
import com.joakimcolloz.stocker.datacollector.util.TestDatabaseUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@DisplayName("IntradayCandlestickDao Integration Tests")
class IntradayCandlestickDaoIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = TestDatabaseUtil.createContainer(
            "stockdb_intraday_test", "test_user", "test_password");

    private DatabaseManager databaseManager;
    private IntradayCandlestickDao intradayDao;

    @BeforeEach
    void setUp() {
        databaseManager = new DatabaseManager(TestDatabaseUtil.createConfig(postgreSQLContainer));
        databaseManager.initialize();
        intradayDao = databaseManager.createIntradayCandlestickDao();
        intradayDao.resetTable();
    }

    @AfterEach
    void tearDown() {
        if (databaseManager != null) {
            databaseManager.close();
        }
    }

    @Test
    @DisplayName("Same timestamp+symbol+interval should upsert")
    void sameTimestampSymbolIntervalShouldUpsert() {
        String symbol = "BOL.ST";
        String interval = "5m";
        long timestamp = 1760943900L;

        Candlestick first = new Candlestick(1.0, 2.0, 0.5, 1.5, 0L, timestamp);
        Candlestick second = new Candlestick(10.0, 12.0, 9.5, 11.5, 5L, timestamp);

        intradayDao.addRows(symbol, interval, List.of(first));
        List<Candlestick> afterFirst = intradayDao.getAllRowsBySymbolAndInterval(symbol, interval);
        assertEquals(1, afterFirst.size());
        assertEquals(1.0, afterFirst.get(0).open(), 0.0001);

        intradayDao.addRows(symbol, interval, List.of(second));
        List<Candlestick> afterSecond = intradayDao.getAllRowsBySymbolAndInterval(symbol, interval);
        assertEquals(1, afterSecond.size());
        assertEquals(10.0, afterSecond.get(0).open(), 0.0001);
        assertEquals(5L, afterSecond.get(0).volume());
    }

    @Test
    @DisplayName("Same timestamp+symbol but different interval should store separate rows")
    void differentIntervalShouldStoreSeparateRows() {
        String symbol = "BOL.ST";
        long timestamp = 1760943900L;

        intradayDao.addRows(symbol, "5m", List.of(new Candlestick(1.0, 2.0, 0.5, 1.5, 0L, timestamp)));
        intradayDao.addRows(symbol, "15m", List.of(new Candlestick(3.0, 4.0, 2.5, 3.5, 1L, timestamp)));

        assertEquals(1, intradayDao.getAllRowsBySymbolAndInterval(symbol, "5m").size());
        assertEquals(1, intradayDao.getAllRowsBySymbolAndInterval(symbol, "15m").size());
        assertEquals(2, intradayDao.getAllRows().size());
    }
}
