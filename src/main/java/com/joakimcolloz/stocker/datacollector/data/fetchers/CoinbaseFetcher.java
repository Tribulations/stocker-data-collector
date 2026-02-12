package com.joakimcolloz.stocker.datacollector.data.fetchers;

import com.joakimcolloz.stocker.datacollector.data.exception.DataFetchException;
import com.joakimcolloz.stocker.datacollector.data.validation.CryptoInputValidator;
import com.joakimcolloz.stocker.datacollector.model.Granularity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetches cryptocurrency candlestick and product data from the Coinbase public API.
 * No authentication is required. Rate limited at 10 req/s per IP.
 *
 * @author Joakim Colloz
 * @version 1.0
 */
public class CoinbaseFetcher {
    private static final Logger logger = LoggerFactory.getLogger(CoinbaseFetcher.class);

    private static final String BASE_URL = "https://api.coinbase.com/api/v3/brokerage/market";
    private static final int MAX_CANDLES_PER_REQUEST = 300;
    private static final int MAX_PRODUCTS_PER_PAGE = 2000;
    private static final int MAX_RETRIES = 3;
    private static final long RETRY_DELAY_MS = 1000;
    private static final long CHUNK_DELAY_MS = 150;

    private final HttpClient httpClient;
    private final CryptoInputValidator validator;

    public CoinbaseFetcher() {
        this.httpClient = HttpClient.newHttpClient();
        this.validator = new CryptoInputValidator();
    }

    public CoinbaseFetcher(HttpClient httpClient, CryptoInputValidator validator) {
        this.httpClient = httpClient;
        this.validator = validator;
    }

    /**
     * Fetches a single chunk of candle data for a product.
     *
     * @param productId   the product ID (e.g., "BTC-EUR")
     * @param startEpoch  start time in seconds since epoch
     * @param endEpoch    end time in seconds since epoch
     * @param granularity the candle granularity
     * @return the JSON response string
     * @throws DataFetchException if the request fails after retries
     */
    public String fetchCandles(String productId, long startEpoch, long endEpoch, Granularity granularity)
            throws DataFetchException {
        validator.validateProductId(productId);
        validator.validateTimeRange(startEpoch, endEpoch);
        validator.validateGranularity(granularity);

        String url = String.format("%s/products/%s/candles?start=%d&end=%d&granularity=%s",
                BASE_URL, productId, startEpoch, endEpoch, granularity.name());

        logger.debug("Fetching candles for {} from {} to {} with granularity {}",
                productId, startEpoch, endEpoch, granularity);

        return executeWithRetry(url, productId);
    }

    /**
     * Fetches all candle data for a product by splitting the time range into chunks
     * of at most {@value MAX_CANDLES_PER_REQUEST} candles each.
     *
     * @param productId   the product ID (e.g., "BTC-EUR")
     * @param startEpoch  start time in seconds since epoch
     * @param endEpoch    end time in seconds since epoch
     * @param granularity the candle granularity
     * @return list of JSON response strings, one per chunk
     * @throws DataFetchException if any chunk request fails after retries
     */
    public List<String> fetchAllCandles(String productId, long startEpoch, long endEpoch, Granularity granularity)
            throws DataFetchException {
        validator.validateProductId(productId);
        validator.validateTimeRange(startEpoch, endEpoch);
        validator.validateGranularity(granularity);

        long windowSize = (long) MAX_CANDLES_PER_REQUEST * granularity.getSeconds();
        List<String> responses = new ArrayList<>();

        long chunkStart = startEpoch;
        int chunkCount = 0;

        while (chunkStart < endEpoch) {
            long chunkEnd = Math.min(chunkStart + windowSize, endEpoch);

            logger.debug("Fetching candle chunk {} for {} [{} - {}]",
                    chunkCount + 1, productId, chunkStart, chunkEnd);

            String response = fetchCandles(productId, chunkStart, chunkEnd, granularity);
            responses.add(response);
            chunkCount++;

            chunkStart = chunkEnd;

            // Delay between chunks to respect rate limits
            if (chunkStart < endEpoch) {
                sleep(CHUNK_DELAY_MS);
            }
        }

        logger.info("Fetched {} candle chunks for {}", chunkCount, productId);
        return responses;
    }

