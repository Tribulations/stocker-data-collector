package stocker.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import stocker.representation.Candlestick;
import stocker.support.StockAppLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the CandlestickDao class.
 * These tests use Mockito to mock database connections and JDBC components.
 */
@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class CandlestickDaoTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockPreparedStatement;

    @Mock
    private ResultSet mockResultSet;

    @Mock
    private StockAppLogger mockLogger;

    private CandlestickDao candlestickDao;

    // Create a test driver to help with testing
    private TestDatabaseDriver testDriver;
    
    @BeforeEach
    void setUp() throws Exception {
        // Initialize the test driver with mocks
        testDriver = new TestDatabaseDriver(mockConnection, mockPreparedStatement, mockResultSet);
        
        // Setup basic mock behavior
        testDriver.setupBasicMockBehavior();
        
        // Create a TestCandlestickDao instance with our mock connection
        candlestickDao = new TestCandlestickDao(mockConnection);
    }

    @Test
    void getAllRowsShouldReturnListOfCandlesticks() throws Exception {
        // Arrange
        testDriver.setupBasicMockBehavior();
        
        // Set up the result set to return 2 rows
        testDriver.setupResultSetWithCandlesticks(
            new long[]{1620000000L, 1620086400L},
            new double[]{100.0, 101.0},
            new double[]{101.0, 102.0},
            new double[]{99.0, 100.0},
            new double[]{102.0, 103.0},
            new long[]{1000L, 1100L}
        );

        // Act
        List<Candlestick> result = candlestickDao.getAllRows();

        // Assert
        assertEquals(2, result.size());
        assertEquals(1620000000L, result.get(0).getTimestamp());
        assertEquals(100.0, result.get(0).getOpen());
        assertEquals(101.0, result.get(0).getClose());
        assertEquals(99.0, result.get(0).getLow());
        assertEquals(102.0, result.get(0).getHigh());
        assertEquals(1000L, result.get(0).getVolume());
        
        assertEquals(1620086400L, result.get(1).getTimestamp());
        
        // Verify interactions
        verify(mockConnection).prepareStatement(eq("SELECT * FROM " + DbConstants.CANDLESTICK_TABLE));
        verify(mockPreparedStatement).executeQuery();
        verify(mockResultSet, times(3)).next();
    }

    @Test
    void getAllRowsByNameWithValidSymbolShouldReturnMatchingCandlesticks() throws Exception {
        // Arrange
        String symbol = "AAPL";
        
        testDriver.setupBasicMockBehavior();
        
        // Set up the result set to return 1 row
        testDriver.setupResultSetWithCandlesticks(
            new long[]{1620000000L},
            new double[]{150.0},
            new double[]{151.0},
            new double[]{149.0},
            new double[]{152.0},
            new long[]{2000L}
        );

        // Act
        List<Candlestick> result = candlestickDao.getAllRowsByName(symbol);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1620000000L, result.get(0).getTimestamp());
        assertEquals(150.0, result.get(0).getOpen());
        
        // Verify interactions
        verify(mockConnection).prepareStatement(eq("SELECT * FROM " + DbConstants.CANDLESTICK_TABLE + " WHERE symbol = ?"));
        verify(mockPreparedStatement).setString(eq(1), eq(symbol));
        verify(mockPreparedStatement).executeQuery();
    }

    @Test
    void getAllRowsByNameWithNullSymbolShouldReturnEmptyList() {
        // Act
        List<Candlestick> result = candlestickDao.getAllRowsByName(null);

        // Assert
        assertTrue(result.isEmpty());
        
        // Verify no database interactions
        verifyNoInteractions(mockConnection);
        verifyNoInteractions(mockPreparedStatement);
    }

    @Test
    void addRowShouldExecuteInsertStatement() throws Exception {
        // Arrange
        String symbol = "MSFT";
        Candlestick candlestick = new Candlestick(200.0, 201.0, 199.0, 202.0, 3000L, 1620000000L, "1d");
        
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        // Act
        candlestickDao.addRow(symbol, candlestick);

        // Assert
        verify(mockConnection).prepareStatement(contains("INSERT INTO"));
        verify(mockPreparedStatement).setLong(eq(1), eq(candlestick.getTimestamp()));
        verify(mockPreparedStatement).setDouble(eq(2), eq(candlestick.getOpen()));
        verify(mockPreparedStatement).setDouble(eq(3), eq(candlestick.getClose()));
        verify(mockPreparedStatement).setDouble(eq(4), eq(candlestick.getLow()));
        verify(mockPreparedStatement).setDouble(eq(5), eq(candlestick.getHigh()));
        verify(mockPreparedStatement).setDouble(eq(6), eq((double)candlestick.getVolume()));
        verify(mockPreparedStatement).setString(eq(7), eq(symbol));
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void addRowOverwriteShouldExecuteUpsertStatement() throws Exception {
        // Arrange
        String symbol = "GOOGL";
        Candlestick candlestick = new Candlestick(300.0, 301.0, 299.0, 302.0, 4000L, 1620000000L, "1d");
        
        testDriver.setupBasicMockBehavior();
        testDriver.setupSuccessfulUpdate(1); // 1 row affected

        // Act
        candlestickDao.addRowOverwrite(symbol, candlestick);

        // Assert
        verify(mockConnection).prepareStatement(contains("ON CONFLICT"));
        verify(mockPreparedStatement).setLong(eq(1), eq(candlestick.getTimestamp()));
        verify(mockPreparedStatement).setDouble(eq(2), eq(candlestick.getOpen()));
        verify(mockPreparedStatement).setDouble(eq(3), eq(candlestick.getClose()));
        verify(mockPreparedStatement).setDouble(eq(4), eq(candlestick.getLow()));
        verify(mockPreparedStatement).setDouble(eq(5), eq(candlestick.getHigh()));
        verify(mockPreparedStatement).setDouble(eq(6), eq((double)candlestick.getVolume()));
        verify(mockPreparedStatement).setString(eq(7), eq(symbol));
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void addRowOverwriteWithNullInputsShouldNotExecuteStatement() {
        // Act
        candlestickDao.addRowOverwrite(null, null);

        // Assert - verify no database interactions
        verifyNoInteractions(mockConnection);
        verifyNoInteractions(mockPreparedStatement);
    }

    @Test
    void addRowNoOverwriteShouldExecuteInsertIgnoreStatement() throws Exception {
        // Arrange
        String symbol = "AMZN";
        Candlestick candlestick = new Candlestick(400.0, 401.0, 399.0, 402.0, 5000L, 1620000000L, "1d");
        
        testDriver.setupBasicMockBehavior();
        testDriver.setupSuccessfulUpdate(1); // 1 row affected

        // Act
        candlestickDao.addRowNoOverwrite(symbol, candlestick);

        // Assert
        verify(mockConnection).prepareStatement(contains("ON CONFLICT DO NOTHING"));
        verify(mockPreparedStatement).setLong(eq(1), anyLong()); // We're adjusting timestamp for 1d interval
        verify(mockPreparedStatement).setDouble(eq(2), eq(candlestick.getOpen()));
        verify(mockPreparedStatement).setDouble(eq(3), eq(candlestick.getClose()));
        verify(mockPreparedStatement).setDouble(eq(4), eq(candlestick.getLow()));
        verify(mockPreparedStatement).setDouble(eq(5), eq(candlestick.getHigh()));
        verify(mockPreparedStatement).setDouble(eq(6), eq((double)candlestick.getVolume()));
        verify(mockPreparedStatement).setString(eq(7), eq(symbol));
        verify(mockPreparedStatement).executeUpdate();
    }

    @Test
    void addRowsShouldExecuteMultipleInserts() throws Exception {
        // Arrange
        String symbol = "TSLA";
        List<Candlestick> candlesticks = new ArrayList<>();
        candlesticks.add(new Candlestick(500.0, 501.0, 499.0, 502.0, 6000L, 1620000000L, "1d"));
        candlesticks.add(new Candlestick(505.0, 506.0, 504.0, 507.0, 6100L, 1620086400L, "1d"));
        
        testDriver.setupBasicMockBehavior();
        testDriver.setupSuccessfulUpdate(1); // 1 row affected each time

        // Act
        candlestickDao.addRows(symbol, candlesticks);

        // Assert
        verify(mockConnection).prepareStatement(anyString());
        // Should execute update twice, once for each candlestick
        verify(mockPreparedStatement, times(2)).executeUpdate();
    }

    @Test
    void addRowsWithEmptyListShouldNotExecuteStatement() {
        // Act
        candlestickDao.addRows("TSLA", new ArrayList<>());

        // Assert - verify no database interactions
        verifyNoInteractions(mockConnection);
        verifyNoInteractions(mockPreparedStatement);
    }

    @Test
    void addMultipleOlderRowsShouldExecuteBatchInsert() throws Exception {
        // Arrange
        String symbol = "FB";
        List<Candlestick> candlesticks = new ArrayList<>();
        candlesticks.add(new Candlestick(600.0, 601.0, 599.0, 602.0, 7000L, 1620000000L, "1d"));
        candlesticks.add(new Candlestick(605.0, 606.0, 604.0, 607.0, 7100L, 1620086400L, "1d"));
        
        testDriver.setupBasicMockBehavior();
        testDriver.setupSuccessfulBatch(new int[]{1, 1}); // Both inserts successful

        // Act
        candlestickDao.addMultipleOlderRows(symbol, candlesticks);

        // Assert
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).prepareStatement(anyString());
        verify(mockPreparedStatement, times(2)).addBatch();
        verify(mockPreparedStatement).executeBatch();
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
    }

    @Test
    void addMultipleOlderRowsWhenExceptionOccursShouldRollback() throws Exception {
        // Arrange
        String symbol = "NFLX";
        List<Candlestick> candlesticks = new ArrayList<>();
        candlesticks.add(new Candlestick(700.0, 701.0, 699.0, 702.0, 8000L, 1620000000L, "1d"));
        
        testDriver.setupBasicMockBehavior();
        testDriver.setupException("executeBatch", new SQLException("Test exception"));

        // Act
        candlestickDao.addMultipleOlderRows(symbol, candlesticks);

        // Assert
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).rollback();
        verify(mockConnection).setAutoCommit(true);
    }

    @Test
    void addMultipleOlderRowsWithEmptyListShouldNotExecuteStatement() {
        // Act
        candlestickDao.addMultipleOlderRows("NFLX", new ArrayList<>());

        // Assert - verify no database interactions
        verifyNoInteractions(mockConnection);
        verifyNoInteractions(mockPreparedStatement);
    }
    
    @Test
    void setCandleStickShouldHandleSQLException() throws Exception {
        // Arrange
        testDriver.setupBasicMockBehavior();
        
        // Set up the result set to throw an exception when accessing a column
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getLong(DbConstants.TIMESTAMP_COLUMN)).thenThrow(new SQLException("Column not found"));
        
        // Act
        List<Candlestick> result = candlestickDao.getAllRows();
        
        // Assert
        assertTrue(result.isEmpty(), "Should return empty list when SQLException occurs");
    }
    
    @Test
    void getAllRowsWhenSQLExceptionOccursShouldReturnEmptyList() throws Exception {
        // Arrange
        testDriver.setupException("prepareStatement", new SQLException("Test exception"));
        
        // Act
        List<Candlestick> result = candlestickDao.getAllRows();
        
        // Assert
        assertTrue(result.isEmpty(), "Should return empty list when SQLException occurs");
    }
    
    @Test
    void addRowNoOverwriteWhenSQLExceptionOccursShouldHandleGracefully() throws Exception {
        // Arrange
        String symbol = "AMZN";
        Candlestick candlestick = new Candlestick(400.0, 401.0, 399.0, 402.0, 5000L, 1620000000L, "1d");
        
        testDriver.setupException("prepareStatement", new SQLException("Test exception"));
        
        // Act & Assert - should not throw exception
        candlestickDao.addRowNoOverwrite(symbol, candlestick);
        // Test passes if no exception is thrown
    }
    
    @Test
    void getAllRowsWhenConnectionFailsShouldReturnEmptyList() throws Exception {
        // Arrange - simulate connection failure by throwing exception on prepareStatement
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Connection failed"));
        
        // Act
        List<Candlestick> result = candlestickDao.getAllRows();
        
        // Assert
        assertTrue(result.isEmpty(), "Should return empty list when connection fails");
    }
}
