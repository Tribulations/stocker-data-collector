package stocker.data.parsers;

import org.junit.jupiter.api.Test;
import stocker.model.TradingPeriod;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class YahooFinanceParserTest {
    @Test
    void shouldCreateOneDayCandleStick() throws IOException {
        // Arrange: Load the test JSON resource from the classpath
        String json;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("BOL.ST-1d-1d.json")) {
            assertNotNull(is, "Test resource BOL.ST-1d-1d.json not found in classpath");
            json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        // Act: Parse the JSON using YahooFinanceParser
        YahooFinanceParser parser = new YahooFinanceParser(json);
        parser.parse();
        TradingPeriod oneDay = parser.getTradingPeriod();

        // Assert: The TradingPeriod should not be null and have 1 candlestick
        assertNotNull(oneDay, "TradingPeriod should not be null after parsing");
        assertEquals(1, oneDay.getCandlesticks().size());
    }

    @Test
    void shouldParse3MonthDailyCandlesticksAndHave60Candles() throws IOException {
        // Arrange: Load the test JSON resource from the classpath
        String json;
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("BOL.ST-1d-3month.json")) {
            assertNotNull(is, "Test resource BOL.ST-1d-3month.json not found in classpath");
            json = new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }

        // Act: Parse the JSON using YahooFinanceParser
        YahooFinanceParser parser = new YahooFinanceParser(json);
        parser.parse();
        TradingPeriod period = parser.getTradingPeriod();

        // Assert: The TradingPeriod should not be null and have 60 candlesticks
        assertNotNull(period, "TradingPeriod should not be null after parsing");
        assertEquals(60, period.getCandlesticks().size(), "Should have 60 daily candlesticks for 3 months");
    }
}
