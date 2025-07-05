package stocker.database;

import stocker.representation.Candlestick;
import stocker.support.StockAppLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static stocker.database.DbConstants.*;

/**
 * Database access object class. Used to interact with the database.
 *
 * @author Joakim Colloz
 * @version 1.0
 * @since 1.0
 */
public class CandlestickDao implements DAO<Candlestick> {

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

        try (Connection connection = getDbConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet resultSet = statement.executeQuery()) {

            int count = 0;
            while (resultSet.next()) {
                Candlestick candlestick = new Candlestick();
                setCandleStick(resultSet, candlestick);
                candlesticks.add(candlestick);
                count++;
            }
            
            StockAppLogger.INSTANCE.logInfo("Retrieved " + count + " candlesticks from the database");
        } catch (SQLException e) {
            StockAppLogger.INSTANCE.logInfo("Error retrieving all candlesticks: " + e.getMessage());
            StockAppLogger.INSTANCE.logInfo("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }

        return candlesticks;
    }

    @Override
    public List<Candlestick> getAllRowsByName(final String name) {
        List<Candlestick> candlesticks = new ArrayList<>();
        
        if (name == null || name.trim().isEmpty()) {
            StockAppLogger.INSTANCE.logInfo("Cannot get rows: Symbol name is null or empty");
            return candlesticks;
        }

        try (Connection connection = getDbConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_SYMBOL_QUERY)) {
            
            statement.setString(1, name); // Set the parameter that was missing
            
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Candlestick candlestick = new Candlestick();
                    setCandleStick(resultSet, candlestick);
                    candlesticks.add(candlestick);
                }
            }
        } catch (SQLException e) {
            StockAppLogger.INSTANCE.logInfo("Error getting candlesticks for symbol " + name + ": " + e.getMessage());
            StockAppLogger.INSTANCE.logInfo("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }

        return candlesticks;
    }

    /**
     * Populates a Candlestick object with data from a ResultSet.
     * This method handles the mapping of database columns to Candlestick properties.
     * 
     * @param resultSet the ResultSet containing the data to extract
     * @param candlestick the Candlestick object to populate
     * @throws SQLException if a database access error occurs or the column names don't exist
     */
    private void setCandleStick(final ResultSet resultSet, final Candlestick candlestick) throws SQLException {
        try {
            candlestick.setTimestamp(resultSet.getLong(TIMESTAMP_COLUMN));
            candlestick.setOpen(resultSet.getDouble(OPEN_COLUMN));
            candlestick.setClose(resultSet.getDouble(CLOSE_COLUMN));
            candlestick.setLow(resultSet.getDouble(LOW_COLUMN));
            candlestick.setHigh(resultSet.getDouble(HIGH_COLUMN));
            candlestick.setVolume(resultSet.getLong(VOLUME_COLUMN));
        } catch (SQLException e) {
            StockAppLogger.INSTANCE.logInfo("Error setting candlestick data: " + e.getMessage());
            StockAppLogger.INSTANCE.logInfo("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            throw e; // Rethrow to allow proper handling by caller
        }
    }

    @Override
    public void addRow(String symbol, Candlestick candlestick) {
        try (Connection connection = getDbConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_CANDLESTICK_QUERY)
        ) {
            statement.setLong(1, candlestick.getTimestamp());
            statement.setDouble(2, candlestick.getOpen());
            statement.setDouble(3, candlestick.getClose());
            statement.setDouble(4, candlestick.getLow());
            statement.setDouble(5, candlestick.getHigh());
            statement.setDouble(6, candlestick.getVolume());
            statement.setString(7, symbol);
            statement.setString(8, candlestick.getInterval());
            statement.executeUpdate();
        } catch (SQLException e) {
            StockAppLogger.INSTANCE.logInfo("Error adding candlestick: " + e.getMessage());
            StockAppLogger.INSTANCE.logInfo("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
    }

    @Override
    public void resetTable() {
        try (Connection connection = getDbConnection();
             PreparedStatement statement = connection.prepareStatement(RESET_TABLE_QUERY)
        ) {
            statement.executeUpdate();
        } catch (SQLException e) {
            StockAppLogger.INSTANCE.logInfo("Error resetting table: " + e.getMessage());
            StockAppLogger.INSTANCE.logInfo("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Gets a database connection.
     * @return a valid database connection
     * @throws SQLException if a database access error occurs or the url is null
     */
    protected Connection getDbConnection() throws SQLException {
        try {
            Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
            if (connection == null) {
                throw new SQLException("Failed to establish database connection");
            }
            return connection;
        } catch (SQLException e) {
            StockAppLogger.INSTANCE.logInfo("Database connection error: " + e.getMessage());
            StockAppLogger.INSTANCE.logInfo("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
            throw e; // Rethrow to allow proper handling by caller
        }
    }

    /**
     * Adds multiple older candlesticks to the database in a single transaction.
     * This method is typically used for adding historical data (e.g., a whole month or more) with 1-day interval.
     * 
     * @param symbol the stock symbol
     * @param candlesticks a list of candlesticks to add to the database
     */
    public void addMultipleOlderRows(String symbol, List<Candlestick> candlesticks) {
        if (symbol == null || symbol.trim().isEmpty() || candlesticks == null || candlesticks.isEmpty()) {
            StockAppLogger.INSTANCE.logInfo("Cannot add multiple rows: Symbol is null or candlesticks list is empty");
            return;
        }

        Connection connection = null;
        try {
            connection = getDbConnection();
            
            // Disable auto-commit to use transactions
            connection.setAutoCommit(false);
            
            String query = INSERT_CANDLESTICK_QUERY;

            try (PreparedStatement statement = connection.prepareStatement(query)) {
                for (Candlestick candlestick : candlesticks) {
                    // Adjust timestamp if needed based on interval
                    long timestamp = candlestick.getTimestamp();
                    if ("1d".equals(candlestick.getInterval())) {
                        // Add 8.5 hours (30540 seconds) to convert from 9:00 to 17:30 market close time
                        timestamp += 30540;
                    }
                    
                    statement.setLong(1, timestamp);
                    statement.setDouble(2, candlestick.getOpen());
                    statement.setDouble(3, candlestick.getClose());
                    statement.setDouble(4, candlestick.getLow());
                    statement.setDouble(5, candlestick.getHigh());
                    statement.setDouble(6, candlestick.getVolume());
                    statement.setString(7, symbol);
                    statement.setString(8, candlestick.getInterval());
                    statement.addBatch();
                }
                
                int[] results = statement.executeBatch();
                connection.commit(); // Commit the transaction
                
                int totalUpdated = 0;
                for (int result : results) {
                    if (result > 0) totalUpdated++;
                }
                
                StockAppLogger.INSTANCE.logInfo("Successfully added " + totalUpdated + 
                                              " out of " + candlesticks.size() + 
                                              " candlesticks for symbol: " + symbol);
            }
        } catch (SQLException e) {
            // Rollback on error
            if (connection != null) {
                try {
                    connection.rollback();
                    StockAppLogger.INSTANCE.logInfo("Transaction rolled back due to error");
                } catch (SQLException rollbackEx) {
                    StockAppLogger.INSTANCE.logInfo("Error during rollback: " + rollbackEx.getMessage());
                    StockAppLogger.INSTANCE.logInfo("Stack trace: " + java.util.Arrays.toString(rollbackEx.getStackTrace()));
                }
            }
            StockAppLogger.INSTANCE.logInfo("Error adding multiple candlesticks for symbol " + symbol + ": " + e.getMessage());
            StockAppLogger.INSTANCE.logInfo("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        } finally {
            // Restore auto-commit and close connection
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    StockAppLogger.INSTANCE.logInfo("Error closing connection: " + closeEx.getMessage());
                    StockAppLogger.INSTANCE.logInfo("Stack trace: " + java.util.Arrays.toString(closeEx.getStackTrace()));
                }
            }
        }
    }

    /**
     * DEPRECATED TO BE REMOVED
     * Adds one row to the database overwriting the open, close, low, high and volume if the timestamp
     * already exists for the current symbol, i.e. if the symbol AAB with datetime 2023-08-01 17:30 already exists
     * the open, close, low, high and volume will be updated for this symbol and datetime/timestamp.
     * @param symbol the stock name/symbol
     * @param candlestick the candlestick containing the price data for the symbol which should be added to th database
     */
    @Deprecated
    @Override
    public void addRowOverwrite(String symbol, Candlestick candlestick) {
        if (symbol == null || candlestick == null) {
            StockAppLogger.INSTANCE.logInfo("Cannot add row: Symbol or candlestick is null");
            return;
        }

        try (Connection connection = getDbConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_ON_CONFLICT_UPDATE_QUERY)) {
            
            statement.setLong(1, candlestick.getTimestamp());
            statement.setDouble(2, candlestick.getOpen());
            statement.setDouble(3, candlestick.getClose());
            statement.setDouble(4, candlestick.getLow());
            statement.setDouble(5, candlestick.getHigh());
            statement.setDouble(6, candlestick.getVolume());
            statement.setString(7, symbol);
            statement.setString(8, candlestick.getInterval());
            
            int rowsAffected = statement.executeUpdate();
            StockAppLogger.INSTANCE.logInfo("Updated/inserted candlestick for symbol " + symbol + 
                                          ", timestamp " + candlestick.getHumanReadableDate() + 
                                          ", rows affected: " + rowsAffected);
        } catch (SQLException e) {
            StockAppLogger.INSTANCE.logInfo("Error updating candlestick for symbol " + symbol + ": " + e.getMessage());
            StockAppLogger.INSTANCE.logInfo("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * Adds multiple candlesticks to the database.
     * Each candlestick is added as a separate database operation.
     * 
     * @param symbol the stock symbol
     * @param candlesticks list of candlesticks to add to the database
     */
    @Override
    public void addRows(String symbol, List<Candlestick> candlesticks) {
        if (symbol == null || symbol.trim().isEmpty() || candlesticks == null || candlesticks.isEmpty()) {
            StockAppLogger.INSTANCE.logInfo("Cannot add rows: Symbol is null or candlesticks list is empty");
            return;
        }

        try (Connection connection = getDbConnection()) {
            String query = INSERT_CANDLESTICK_QUERY;
            
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                int successCount = 0;
                
                for (Candlestick candlestick : candlesticks) {
//                    // Adjust timestamp if needed based on interval
//                    long timestamp = candlestick.getTimestamp();
//                    if ("1d".equals(candlestick.getInterval())) {
//                        // Add 8.5 hours (30540 seconds) to convert from 9:00 to 17:30 market close time
//                        timestamp += 30540;
//                    }

                    long timestamp = candlestick.getTimestamp();
                    statement.setLong(1, timestamp);
                    statement.setDouble(2, candlestick.getOpen());
                    statement.setDouble(3, candlestick.getClose());
                    statement.setDouble(4, candlestick.getLow());
                    statement.setDouble(5, candlestick.getHigh());
                    statement.setDouble(6, candlestick.getVolume());
                    statement.setString(7, symbol);
                    
                    int rowsAffected = statement.executeUpdate();
                    if (rowsAffected > 0) {
                        successCount++;
                    }
                }
                
                StockAppLogger.INSTANCE.logInfo("Successfully added " + successCount + 
                                              " out of " + candlesticks.size() + 
                                              " candlesticks for symbol: " + symbol);
            }
        } catch (SQLException e) {
            StockAppLogger.INSTANCE.logInfo("Error adding candlesticks for symbol " + symbol + ": " + e.getMessage());
            StockAppLogger.INSTANCE.logInfo("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * DEPRECATED TO BE REMOVED
     * Adds one row to the database if the timestamp for the current symbol does not already exist in the database.
     * This method will silently ignore conflicts (no update will be performed if the record already exists).
     * 
     * @param symbol the stock name/symbol
     * @param candlestick the candlestick containing the price data for the symbol which should be added to the database
     */
    @Deprecated
    @Override
    public void addRowNoOverwrite(String symbol, Candlestick candlestick) {
        if (symbol == null || symbol.trim().isEmpty() || candlestick == null) {
            StockAppLogger.INSTANCE.logInfo("Cannot add row: Symbol is null/empty or candlestick is null");
            return;
        }
        
        try (Connection connection = getDbConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT_ON_CONFLICT_DO_NOTHING_QUERY)) {
            // Adjust timestamp if needed based on interval
            long timestamp = candlestick.getTimestamp();
            if ("1d".equals(candlestick.getInterval())) {
                // Add 8.5 hours (30540 seconds) to convert from 9:00 to 17:30 market close time
                timestamp += 30540;
            }
            
            statement.setLong(1, timestamp);
            statement.setDouble(2, candlestick.getOpen());
            statement.setDouble(3, candlestick.getClose());
            statement.setDouble(4, candlestick.getLow());
            statement.setDouble(5, candlestick.getHigh());
            statement.setDouble(6, candlestick.getVolume());
            statement.setString(7, symbol);
            statement.setString(8, candlestick.getInterval());
            
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                StockAppLogger.INSTANCE.logInfo("Added new candlestick for symbol " + symbol + 
                                              " at timestamp " + candlestick.getHumanReadableDate());
            } else {
                StockAppLogger.INSTANCE.logInfo("Candlestick already exists for symbol " + symbol + 
                                              " at timestamp " + candlestick.getHumanReadableDate() + 
                                              ", no changes made");
            }
        } catch (SQLException e) {
            StockAppLogger.INSTANCE.logInfo("Error adding candlestick for symbol " + symbol + ": " + e.getMessage());
            StockAppLogger.INSTANCE.logInfo("Stack trace: " + java.util.Arrays.toString(e.getStackTrace()));
        }
    }
}
