package com.joakimcolloz.stocker.datacollector.data;

import com.joakimcolloz.stocker.datacollector.data.exception.DataFetchException;
import com.joakimcolloz.stocker.datacollector.data.fetchers.EodhdIntradayFetcher;
import com.joakimcolloz.stocker.datacollector.data.parsers.EodhdIntradayParser;
import com.joakimcolloz.stocker.datacollector.data.validation.DataFetcherInputValidator;
import com.joakimcolloz.stocker.datacollector.database.IntradayCandlestickDao;
import com.joakimcolloz.stocker.datacollector.model.Interval;
import com.joakimcolloz.stocker.datacollector.model.Range;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class IntradayStockDataServiceTest {

    @Test
    void shouldFetchParseAndStoreCandles() throws Exception {
        EodhdIntradayFetcher fetcher = mock(EodhdIntradayFetcher.class);
        EodhdIntradayParser parser = new EodhdIntradayParser();
        IntradayCandlestickDao dao = mock(IntradayCandlestickDao.class);

        String json = "[{\"timestamp\":1000,\"open\":1.0,\"high\":2.0,\"low\":0.5,\"close\":1.5,\"volume\":0}]";
        when(fetcher.fetchIntraday("BOL.ST", "5m")).thenReturn(json);

        IntradayStockDataService service = new IntradayStockDataService(fetcher, parser, dao, new DataFetcherInputValidator());
        service.setDelayInMs(0);

        service.addIntradayPriceDataToDb(List.of("BOL"), Interval.FIVE_MINUTES);

        verify(dao, times(1)).addRows(eq("BOL.ST"), eq("5m"), anyList());
    }

    @Test
    void shouldContinueOnFetchFailure() throws Exception {
        EodhdIntradayFetcher fetcher = mock(EodhdIntradayFetcher.class);
        EodhdIntradayParser parser = new EodhdIntradayParser();
        IntradayCandlestickDao dao = mock(IntradayCandlestickDao.class);

        when(fetcher.fetchIntraday("BOL.ST", "5m")).thenThrow(new DataFetchException("fail"));
        when(fetcher.fetchIntraday("ABB.ST", "5m")).thenReturn("[]");

        IntradayStockDataService service = new IntradayStockDataService(fetcher, parser, dao, new DataFetcherInputValidator());
        service.setDelayInMs(0);

        service.addIntradayPriceDataToDb(List.of("BOL", "ABB"), Interval.FIVE_MINUTES);

        verify(dao, times(0)).addRows(eq("BOL.ST"), eq("5m"), anyList());
        verify(dao, times(0)).addRows(eq("ABB.ST"), eq("5m"), anyList());
    }

    @Test
    void shouldFetchChunkedByMaxBarsPerRequest() throws Exception {
        EodhdIntradayFetcher fetcher = mock(EodhdIntradayFetcher.class);
        EodhdIntradayParser parser = new EodhdIntradayParser();
        IntradayCandlestickDao dao = mock(IntradayCandlestickDao.class);

        long fromEpoch = 1_000_000L;
        long toEpoch = fromEpoch + (36_000L * 2) + 123L;
        String json = "[]";

        when(fetcher.fetchIntraday("BOL.ST", "5m", fromEpoch, fromEpoch + 36_000L)).thenReturn(json);
        when(fetcher.fetchIntraday("BOL.ST", "5m", fromEpoch + 36_000L, fromEpoch + 72_000L)).thenReturn(json);
        when(fetcher.fetchIntraday("BOL.ST", "5m", fromEpoch + 72_000L, toEpoch)).thenReturn(json);

        IntradayStockDataService service = new IntradayStockDataService(fetcher, parser, dao, new DataFetcherInputValidator());
        service.setDelayInMs(0);

        service.addIntradayPriceDataToDbChunked(List.of("BOL"), Interval.FIVE_MINUTES, fromEpoch, toEpoch, 120);

        verify(fetcher, times(1)).fetchIntraday("BOL.ST", "5m", fromEpoch, fromEpoch + 36_000L);
        verify(fetcher, times(1)).fetchIntraday("BOL.ST", "5m", fromEpoch + 36_000L, fromEpoch + 72_000L);
        verify(fetcher, times(1)).fetchIntraday("BOL.ST", "5m", fromEpoch + 72_000L, toEpoch);
        verifyNoMoreInteractions(fetcher);
    }

    @Test
    void shouldFetchChunkedByMaxDaysPerRequest() throws Exception {
        EodhdIntradayFetcher fetcher = mock(EodhdIntradayFetcher.class);
        EodhdIntradayParser parser = new EodhdIntradayParser();
        IntradayCandlestickDao dao = mock(IntradayCandlestickDao.class);

        long fromEpoch = 1_000_000L;
        long toEpoch = fromEpoch + (86400L * 2) + 1;
        String json = "[]";

        when(fetcher.fetchIntraday("BOL.ST", "5m", fromEpoch, fromEpoch + 86400L)).thenReturn(json);
        when(fetcher.fetchIntraday("BOL.ST", "5m", fromEpoch + 86400L, fromEpoch + (86400L * 2))).thenReturn(json);
        when(fetcher.fetchIntraday("BOL.ST", "5m", fromEpoch + (86400L * 2), toEpoch)).thenReturn(json);

        IntradayStockDataService service = new IntradayStockDataService(fetcher, parser, dao, new DataFetcherInputValidator());
        service.setDelayInMs(0);

        service.addIntradayPriceDataToDbChunkedMaxDays(List.of("BOL"), Interval.FIVE_MINUTES, fromEpoch, toEpoch, 1);

        verify(fetcher, times(1)).fetchIntraday("BOL.ST", "5m", fromEpoch, fromEpoch + 86400L);
        verify(fetcher, times(1)).fetchIntraday("BOL.ST", "5m", fromEpoch + 86400L, fromEpoch + (86400L * 2));
        verify(fetcher, times(1)).fetchIntraday("BOL.ST", "5m", fromEpoch + (86400L * 2), toEpoch);
        verifyNoMoreInteractions(fetcher);
    }

    @Test
    void shouldFetchByRangeUsingMaxDaysChunking() throws Exception {
        EodhdIntradayFetcher fetcher = mock(EodhdIntradayFetcher.class);
        EodhdIntradayParser parser = new EodhdIntradayParser();
        IntradayCandlestickDao dao = mock(IntradayCandlestickDao.class);

        long toEpoch = 1_700_000_000L;
        long fromEpoch = toEpoch - 86400L;
        String json = "[]";

        when(fetcher.fetchIntraday("BOL.ST", "5m", fromEpoch, toEpoch)).thenReturn(json);

        IntradayStockDataService service = new IntradayStockDataService(fetcher, parser, dao, new DataFetcherInputValidator());
        service.setDelayInMs(0);

        service.addIntradayPriceDataToDbRange(List.of("BOL"), Interval.FIVE_MINUTES, Range.ONE_DAY, toEpoch, 600);

        verify(fetcher, times(1)).fetchIntraday("BOL.ST", "5m", fromEpoch, toEpoch);
        verifyNoMoreInteractions(fetcher);
    }
}
