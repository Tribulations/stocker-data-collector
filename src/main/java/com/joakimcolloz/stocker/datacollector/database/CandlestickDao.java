package com.joakimcolloz.stocker.datacollector.database;

import com.joakimcolloz.stocker.datacollector.model.Candlestick;
import com.joakimcolloz.stocker.datacollector.database.validation.DatabaseInputValidator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static com.joakimcolloz.stocker.datacollector.database.DbConstants.CLOSE_COLUMN;
import static com.joakimcolloz.stocker.datacollector.database.DbConstants.HIGH_COLUMN;
import static com.joakimcolloz.stocker.datacollector.database.DbConstants.INSERT_CANDLESTICK_QUERY;
import static com.joakimcolloz.stocker.datacollector.database.DbConstants.LOW_COLUMN;
import static com.joakimcolloz.stocker.datacollector.database.DbConstants.OPEN_COLUMN;
import static com.joakimcolloz.stocker.datacollector.database.DbConstants.RESET_TABLE_QUERY;
import static com.joakimcolloz.stocker.datacollector.database.DbConstants.SELECT_ALL_QUERY;
import static com.joakimcolloz.stocker.datacollector.database.DbConstants.SELECT_BY_SYMBOL_QUERY;
import static com.joakimcolloz.stocker.datacollector.database.DbConstants.TIMESTAMP_COLUMN;
import static com.joakimcolloz.stocker.datacollector.database.DbConstants.VOLUME_COLUMN;

/**
 * Database access object class. Used to interact with the database for candlestick data.
 * Uses {@link DatabaseManager} for connection management.
 *
 * @author Joakim Colloz
 * @version 2.0
 * @see DatabaseInputValidator
 * @see Candlestick
 * @see DAO
 * @see DbConstants
 * @see DatabaseManager
 * @since 1.0
 */
public class CandlestickDao implements DAO<Candlestick> {
    private static final Logger logger = LoggerFactory.getLogger(CandlestickDao.class);
    private final DatabaseInputValidator validator;
    private final DatabaseManager databaseManager;

