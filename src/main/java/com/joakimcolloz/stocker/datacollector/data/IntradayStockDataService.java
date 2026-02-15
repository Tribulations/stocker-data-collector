package com.joakimcolloz.stocker.datacollector.data;

import com.joakimcolloz.stocker.datacollector.data.exception.DataFetchException;
import com.joakimcolloz.stocker.datacollector.data.fetchers.EodhdIntradayFetcher;
import com.joakimcolloz.stocker.datacollector.data.parsers.EodhdIntradayParser;
import com.joakimcolloz.stocker.datacollector.data.validation.DataFetcherInputValidator;
import com.joakimcolloz.stocker.datacollector.database.DatabaseManager;
import com.joakimcolloz.stocker.datacollector.database.IntradayCandlestickDao;
import com.joakimcolloz.stocker.datacollector.model.Candlestick;
import com.joakimcolloz.stocker.datacollector.model.Interval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class IntradayStockDataService {
    private static final Logger logger = LoggerFactory.getLogger(IntradayStockDataService.class);
    private static final String MARKET_SUFFIX_SWE = ".ST";

    private final EodhdIntradayFetcher fetcher;
    private final EodhdIntradayParser parser;
    private final IntradayCandlestickDao intradayDao;
    private final DataFetcherInputValidator validator;

    private long delayInMs = 200;

    public IntradayStockDataService(DatabaseManager databaseManager) {
        this(new EodhdIntradayFetcher(), new EodhdIntradayParser(), databaseManager.createIntradayCandlestickDao(), new DataFetcherInputValidator());
    }

    public IntradayStockDataService(EodhdIntradayFetcher fetcher,
                                   EodhdIntradayParser parser,
                                   IntradayCandlestickDao intradayDao,
                                   DataFetcherInputValidator validator) {
        if (fetcher == null) {
            throw new IllegalArgumentException("Fetcher cannot be null");
        }
        if (parser == null) {
            throw new IllegalArgumentException("Parser cannot be null");
        }
        if (intradayDao == null) {
            throw new IllegalArgumentException("Intraday DAO cannot be null");
        }
        if (validator == null) {
            throw new IllegalArgumentException("Validator cannot be null");
        }

        this.fetcher = fetcher;
        this.parser = parser;
        this.intradayDao = intradayDao;
        this.validator = validator;
    }

    public void addIntradayPriceDataToDb(List<String> stockSymbols, Interval interval) {
        validator.validateStockSymbolsList(stockSymbols);
        if (interval == null) {
            throw new IllegalArgumentException("Interval cannot be null");
        }

        String intervalString = interval.toString();

        for (String symbol : stockSymbols) {
            String fullSymbol = toFullSymbol(symbol);

            try {
                String json = fetcher.fetchIntraday(fullSymbol, intervalString);
                List<Candlestick> candles = parser.parseCandles(json);

                if (candles.isEmpty()) {
                    continue;
                }

                intradayDao.addRows(fullSymbol, intervalString, candles);
            } catch (DataFetchException e) {
                logger.error("Failed to fetch intraday data for {}: {}", fullSymbol, e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Failed to process intraday data for {}: {}", fullSymbol, e.getMessage(), e);
            }

            sleep(delayInMs);
        }
    }

    public void addIntradayPriceDataToDb(List<String> stockSymbols, Interval interval, long fromEpoch, long toEpoch) {
        validator.validateStockSymbolsList(stockSymbols);
        if (interval == null) {
            throw new IllegalArgumentException("Interval cannot be null");
        }

        String intervalString = interval.toString();

        for (String symbol : stockSymbols) {
            String fullSymbol = toFullSymbol(symbol);

            try {
                String json = fetcher.fetchIntraday(fullSymbol, intervalString, fromEpoch, toEpoch);
                List<Candlestick> candles = parser.parseCandles(json);

                if (candles.isEmpty()) {
                    continue;
                }

                intradayDao.addRows(fullSymbol, intervalString, candles);
            } catch (DataFetchException e) {
                logger.error("Failed to fetch intraday data for {}: {}", fullSymbol, e.getMessage(), e);
            } catch (Exception e) {
                logger.error("Failed to process intraday data for {}: {}", fullSymbol, e.getMessage(), e);
            }

            sleep(delayInMs);
        }
    }

    public void setDelayInMs(long delayInMs) {
        this.delayInMs = delayInMs;
    }

    public long getDelayInMs() {
        return delayInMs;
    }

    private String toFullSymbol(String symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("Symbol cannot be null");
        }
        String trimmed = symbol.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Symbol cannot be empty");
        }
        if (trimmed.contains(".")) {
            return trimmed;
        }
        return trimmed + MARKET_SUFFIX_SWE;
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
