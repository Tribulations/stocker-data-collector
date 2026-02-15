package com.joakimcolloz.stocker.datacollector.database;

import com.joakimcolloz.stocker.datacollector.database.validation.DatabaseInputValidator;
import com.joakimcolloz.stocker.datacollector.model.Candlestick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class IntradayCandlestickDao {
    private static final Logger logger = LoggerFactory.getLogger(IntradayCandlestickDao.class);

    private static final String TABLE = "stock_prices_schema.stock_prices_intraday";

    private static final String INSERT_QUERY = "INSERT INTO " + TABLE +
            " (timestamp, interval, open, close, low, high, volume, symbol) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (timestamp, symbol, interval) DO UPDATE SET " +
            "open = EXCLUDED.open, " +
            "high = EXCLUDED.high, " +
            "low = EXCLUDED.low, " +
            "close = EXCLUDED.close, " +
            "volume = EXCLUDED.volume, " +
            "updated_at = CURRENT_TIMESTAMP";

    private static final String SELECT_ALL_QUERY = "SELECT * FROM " + TABLE;

    private static final String SELECT_BY_SYMBOL_AND_INTERVAL_QUERY = "SELECT * FROM " + TABLE +
            " WHERE symbol = ? AND interval = ? ORDER BY timestamp";

    private static final String RESET_TABLE_QUERY = "TRUNCATE TABLE " + TABLE;

    private final DatabaseInputValidator validator;
    private final DatabaseManager databaseManager;

    public IntradayCandlestickDao(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
        this.validator = new DatabaseInputValidator();
    }

    public IntradayCandlestickDao(DatabaseManager databaseManager, DatabaseInputValidator validator) {
        this.databaseManager = databaseManager;
        this.validator = validator;
    }

    public List<Candlestick> getAllRows() {
        List<Candlestick> candlesticks = new ArrayList<>();

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                candlesticks.add(createCandlestick(resultSet));
            }
        } catch (SQLException e) {
            logger.error("Error retrieving intraday candlesticks: {}", e.getMessage(), e);
        }

        return candlesticks;
    }

    public List<Candlestick> getAllRowsBySymbolAndInterval(String symbol, String interval) {
        List<Candlestick> candlesticks = new ArrayList<>();

        validator.validateSymbol(symbol);
        validateInterval(interval);

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(SELECT_BY_SYMBOL_AND_INTERVAL_QUERY)) {

            statement.setString(1, symbol);
            statement.setString(2, interval);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    candlesticks.add(createCandlestick(resultSet));
                }
            }

        } catch (SQLException e) {
            logger.error("Error retrieving intraday candlesticks for symbol {} interval {}: {}",
                    symbol, interval, e.getMessage(), e);
        }

        return candlesticks;
    }

    public void addRows(String symbol, String interval, List<Candlestick> candlesticks) {
        validator.validateSymbol(symbol);
        validateInterval(interval);
        validator.validateCandlesticksList(candlesticks);

        for (int i = 0; i < candlesticks.size(); i++) {
            validator.validateCandlestick(candlesticks.get(i));
        }

        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement statement = connection.prepareStatement(INSERT_QUERY)) {
                for (Candlestick candlestick : candlesticks) {
                    statement.setLong(1, candlestick.timestamp());
                    statement.setString(2, interval);
                    statement.setDouble(3, candlestick.open());
                    statement.setDouble(4, candlestick.close());
                    statement.setDouble(5, candlestick.low());
                    statement.setDouble(6, candlestick.high());
                    statement.setLong(7, candlestick.volume());
                    statement.setString(8, symbol);
                    statement.addBatch();
                }

                int[] results = statement.executeBatch();
                connection.commit();
                validateBatchResults(results, candlesticks.size(), symbol, interval);

            } catch (SQLException e) {
                attemptRollback(connection, symbol, interval);
                throw new RuntimeException("Failed to add intraday candlesticks for symbol: " + symbol + " interval: " + interval, e);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to establish database connection for intraday insert", e);
        }
    }

    public void resetTable() {
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(RESET_TABLE_QUERY)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to reset intraday table", e);
        }
    }

    private void validateInterval(String interval) {
        if (interval == null) {
            throw new IllegalArgumentException("Interval cannot be null");
        }
        if (interval.trim().isEmpty()) {
            throw new IllegalArgumentException("Interval cannot be empty");
        }
    }

    private void validateBatchResults(int[] results, int expectedCount, String symbol, String interval) {
        int successCount = 0;
        int failureCount = 0;

        for (int result : results) {
            if (result > 0 || result == Statement.SUCCESS_NO_INFO || result == 0) {
                successCount++;
            } else {
                failureCount++;
            }
        }

        if (failureCount > 0 || successCount != expectedCount) {
            throw new RuntimeException("Batch insert had failures for symbol " + symbol + " interval " + interval);
        }
    }

    private void attemptRollback(Connection connection, String symbol, String interval) {
        try {
            connection.rollback();
        } catch (SQLException rollbackEx) {
            logger.error("Error during rollback for intraday insert for symbol {} interval {}: {}",
                    symbol, interval, rollbackEx.getMessage(), rollbackEx);
        }
    }

    private Candlestick createCandlestick(ResultSet resultSet) throws SQLException {
        return new Candlestick(
                resultSet.getDouble("open"),
                resultSet.getDouble("high"),
                resultSet.getDouble("low"),
                resultSet.getDouble("close"),
                resultSet.getLong("volume"),
                resultSet.getLong("timestamp"));
    }
}
