package com.joakimcolloz.stocker.datacollector.data;

import com.joakimcolloz.stocker.datacollector.data.exception.DataFetchException;
import com.joakimcolloz.stocker.datacollector.data.fetchers.EodhdIntradayFetcher;
import com.joakimcolloz.stocker.datacollector.data.parsers.EodhdIntradayParser;
import com.joakimcolloz.stocker.datacollector.data.validation.DataFetcherInputValidator;
import com.joakimcolloz.stocker.datacollector.database.DatabaseManager;
import com.joakimcolloz.stocker.datacollector.database.IntradayCandlestickDao;
import com.joakimcolloz.stocker.datacollector.model.Candlestick;
import com.joakimcolloz.stocker.datacollector.model.Interval;
import com.joakimcolloz.stocker.datacollector.model.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

public class IntradayStockDataService {
    private static final Logger logger = LoggerFactory.getLogger(IntradayStockDataService.class);
    private static final String MARKET_SUFFIX_SWE = ".ST";

    private static final long DEFAULT_MAX_DAYS_PER_REQUEST_5M = 600;

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

    public void addIntradayPriceDataToDbChunked(List<String> stockSymbols,
                                                Interval interval,
                                                long fromEpoch,
                                                long toEpoch,
                                                int maxBarsPerRequest) {
        validator.validateStockSymbolsList(stockSymbols);
        if (interval == null) {
            throw new IllegalArgumentException("Interval cannot be null");
        }
        if (fromEpoch <= 0) {
            throw new IllegalArgumentException("fromEpoch must be positive, got: " + fromEpoch);
        }
        if (toEpoch <= 0) {
            throw new IllegalArgumentException("toEpoch must be positive, got: " + toEpoch);
        }
        if (fromEpoch >= toEpoch) {
            throw new IllegalArgumentException("fromEpoch (" + fromEpoch + ") must be before toEpoch (" + toEpoch + ")");
        }
        if (maxBarsPerRequest <= 0) {
            throw new IllegalArgumentException("maxBarsPerRequest must be positive, got: " + maxBarsPerRequest);
        }

        String intervalString = interval.toString();
        long intervalSeconds = toSeconds(interval);
        long chunkSeconds = intervalSeconds * maxBarsPerRequest;

        addIntradayPriceDataToDbChunkedBySeconds(stockSymbols, intervalString, fromEpoch, toEpoch, chunkSeconds);
    }

    public void addIntradayPriceDataToDbChunkedMaxDays(List<String> stockSymbols,
                                                       Interval interval,
                                                       long fromEpoch,
                                                       long toEpoch,
                                                       long maxDaysPerRequest) {
        validator.validateStockSymbolsList(stockSymbols);
        if (interval == null) {
            throw new IllegalArgumentException("Interval cannot be null");
        }
        if (fromEpoch <= 0) {
            throw new IllegalArgumentException("fromEpoch must be positive, got: " + fromEpoch);
        }
        if (toEpoch <= 0) {
            throw new IllegalArgumentException("toEpoch must be positive, got: " + toEpoch);
        }
        if (fromEpoch >= toEpoch) {
            throw new IllegalArgumentException("fromEpoch (" + fromEpoch + ") must be before toEpoch (" + toEpoch + ")");
        }
        if (maxDaysPerRequest <= 0) {
            throw new IllegalArgumentException("maxDaysPerRequest must be positive, got: " + maxDaysPerRequest);
        }

        long chunkSeconds = maxDaysPerRequest * 86400L;
        addIntradayPriceDataToDbChunkedBySeconds(stockSymbols, interval.toString(), fromEpoch, toEpoch, chunkSeconds);
    }

    public void addIntradayPriceDataToDbRange(List<String> stockSymbols,
                                              Interval interval,
                                              Range range) {
        long maxDaysPerRequest = defaultMaxDaysPerRequest(interval);
        addIntradayPriceDataToDbRange(stockSymbols, interval, range, maxDaysPerRequest);
    }

    public void addIntradayPriceDataToDbRange(List<String> stockSymbols,
                                              Interval interval,
                                              Range range,
                                              long maxDaysPerRequest) {
        long toEpoch = Instant.now().getEpochSecond();
        addIntradayPriceDataToDbRange(stockSymbols, interval, range, toEpoch, maxDaysPerRequest);
    }

    void addIntradayPriceDataToDbRange(List<String> stockSymbols,
                                       Interval interval,
                                       Range range,
                                       long toEpoch,
                                       long maxDaysPerRequest) {
        if (range == null) {
            throw new IllegalArgumentException("Range cannot be null");
        }
        if (toEpoch <= 0) {
            throw new IllegalArgumentException("toEpoch must be positive, got: " + toEpoch);
        }
        if (maxDaysPerRequest <= 0) {
            throw new IllegalArgumentException("maxDaysPerRequest must be positive, got: " + maxDaysPerRequest);
        }

        long fromEpoch = computeFromEpoch(range, toEpoch);
        addIntradayPriceDataToDbChunkedMaxDays(stockSymbols, interval, fromEpoch, toEpoch, maxDaysPerRequest);
    }

