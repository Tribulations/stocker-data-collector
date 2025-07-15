//package stocker;
//
//import org.junit.jupiter.api.AfterAll;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import stocker.data.parsers.YahooFinanceParser;
//import stocker.database.exception.DatabaseExceptionHandler;
//import stocker.database.validation.DatabaseInputValidator;
//import stocker.database.connection.DatabaseConnectionManager;
//import stocker.representation.Candlestick;
//import stocker.representation.TradingPeriod;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.StandardCharsets;
//import java.util.List;
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * Integration test for testing database insertion and querying using predefined JSON files.
// * This class uses the same JSON files as YahooFinanceParserTest but tests the database operations
// * without making actual API calls.
// */
//public class JsonToDatabaseIntegrationTest {
//    private static final Logger logger = LoggerFactory.getLogger(JsonToDatabaseIntegrationTest.class);
//    private static final String STOCK_SYMBOL = "BOL.ST";
//    private static CandlestickDao candlestickDao;
//    @BeforeEach
//    void setUp() {
//        try {
//            // Initialize the DAO with proper validation and exception handling
//            DatabaseInputValidator validator = new DatabaseInputValidator();
//            // Initialize the DAO and reset the table before each test
//            candlestickDao = new CandlestickDao(validator, exceptionHandler, connectionManager);
//            candlestickDao.resetTable();
//            logger.info("Test database table reset successfully");
//        } catch (Exception e) {
//            logger.error("Failed to set up test: {}", e.getMessage(), e);
//            fail("Test setup failed: " + e.getMessage());
//        }
//    }
//    @AfterAll
//    static void tearDown() {
//        try {
//            // Clean up after all tests
//            if (candlestickDao == null) {
//                DatabaseInputValidator validator = new DatabaseInputValidator();
//                DatabaseExceptionHandler exceptionHandler = new DatabaseExceptionHandler();
//                DatabaseConnectionManager connectionManager = new DatabaseConnectionManager();
//                candlestickDao = new CandlestickDao(validator, exceptionHandler, connectionManager);
//            }
//            candlestickDao.resetTable();
//            logger.info("Test cleanup completed successfully");
//        } catch (Exception e) {
//            logger.error("Failed to clean up after tests: {}", e.getMessage(), e);
//            // We don't fail the test here as it's just cleanup
//        }
//    }
//    @Test
//    void testAddSingleDayCandlestickToDatabase() {
//        try {
//            logger.info("Starting single day candlestick test");
//            // Arrange: Load the test JSON resource for a single day
//            String json = loadJsonFromResource("BOL.ST-1d-1d.json");
//            // Act: Parse the JSON and insert into database
//            YahooFinanceParser parser = new YahooFinanceParser(json);
//            parser.parse();
//            TradingPeriod tradingPeriod = parser.getTradingPeriod();
//            // Check if we have valid data before proceeding
//            List<Candlestick> candlesticks = tradingPeriod.getCandlesticks();
//            if (candlesticks == null || candlesticks.isEmpty()) {
//                logger.error("No candlesticks parsed from test JSON");
//                fail("No candlesticks parsed from test JSON");
//                return;
//            }
//            logger.info("Parsed {} candlesticks from JSON", candlesticks.size());
//            // Insert the data into the database
//            candlestickDao.addRows(STOCK_SYMBOL, candlesticks);
//            logger.info("Successfully inserted candlesticks into database");
//            // Query the database
//            List<Candlestick> retrievedCandlesticks = candlestickDao.getAllRowsByName(STOCK_SYMBOL);
//            logger.info("Retrieved {} candlesticks from database", retrievedCandlesticks.size());
//            // Assert
//            assertFalse(retrievedCandlesticks.isEmpty(), "Should have retrieved candlesticks from the database");
//            assertEquals(1, retrievedCandlesticks.size(), "Should have exactly one candlestick");
//            // Verify the candlestick data matches what was inserted
//            Candlestick original = candlesticks.get(0);
//            Candlestick retrieved = retrievedCandlesticks.get(0);
//            assertEquals(original.getTimestamp(), retrieved.getTimestamp(), "Timestamp should match");
//            assertEquals(original.getOpen(), retrieved.getOpen(), 0.001, "Open price should match");
//            assertEquals(original.getClose(), retrieved.getClose(), 0.001, "Close price should match");
//            assertEquals(original.getHigh(), retrieved.getHigh(), 0.001, "High price should match");
//            assertEquals(original.getLow(), retrieved.getLow(), 0.001, "Low price should match");
//            assertEquals(original.getVolume(), retrieved.getVolume(), "Volume should match");
//            logger.info("Single day candlestick test completed successfully");
//        } catch (IOException e) {
//            logger.error("Failed to load or parse test resources: {}", e.getMessage(), e);
//            fail("Test failed due to IO error: " + e.getMessage());
//        } catch (Exception e) {
//            logger.error("Unexpected error in single day test: {}", e.getMessage(), e);
//            fail("Test failed with unexpected error: " + e.getMessage());
//        }
//    }
//    @Test
//    void testAdd3MonthCandlesticksToDatabase() {
//        try {
//            logger.info("Starting 3-month candlesticks test");
//            // Arrange: Load the test JSON resource for 3 months
//            String json = loadJsonFromResource("BOL.ST-1d-3month.json");
//            // Act: Parse the JSON and insert into database
//            YahooFinanceParser parser = new YahooFinanceParser(json);
//            parser.parse();
//            TradingPeriod tradingPeriod = parser.getTradingPeriod();
//            // Check if we have valid data before proceeding
//            List<Candlestick> candlesticks = tradingPeriod.getCandlesticks();
//            if (candlesticks == null || candlesticks.isEmpty()) {
//                logger.error("No candlesticks parsed from 3-month test JSON");
//                fail("No candlesticks parsed from 3-month test JSON");
//                return;
//            }
//            logger.info("Parsed {} candlesticks from JSON", candlesticks.size());
//            // Insert the data into the database
//            candlestickDao.addRows(STOCK_SYMBOL, candlesticks);
//            logger.info("Successfully inserted candlesticks into database");
//            // Query the database
//            List<Candlestick> retrievedCandlesticks = candlestickDao.getAllRowsByName(STOCK_SYMBOL);
//            logger.info("Retrieved {} candlesticks from database", retrievedCandlesticks.size());
//            // Assert
//            assertFalse(retrievedCandlesticks.isEmpty(), "Should have retrieved candlesticks from the database");
//            assertEquals(60, retrievedCandlesticks.size(), "Should have 60 candlesticks for 3 months of daily data");
//            // Verify first and last candlesticks match
//            verifyFirstAndLastCandlesticks(candlesticks, retrievedCandlesticks);
//            logger.info("3-month candlesticks test completed successfully");
//        } catch (IOException e) {
//            logger.error("Failed to load or parse 3-month test resource: {}", e.getMessage(), e);
//            fail("Test failed due to IO error: " + e.getMessage());
//        } catch (Exception e) {
//            logger.error("Unexpected error in 3-month test: {}", e.getMessage(), e);
//            fail("Test failed with unexpected error: " + e.getMessage());
//        }
//    }
//    @Test
//    void testAddMultipleStocksToDatabase() {
//        try {
//            logger.info("Starting multiple stocks test");
//            // Arrange: Load both JSON resources
//            String jsonOneDay = loadJsonFromResource("BOL.ST-1d-1d.json");
//            String json3Month = loadJsonFromResource("BOL.ST-1d-3month.json");
//            // Create a second stock symbol for testing
//            String secondStockSymbol = "TEST.ST";
//            // Act: Parse the JSONs and insert into database
//            // First stock with 1-day data
//            YahooFinanceParser parser1 = new YahooFinanceParser(jsonOneDay);
//            parser1.parse();
//            TradingPeriod tradingPeriod1 = parser1.getTradingPeriod();
//            List<Candlestick> candlesticks1 = tradingPeriod1.getCandlesticks();
//            // Second stock with 3-month data
//            YahooFinanceParser parser2 = new YahooFinanceParser(json3Month);
//            parser2.parse();
//            TradingPeriod tradingPeriod2 = parser2.getTradingPeriod();
//            List<Candlestick> candlesticks2 = tradingPeriod2.getCandlesticks();
//            // Check if we have valid data before proceeding
//            if (candlesticks1 == null || candlesticks1.isEmpty()) {
//                logger.error("No candlesticks parsed from 1-day test JSON");
//                fail("No candlesticks parsed from 1-day test JSON");
//                return;
//            }
//            if (candlesticks2 == null || candlesticks2.isEmpty()) {
//                logger.error("No candlesticks parsed from 3-month test JSON");
//                fail("No candlesticks parsed from 3-month test JSON");
//                return;
//            }
//            logger.info("Parsed {} candlesticks for first stock and {} for second stock",
//                    candlesticks1.size(), candlesticks2.size());
//            // Insert the data into the database
//            candlestickDao.addRows(STOCK_SYMBOL, candlesticks1);
//            candlestickDao.addRows(secondStockSymbol, candlesticks2);
//            logger.info("Successfully inserted candlesticks for both stocks into database");
//            // Query the database for both stocks
//            List<Candlestick> retrievedStock1 = candlestickDao.getAllRowsByName(STOCK_SYMBOL);
//            List<Candlestick> retrievedStock2 = candlestickDao.getAllRowsByName(secondStockSymbol);
//            logger.info("Retrieved {} candlesticks for first stock and {} for second stock",
//                    retrievedStock1.size(), retrievedStock2.size());
//            // Assert
//            assertFalse(retrievedStock1.isEmpty(), "Should have retrieved candlesticks for first stock");
//            assertFalse(retrievedStock2.isEmpty(), "Should have retrieved candlesticks for second stock");
//            assertEquals(1, retrievedStock1.size(), "Should have 1 candlestick for first stock");
//            assertEquals(60, retrievedStock2.size(), "Should have 60 candlesticks for second stock");
//            // Query all candlesticks to verify total count
//            List<Candlestick> allCandlesticks = candlestickDao.getAllRows();
//            assertEquals(61, allCandlesticks.size(), "Total should be 61 candlesticks (1 + 60)");
//            logger.info("Multiple stocks test completed successfully");
//        } catch (IOException e) {
//            logger.error("Failed to load or parse test resources: {}", e.getMessage(), e);
//            fail("Test failed due to IO error: " + e.getMessage());
//        } catch (Exception e) {
//            logger.error("Unexpected error in multiple stocks test: {}", e.getMessage(), e);
//            fail("Test failed with unexpected error: " + e.getMessage());
//        }
//    }
//    /**
//     * Helper method to load JSON from a resource file
//     * @param resourceName Name of the resource file to load
//     * @return String containing the JSON content
//     * @throws IOException if the resource cannot be read
//     */
//    private String loadJsonFromResource(String resourceName) throws IOException {
//        logger.debug("Loading test resource: {}", resourceName);
//        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName)) {
//            assertNotNull(is, "Test resource " + resourceName + " not found in classpath");
//            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
//            logger.debug("Successfully loaded {} bytes from {}", content.length(), resourceName);
//            return content;
//        } catch (IOException e) {
//            logger.error("Failed to load resource {}: {}", resourceName, e.getMessage(), e);
//            throw e; // Re-throw to fail the test
//        }
//    }
//    /**
//     * Helper method to verify that the first and last candlesticks in two lists match
//     *
//     * @param original The original list of candlesticks
//     * @param retrieved The list of candlesticks retrieved from the database
//     */
//    private void verifyFirstAndLastCandlesticks(List<Candlestick> original, List<Candlestick> retrieved) {
//        try {
//            // Verify the lists are not empty
//            if (original == null || original.isEmpty() || retrieved == null || retrieved.isEmpty()) {
//                logger.error("Cannot verify candlesticks: One or both lists are null or empty");
//                fail("Cannot verify candlesticks: One or both lists are null or empty");
//                return;
//            }
//            // Verify first candlestick
//            Candlestick firstOriginal = original.get(0);
//            Candlestick firstRetrieved = retrieved.get(0);
//            assertEquals(firstOriginal.getTimestamp(), firstRetrieved.getTimestamp(), "First timestamp should match");
//            assertEquals(firstOriginal.getOpen(), firstRetrieved.getOpen(), 0.001, "First open price should match");
//            assertEquals(firstOriginal.getClose(), firstRetrieved.getClose(), 0.001, "First close price should match");
//            logger.debug("First candlestick verification successful");
//            // Verify last candlestick
//            Candlestick lastOriginal = original.get(original.size() - 1);
//            Candlestick lastRetrieved = retrieved.get(retrieved.size() - 1);
//            assertEquals(lastOriginal.getTimestamp(), lastRetrieved.getTimestamp(), "Last timestamp should match");
//            assertEquals(lastOriginal.getOpen(), lastRetrieved.getOpen(), 0.001, "Last open price should match");
//            assertEquals(lastOriginal.getClose(), lastRetrieved.getClose(), 0.001, "Last close price should match");
//            logger.debug("Last candlestick verification successful");
//        } catch (IndexOutOfBoundsException e) {
//            logger.error("Index out of bounds while verifying candlesticks: {}", e.getMessage(), e);
//            fail("Index out of bounds while verifying candlesticks: " + e.getMessage());
//        } catch (Exception e) {
//            logger.error("Unexpected error while verifying candlesticks: {}", e.getMessage(), e);
//            fail("Unexpected error while verifying candlesticks: " + e.getMessage());
//        }
//    }
//
//}