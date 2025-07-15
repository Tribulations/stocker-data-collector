package stocker;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stocker.data.parsers.YahooFinanceParser;
import stocker.database.CandlestickDao;
import stocker.database.validation.DatabaseInputValidator;
import stocker.model.Candlestick;
import stocker.model.TradingPeriod;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Integration tests for parsing JSON fetched from Yahoo Finance and inserting into the database.
 * Tests the complete flow from JSON data to database storage and retrieval
 * using predefined test JSON files without making actual API calls.
 */
@DisplayName("Yahoo Finance JSON to Database Integration Tests")
class YahooFinanceJsonToDatabaseTest {

    private static final Logger logger = LoggerFactory.getLogger(YahooFinanceJsonToDatabaseTest.class);
    private static final String PRIMARY_STOCK_SYMBOL = "BOL.ST";
    private static final String SECONDARY_STOCK_SYMBOL = "TEST.ST";
    private static final double PRICE_DELTA = 0.01; // Tolerance for floating-point price comparisons
    private static final int EXPECTED_THREE_MONTH_CANDLESTICKS = 60;

    private static CandlestickDao candlestickDao;

    @BeforeEach
    void setUp() {
        logger.info("Setting up integration test");
        try {
            DatabaseInputValidator validator = new DatabaseInputValidator();
            candlestickDao = new CandlestickDao(validator);
            candlestickDao.resetTable();
        } catch (Exception e) {
            logger.error("Failed to set up test: {}", e.getMessage(), e);
            fail("Test setup failed: " + e.getMessage());
        }
    }

    @AfterAll
    static void tearDown() {
        logger.info("Cleaning up after integration tests");
        try {
            if (candlestickDao == null) {
                DatabaseInputValidator validator = new DatabaseInputValidator();
                candlestickDao = new CandlestickDao(validator);
            }
            candlestickDao.resetTable();
            logger.debug("Test cleanup completed successfully");
        } catch (Exception e) {
            logger.error("Failed to clean up after tests: {}", e.getMessage(), e);
        }
    }

    @Test
    @DisplayName("Should successfully insert and retrieve single day candlestick data")
    void shouldInsertAndRetrieveSingleDayCandlestick() {
        // Arrange
        String json = loadJsonFromResource("BOL.ST-1d-1d.json");
        List<Candlestick> originalCandlesticks = parseJsonToCandlesticks(json);

        assertFalse(originalCandlesticks.isEmpty(), "Should have parsed candlesticks from JSON");

        // Act
        candlestickDao.addRows(PRIMARY_STOCK_SYMBOL, originalCandlesticks);
        List<Candlestick> retrievedCandlesticks = candlestickDao.getAllRowsByName(PRIMARY_STOCK_SYMBOL);

        // Assert
        assertEquals(1, retrievedCandlesticks.size(), "Should have exactly one candlestick");
        verifyCandlestickDataMatches(originalCandlesticks.get(0), retrievedCandlesticks.get(0));
    }

    @Test
    @DisplayName("Should successfully insert and retrieve three months of candlestick data")
    void shouldInsertAndRetrieveThreeMonthsCandlesticks() {
        // Arrange
        String json = loadJsonFromResource("BOL.ST-1d-3month.json");
        List<Candlestick> originalCandlesticks = parseJsonToCandlesticks(json);

        assertFalse(originalCandlesticks.isEmpty(), "Should have parsed candlesticks from JSON");

        // Act
        candlestickDao.addRows(PRIMARY_STOCK_SYMBOL, originalCandlesticks);
        List<Candlestick> retrievedCandlesticks = candlestickDao.getAllRowsByName(PRIMARY_STOCK_SYMBOL);

        // Assert
        assertEquals(EXPECTED_THREE_MONTH_CANDLESTICKS, retrievedCandlesticks.size(),
                "Should have " + EXPECTED_THREE_MONTH_CANDLESTICKS + " candlesticks for 3 months of daily data");
        verifyFirstAndLastCandlesticks(originalCandlesticks, retrievedCandlesticks);
    }