    public void addIntradayPriceDataToDbChunkedLastDays(List<String> stockSymbols,
                                                        Interval interval,
                                                        long days,
                                                        int maxBarsPerRequest) {
        if (days <= 0) {
            throw new IllegalArgumentException("days must be positive, got: " + days);
        }

        long toEpoch = Instant.now().getEpochSecond();
        long fromEpoch = Instant.now().minusSeconds(days * 86400).getEpochSecond();
        addIntradayPriceDataToDbChunked(stockSymbols, interval, fromEpoch, toEpoch, maxBarsPerRequest);
    }

    public void addIntradayPriceDataToDbChunkedLastDaysMaxDays(List<String> stockSymbols,
                                                               Interval interval,
                                                               long days,
                                                               long maxDaysPerRequest) {
        if (days <= 0) {
            throw new IllegalArgumentException("days must be positive, got: " + days);
        }
        long toEpoch = Instant.now().getEpochSecond();
        long fromEpoch = Instant.now().minusSeconds(days * 86400).getEpochSecond();
        addIntradayPriceDataToDbChunkedMaxDays(stockSymbols, interval, fromEpoch, toEpoch, maxDaysPerRequest);
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

    private void addIntradayPriceDataToDbChunkedBySeconds(List<String> stockSymbols,
                                                          String intervalString,
                                                          long fromEpoch,
                                                          long toEpoch,
                                                          long chunkSeconds) {
        if (chunkSeconds <= 0) {
            throw new IllegalArgumentException("chunkSeconds must be positive, got: " + chunkSeconds);
        }

        for (String symbol : stockSymbols) {
            String fullSymbol = toFullSymbol(symbol);

            long chunkStart = fromEpoch;
            while (chunkStart < toEpoch) {
                long chunkEnd = Math.min(chunkStart + chunkSeconds, toEpoch);

                try {
                    String json = fetcher.fetchIntraday(fullSymbol, intervalString, chunkStart, chunkEnd);
                    List<Candlestick> candles = parser.parseCandles(json);

                    if (!candles.isEmpty()) {
                        intradayDao.addRows(fullSymbol, intervalString, candles);
                    }
                } catch (DataFetchException e) {
                    logger.error("Failed to fetch intraday data for {} [{}-{}]: {}",
                            fullSymbol, chunkStart, chunkEnd, e.getMessage(), e);
                } catch (Exception e) {
                    logger.error("Failed to process intraday data for {} [{}-{}]: {}",
                            fullSymbol, chunkStart, chunkEnd, e.getMessage(), e);
                }

                chunkStart = chunkEnd;
                if (chunkStart < toEpoch) {
                    sleep(delayInMs);
                }
            }

            sleep(delayInMs);
        }
    }

    private long defaultMaxDaysPerRequest(Interval interval) {
        if (interval == null) {
            throw new IllegalArgumentException("Interval cannot be null");
        }
        return switch (interval) {
            case FIVE_MINUTES -> DEFAULT_MAX_DAYS_PER_REQUEST_5M;
            default -> 30;
        };
    }

    private long computeFromEpoch(Range range, long toEpoch) {
        ZonedDateTime now = ZonedDateTime.ofInstant(Instant.ofEpochSecond(toEpoch), ZoneOffset.UTC);
        return switch (range) {
            case ONE_DAY -> now.minusDays(1).toEpochSecond();
            case FIVE_DAY -> now.minusDays(5).toEpochSecond();
            case ONE_WEEK -> now.minusWeeks(1).toEpochSecond();
            case ONE_MONTH -> now.minusMonths(1).toEpochSecond();
            case THREE_MONTHS -> now.minusMonths(3).toEpochSecond();
            case SIX_MONTHS -> now.minusMonths(6).toEpochSecond();
            case ONE_YEAR -> now.minusYears(1).toEpochSecond();
            case TWO_YEAR -> now.minusYears(2).toEpochSecond();
            case FIVE_YEARS -> now.minusYears(5).toEpochSecond();
            case TEN_YEARS -> now.minusYears(10).toEpochSecond();
            case YTD -> now.withDayOfYear(1).toLocalDate().atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            case MAX -> now.minusDays(DEFAULT_MAX_DAYS_PER_REQUEST_5M).toEpochSecond();
        };
    }

    private long toSeconds(Interval interval) {
        return switch (interval) {
            case ONE_MINUTE -> 60;
            case FIVE_MINUTES -> 300;
            case FIFTEEN_MINUTES -> 900;
            case ONE_HOUR -> 3600;
            default -> throw new IllegalArgumentException("Unsupported intraday interval: " + interval);
        };
    }
}
