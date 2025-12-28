package com.joakimcolloz.stocker.datacollector.data.parsers;

import com.google.gson.JsonParseException;
import com.joakimcolloz.stocker.datacollector.model.Candlestick;
import com.joakimcolloz.stocker.datacollector.model.TradingPeriod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.joakimcolloz.stocker.datacollector.data.fetchers.JsonConstants.TIMESTAMP;
import static com.joakimcolloz.stocker.datacollector.data.fetchers.JsonConstants.OPEN;
import static com.joakimcolloz.stocker.datacollector.data.fetchers.JsonConstants.CLOSE;
import static com.joakimcolloz.stocker.datacollector.data.fetchers.JsonConstants.LOW;
import static com.joakimcolloz.stocker.datacollector.data.fetchers.JsonConstants.HIGH;
import static com.joakimcolloz.stocker.datacollector.data.fetchers.JsonConstants.VOLUME;
import static com.joakimcolloz.stocker.datacollector.data.fetchers.JsonConstants.DATA_GRANULARITY;
import static com.joakimcolloz.stocker.datacollector.data.fetchers.JsonConstants.SYMBOL;
import static com.joakimcolloz.stocker.datacollector.data.fetchers.JsonConstants.INDICATORS;
import static com.joakimcolloz.stocker.datacollector.data.fetchers.JsonConstants.RANGE;

/**
 * Parser for Yahoo Finance JSON data.
 * Extracts candlestick data (OHLCV) and metadata from Yahoo Finance API responses.
 *
 * @author Joakim Colloz
 * @version 1.1
 */
public class YahooFinanceParser extends BaseParser {
    private static final Logger logger = LoggerFactory.getLogger(YahooFinanceParser.class);
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private String symbol;
    private final List<Long> timestampList;
    private final List<Long> volumeList;
    private final List<Double> openList;
    private final List<Double> closeList;
    private final List<Double> lowList;
    private final List<Double> highList;
    private String interval;
    private String range;
    private TradingPeriod tradingPeriod;

    /**
     * Creates a new YahooFinanceParser for the given JSON string.
     *
     * @param jsonString the Yahoo Finance JSON response to parse
     * @throws IllegalArgumentException if jsonString is null or empty
     */
    public YahooFinanceParser(String jsonString) {
        super(jsonString);
        this.symbol = null;
        this.timestampList = new ArrayList<>();
        this.volumeList = new ArrayList<>();
        this.openList = new ArrayList<>();
        this.closeList = new ArrayList<>();
        this.lowList = new ArrayList<>();
        this.highList = new ArrayList<>();
        this.interval = null;
        this.range = null;

        logger.debug("YahooFinanceParser initialized");
    }

    @Override
    protected void finalizeParsingResult() {
        logger.info("Initializing trading period from parsed data");

        // Validate that we have consistent data
        validateParsedData();

        logger.debug("Creating trading period with {} candlesticks", timestampList.size());
        tradingPeriod = createTradingPeriod();

        logger.info("Successfully created trading period for symbol '{}' with {} candlesticks, interval: {}, range: {}",
                symbol, tradingPeriod.candlesticks().size(), interval, range);
    }

    /**
     * Validates that the parsed data is consistent and complete.
     *
     * @throws JsonParseException if data validation fails
     */
    private void validateParsedData() {
        int expectedSize = timestampList.size();

        if (expectedSize == 0) {
            logger.error("No timestamp data found in JSON");
            throw new JsonParseException("No candlestick data found in Yahoo Finance response");
        }

        // Check that all lists have the same size
        if (openList.size() != expectedSize || closeList.size() != expectedSize ||
                lowList.size() != expectedSize || highList.size() != expectedSize ||
                volumeList.size() != expectedSize) {

            logger.error("Inconsistent data sizes - timestamps: {}, open: {}, close: {}, low: {}, high: {}, volume: {}",
                    timestampList.size(), openList.size(), closeList.size(),
                    lowList.size(), highList.size(), volumeList.size());
            throw new JsonParseException("Inconsistent data arrays in Yahoo Finance response");
        }

        logger.debug("Data validation passed - all arrays have {} elements", expectedSize);
    }

    /**
     * Creates a TradingPeriod from the parsed data lists.
     *
     * @return the created TradingPeriod
     * @throws JsonParseException if candlestick creation fails
     */
    private TradingPeriod createTradingPeriod() {
        try {
            List<Candlestick> candlestickList = new ArrayList<>();

            IntStream.range(0, openList.size()).forEach(i -> {
                try {
                    Candlestick candlestick = new Candlestick(
                            openList.get(i), highList.get(i), lowList.get(i),
                            closeList.get(i), volumeList.get(i), timestampList.get(i));
                    candlestickList.add(candlestick);

                    logger.trace("Created candlestick {} with timestamp {}", i, timestampList.get(i));
                } catch (Exception e) {
                    logger.error("Failed to create candlestick at index {}: {}", i, e.getMessage(), e);
                    throw new JsonParseException("Failed to create candlestick at index " + i, e);
                }
            });

            return new TradingPeriod(candlestickList, range, interval);

        } catch (Exception e) {
            logger.error("Failed to create trading period: {}", e.getMessage(), e);
            throw new JsonParseException("Failed to create trading period from parsed data", e);
        }
    }

