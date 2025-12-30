package com.joakimcolloz.stocker.datacollector.data.parsers;

import org.junit.jupiter.api.Test;
import com.joakimcolloz.stocker.datacollector.model.TradingPeriod;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FinanceBirdParserTest {
    @Test
    void shouldCreateOneDayCandleStick() {
        // Arrange: Load the test JSON resource from the classpath
        String json = loadTestJSON("FinanceBird-BOL.ST-1d-1d.json");

        // Act: Parse the JSON using FinanceBirdParser
        try (FinanceBirdParser parser = new FinanceBirdParser()) {
            parser.setJsonString(json);
            parser.parse();
            TradingPeriod oneDay = parser.getTradingPeriod();

            // Assert: The TradingPeriod should not be null and have 1 candlestick
            assertNotNull(oneDay, "TradingPeriod should not be null after parsing");
            assertEquals(1, oneDay.candlesticks().size());
        }
    }

    @Test
    void shouldParse3MonthDailyCandlesticksAndHave64Candles() {
        // Arrange: Load the test JSON resource from the classpath
        String json = loadTestJSON("FinanceBird-BOL.ST-1d-3month.json");

        // Act: Parse the JSON using FinanceBirdParser
        try (FinanceBirdParser parser = new FinanceBirdParser()) {
            parser.setJsonString(json);
            parser.parse();
            TradingPeriod period = parser.getTradingPeriod();

            // Assert: The TradingPeriod should not be null and have 64 candlesticks
            assertNotNull(period, "TradingPeriod should not be null after parsing");
            assertEquals(64, period.candlesticks().size(), "Should have 64 daily candlesticks for 3 months");
        }
    }

    /**
     * Tests parsing of 1 year daily candlesticks and ensures the TradingPeriod has 250 candlesticks
     * as the used 1 year json file has 250 candlesticks.
     */
    @Test
    void shouldParse1YearDailyCandlesticksAndHave250Candles() {
        // Arrange: Load the test JSON resource from the classpath
        String json = loadTestJSON("FinanceBird-BOL.ST-1d-1y.json");

        // Act: Parse the JSON using FinanceBirdParser
        try (FinanceBirdParser parser = new FinanceBirdParser()) {
            parser.setJsonString(json);
            parser.parse();
            TradingPeriod period = parser.getTradingPeriod();

            // Assert: The TradingPeriod should not be null and have 250 candlesticks
            assertNotNull(period, "TradingPeriod should not be null after parsing");
            assertEquals(250, period.candlesticks().size(), "Should have 250 daily candlesticks for 1 year");
        }
    }

    /**
     * Tests parsing of 2 year daily candlesticks and ensures the TradingPeriod has 503 candlesticks
     * as the used 2 year json file has 503 candlesticks.
     */
    @Test
    void shouldParse2YearDailyCandlesticksAndHave503Candles() {
        // Arrange: Load the test JSON resource from the classpath
        String json = loadTestJSON("FinanceBird-BOL.ST-1d-2y.json");

        // Act: Parse the JSON using FinanceBirdParser
        try (FinanceBirdParser parser = new FinanceBirdParser()) {
            parser.setJsonString(json);
            parser.parse();
            TradingPeriod period = parser.getTradingPeriod();

            // Assert: The TradingPeriod should not be null and have 503 candlesticks
            assertNotNull(period, "TradingPeriod should not be null after parsing");
            assertEquals(503, period.candlesticks().size(), "Should have 503 daily candlesticks for 2 years");
        }
    }

    /**
     * Tests parsing of 3 year daily candlesticks and ensures the TradingPeriod has 755 candlesticks
     * as the used 3 year json file has 755 candlesticks.
     */
    @Test
    void shouldParse3YearDailyCandlesticksAndHave755Candles() {
        // Arrange: Load the test JSON resource from the classpath
        String json = loadTestJSON("FinanceBird-BOL.ST-1d-3y.json");

        // Act: Parse the JSON using FinanceBirdParser
        try (FinanceBirdParser parser = new FinanceBirdParser()) {
            parser.setJsonString(json);
            parser.parse();
            TradingPeriod period = parser.getTradingPeriod();

            // Assert: The TradingPeriod should not be null and have 755 candlesticks
            assertNotNull(period, "TradingPeriod should not be null after parsing");
            assertEquals(755, period.candlesticks().size(), "Should have 755 daily candlesticks for 3 years");
        }
    }

    /**
     * Tests parsing of 10 year daily candlesticks and ensures the TradingPeriod has 2516 candlesticks
     * as the used 10 year json file has 2516 candlesticks.
     */
    @Test
    void shouldParse10YearDailyCandlesticksAndHave2516Candles() {
        // Arrange: Load the test JSON resource from the classpath
        String json = loadTestJSON("FinanceBird-BOL.ST-1d-10y.json");

        // Act: Parse the JSON using FinanceBirdParser
        try (FinanceBirdParser parser = new FinanceBirdParser()) {
            parser.setJsonString(json);
            parser.parse();
            TradingPeriod period = parser.getTradingPeriod();

            // Assert: The TradingPeriod should not be null and have 2516 candlesticks
            assertNotNull(period, "TradingPeriod should not be null after parsing");
            assertEquals(2516, period.candlesticks().size(), "Should have 2516 daily candlesticks for 10 years");
        }
    }

    private String loadTestJSON(final String jsonFileName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(jsonFileName)) {
            assertNotNull(is, "Test resource " + jsonFileName + " not found in classpath");
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON test resource " + jsonFileName + " from classpath", e);
        }
    }
}
