package stocker.database;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * A test helper class for database tests.
 * This class helps set up JDBC mocks for testing database access objects.
 */
public class TestDatabaseDriver {
    private final Connection mockConnection;
    private final PreparedStatement mockPreparedStatement;
    private final ResultSet mockResultSet;

    /**
     * Creates a new TestDatabaseDriver with the provided mocks.
     *
     * @param mockConnection the mocked Connection
     * @param mockPreparedStatement the mocked PreparedStatement
     * @param mockResultSet the mocked ResultSet
     */
    public TestDatabaseDriver(Connection mockConnection, PreparedStatement mockPreparedStatement, ResultSet mockResultSet) {
        this.mockConnection = mockConnection;
        this.mockPreparedStatement = mockPreparedStatement;
        this.mockResultSet = mockResultSet;
    }

    /**
     * Sets up the basic mock behavior for database operations.
     *
     * @throws SQLException if a database access error occurs
     */
    public void setupBasicMockBehavior() throws SQLException {
        // Setup connection to return prepared statement
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        
        // Setup prepared statement to return result set
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
    }

    /**
     * Sets up the mock behavior for a successful insert/update operation.
     *
     * @param rowsAffected the number of rows affected by the operation
     * @throws SQLException if a database access error occurs
     */
    public void setupSuccessfulUpdate(int rowsAffected) throws SQLException {
        when(mockPreparedStatement.executeUpdate())
                .thenReturn(rowsAffected);
    }

    /**
     * Sets up the mock behavior for a successful batch operation.
     *
     * @param results the array of results from the batch operation
     * @throws SQLException if a database access error occurs
     */
    public void setupSuccessfulBatch(int[] results) throws SQLException {
        when(mockPreparedStatement.executeBatch())
                .thenReturn(results);
    }

    /**
     * Sets up the mock behavior for a database exception.
     *
     * @param method the method that should throw the exception
     * @param exception the exception to throw
     * @throws SQLException if a database access error occurs
     */
    public void setupException(String method, SQLException exception) throws SQLException {
        switch (method) {
            case "prepareStatement":
                when(mockConnection.prepareStatement(anyString()))
                        .thenThrow(exception);
                break;
            case "executeQuery":
                when(mockPreparedStatement.executeQuery())
                        .thenThrow(exception);
                break;
            case "executeUpdate":
                when(mockPreparedStatement.executeUpdate())
                        .thenThrow(exception);
                break;
            case "executeBatch":
                when(mockPreparedStatement.executeBatch())
                        .thenThrow(exception);
                break;
            default:
                throw new IllegalArgumentException("Unknown method: " + method);
        }
    }

    /**
     * Sets up the mock behavior for a result set with candlestick data.
     *
     * @param timestamps array of timestamps for each row
     * @param opens array of open prices for each row
     * @param closes array of close prices for each row
     * @param lows array of low prices for each row
     * @param highs array of high prices for each row
     * @param volumes array of volumes for each row
     * @throws SQLException if a database access error occurs
     */
    public void setupResultSetWithCandlesticks(
            long[] timestamps, double[] opens, double[] closes, 
            double[] lows, double[] highs, long[] volumes) throws SQLException {
        
        // Setup result set to return multiple rows
        Boolean[] nextResults = new Boolean[timestamps.length + 1];
        for (int i = 0; i < timestamps.length; i++) {
            nextResults[i] = true;
        }
        nextResults[timestamps.length] = false;
        
        when(mockResultSet.next())
                .thenReturn(nextResults[0], Arrays.copyOfRange(nextResults, 1, nextResults.length));
        
        // Create answer objects that will return different values based on the number of times they're called
        Answer<Long> timestampAnswer = new Answer<Long>() {
            private int callCount = 0;
            @Override
            public Long answer(InvocationOnMock invocation) {
                if (callCount >= timestamps.length) {
                    return timestamps[timestamps.length - 1]; // Return last value if called too many times
                }
                return timestamps[callCount++];
            }
        };
        
        Answer<Double> openAnswer = new Answer<Double>() {
            private int callCount = 0;
            @Override
            public Double answer(InvocationOnMock invocation) {
                if (callCount >= opens.length) {
                    return opens[opens.length - 1];
                }
                return opens[callCount++];
            }
        };
        
        Answer<Double> closeAnswer = new Answer<Double>() {
            private int callCount = 0;
            @Override
            public Double answer(InvocationOnMock invocation) {
                if (callCount >= closes.length) {
                    return closes[closes.length - 1];
                }
                return closes[callCount++];
            }
        };
        
        Answer<Double> lowAnswer = new Answer<Double>() {
            private int callCount = 0;
            @Override
            public Double answer(InvocationOnMock invocation) {
                if (callCount >= lows.length) {
                    return lows[lows.length - 1];
                }
                return lows[callCount++];
            }
        };
        
        Answer<Double> highAnswer = new Answer<Double>() {
            private int callCount = 0;
            @Override
            public Double answer(InvocationOnMock invocation) {
                if (callCount >= highs.length) {
                    return highs[highs.length - 1];
                }
                return highs[callCount++];
            }
        };
        
        Answer<Long> volumeAnswer = new Answer<Long>() {
            private int callCount = 0;
            @Override
            public Long answer(InvocationOnMock invocation) {
                if (callCount >= volumes.length) {
                    return volumes[volumes.length - 1];
                }
                return volumes[callCount++];
            }
        };

        // Setup the mock ResultSet to use our answer objects
        when(mockResultSet.getLong(DbConstants.TIMESTAMP_COLUMN)).thenAnswer(timestampAnswer);
        when(mockResultSet.getDouble(DbConstants.OPEN_COLUMN)).thenAnswer(openAnswer);
        when(mockResultSet.getDouble(DbConstants.CLOSE_COLUMN)).thenAnswer(closeAnswer);
        when(mockResultSet.getDouble(DbConstants.LOW_COLUMN)).thenAnswer(lowAnswer);
        when(mockResultSet.getDouble(DbConstants.HIGH_COLUMN)).thenAnswer(highAnswer);
        when(mockResultSet.getLong(DbConstants.VOLUME_COLUMN)).thenAnswer(volumeAnswer);
    }
}
