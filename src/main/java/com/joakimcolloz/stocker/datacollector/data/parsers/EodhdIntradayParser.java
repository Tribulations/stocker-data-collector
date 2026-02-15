package com.joakimcolloz.stocker.datacollector.data.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.joakimcolloz.stocker.datacollector.model.Candlestick;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EodhdIntradayParser {

    public List<Candlestick> parseCandles(String json) {
        return parseCandlesWithStats(json).candlesticks();
    }

    public ParseResult parseCandlesWithStats(String json) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }

        JsonElement root = JsonParser.parseString(json);
        if (!root.isJsonArray()) {
            throw new IllegalArgumentException("Unexpected EODHD intraday JSON structure: expected a JSON array");
        }

        JsonArray array = root.getAsJsonArray();
        List<Candlestick> candlesticks = new ArrayList<>(array.size());
        int inputCount = array.size();
        int skippedCount = 0;

        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                skippedCount++;
                continue;
            }

            JsonObject obj = element.getAsJsonObject();

            Long timestamp = getAsLongOrNull(obj.get("timestamp"));
            Double open = getAsDoubleOrNull(obj.get("open"));
            Double high = getAsDoubleOrNull(obj.get("high"));
            Double low = getAsDoubleOrNull(obj.get("low"));
            Double close = getAsDoubleOrNull(obj.get("close"));

            if (timestamp == null || open == null || high == null || low == null || close == null) {
                skippedCount++;
                continue;
            }

            long volume = 0;
            JsonElement volumeElement = obj.get("volume");
            if (volumeElement != null && !volumeElement.isJsonNull()) {
                try {
                    volume = volumeElement.getAsLong();
                } catch (Exception ignored) {
                    volume = 0;
                }
            }

            candlesticks.add(new Candlestick(open, high, low, close, volume, timestamp));
        }

        candlesticks.sort(Comparator.comparingLong(Candlestick::timestamp));
        return new ParseResult(candlesticks, inputCount, skippedCount);
    }

    public record ParseResult(List<Candlestick> candlesticks, int inputCount, int skippedCount) {
    }

    private Double getAsDoubleOrNull(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        try {
            return element.getAsDouble();
        } catch (Exception e) {
            return null;
        }
    }

    private Long getAsLongOrNull(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return null;
        }
        try {
            return element.getAsLong();
        } catch (Exception e) {
            return null;
        }
    }
}