    /**
     * Fetches a single page of products.
     *
     * @param cursor the pagination cursor (null or empty for first page)
     * @param limit  the number of products per page (max 250)
     * @return the JSON response string
     * @throws DataFetchException if the request fails after retries
     */
    public String fetchProducts(String cursor, int limit) throws DataFetchException {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL)
                .append("/products?limit=").append(limit);

        if (cursor != null && !cursor.isEmpty()) {
            urlBuilder.append("&offset=").append(cursor);
        }

        String url = urlBuilder.toString();
        logger.debug("Fetching products page with cursor: {}, limit: {}", cursor, limit);

        return executeWithRetry(url, "products");
    }

    /**
     * Fetches all products by paginating through all pages using offset-based pagination.
     * Continues fetching until a page returns fewer products than the requested limit.
     *
     * @return list of JSON response strings, one per page
     * @throws DataFetchException if any page request fails after retries
     */
    public List<String> fetchAllProducts() throws DataFetchException {
        List<String> responses = new ArrayList<>();
        int offset = 0;
        int pageCount = 0;

        while (true) {
            String cursor = offset > 0 ? String.valueOf(offset) : null;
            String response = fetchProducts(cursor, MAX_PRODUCTS_PER_PAGE);
            responses.add(response);
            pageCount++;

            int productsInPage = countProductsInResponse(response);
            offset += productsInPage;

            if (productsInPage < MAX_PRODUCTS_PER_PAGE) {
                break;
            }

            sleep(CHUNK_DELAY_MS);
        }

        logger.info("Fetched {} product pages", pageCount);
        return responses;
    }

    private String executeWithRetry(String url, String context) throws DataFetchException {
        DataFetchException lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                return executeRequest(url, context);
            } catch (DataFetchException e) {
                lastException = e;
                if (attempt < MAX_RETRIES && isRetryable(e)) {
                    logger.warn("Request failed for {} (attempt {}/{}), retrying in {}ms: {}",
                            context, attempt, MAX_RETRIES, RETRY_DELAY_MS * attempt, e.getMessage());
                    sleep(RETRY_DELAY_MS * attempt);
                } else if (!isRetryable(e)) {
                    throw e;
                }
            }
        }

        throw new DataFetchException("Failed after " + MAX_RETRIES + " attempts for " + context,
                lastException);
    }

    private String executeRequest(String url, String context) throws DataFetchException {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Stocker-Data-Collector/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 429) {
                throw new DataFetchException("Rate limited (HTTP 429) for " + context);
            }

            if (response.statusCode() >= 400) {
                throw new DataFetchException("HTTP request failed for " + context +
                        " with status code: " + response.statusCode());
            }

            String body = response.body();
            if (body == null || body.trim().isEmpty()) {
                throw new DataFetchException("Empty response body for " + context);
            }

            logger.debug("Successfully fetched data for {} ({} chars)", context, body.length());
            return body;

        } catch (DataFetchException e) {
            throw e;
        } catch (IOException e) {
            logger.error("Network error while fetching data for {}: {}", context, e.getMessage(), e);
            throw new DataFetchException("Network error while fetching data for " + context, e);
        } catch (InterruptedException e) {
            logger.error("Request interrupted while fetching data for {}: {}", context, e.getMessage(), e);
            Thread.currentThread().interrupt();
            throw new DataFetchException("Request was interrupted while fetching data for " + context, e);
        }
    }

    private boolean isRetryable(DataFetchException e) {
        String message = e.getMessage();
        return message != null && (message.contains("429") || message.contains("Network error"));
    }

    /**
     * Counts the number of products in a JSON response by looking for the "num_products" field.
     * Falls back to counting "product_id" occurrences if the field is missing.
     */
    private int countProductsInResponse(String json) {
        // Look for "num_products":<value> pattern
        int idx = json.indexOf("\"num_products\"");
        if (idx != -1) {
            int colonIdx = json.indexOf(':', idx);
            if (colonIdx != -1) {
                int start = colonIdx + 1;
                while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
                    start++;
                }
                int end = start;
                while (end < json.length() && Character.isDigit(json.charAt(end))) {
                    end++;
                }
                if (end > start) {
                    return Integer.parseInt(json.substring(start, end));
                }
            }
        }
        return 0;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            logger.warn("Sleep interrupted", e);
            Thread.currentThread().interrupt();
        }
    }
}
