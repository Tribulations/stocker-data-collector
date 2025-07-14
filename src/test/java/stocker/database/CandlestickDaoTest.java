package stocker.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import stocker.database.validation.DatabaseInputValidator;
import stocker.representation.Candlestick;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


/**
 * Unit tests for {@link CandlestickDao} class using mocked dependencies.
 * Tests validator integration, database operations, and error handling.
 *
 * Complements DatabaseInputValidatorTest by testing integration rather than validation rules.
 */
@ExtendWith(MockitoExtension.class)
class CandlestickDaoTest {

    @Mock
    private DatabaseInputValidator mockValidator;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    private TestableCandlestickDao dao;

    @BeforeEach
    void setUp() {
        dao = new TestableCandlestickDao(mockValidator, mockConnection);
    }

    // Symbol validation tests
    @Test
    void getAllRowsByNameWithValidSymbolShouldPass() throws SQLException {
        // Arrange
        String symbol = "BOL.ST";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        setupSingleCandlestickResult();

        // Act
        List<Candlestick> result = dao.getAllRowsByName(symbol);

        // Assert
        verify(mockValidator).validateSymbol(symbol);
        assertEquals(1, result.size());
    }

    @Test
    void getAllRowsByNameWithInvalidSymbolShouldReturnEmptyList() {
        // Arrange
        String invalidSymbol = "";
        doThrow(new IllegalArgumentException("Invalid symbol"))
                .when(mockValidator).validateSymbol(invalidSymbol);

        // Act
        List<Candlestick> result = dao.getAllRowsByName(invalidSymbol);

        // Assert
        assertTrue(result.isEmpty());
        verify(mockValidator).validateSymbol(invalidSymbol);
        verifyNoInteractions(mockConnection);
    }

    @Test
    void addRowWithValidInputsShouldPass() throws SQLException {
        // Arrange
        String symbol = "BOL.ST";
        Candlestick candlestick = createValidCandlestick();
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        dao.addRow(symbol, candlestick);

        // Assert
        verify(mockValidator).validateSymbol(symbol);
        verify(mockValidator).validateCandlestick(candlestick);
        verify(mockStatement).executeUpdate();
    }

    @Test
    void addRowWithInvalidSymbolShouldThrowException() {
        // Arrange
        String invalidSymbol = null;
        Candlestick candlestick = createValidCandlestick();
        doThrow(new IllegalArgumentException("Symbol cannot be null"))
                .when(mockValidator).validateSymbol(invalidSymbol);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.addRow(invalidSymbol, candlestick));

