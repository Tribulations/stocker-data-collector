package com.joakimcolloz.stocker.datacollector.data.fetchers;

import com.joakimcolloz.stocker.datacollector.data.exception.DataFetchException;
import com.joakimcolloz.stocker.datacollector.data.validation.DataFetcherInputValidator;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class EodhdIntradayFetcher {
    private static final Logger logger = LoggerFactory.getLogger(EodhdIntradayFetcher.class);

    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .systemProperties()
            .load();

    private static final String API_TOKEN_ENV_VAR = "EODHD_API_TOKEN";
    private static final String BASE_URL = "https://eodhd.com/api/intraday/";
    private static final String FORMAT = "json";
    private static final String SUPPORTED_INTERVAL = "5m";

    private final HttpClient httpClient;
    private final DataFetcherInputValidator validator;
    private final String apiToken;

    public EodhdIntradayFetcher() {
        this(HttpClient.newHttpClient(), new DataFetcherInputValidator(), dotenv.get(API_TOKEN_ENV_VAR));
    }

    public EodhdIntradayFetcher(HttpClient httpClient, DataFetcherInputValidator validator, String apiToken) {
        if (httpClient == null) {
            throw new IllegalArgumentException("HttpClient cannot be null");
        }
        if (validator == null) {
            throw new IllegalArgumentException("Validator cannot be null");
        }
        if (apiToken == null || apiToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Missing API token: " + API_TOKEN_ENV_VAR);
        }

        this.httpClient = httpClient;
        this.validator = validator;
        this.apiToken = apiToken;
    }

    public String fetchIntraday(String symbol, String interval) throws DataFetchException {
        validateInputs(symbol, interval);
        String url = buildUrl(symbol, interval, null, null);
        return executeRequest(url, symbol);
    }

    public String fetchIntraday(String symbol, String interval, long fromEpoch, long toEpoch) throws DataFetchException {
        validateInputs(symbol, interval);
        validateTimeRange(fromEpoch, toEpoch);

        String url = buildUrl(symbol, interval, fromEpoch, toEpoch);
        return executeRequest(url, symbol);
    }

    private void validateInputs(String symbol, String interval) {
        validator.validateSymbol(symbol);
        if (interval == null || interval.trim().isEmpty()) {
            throw new IllegalArgumentException("Interval cannot be null or empty");
        }
        if (!SUPPORTED_INTERVAL.equals(interval)) {
            throw new IllegalArgumentException("Unsupported EODHD intraday interval: " + interval + ". Supported: " + SUPPORTED_INTERVAL);
        }
    }

    private void validateTimeRange(long fromEpoch, long toEpoch) {
        if (fromEpoch <= 0) {
            throw new IllegalArgumentException("fromEpoch must be positive, got: " + fromEpoch);
        }
        if (toEpoch <= 0) {
            throw new IllegalArgumentException("toEpoch must be positive, got: " + toEpoch);
        }
        if (fromEpoch >= toEpoch) {
            throw new IllegalArgumentException("fromEpoch (" + fromEpoch + ") must be before toEpoch (" + toEpoch + ")");
        }
    }

    private String buildUrl(String symbol, String interval, Long fromEpoch, Long toEpoch) {
        StringBuilder sb = new StringBuilder(BASE_URL)
                .append(symbol)
                .append("?api_token=")
                .append(apiToken)
                .append("&fmt=")
                .append(FORMAT)
                .append("&interval=")
                .append(interval);

        if (fromEpoch != null && toEpoch != null) {
            sb.append("&from=").append(fromEpoch);
            sb.append("&to=").append(toEpoch);
        }

        return sb.toString();
    }

    private String executeRequest(String url, String context) throws DataFetchException {
        try {
            logger.debug("Fetching EODHD intraday data for {}: {}", context, url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Accept", "application/json")
                    .header("User-Agent", "Stocker-Data-Collector/1.0")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new DataFetchException("HTTP request failed for " + context +
                        " with status code: " + response.statusCode());
            }

            String body = response.body();
            if (body == null || body.trim().isEmpty()) {
                throw new DataFetchException("Empty response body for " + context);
            }

            return body;

        } catch (DataFetchException e) {
            throw e;
        } catch (IOException e) {
            throw new DataFetchException("Network error while fetching data for " + context, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DataFetchException("Request was interrupted while fetching data for " + context, e);
        } catch (Exception e) {
            throw new DataFetchException("Unexpected error while fetching data for " + context, e);
        }
    }
}
