package com.joakimcolloz.stocker.datacollector.database.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import com.joakimcolloz.stocker.datacollector.model.Candlestick;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the {@link DatabaseInputValidator} class.
 *
 * @author Joakim Colloz
 * @version 1.0
 */
class DatabaseInputValidatorTest {

    private DatabaseInputValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DatabaseInputValidator();
    }

    // Symbol Validation Tests
    @Test
    void validSymbolShouldPass() {
        assertDoesNotThrow(() -> validator.validateSymbol("BOl.ST"));
    }

    @Test
    void nullSymbolShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateSymbol(null));
        assertTrue(exception.getMessage().contains("Stock symbol cannot be null"));
    }

    @Test
    void emptySymbolShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateSymbol(""));
        assertTrue(exception.getMessage().contains("Stock symbol cannot be empty"));
    }

    @Test
    void symbolWithOnlySpacesShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateSymbol("   "));
        assertTrue(exception.getMessage().contains("Stock symbol cannot be empty"));
    }

    // Candlesticks List Validation Tests
    @Test
    void validCandlesticksListShouldPass() {
        List<Candlestick> candlesticks = Arrays.asList(createValidCandlestick(), createValidCandlestick());
        assertDoesNotThrow(() -> validator.validateCandlesticksList(candlesticks));
    }

    @Test
    void nullCandlesticksListShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlesticksList(null));
        assertTrue(exception.getMessage().contains("Candlesticks list cannot be null"));
    }

    @Test
    void emptyCandlesticksListShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlesticksList(Collections.emptyList()));
        assertTrue(exception.getMessage().contains("Candlesticks list cannot be empty"));
    }

    // Individual Candlestick Validation Tests
    @Test
    void validCandlestickShouldPass() {
        Candlestick candlestick = createValidCandlestick();
        assertDoesNotThrow(() -> validator.validateCandlestick(candlestick));
    }

    @Test
    void nullCandlestickShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlestick(null));
        assertTrue(exception.getMessage().contains("Candlestick cannot be null"));
    }

    @Test
    void candlestickWithZeroTimestampShouldThrowException() {
        Candlestick candlestick = new Candlestick(100.0, 105.0, 95.0,
                110.0, 1000L, 0);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlestick(candlestick));
        assertTrue(exception.getMessage().contains("timestamp must be positive"));
    }

    @Test
    void candlestickWithNegativeTimestampShouldThrowException() {
        Candlestick candlestick = new Candlestick(100.0, 105.0, 95.0,
                110.0, 1000L, -1000);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlestick(candlestick));
        assertTrue(exception.getMessage().contains("timestamp must be positive"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0, -0.01, -100.0})
    void candlestickWithNegativeOpenPriceShouldThrowException(double negativePrice) {
        Candlestick candlestick = new Candlestick(negativePrice, 105.0, 95.0,
                110.0, 1000L, System.currentTimeMillis());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlestick(candlestick));
        assertTrue(exception.getMessage().contains("open price cannot be negative"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0, -0.01, -100.0})
    void candlestickWithNegativeClosePriceShouldThrowException(double negativePrice) {
        Candlestick candlestick = new Candlestick(100.0, negativePrice, 95.0,
                110.0, 1000L, System.currentTimeMillis());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlestick(candlestick));
        assertTrue(exception.getMessage().contains("close price cannot be negative"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0, -0.01, -100.0})
    void candlestickWithNegativeHighPriceShouldThrowException(double negativePrice) {
        Candlestick candlestick = new Candlestick(100.0, 105.0, 95.0,
                negativePrice, 1000L, System.currentTimeMillis());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlestick(candlestick));
        assertTrue(exception.getMessage().contains("high price cannot be negative"));
    }

    @ParameterizedTest
    @ValueSource(doubles = {-1.0, -0.01, -100.0})
    void candlestickWithNegativeLowPriceShouldThrowException(double negativePrice) {
        Candlestick candlestick = new Candlestick(100.0, 105.0, negativePrice,
                110.0, 1000L, System.currentTimeMillis());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlestick(candlestick));
        assertTrue(exception.getMessage().contains("low price cannot be negative"));
    }

    @ParameterizedTest
    @ValueSource(longs = {-1L, -100L, -1000L})
    void candlestickWithNegativeVolumeShouldThrowException(long negativeVolume) {
        Candlestick candlestick = new Candlestick(100.0, 105.0, 95.0,
                110.0, negativeVolume, System.currentTimeMillis());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlestick(candlestick));
        assertTrue(exception.getMessage().contains("volume cannot be negative"));
    }

    @Test
    void shouldThrowExceptionWhenLowIsGreaterThanHigh() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlestick(new Candlestick(100.0, 105.0, 150.0,
                        100.0, 1000L, System.currentTimeMillis())));
        assertTrue(exception.getMessage().contains("high price cannot be less than low price"));
    }

    @Test
    void shouldThrowExceptionWhenOpenIsAboveHigh() {
        Candlestick candlestick = new Candlestick(110.0, 105.0, 90.0,
                100.0, 1000L, System.currentTimeMillis());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlestick(candlestick));
        assertTrue(exception.getMessage().contains("open price must be between high and low prices"));
    }

    @Test
    void shouldThrowExceptionWhenOpenIsBelowLow() {
        Candlestick candlestick = new Candlestick(80.0, 105.0, 90.0,
                100.0, 1000L, System.currentTimeMillis());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlestick(candlestick));
        assertTrue(exception.getMessage().contains("open price must be between high and low prices"));
    }

    @Test
    void shouldThrowExceptionWhenCloseIsAboveHigh() {
        Candlestick candlestick = new Candlestick(95.0, 110.0, 90.0,
                100.0, 1000L, System.currentTimeMillis());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlestick(candlestick));
        assertTrue(exception.getMessage().contains("close price must be between high and low prices"));
    }

    @Test
    void shouldThrowExceptionWhenCloseIsBelowLow() {
        Candlestick candlestick = new Candlestick(95.0, 80.0, 90.0,
                100.0, 1000L, System.currentTimeMillis());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateCandlestick(candlestick));
        assertTrue(exception.getMessage().contains("close price must be between high and low prices"));
    }

    // All Candlesticks Validation Tests
    @Test
    void oneInvalidCandlestickShouldThrowException() {
        Candlestick validCandlestick = createValidCandlestick();
        Candlestick invalidCandlestick = new Candlestick(100.0, 105.0, 99.0,
                115.0, 1000L, -1000); // Invalid timestamp

        List<Candlestick> candlesticks = Arrays.asList(validCandlestick, invalidCandlestick);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateAllCandlesticks(candlesticks));
        assertTrue(exception.getMessage().contains("Invalid candlestick at index 1"));
    }

    // Database URL Validation Tests
    @ParameterizedTest
    @ValueSource(strings = {"jdbc:mysql://localhost:3306/test", "jdbc:postgresql://localhost/testdb", "jdbc:h2:mem:testdb"})
    void validUrlShouldPass(String validUrl) {
        assertDoesNotThrow(() -> validator.validateDatabaseUrl(validUrl));
    }

    @Test
    void nullUrlShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateDatabaseUrl(null));
        assertTrue(exception.getMessage().contains("Database URL cannot be null or empty"));
    }

    @Test
    void emptyUrlShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateDatabaseUrl(""));
        assertTrue(exception.getMessage().contains("Database URL cannot be null or empty"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"mysql://localhost:3306/test", "http://localhost/test", "invalid-url"})
    void invalidUrlShouldThrowException(String invalidUrl) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateDatabaseUrl(invalidUrl));
        assertTrue(exception.getMessage().contains("Database URL must start with 'jdbc:'"));
    }

    // Database Username Validation Tests
    @Test
    void validUsernameShouldPass() {
        assertDoesNotThrow(() -> validator.validateDatabaseUsername("testuser"));
    }

    @Test
    void nullUsernameShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateDatabaseUsername(null));
        assertTrue(exception.getMessage().contains("Database username cannot be null or empty"));
    }

    @Test
    void emptyUsernameShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateDatabaseUsername(""));
        assertTrue(exception.getMessage().contains("Database username cannot be null or empty"));
    }

    // Database Password Validation Tests
    @ParameterizedTest
    @ValueSource(strings = {"password123", ""})
    void validPasswordShouldPass() {
        assertDoesNotThrow(() -> validator.validateDatabasePassword("password123"));
    }

    @Test
    void nullPasswordShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateDatabasePassword(null));
        assertTrue(exception.getMessage().contains("Database password cannot be null"));
    }

    // Helper method to create valid candlestick
    private Candlestick createValidCandlestick() {
        return new Candlestick(100.0, 105.0, 95.0,
                110.0, 1000L, System.currentTimeMillis());
    }
}