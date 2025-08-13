package com.joakimcolloz.stocker.datacollector;

import com.joakimcolloz.stocker.datacollector.data.parsers.YahooFinanceParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.joakimcolloz.stocker.datacollector.data.StockDataService;
import com.joakimcolloz.stocker.datacollector.data.fetchers.YahooFinanceFetcher;
import com.joakimcolloz.stocker.datacollector.data.validation.DataFetcherInputValidator;
import com.joakimcolloz.stocker.datacollector.database.CandlestickDao;
import com.joakimcolloz.stocker.datacollector.database.DatabaseManager;
import com.joakimcolloz.stocker.datacollector.database.DatabaseConfig;
import com.joakimcolloz.stocker.datacollector.model.Candlestick;
import com.joakimcolloz.stocker.datacollector.model.Interval;
import com.joakimcolloz.stocker.datacollector.model.Range;
import com.joakimcolloz.stocker.datacollector.util.TestDatabaseUtil;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class tests the integration between the API and the database, ensuring that the data fetched from the API
 * is correctly saved to the database.
 *
 * @author Joakim Colloz
 * @version 2.0
 * @see StockDataService
 * @see YahooFinanceFetcher
 * @see YahooFinanceParser
 * @see CandlestickDao
 * @see DatabaseManager
 */
@Testcontainers
@DisplayName("API to Database Integration Tests")
public class ApiToDatabaseIntegrationIT {
    private static final Logger logger = LoggerFactory.getLogger(ApiToDatabaseIntegrationIT.class);

    @Container
    static PostgreSQLContainer<?> postgreSQLContainer = TestDatabaseUtil.createContainer(
            "stockdb_integration_test", "integration_user", "integration_password");

    private StockDataService stockDataService;
    private CandlestickDao candlestickDao;
    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() {
        logger.debug("Setting up integration test with container: {}:{}",
                postgreSQLContainer.getHost(), postgreSQLContainer.getFirstMappedPort());

        // Create config using container connection details
        DatabaseConfig config = TestDatabaseUtil.createConfig(postgreSQLContainer);

        // Initialize DatabaseManager and run migrations
        databaseManager = new DatabaseManager(config);
        databaseManager.initialize();

        // Create DAO using DatabaseManager
        candlestickDao = databaseManager.createCandlestickDao();
        candlestickDao.resetTable();

        // Initialize StockDataService
        stockDataService = new StockDataService(new DataFetcherInputValidator(), databaseManager);
        logger.debug("Integration test setup completed successfully");
    }

    @AfterEach
    void tearDown() {
        try {
            if (candlestickDao != null) {
                candlestickDao.resetTable();
            }
            if (databaseManager != null) {
                databaseManager.close();
            }
        } catch (Exception e) {
            logger.warn("Error during integration test cleanup: {}", e.getMessage());
        }
    }

    @Test
    void testAddCurrentDayDataForAAkAndABBToDb() {
        // Fetch and insert data to DB
        stockDataService.addPriceDataToDb(List.of("AAK", "ABB"), Range.ONE_DAY,
                Interval.ONE_DAY);

        // Get data from DB
        List<Candlestick> aak = candlestickDao.getAllRowsByName("AAK.ST");
        List<Candlestick> abb = candlestickDao.getAllRowsByName("ABB.ST");

        assertFalse(aak.isEmpty(), "AAK current day data should exist in DB");
        assertEquals(1, aak.size(), "There should be 1 day of AAK price data in the database");
        assertFalse(abb.isEmpty(), "ABB current day data should exist in DB");
        assertEquals(1, abb.size(), "There should be 1 day of ABB price data in the database");
    }

    @Test
    void testAddHistoricalDataForAAkAndABBToDb() {
        // Fetch and insert data to DB
        stockDataService.addPriceDataToDb(List.of("AAK", "ABB"), Range.THREE_MONTHS,
                Interval.ONE_DAY);

        // Get data from DB
        List<Candlestick> aak = candlestickDao.getAllRowsByName("AAK.ST");
        List<Candlestick> abb = candlestickDao.getAllRowsByName("ABB.ST");

        assertFalse(aak.isEmpty(), "AAK historical data should exist in DB");
        assertTrue(is3MonthsOfPriceDataInDB(aak), "There should be 3 months of AAK price data in the database");
        assertFalse(abb.isEmpty(), "ABB historical data should exist in DB");
        assertTrue(is3MonthsOfPriceDataInDB(abb), "There should be 3 months of ABB price data in the database");
    }

    private static boolean is3MonthsOfPriceDataInDB(List<Candlestick> candlesticks) {
        return candlesticks.size() >= 57 && candlesticks.size() <= 70;
    }
}
