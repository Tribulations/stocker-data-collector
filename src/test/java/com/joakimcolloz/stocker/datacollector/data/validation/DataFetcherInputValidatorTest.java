package com.joakimcolloz.stocker.datacollector.data.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class DataFetcherInputValidatorTest {

    private DataFetcherInputValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DataFetcherInputValidator();
    }

    // Symbol Validation Tests
    @ParameterizedTest
    @ValueSource(strings = {"BOL.ST", "HACKSAW", "VOLVOCAR-B", "SAMPO-SDB", "NP3"})
    void validSymbolShouldPass(String symbol) {
        assertDoesNotThrow(() -> validator.validateSymbol(symbol));
    }

    @Test
    void nullSymbolShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateSymbol(null));
        assertTrue(exception.getMessage().contains("Stock symbol"));
    }

    @Test
    void emptySymbolShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateSymbol(""));
        assertTrue(exception.getMessage().contains("Stock symbol"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"INVALSymbol123", "bol", "bOl"})
    void invalidSymbolFormatShouldThrowException(String symbol) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateSymbol(symbol));
        assertTrue(exception.getMessage().contains("Invalid stock symbol format"));
    }

    // Range Validation Tests
    @ParameterizedTest
    @ValueSource(strings = {"1d", "5d", "1wk", "1mo", "3mo", "6mo", "1y", "2y", "5y", "10y", "ytd", "max"})
    void validRangeShouldPass(String range) {
        assertDoesNotThrow(() -> validator.validateRange(range));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "5D"})
    void invalidRangeShouldThrowException(String range) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateRange(range));
        assertTrue(exception.getMessage().contains("Invalid range"));
    }

    @Test
    void nullRangeShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateRange(null));
        assertTrue(exception.getMessage().contains("Range"));
    }

    // Interval Validation Tests
    @ParameterizedTest
    @ValueSource(strings = {"1m", "5m", "15m", "1h", "1d", "5d", "1wk", "1mo", "3mo", "6mo", "1y", "2y",
            "5y", "10y", "ytd", "max"})
    void validIntervalShouldPass(String interval) {
        assertDoesNotThrow(() -> validator.validateInterval(interval));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid", "5M"})
    void invalidIntervalShouldThrowException(String interval) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateInterval(interval));
        assertTrue(exception.getMessage().contains("Invalid interval"));
    }

    // List Validation Tests
    @Test
    void validStockSymbolListShouldPass() {
        assertDoesNotThrow(() -> validator.validateStockSymbolsList(Arrays.asList("BOL.ST", "ABB.ST",
                "AAK.ST")));
    }

    @Test
    void nullStockSymbolListShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateStockSymbolsList(null));
        assertTrue(exception.getMessage().contains("cannot be null"));
    }

    @Test
    void emptyStockSymbolListShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateStockSymbolsList(Collections.emptyList()));
        assertTrue(exception.getMessage().contains("cannot be empty"));
    }

    @Test
    void validJsonShouldPass() {
        assertDoesNotThrow(() -> validator.validateJsonData("{\"key\": \"value\"}"));
    }

    @Test
    void invalidJsonShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateJsonData("invalid"));
        assertTrue(exception.getMessage().contains("Invalid JSON format"));
    }

    // API Config Validation Tests
    @Test
    void validConfigShouldPass() {
        assertDoesNotThrow(() -> validator.validateApiConfig(
                "X-API-Key", "X-API-Host", "key", "host", "https://api.example.com"));
    }

    @Test
    void nullApiKeyShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateApiConfig("header", "host-header", null, "host", "https://api.com"));
        assertTrue(exception.getMessage().contains("API key"));
    }

    @Test
    void invalidUrlShouldThrowException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> validator.validateApiConfig("header", "host-header", "key", "host", "invalid-url"));
        assertTrue(exception.getMessage().contains("Invalid API URL format"));
    }
}
