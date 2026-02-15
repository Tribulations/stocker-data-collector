package com.joakimcolloz.stocker.datacollector.data.parsers;

import com.joakimcolloz.stocker.datacollector.model.Candlestick;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EodhdIntradayParserTest {

    @Test
    void shouldParseCandlesWithMissingVolumeAsZero() {
        String json = loadTestJSON("EodhdIntraday-BOL.ST-5m-volume-missing.json");

        EodhdIntradayParser parser = new EodhdIntradayParser();
        List<Candlestick> candles = parser.parseCandles(json);

        assertNotNull(candles);
        assertEquals(2, candles.size());
        assertEquals(0L, candles.get(0).volume());
        assertEquals(10L, candles.get(1).volume());
    }

    @Test
    void shouldParseCandlesWithNullVolumeAsZero() {
        String json = loadTestJSON("EodhdIntraday-BOL.ST-5m-volume-null.json");

        EodhdIntradayParser parser = new EodhdIntradayParser();
        List<Candlestick> candles = parser.parseCandles(json);

        assertNotNull(candles);
        assertEquals(1, candles.size());
        assertEquals(0L, candles.get(0).volume());
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