    @Test
    @DisplayName("Should handle multiple stocks with different data sets")
    void shouldHandleMultipleStocksWithDifferentDataSets() {
        // Arrange
        String oneDayJson = loadJsonFromResource("BOL.ST-1d-1d.json");
        String threeMonthJson = loadJsonFromResource("BOL.ST-1d-3month.json");

        List<Candlestick> oneDayCandlesticks = parseJsonToCandlesticks(oneDayJson);
        List<Candlestick> threeMonthCandlesticks = parseJsonToCandlesticks(threeMonthJson);

        assertFalse(oneDayCandlesticks.isEmpty(), "Should have parsed 1-day candlesticks");
        assertFalse(threeMonthCandlesticks.isEmpty(), "Should have parsed 3-month candlesticks");

        // Act
        candlestickDao.addRows(PRIMARY_STOCK_SYMBOL, oneDayCandlesticks);
        candlestickDao.addRows(SECONDARY_STOCK_SYMBOL, threeMonthCandlesticks);

        List<Candlestick> primaryStockData = candlestickDao.getAllRowsByName(PRIMARY_STOCK_SYMBOL);
        List<Candlestick> secondaryStockData = candlestickDao.getAllRowsByName(SECONDARY_STOCK_SYMBOL);
        List<Candlestick> allCandlesticks = candlestickDao.getAllRows();

        // Assert
        assertEquals(1, primaryStockData.size(), "Should have 1 candlestick for primary stock");
        assertEquals(EXPECTED_THREE_MONTH_CANDLESTICKS, secondaryStockData.size(),
                "Should have " + EXPECTED_THREE_MONTH_CANDLESTICKS + " candlesticks for secondary stock");
        assertEquals(61, allCandlesticks.size(), "Total should be 61 candlesticks (1 + 60)");
    }

    @Test
    @DisplayName("Should handle empty result when querying non-existent stock")
    void shouldHandleEmptyResultForNonExistentStock() {
        // Act
        List<Candlestick> result = candlestickDao.getAllRowsByName("NON_EXISTENT");

        // Assert
        assertTrue(result.isEmpty(), "Should return empty list for non-existent stock");
    }

    /**
     * Loads JSON content from a resource file.
     *
     * @param resourceName the name of the resource file
     * @return JSON content as string
     * @throws RuntimeException if the resource cannot be loaded
     */
    private String loadJsonFromResource(String resourceName) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName)) {
            assertNotNull(inputStream, "Test resource '" + resourceName + "' not found in classpath");

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("loadJsonFromResource Failed to load test resource: " + resourceName, e);
        }
    }

    /**
     * Parses JSON string into a list of candlesticks.
     *
     * @param json the JSON string to parse
     * @return list of parsed candlesticks
     * @throws RuntimeException if parsing fails
     */
    private List<Candlestick> parseJsonToCandlesticks(String json) {
        try {
            YahooFinanceParser parser = new YahooFinanceParser(json);
            parser.parse();
            TradingPeriod tradingPeriod = parser.getTradingPeriod();

            assertNotNull(tradingPeriod, "Trading period should not be null");
            assertNotNull(tradingPeriod.candlesticks(), "Candlesticks list should not be null");

            return tradingPeriod.candlesticks();

        } catch (Exception e) {
            throw new RuntimeException("parseJsonToCandlesticks Failed to parse JSON data", e);
        }
    }

    /**
     * Verifies that two candlesticks have matching data.
     *
     * @param expected the expected candlestick
     * @param actual the actual candlestick
     */
    private void verifyCandlestickDataMatches(Candlestick expected, Candlestick actual) {
        assertAll("Candlestick data should match",
                () -> assertEquals(expected.getTimestamp(), actual.getTimestamp(), "Timestamp should match"),
                () -> assertEquals(expected.getOpen(), actual.getOpen(), PRICE_DELTA, "Open price should match"),
                () -> assertEquals(expected.getClose(), actual.getClose(), PRICE_DELTA, "Close price should match"),
                () -> assertEquals(expected.getHigh(), actual.getHigh(), PRICE_DELTA, "High price should match"),
                () -> assertEquals(expected.getLow(), actual.getLow(), PRICE_DELTA, "Low price should match"),
                () -> assertEquals(expected.getVolume(), actual.getVolume(), "Volume should match")
        );
    }

    /**
     * Verifies that the first and last candlesticks in two lists match.
     *
     * @param original the original list of candlesticks
     * @param retrieved the list of candlesticks retrieved from the database
     */
    private void verifyFirstAndLastCandlesticks(List<Candlestick> original, List<Candlestick> retrieved) {
        assertFalse(original.isEmpty(), "Original list should not be empty");
        assertFalse(retrieved.isEmpty(), "Retrieved list should not be empty");
        assertEquals(original.size(), retrieved.size(), "Lists should have the same size");

        // Verify first candlestick
        verifyCandlestickDataMatches(original.get(0), retrieved.get(0));

        // Verify last candlestick
        int lastIndex = original.size() - 1;
        verifyCandlestickDataMatches(original.get(lastIndex), retrieved.get(lastIndex));
    }
}