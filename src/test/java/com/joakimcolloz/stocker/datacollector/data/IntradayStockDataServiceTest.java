package com.joakimcolloz.stocker.datacollector.data;

import com.joakimcolloz.stocker.datacollector.data.exception.DataFetchException;
import com.joakimcolloz.stocker.datacollector.data.fetchers.EodhdIntradayFetcher;
import com.joakimcolloz.stocker.datacollector.data.parsers.EodhdIntradayParser;
import com.joakimcolloz.stocker.datacollector.data.validation.DataFetcherInputValidator;
import com.joakimcolloz.stocker.datacollector.database.IntradayCandlestickDao;
import com.joakimcolloz.stocker.datacollector.model.Interval;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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
}
