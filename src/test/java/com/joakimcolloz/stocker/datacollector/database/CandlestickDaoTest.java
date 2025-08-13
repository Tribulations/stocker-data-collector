package com.joakimcolloz.stocker.datacollector.database;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.joakimcolloz.stocker.datacollector.database.validation.DatabaseInputValidator;
import com.joakimcolloz.stocker.datacollector.model.Candlestick;

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
 */
@ExtendWith(MockitoExtension.class)
class CandlestickDaoTest {

    @Mock
    private DatabaseInputValidator mockValidator;

    @Mock
    private DatabaseManager mockDatabaseManager;

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    private CandlestickDao dao;

    @BeforeEach
    void setUp() {
        dao = new CandlestickDao(mockDatabaseManager, mockValidator);
    }

    // Symbol validation tests
    @Test
    void getAllRowsByNameWithValidSymbolShouldPass() throws SQLException {
        // Arrange
        String symbol = "BOL.ST";
        when(mockDatabaseManager.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        setupSingleCandlestickResult();

        // Act
        List<Candlestick> result = dao.getAllRowsByName(symbol);

        // Assert
        verify(mockValidator).validateSymbol(symbol);
        assertEquals(1, result.size());
        verify(mockDatabaseManager).getConnection();
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
        verifyNoInteractions(mockDatabaseManager);
    }

    @Test
    void addRowWithValidInputsShouldPass() throws SQLException {
        // Arrange
        String symbol = "BOL.ST";
        Candlestick candlestick = createValidCandlestick();
        when(mockDatabaseManager.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        dao.addRow(symbol, candlestick);

        // Assert
        verify(mockValidator).validateSymbol(symbol);
        verify(mockValidator).validateCandlestick(candlestick);
        verify(mockStatement).executeUpdate();
        verify(mockDatabaseManager).getConnection();
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
        verifyNoInteractions(mockDatabaseManager);
    }

    @Test
    void addRowWithInvalidCandlestickShouldThrowException() {
        // Arrange
        String symbol = "BOL.ST";
        Candlestick invalidCandlestick = new Candlestick(100.0, 105.0, 99.0,
                115.0, 1000L, -1); // Invalid timestamp

        doThrow(new IllegalArgumentException("Invalid timestamp"))
                .when(mockValidator).validateCandlestick(invalidCandlestick);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> dao.addRow(symbol, invalidCandlestick));

        assertTrue(exception.getMessage().contains("Invalid timestamp"));
        verify(mockValidator).validateSymbol(symbol);
        verify(mockValidator).validateCandlestick(invalidCandlestick);
        verifyNoInteractions(mockDatabaseManager);
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
        when(mockDatabaseManager.getConnection()).thenReturn(mockConnection);
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
        verify(mockConnection).setAutoCommit(false); // Transaction should be started
        verify(mockConnection).commit();
        verify(mockConnection).close(); // Connection should be closed
        verify(mockDatabaseManager).getConnection();
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
        verifyNoInteractions(mockDatabaseManager);
    }

    @Test
    void addRowsWithDatabaseErrorThrowsRuntimeException() throws SQLException {
        // Arrange
        String symbol = "BOL.ST";
        List<Candlestick> candlesticks = List.of(createValidCandlestick());

        when(mockDatabaseManager.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dao.addRows(symbol, candlesticks));

        assertTrue(exception.getMessage().contains("Failed to add candlesticks"));
        assertInstanceOf(SQLException.class, exception.getCause());

        // Verify transaction management even during errors
        verify(mockConnection).setAutoCommit(false); // Transaction should still be started
        verify(mockConnection).close(); // Connection should be closed
        verify(mockDatabaseManager).getConnection();
    }

    @Test
    void resetTableShouldExecuteSuccessfully() throws SQLException {
        // Arrange
        when(mockDatabaseManager.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        // Act
        dao.resetTable();

        // Assert
        verify(mockStatement).executeUpdate();
        verify(mockDatabaseManager).getConnection();
    }

    @Test
    void resetTableWithSqlExceptionShouldThrowRuntimeException() throws SQLException {
        // Arrange
        when(mockDatabaseManager.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenThrow(new SQLException("Reset failed"));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> dao.resetTable());

        assertTrue(exception.getMessage().contains("Failed to reset table"));
        verify(mockDatabaseManager).getConnection();
    }

    @Test
    void getAllRowsShouldReturnAllCandlesticks() throws SQLException {
        // Arrange
        when(mockDatabaseManager.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        setupSingleCandlestickResult();

        // Act
        List<Candlestick> result = dao.getAllRows();

        // Assert
        assertEquals(1, result.size());
        verify(mockDatabaseManager).getConnection();
    }

    // Helper methods
    private void setupSingleCandlestickResult() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getLong(anyString())).thenReturn(1620000000L);
        when(mockResultSet.getDouble(anyString())).thenReturn(100.0);
    }

    private Candlestick createValidCandlestick() {
        return new Candlestick(100.0, 105.0, 95.0,
                110.0, 1000L, System.currentTimeMillis());
    }
}
