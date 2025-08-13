package com.joakimcolloz.stocker.datacollector.data.fetchers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.joakimcolloz.stocker.datacollector.data.exception.DataFetchException;
import com.joakimcolloz.stocker.datacollector.data.validation.DataFetcherInputValidator;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Base data fetcher used as template for other concrete fetchers.
 * Provides common functionality for fetching data from a stock data API.
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public abstract class BaseDataFetcher {
    private static final Logger logger = LoggerFactory.getLogger(BaseDataFetcher.class);

    private final String apiKeyHeader;
    private final String apiHostHeader;
    private final String apiKey;
    private final String apiHost;
    private final String apiUrl;

    protected final DataFetcherInputValidator validator;

    /**
     * Creates a new BaseDataFetcher with the specified API configuration.
     * Validates the API configuration parameters using the {@link DataFetcherInputValidator}.
     */
    protected BaseDataFetcher(final String apiKeyHeader, final String apiHostHeader,
                              final String apiKey, final String apiHost, final String apiUrl) {
        this.validator = new DataFetcherInputValidator();

        // Validate API configuration parameters
        try {
            validator.validateApiConfig(apiKeyHeader, apiHostHeader, apiKey, apiHost, apiUrl);
            logger.debug("API configuration validation passed");
        } catch (IllegalArgumentException e) {
            logger.error("API configuration validation failed: {}", e.getMessage());
            throw e;
        }

        this.apiKeyHeader = apiKeyHeader;
        this.apiHostHeader = apiHostHeader;
        this.apiKey = apiKey;
        this.apiHost = apiHost;
        this.apiUrl = apiUrl;

        logger.debug("BaseDataFetcher initialized for API: {}", apiUrl);
    }

    protected BaseDataFetcher(final String apiKeyHeader, final String apiHostHeader,
                              final String apiKey, final String apiHost, final String apiUrl,
                              final DataFetcherInputValidator validator) {
        this.validator = validator;

        // Validate API configuration parameters
        try {
            validator.validateApiConfig(apiKeyHeader, apiHostHeader, apiKey, apiHost, apiUrl);
            logger.debug("API configuration validation passed");
        } catch (IllegalArgumentException e) {
            logger.error("API configuration validation failed: {}", e.getMessage());
            throw e;
        }

        this.apiKeyHeader = apiKeyHeader;
        this.apiHostHeader = apiHostHeader;
        this.apiKey = apiKey;
        this.apiHost = apiHost;
        this.apiUrl = apiUrl;

        logger.debug("BaseDataFetcher initialized with injected validator for API: {}", apiUrl);
    }

    /**
     * Fetches stock data and returns the response as a string in JSON format.
     *
     * @param stockName the stock symbol to fetch data for (e.g., "ABB")
     * @param range the time range to fetch data for
     * @param interval the interval of the data to fetch
     * @return the response as a string in JSON format
     * @throws DataFetchException if the request fails
     */
    public String fetchData(final String stockName, final String range, final String interval)
            throws DataFetchException {

        logger.debug("Starting fetchData for stock: {}, range: {}, interval: {}", stockName, range, interval);

        // Validate input parameters using validator
        try {
            validator.validateSymbol(stockName);
            validator.validateRange(range);
            validator.validateInterval(interval);
            logger.debug("Input validation passed for stock: {}", stockName);
        } catch (IllegalArgumentException e) {
            logger.error("Input validation failed for stock {}: {}", stockName, e.getMessage());
            throw e; // Let unchecked exception bubble up
        }

        String apiUrl = buildApiUrl(stockName, range, interval);
        logger.info("Fetching data for stock: {}, range: {}, interval: {}", stockName, range, interval);

        HttpRequest request = createHttpRequest(apiUrl);
        HttpResponse<String> response = executeRequest(request, stockName);

        String responseBody = validateAndGetResponseBody(response, stockName);

        logger.info("Successfully received data for {}", stockName);
        if (logger.isDebugEnabled()) {
            logger.debug("Received response for {}: {}", stockName, responseBody);
        }

        return responseBody;
    }

    private String buildApiUrl(String stockName, String range, String interval) {
        return apiUrl + stockName + "?range=" + range + "&interval=" + interval;
    }

    private HttpRequest createHttpRequest(String apiUrl) throws DataFetchException {
        try {
            logger.debug("Creating HTTP request for URL: {}", apiUrl);
            return HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header(apiKeyHeader, apiKey)
                    .header(apiHostHeader, apiHost)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    .header("Accept", "application/json, text/plain, */*")
                    .header("Accept-Language", "en-US,en;q=0.9")
                    .method("GET", HttpRequest.BodyPublishers.noBody())
                    .build();
        } catch (IllegalArgumentException e) {
            logger.error("Invalid URI: {}", apiUrl, e);
            throw new DataFetchException("Failed to create HTTP request for URL: " + apiUrl, e);
        }
    }

    private HttpResponse<String> executeRequest(HttpRequest request, String stockName)
            throws DataFetchException {
        try {
            logger.debug("Executing HTTP request for stock: {}", stockName);
            HttpResponse<String> response = HttpClient.newHttpClient().send(
                    request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                logger.error("HTTP request failed for {} with status code: {}", stockName, response.statusCode());
                throw new DataFetchException("HTTP request failed for " + stockName +
                        " with status code: " + response.statusCode());
            }

            logger.debug("HTTP request successful for stock: {}, status: {}", stockName, response.statusCode());
            return response;

        } catch (DataFetchException e) {
            // Re-throw DataFetchException without wrapping to avoid double-wrapping
            throw e;
        } catch (IOException e) {
            logger.error("Network error while fetching data for {}: {}", stockName, e.getMessage(), e);
            throw new DataFetchException("Network error while fetching data for " + stockName, e);
        } catch (InterruptedException e) {
            logger.error("Request interrupted while fetching data for {}: {}", stockName, e.getMessage(), e);
            Thread.currentThread().interrupt(); // Restore interrupted status
            throw new DataFetchException("Request was interrupted while fetching data for " + stockName, e);
        } catch (Exception e) {
            logger.error("Unexpected error during HTTP request for {}: {}", stockName, e.getMessage(), e);
            throw new DataFetchException("Unexpected error during HTTP request for " + stockName, e);
        }
    }

    private String validateAndGetResponseBody(HttpResponse<String> response, String stockName)
            throws DataFetchException {
        String responseBody = response.body();
        if (responseBody == null) {
            logger.error("HTTP request failed for {}: Response body was null", stockName);
            throw new DataFetchException("Response body was null for stock: " + stockName);
        }

        // Validate JSON format
        try {
            validator.validateJsonData(responseBody);
            logger.debug("JSON response validation passed for stock: {}", stockName);
        } catch (IllegalArgumentException e) {
            logger.error("Invalid JSON response for stock {}: {}", stockName, e.getMessage());
            throw new DataFetchException("Invalid JSON response for stock: " + stockName, e);
        }

        return responseBody;
    }
}
