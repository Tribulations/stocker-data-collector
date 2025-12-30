package com.joakimcolloz.stocker.datacollector.data.parsers;

import org.junit.jupiter.api.Test;
import com.joakimcolloz.stocker.datacollector.model.TradingPeriod;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class YahooFinanceParserTest {
    @Test
    void shouldCreateOneDayCandleStick() {
        // Arrange: Load the test JSON resource from the classpath
        String json = loadTestJSON("YahooFinance-BOL.ST-1d-1d.json");

        // Act: Parse the JSON using YahooFinanceParser
        try (YahooFinanceParser parser = new YahooFinanceParser()) {
            parser.setJsonString(json);
            parser.parse();
            TradingPeriod oneDay = parser.getTradingPeriod();

            // Assert: The TradingPeriod should not be null and have 1 candlestick
            assertNotNull(oneDay, "TradingPeriod should not be null after parsing");
            assertEquals(1, oneDay.candlesticks().size());
        }
    }

    @Test
    void shouldParse3MonthDailyCandlesticksAndHave60Candles() {
        // Arrange: Load the test JSON resource from the classpath
        String json = loadTestJSON("YahooFinance-BOL.ST-1d-3month.json");

        // Act: Parse the JSON using YahooFinanceParser
        try (YahooFinanceParser parser = new YahooFinanceParser()) {
            parser.setJsonString(json);
            parser.parse();
            TradingPeriod period = parser.getTradingPeriod();

            // Assert: The TradingPeriod should not be null and have 60 candlesticks
            assertNotNull(period, "TradingPeriod should not be null after parsing");
            assertEquals(60, period.candlesticks().size(), "Should have 60 daily candlesticks for 3 months");
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
