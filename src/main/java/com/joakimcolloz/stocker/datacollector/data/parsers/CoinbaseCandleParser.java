package com.joakimcolloz.stocker.datacollector.data.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.joakimcolloz.stocker.datacollector.model.Candlestick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Parses Coinbase candle JSON responses into {@link Candlestick} objects.
 * Uses GSON tree-model parsing for the flat Coinbase response structure.
 *
 * Expected JSON format:
 * <pre>
 * {
 *   "candles": [
 *     { "start": "1704067200", "low": "42000.00", "high": "43000.00",
 *       "open": "42500.00", "close": "42800.00", "volume": "123.45" }
 *   ]
 * }
 * </pre>
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public class CoinbaseCandleParser {
    private static final Logger logger = LoggerFactory.getLogger(CoinbaseCandleParser.class);

    /**
     * Parses a single JSON response into a list of candlesticks.
     *
     * @param json the JSON response from Coinbase candles endpoint
     * @return list of parsed candlesticks
     * @throws IllegalArgumentException if the JSON is null, empty, or has unexpected structure
     */
    public List<Candlestick> parseCandles(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }

        List<Candlestick> candlesticks = new ArrayList<>();

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray candles = root.getAsJsonArray("candles");

        if (candles == null) {
            logger.warn("No 'candles' array found in response");
            return candlesticks;
        }

        for (JsonElement element : candles) {
            JsonObject candle = element.getAsJsonObject();

            long timestamp = Long.parseLong(candle.get("start").getAsString());
            double open = Double.parseDouble(candle.get("open").getAsString());
            double high = Double.parseDouble(candle.get("high").getAsString());
            double low = Double.parseDouble(candle.get("low").getAsString());
            double close = Double.parseDouble(candle.get("close").getAsString());
            long volume = Math.round(Double.parseDouble(candle.get("volume").getAsString()));

            candlesticks.add(new Candlestick(open, high, low, close, volume, timestamp));
        }

        logger.debug("Parsed {} candlesticks from response", candlesticks.size());
        return candlesticks;
    }

    /**
     * Parses multiple JSON responses (from chunked fetching), deduplicates by timestamp,
     * and returns sorted candlesticks in chronological order.
     *
     * @param jsonResponses list of JSON response strings
     * @return deduplicated and sorted list of candlesticks
     */
    public List<Candlestick> parseAllCandles(List<String> jsonResponses) {
        if (jsonResponses == null || jsonResponses.isEmpty()) {
            throw new IllegalArgumentException("JSON responses list cannot be null or empty");
        }

        // Use LinkedHashMap keyed by timestamp to deduplicate
        Map<Long, Candlestick> candleMap = new LinkedHashMap<>();

        for (String json : jsonResponses) {
            List<Candlestick> parsed = parseCandles(json);
            for (Candlestick candle : parsed) {
                candleMap.putIfAbsent(candle.timestamp(), candle);
            }
        }

        List<Candlestick> result = new ArrayList<>(candleMap.values());
        result.sort(Comparator.comparingLong(Candlestick::timestamp));

        logger.info("Parsed {} unique candlesticks from {} response chunks",
                result.size(), jsonResponses.size());
        return result;
    }
}
