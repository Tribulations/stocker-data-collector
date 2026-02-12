package com.joakimcolloz.stocker.datacollector.data.parsers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Parses Coinbase product listing JSON responses to extract trading pair IDs.
 * Uses GSON tree-model parsing.
 *
 * Expected JSON format:
 * <pre>
 * {
 *   "products": [
 *     { "product_id": "BTC-EUR", "quote_currency_id": "EUR", "status": "online", ... }
 *   ],
 *   "num_products": 583
 * }
 * </pre>
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public class CoinbaseProductParser {
    private static final Logger logger = LoggerFactory.getLogger(CoinbaseProductParser.class);

    /**
     * Parses a single JSON response and extracts product IDs filtered by quote currency.
     * Only includes products with {@code "status": "online"}.
     *
     * @param json          the JSON response from Coinbase products endpoint
     * @param quoteCurrency the quote currency to filter by (e.g., "EUR")
     * @return list of matching product IDs (e.g., ["BTC-EUR", "ETH-EUR"])
     * @throws IllegalArgumentException if the JSON or quoteCurrency is null or empty
     */
    public List<String> parseProductIds(String json, String quoteCurrency) {
        if (json == null || json.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }
        if (quoteCurrency == null || quoteCurrency.trim().isEmpty()) {
            throw new IllegalArgumentException("Quote currency cannot be null or empty");
        }

        List<String> productIds = new ArrayList<>();

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonArray products = root.getAsJsonArray("products");

        if (products == null) {
            logger.warn("No 'products' array found in response");
            return productIds;
        }

        for (JsonElement element : products) {
            JsonObject product = element.getAsJsonObject();

            String status = getStringField(product, "status");
            String quoteCurrencyId = getStringField(product, "quote_currency_id");
            String productId = getStringField(product, "product_id");

            if ("online".equals(status)
                    && quoteCurrency.equals(quoteCurrencyId)
                    && productId != null) {
                productIds.add(productId);
            }
        }

        logger.debug("Parsed {} {} product IDs from response", productIds.size(), quoteCurrency);
        return productIds;
    }

    /**
     * Parses multiple JSON responses (from paginated fetching) and extracts deduplicated
     * product IDs filtered by quote currency.
     *
     * @param jsonPages     list of JSON response strings
     * @param quoteCurrency the quote currency to filter by
     * @return deduplicated list of matching product IDs
     */
    public List<String> parseProductIds(List<String> jsonPages, String quoteCurrency) {
        if (jsonPages == null || jsonPages.isEmpty()) {
            throw new IllegalArgumentException("JSON pages list cannot be null or empty");
        }

        Set<String> productIdSet = new LinkedHashSet<>();

        for (String json : jsonPages) {
            List<String> ids = parseProductIds(json, quoteCurrency);
            productIdSet.addAll(ids);
        }

        List<String> result = new ArrayList<>(productIdSet);
        logger.info("Parsed {} unique {} product IDs from {} pages",
                result.size(), quoteCurrency, jsonPages.size());
        return result;
    }

    /**
     * Extracts the pagination cursor from a products response.
     *
     * @param json the JSON response from Coinbase products endpoint
     * @return the cursor string for the next page, or null if no more pages
     */
    public String parseCursor(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        JsonElement cursorElement = root.get("cursor");

        if (cursorElement == null || cursorElement.isJsonNull()) {
            return null;
        }

        String cursor = cursorElement.getAsString();
        return cursor.isEmpty() ? null : cursor;
    }

    private String getStringField(JsonObject obj, String fieldName) {
        JsonElement element = obj.get(fieldName);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        return element.getAsString();
    }
}