    @Override
    protected void handleNumberToken() throws IOException {
        try {
            switch (currentKey) {
                case TIMESTAMP -> {
                    long timestamp = jsonReader.nextLong();
                    timestampList.add(timestamp);
                    logger.trace("Added timestamp: {}", timestamp);
                }
                case OPEN -> {
                    double open = processPrice(jsonReader.nextDouble());
                    openList.add(open);
                    logger.trace("Added open price: {}", open);
                }
                case CLOSE -> {
                    double close = processPrice(jsonReader.nextDouble());
                    closeList.add(close);
                    logger.trace("Added close price: {}", close);
                }
                case LOW -> {
                    double low = processPrice(jsonReader.nextDouble());
                    lowList.add(low);
                    logger.trace("Added low price: {}", low);
                }
                case HIGH -> {
                    double high = processPrice(jsonReader.nextDouble());
                    highList.add(high);
                    logger.trace("Added high price: {}", high);
                }
                case VOLUME -> {
                    long volume = jsonReader.nextLong();
                    volumeList.add(volume);
                    logger.trace("Added volume: {}", volume);
                }
                default -> {
                    String value = jsonReader.nextString();
                    logger.debug("Unhandled number token at key '{}' with value: {}", currentKey, value);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading number token at key '{}': {}", currentKey, e.getMessage(), e);
            throw new IOException("Failed to read number token at key: " + currentKey, e);
        } catch (Exception e) {
            logger.error("Error processing number token at key '{}': {}", currentKey, e.getMessage(), e);
            throw new JsonParseException("Failed to process number token at key: " + currentKey, e);
        }
    }

    /**
     * Processes a price value, applying decimal formatting if needed.
     *
     * @param price the raw price value
     * @return the processed price value
     */
    private double processPrice(double price) {
        try {
            // Apply decimal formatting to handle locale-specific decimal separators
            return Double.parseDouble(decimalFormat.format(price));
        } catch (NumberFormatException e) {
            logger.warn("Failed to format price {}, using original value: {}", price, e.getMessage());
            return price;
        }
    }

    @Override
    protected void handleStringToken() throws IOException {
        try {
            final String currentString = jsonReader.nextString();
            logger.trace("Processing string token: '{}' at key: '{}'", currentString, currentKey);

            switch (currentKey) {
                case DATA_GRANULARITY -> {
                    interval = currentString;
                    logger.debug("Set interval to: {}", interval);
                }
                case RANGE -> {
                    range = currentString;
                    logger.debug("Set range to: {}", range);
                }
                case SYMBOL -> {
                    symbol = currentString;
                    logger.debug("Set symbol to: {}", symbol);
                }
                default -> {
                    logger.trace("Unhandled string token at key '{}': {}", currentKey, currentString);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading string token at key '{}': {}", currentKey, e.getMessage(), e);
            throw new IOException("Failed to read string token at key: " + currentKey, e);
        }
    }

    @Override
    protected void handleBooleanToken() throws IOException {
        try {
            final boolean currentBoolean = jsonReader.nextBoolean();
            logger.trace("Processing boolean token: {} at key: {}", currentBoolean, currentKey);
        } catch (IOException e) {
            logger.error("Error reading boolean token at key '{}': {}", currentKey, e.getMessage(), e);
            throw new IOException("Failed to read boolean token at key: " + currentKey, e);
        }
    }

    @Override
    protected void handleNameToken() throws IOException {
        try {
            final String currentName = jsonReader.nextName();
            updateJsonKeyHistory(currentName);
            logger.trace("Processing name token: {}", currentName);

            switch (currentName) {
                case INDICATORS -> {
                    logger.debug("Entering indicators section");
                }
                case SYMBOL -> {
                    logger.debug("Found symbol field");
                    // Don't set symbol here - it gets set in handleStringToken
                }
                default -> {
                    logger.trace("Processing field: {}", currentName);
                }
            }
        } catch (IOException e) {
            logger.error("Error reading name token: {}", e.getMessage(), e);
            throw new IOException("Failed to read name token", e);
        }
    }

    @Override
    protected void handleNullToken() throws IOException {
        try {
            jsonReader.nextNull();
            logger.trace("Processing null token at key: {}", currentKey);
        } catch (IOException e) {
            logger.error("Error reading null token at key '{}': {}", currentKey, e.getMessage(), e);
            throw new IOException("Failed to read null token at key: " + currentKey, e);
        }
    }

    /**
     * Returns the parsed trading period.
     *
     * @return the trading period, or null if parsing hasn't completed successfully
     */
    public TradingPeriod getTradingPeriod() {
        if (tradingPeriod == null) {
            logger.warn("getTradingPeriod() called before parsing completed");
        }
        return tradingPeriod;
    }

    /**
     * Returns the parsed symbol.
     *
     * @return the symbol, or null if not found in JSON
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * Returns the parsed interval.
     *
     * @return the interval, or null if not found in JSON
     */
    public String getInterval() {
        return interval;
    }

    /**
     * Returns the parsed range.
     *
     * @return the range, or null if not found in JSON
     */
    public String getRange() {
        return range;
    }
}
