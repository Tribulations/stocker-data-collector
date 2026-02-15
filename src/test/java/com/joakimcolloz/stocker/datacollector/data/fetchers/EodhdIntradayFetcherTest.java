package com.joakimcolloz.stocker.datacollector.data.fetchers;

import com.joakimcolloz.stocker.datacollector.data.exception.DataFetchException;
import com.joakimcolloz.stocker.datacollector.data.validation.DataFetcherInputValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EodhdIntradayFetcherTest {

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpResponse<String> mockHttpResponse;

    private EodhdIntradayFetcher fetcher;

    @BeforeEach
    void setUp() {
        fetcher = new EodhdIntradayFetcher(mockHttpClient, new DataFetcherInputValidator(), "test-token");
    }

    @Test
    void shouldBuildUrlWithoutFromTo() throws Exception {
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn("[]");

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        when(mockHttpClient.send(captor.capture(), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockHttpResponse);

        fetcher.fetchIntraday("BOL.ST", "5m");

        assertEquals(
                "https://eodhd.com/api/intraday/BOL.ST?api_token=test-token&fmt=json&interval=5m",
                captor.getValue().uri().toString());
    }

    @Test
    void shouldBuildUrlWithFromTo() throws Exception {
        when(mockHttpResponse.statusCode()).thenReturn(200);
        when(mockHttpResponse.body()).thenReturn("[]");

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        when(mockHttpClient.send(captor.capture(), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockHttpResponse);

        fetcher.fetchIntraday("BOL.ST", "5m", 1627896900L, 1630575300L);

        assertEquals(
                "https://eodhd.com/api/intraday/BOL.ST?api_token=test-token&fmt=json&interval=5m&from=1627896900&to=1630575300",
                captor.getValue().uri().toString());
    }

    @Test
    void invalidIntervalThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> fetcher.fetchIntraday("BOL.ST", "1m"));
    }

    @Test
    void httpErrorThrowsDataFetchException() throws Exception {
        when(mockHttpResponse.statusCode()).thenReturn(404);
        when(mockHttpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenReturn(mockHttpResponse);

        assertThrows(DataFetchException.class, () -> fetcher.fetchIntraday("BOL.ST", "5m"));
    }

    @Test
    void networkErrorThrowsDataFetchException() throws Exception {
        when(mockHttpClient.send(any(HttpRequest.class), ArgumentMatchers.<HttpResponse.BodyHandler<String>>any()))
                .thenThrow(new IOException("Network error"));

        assertThrows(DataFetchException.class, () -> fetcher.fetchIntraday("BOL.ST", "5m"));
    }
}