    /**
     * Constructor that uses {@link DatabaseManager} for connection management.
     * Preferably use {@link DatabaseManager#createCandlestickDao()} instead of calling this constructor directly.
     * @param databaseManager the database manager to use for connections
     */
    public CandlestickDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.validator = new DatabaseInputValidator();
        logger.debug("CandlestickDao initialized with DatabaseManager and validator");
    }

    /**
     * Constructor for dependency injection (for testing).
     *
     * @param databaseManager the database manager to use for connections
     * @param validator the database input validator to use
     */
    public CandlestickDao(DatabaseManager databaseManager, DatabaseInputValidator validator) {
        this.databaseManager = databaseManager;
        this.validator = validator;
        logger.debug("CandlestickDao initialized with injected DatabaseManager and validator");
    }

    /**
     * Retrieves all candlesticks from the database.
     * Note: This method could potentially return a very large dataset if the table has many records.
     * Consider using pagination or more specific queries when possible.
     *
     * @return a list of all candlesticks in the database
     */
    @Override
    public List<Candlestick> getAllRows() {
        List<Candlestick> candlesticks = new ArrayList<>();
        logger.debug("Starting to retrieve all candlesticks from database");

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet resultSet = statement.executeQuery()) {

            int count = 0;
            while (resultSet.next()) {
                Candlestick candlestick = createCandlestick(resultSet);
                candlesticks.add(candlestick);
                count++;
            }

            logger.info("Retrieved {} candlesticks from the database", count);
        } catch (SQLException e) {
            logger.error("Error retrieving all candlesticks: {}", e.getMessage(), e);
        }

        return candlesticks;
    }

    @Override
    public List<Candlestick> getAllRowsByName(final String name) {
        List<Candlestick> candlesticks = new ArrayList<>();

        // Validate input
        try {
            validator.validateSymbol(name);
            logger.debug("Symbol validation passed for: {}", name);
        } catch (IllegalArgumentException e) {
            logger.error("Symbol validation failed: {}", e.getMessage());
            return candlesticks; // Return empty list for invalid input
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_SYMBOL_QUERY)) {

            statement.setString(1, name);
            logger.debug("Executing query for symbol: {}", name);

            try (ResultSet resultSet = statement.executeQuery()) {
                int count = 0;
                while (resultSet.next()) {
                    Candlestick candlestick = createCandlestick(resultSet);
                    candlesticks.add(candlestick);
                    count++;
                }
                logger.info("Retrieved {} candlesticks for symbol: {}", count, name);
            }
        } catch (SQLException e) {
            logger.error("Error getting candlesticks for symbol {}: {}", name, e.getMessage(), e);
        }

        return candlesticks;
    }

    @Override
    public void addRow(String symbol, Candlestick candlestick) {
        logger.debug("Starting to add single candlestick for symbol: {}", symbol);

        // Validate inputs
        try {
            validator.validateSymbol(symbol);
            validator.validateCandlestick(candlestick);
            logger.debug("Input validation passed for addRow");
        } catch (IllegalArgumentException e) {
            logger.error("Validation failed for addRow: {}", e.getMessage());
            throw e; // Let unchecked exception bubble up
        }

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_CANDLESTICK_QUERY)) {

            statement.setLong(1, candlestick.timestamp());
            statement.setDouble(2, candlestick.open());
            statement.setDouble(3, candlestick.close());
            statement.setDouble(4, candlestick.low());
            statement.setDouble(5, candlestick.high());
            statement.setDouble(6, candlestick.volume());
            statement.setString(7, symbol);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully added candlestick for symbol: {}", symbol);
            } else {
                logger.warn("No rows affected when adding candlestick for symbol: {}", symbol);
            }
        } catch (SQLException e) {
            logger.error("Error adding candlestick for symbol {}: {}", symbol, e.getMessage(), e);
            throw new RuntimeException("Failed to add candlestick for symbol: " + symbol, e);
        }
    }

    @Override
    public void resetTable() {
        logger.info("Starting to reset/truncate candlestick table");

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(RESET_TABLE_QUERY)) {

            statement.executeUpdate();
            logger.info("Table truncated successfully.");
        } catch (SQLException e) {
            logger.error("Error truncating table: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to reset table", e);
        }
    }

    /**
     * Adds multiple candlesticks to the database using batch processing.
     * If a candlestick with the same symbol and date already exists,
     * a database trigger will update the existing row with the new data instead of inserting a duplicate.
     * <p>
     * The batch result is considered successful if the result is:
     * <ul>
     *   <li>&gt;0: row inserted</li>
     *   <li>0: trigger updated an existing row (insert skipped)</li>
     *   <li>Statement.SUCCESS_NO_INFO: driver could not determine affected row count</li>
     * </ul>
     * Any other result or a thrown SQLException is considered a failure. The operation is transactional:
     * all candlesticks must pass validation and be successfully processed, or no changes will be made to the database.
     *
     * @param symbol        the stock symbol
     * @param candlesticks  list of candlesticks to add to the database
     * @throws IllegalArgumentException if symbol is invalid, candlesticks list is null/empty,
     *                                  or any individual candlestick fails validation
     * @throws RuntimeException         if database connection fails, batch execution fails,
     *                                  or not all candlesticks are successfully processed
     */
    @Override
    public void addRows(String symbol, List<Candlestick> candlesticks) {
        logger.info("Starting to add {} candlesticks for symbol: {}",
                candlesticks != null ? candlesticks.size() : 0, symbol);

        validateInputs(symbol, candlesticks);
        logger.debug("All {} candlesticks passed validation for symbol: {}", candlesticks.size(), symbol);

        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false); // Start transaction
            logger.debug("Starting batch insert of {} candlesticks for symbol: {}", candlesticks.size(), symbol);

            try (PreparedStatement statement = connection.prepareStatement(INSERT_CANDLESTICK_QUERY)) {
                batchInsert(symbol, candlesticks, statement, connection);
            } catch (SQLException e) {
                logger.error("Database error during batch insert for symbol {}: {}", symbol, e.getMessage(), e);
                attemptRollback(symbol, connection);
                throw new RuntimeException("Failed to add candlesticks for symbol: " + symbol, e);
            }

        } catch (SQLException e) {
            logger.error("Error establishing database connection for symbol {}: {}", symbol, e.getMessage(), e);
            throw new RuntimeException("Failed to establish database connection for symbol: " + symbol, e);
        }

        logger.info("Successfully processed all {} candlesticks for symbol: {}", candlesticks.size(), symbol);
    }

    private void validateInputs(String symbol, List<Candlestick> candlesticks) {
        try {
            validator.validateSymbol(symbol);
            validator.validateCandlesticksList(candlesticks);
            logger.debug("Input validation passed for addRows");
        } catch (IllegalArgumentException e) {
            logger.error("Validation failed for addRows: {}", e.getMessage());
            throw e; // Let unchecked exception bubble up
        }

        // All candlesticks must be valid or none are processed
        for (int i = 0; i < candlesticks.size(); i++) {
            try {
                validator.validateCandlestick(candlesticks.get(i));
            } catch (IllegalArgumentException e) {
                String errorMsg = "Invalid candlestick at index " + i + " for symbol " + symbol + ": " + e.getMessage();
                logger.error(errorMsg);
                throw new IllegalArgumentException(errorMsg);
            }
        }
    }

    private void batchInsert(String symbol, List<Candlestick> candlesticks,
                             PreparedStatement statement, Connection connection) throws SQLException {
        for (Candlestick candlestick : candlesticks) {
            statement.setLong(1, candlestick.timestamp());
            statement.setDouble(2, candlestick.open());
            statement.setDouble(3, candlestick.close());
            statement.setDouble(4, candlestick.low());
            statement.setDouble(5, candlestick.high());
            statement.setDouble(6, candlestick.volume());
            statement.setString(7, symbol);
            statement.addBatch();
        }

        // Execute batch and commit transaction
        int[] results = statement.executeBatch();
        connection.commit();

        validateBatchResults(results, candlesticks.size(), symbol);
    }

    /**
     * Validates the results of a batch insert.
     * Treats result > 0 (inserted), 0 (updated by trigger), and Statement.SUCCESS_NO_INFO as success.
     * Any other result or a thrown SQLException is considered a failure.
     *
     * @param results       the batch execution result array
     * @param expectedCount the number of attempted inserts
     * @param symbol        the stock symbol
     * @throws RuntimeException if any batch result is a true failure or not all were successful
     */
    private void validateBatchResults(int[] results, int expectedCount, String symbol) {
        int successCount = 0;
        int failureCount = 0;

        for (int result : results) {
            if (result > 0 || result == Statement.SUCCESS_NO_INFO || result == 0) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        logger.info("Successfully added {} candlesticks for symbol: {}", successCount, symbol);

        if (failureCount > 0) {
            String errorMsg = "Batch execution had " + failureCount + " failures out of " +
                    expectedCount + " attempts for symbol: " + symbol;
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }

        // Ensure all candlesticks were successfully inserted
        if (successCount != expectedCount) {
            String errorMsg = "Expected to insert " + expectedCount +
                    " candlesticks but only " + successCount +
                    " were successful for symbol: " + symbol;
            logger.error(errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }

    private void attemptRollback(String symbol, Connection connection) {
        try {
            connection.rollback();
            logger.info("Transaction rolled back successfully for symbol: {}", symbol);
        } catch (SQLException rollbackEx) {
            logger.error("Error during rollback for symbol {}: {}", symbol, rollbackEx.getMessage(), rollbackEx);
        }
    }

    /**
     * Populates a Candlestick object with data from a ResultSet.
     * This method handles the mapping of database columns to Candlestick properties.
     *
     * @param resultSet the ResultSet containing the data to extract
     * @return the populated Candlestick object
     * @throws SQLException if a database access error occurs or the column names don't exist
     */
    private Candlestick createCandlestick(final ResultSet resultSet) throws SQLException {
        try {
            return new Candlestick(resultSet.getDouble(OPEN_COLUMN), resultSet.getDouble(CLOSE_COLUMN),
                    resultSet.getDouble(LOW_COLUMN), resultSet.getDouble(HIGH_COLUMN),
                    resultSet.getLong(VOLUME_COLUMN), resultSet.getLong(TIMESTAMP_COLUMN));
        } catch (SQLException e) {
            logger.error("Error setting candlestick properties: {}", e.getMessage(), e);
            throw e; // Rethrow to allow handling by caller
        }
    }
}
