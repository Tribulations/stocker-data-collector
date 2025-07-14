package stocker.data.validation;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validates input parameters for data fetcher operations.
 * This class centralizes validation logic for stock symbols, range, interval, and API configuration.
 */
public class DataFetcherInputValidator {

    // Stock symbol pattern: 1-5 letters, optionally followed by a dot and 1-2 letters
    private static final Pattern STOCK_SYMBOL_PATTERN = Pattern.compile("^[A-Z]{1,5}(\\.[A-Z]{1,2})?$");

    // Fixed the typo: removed comma from "1wk"
    private static final String[] VALID_RANGES = {"1d", "5d", "1wk", "1mo", "3mo", "6mo",
            "1y", "2y", "5y", "10y", "ytd", "max"};

    private static final String[] VALID_INTERVALS = {"1m", "5m", "15m", "30m", "1h", "1d",
            "5d", "1wk", "1mo", "3mo", "6mo", "1y", "2y", "5y", "10y", "ytd", "max"};

    /**
     * Validates a stock symbol.
     *
     * @param symbol the stock symbol to validate
     * @throws IllegalArgumentException if the symbol is null, empty, or has invalid format
     */
    public void validateSymbol(final String symbol) {
        validateNotNullOrEmpty(symbol, "Stock symbol");

        if (!STOCK_SYMBOL_PATTERN.matcher(symbol).matches()) {
            throw new IllegalArgumentException("Invalid stock symbol format: " + symbol +
                    ". Expected 1-5 letters all caps, optionally followed by a dot and 1-2 letters (e.g., BOL.ST, ABB.ST)");
        }
    }

    /**
     * Validates a range parameter.
     *
     * @param range the range to validate
     * @throws IllegalArgumentException if the range is null, empty, or not supported
     */
    public void validateRange(final String range) {
        validateNotNullOrEmpty(range, "Range");

        for (String validRange : VALID_RANGES) {
            if (validRange.equals(range)) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid range: " + range +
                ". Valid ranges are: " + String.join(", ", VALID_RANGES));
    }

    /**
     * Validates an interval parameter.
     *
     * @param interval the interval to validate
     * @throws IllegalArgumentException if the interval is null, empty, or not supported
     */
    public void validateInterval(final String interval) {
        validateNotNullOrEmpty(interval, "Interval");

        for (String validInterval : VALID_INTERVALS) {
            if (validInterval.equals(interval)) {
                return;
            }
        }
        throw new IllegalArgumentException("Invalid interval: " + interval +
                ". Valid intervals are: " + String.join(", ", VALID_INTERVALS));
    }

    /**
     * Validates a list of stock symbols.
     *
     * @param stockSymbols the list of stock symbols to validate
     * @throws IllegalArgumentException if the list is null or empty, or contains invalid symbols
     */
    public void validateStockSymbolsList(final List<String> stockSymbols) {
        if (stockSymbols == null) {
            throw new IllegalArgumentException("Stock symbols list cannot be null");
        }
        if (stockSymbols.isEmpty()) {
            throw new IllegalArgumentException("Stock symbols list cannot be empty");
        }

        for (int i = 0; i < stockSymbols.size(); i++) {
            try {
                validateSymbol(stockSymbols.get(i));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid symbol at index " + i + ": " + e.getMessage());
            }
        }
    }

    /**
     * Validates JSON data.
     *
     * @param jsonString the JSON string to validate
     * @throws IllegalArgumentException if the JSON string is null or empty
     */
    public void validateJsonData(String jsonString) {
        validateNotNullOrEmpty(jsonString, "JSON string");

        // Basic JSON validation - check if it starts with { or [
        String trimmed = jsonString.trim();
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            throw new IllegalArgumentException("Invalid JSON format: must start with '{' or '['");
        }
    }

    /**
     * Validates API configuration parameters.
     *
     * @param apiKeyHeader the API key header
     * @param apiHostHeader the API host header
     * @param apiKey the API key
     * @param apiHost the API host
     * @param apiUrl the API URL
     * @throws IllegalArgumentException if any parameter is null, empty, or invalid
     */
    public void validateApiConfig(String apiKeyHeader, String apiHostHeader, String apiKey, String apiHost, String apiUrl) {
        validateNotNullOrEmpty(apiKeyHeader, "API key header");
        validateNotNullOrEmpty(apiHostHeader, "API host header");
        validateNotNullOrEmpty(apiKey, "API key");
        validateNotNullOrEmpty(apiHost, "API host");
        validateNotNullOrEmpty(apiUrl, "API URL");

        // Validate URL format
        try {
            new URL(apiUrl);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid API URL format: " + apiUrl, e);
        }
    }

    /**
     * Helper method to validate that a string is not null or empty.
     *
     * @param value the string to validate
     * @param fieldName the name of the field for error messages
     * @throws IllegalArgumentException if the value is null or empty
     */
    private void validateNotNullOrEmpty(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
        if (value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }
    }
}