        assertTrue(exception.getMessage().contains("Symbol cannot be null"));
        verify(mockValidator).validateSymbol(invalidSymbol);
        verifyNoInteractions(mockConnection);
    }

    @Test
    void addRowWithInvalidCandlestickShouldThrowException() {
        // Arrange
        String symbol = "BOL.ST";
        Candlestick invalidCandlestick = new Candlestick();
        invalidCandlestick.setTimestamp(-1); // Invalid timestamp

        doThrow(new IllegalArgumentException("Invalid timestamp"))
                .when(mockValidator).validateCandlestick(invalidCandlestick);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.addRow(symbol, invalidCandlestick));

        assertTrue(exception.getMessage().contains("Invalid timestamp"));
        verify(mockValidator).validateSymbol(symbol);
        verify(mockValidator).validateCandlestick(invalidCandlestick);
        verifyNoInteractions(mockConnection);
    }

    @Test
    void addRowsWithValidInputsShouldPass() throws SQLException {
        // Arrange
        String symbol = "BOL.ST";
        List<Candlestick> candlesticks = Arrays.asList(
                createValidCandlestick(),
                createValidCandlestick()
        );

        // Mock batch operations and transaction management
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeBatch()).thenReturn(new int[]{1, 1}); // Success for both

        // Act
        dao.addRows(symbol, candlesticks);

        // Assert
        verify(mockValidator).validateSymbol(symbol);
        verify(mockValidator).validateCandlesticksList(candlesticks);
        verify(mockValidator, times(2)).validateCandlestick(any(Candlestick.class));
        verify(mockStatement, times(2)).addBatch();
        verify(mockStatement).executeBatch();
        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
        verify(mockConnection).close();
    }

    @Test
    void addRowsWithEmptyListShouldThrowException() {
        // Arrange
        String symbol = "BOL.ST";
        List<Candlestick> emptyCandlesticks = Collections.emptyList();
        doThrow(new IllegalArgumentException("List cannot be empty"))
                .when(mockValidator).validateCandlesticksList(emptyCandlesticks);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.addRows(symbol, emptyCandlesticks));

        assertTrue(exception.getMessage().contains("List cannot be empty"));
        verify(mockValidator).validateSymbol(symbol);
        verify(mockValidator).validateCandlesticksList(emptyCandlesticks);
        verifyNoInteractions(mockConnection);
    }

    @Test
    void addRowsWithDatabaseErrorThrowsRuntimeException() throws SQLException {
        // Arrange
        String symbol = "BOL.ST";
        List<Candlestick> candlesticks = List.of(createValidCandlestick());

        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dao.addRows(symbol, candlesticks));

        assertTrue(exception.getMessage().contains("Failed to add candlesticks"));
        assertInstanceOf(SQLException.class, exception.getCause());

        // Verify transaction management even during errors
        verify(mockConnection).setAutoCommit(false); // Transaction should still be started
        verify(mockConnection).setAutoCommit(true); // Auto-commit should be restored in finally
        verify(mockConnection).close(); // Connection should be closed in finally
    }

    @Test
    void resetTableShouldExecuteSuccessfully() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        dao.resetTable();

        // Assert
        verify(mockStatement).executeUpdate();
    }

    @Test
    void resetTableWithSqlExceptionShouldThrowRuntimeException() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("Reset failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dao.resetTable());

        assertTrue(exception.getMessage().contains("Failed to reset table"));
    }

    @Test
    void validateConnectionParametersShouldPass() throws SQLException {
        // Act
        dao.testGetDbConnection();

        // Assert
        verify(mockValidator).validateDatabaseUrl(anyString());
        verify(mockValidator).validateDatabaseUsername(anyString());
        verify(mockValidator).validateDatabasePassword(anyString());
    }

    @Test
    void getDbConnectionWithInvalidConfigShouldThrowSqlException() {
        // Arrange
        doThrow(new IllegalArgumentException("Invalid URL"))
                .when(mockValidator).validateDatabaseUrl(anyString());

        // Act & Assert
        SQLException exception = assertThrows(SQLException.class,
                () -> dao.testGetDbConnection());

        assertTrue(exception.getMessage().contains("Invalid database configuration"));
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
    }

    // Helper methods
    private void setupSingleCandlestickResult() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getLong(anyString())).thenReturn(1620000000L);
        when(mockResultSet.getDouble(anyString())).thenReturn(100.0);
    }

    private Candlestick createValidCandlestick() {
        Candlestick candlestick = new Candlestick();
        candlestick.setTimestamp(System.currentTimeMillis());
        candlestick.setOpen(100.0);
        candlestick.setClose(105.0);
        candlestick.setLow(95.0);
        candlestick.setHigh(110.0);
        candlestick.setVolume(1000L);
        candlestick.setInterval("1d");
        return candlestick;
    }

    // Simple testable subclass
    private static class TestableCandlestickDao extends CandlestickDao {
        private final Connection mockConnection;

        public TestableCandlestickDao(DatabaseInputValidator validator, Connection mockConnection) {
            super(validator);
            this.mockConnection = mockConnection;
        }

        @Override
        protected Connection getDbConnection() throws SQLException {
            // Still call validation from parent class
            super.getDbConnection();
            return mockConnection;
        }

        // Expose protected method for testing
        public Connection testGetDbConnection() throws SQLException {
            return super.getDbConnection();
        }
    }
}