package com.joakimcolloz.stocker.datacollector.data.fetchers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import com.joakimcolloz.stocker.datacollector.data.exception.DataFetchException;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link BaseDataFetcher} class focusing on HTTP request handling
 * and exception management.
 *
 * @author Joakim Colloz
 * @version 1.0
 */
@ExtendWith(MockitoExtension.class)
class BaseDataFetcherTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    private TestableBaseDataFetcher fetcher;
    private static final String apiUrl = "https://api.example.com/stock/";

    @BeforeEach
    void setUp() {
        fetcher = new TestableBaseDataFetcher(
                "X-API-Key",
                "X-API-Host",
                "test-api-key",
                "test-api-host",
                apiUrl
        );
    }

    @Test
    void validInputsReturnsResponseBody() throws Exception {
        // Arrange
        String expectedResponse = "{\"data\": \"test\"}";
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(expectedResponse);

        try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {
            mockedHttpClient.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);
            when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(mockHttpResponse);

            // Act
            String result = fetcher.fetchData("BOL.ST", "1d", "1m");

            // Assert
            assertEquals(expectedResponse, result);
        }
    }

    @Test
    void invalidInputThrowsIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> fetcher.fetchData(null, "1d", "1m"));
    }

    @Test
    void httpErrorThrowsDataFetchException() throws Exception {
        // Arrange
        when(mockHttpResponse.statusCode()).thenReturn(404);

        try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {
            mockedHttpClient.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);
            when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(mockHttpResponse);

            // Act & Assert
            DataFetchException exception = assertThrows(DataFetchException.class,
                    () -> fetcher.fetchData("BOL.ST", "1d", "1m"));

            // The exception message will be wrapped by the general exception handler
            assertTrue(exception.getMessage().contains("Unexpected error during HTTP request") ||
                    exception.getMessage().contains("status code: 404"));
        }
    }

    @Test
    void networkErrorThrowsDataFetchException() throws Exception {
        // Arrange
        IOException ioException = new IOException("Network error");

        try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {
            mockedHttpClient.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);
            when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenThrow(ioException);

            // Act & Assert
            DataFetchException exception = assertThrows(DataFetchException.class,
                    () -> fetcher.fetchData("BOL.ST", "1d", "1m"));

            assertTrue(exception.getMessage().contains("Network error"));
            assertEquals(ioException, exception.getCause());
        }
    }

    @Test
    void nullResponseBodyThrowsDataFetchException() throws Exception {
        // Arrange
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn(null);

        try (MockedStatic<HttpClient> mockedHttpClient = mockStatic(HttpClient.class)) {
            mockedHttpClient.when(HttpClient::newHttpClient).thenReturn(mockHttpClient);
            when(mockHttpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                    .thenReturn(mockHttpResponse);

            // Act & Assert
            DataFetchException exception = assertThrows(DataFetchException.class,
                    () -> fetcher.fetchData("BOL.ST", "1d", "1m"));

            assertTrue(exception.getMessage().contains("Response body was null"));
        }
    }

    @Test
    void invalidUrlThrowsIllegalArgumentException() {
        // Act & Assert - Exception should be thrown during construction
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            new TestableBaseDataFetcher(
                    "X-API-Key",
                    "X-API-Host",
                    "test-api-key",
                    "test-api-host",
                    "invalid-url-format"
            );
        });

        assertTrue(exception.getMessage().contains("Invalid API URL format"));
    }

    // Simple testable subclass
    private static class TestableBaseDataFetcher extends BaseDataFetcher {
        public TestableBaseDataFetcher(String apiKeyHeader, String apiHostHeader,
                                       String apiKey, String apiHost, String apiUrl) {
            super(apiKeyHeader, apiHostHeader, apiKey, apiHost, apiUrl);
        }

        @Override
        protected String buildApiUrl(String stockName, String range, String interval) {
            return apiUrl + stockName + "?range=" + range + "&interval=" + interval;
        }
    }
}
