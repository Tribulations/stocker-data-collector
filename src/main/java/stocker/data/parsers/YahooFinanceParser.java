package stocker.data.parsers;

import stocker.model.Candlestick;
import stocker.model.Stock;
import stocker.model.TradingPeriod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static stocker.data.fetchers.JsonConstants.TIMESTAMP;
import static stocker.data.fetchers.JsonConstants.OPEN;
import static stocker.data.fetchers.JsonConstants.CLOSE;
import static stocker.data.fetchers.JsonConstants.LOW;
import static stocker.data.fetchers.JsonConstants.HIGH;
import static stocker.data.fetchers.JsonConstants.VOLUME;
import static stocker.data.fetchers.JsonConstants.DATA_GRANULARITY;
import static stocker.data.fetchers.JsonConstants.SYMBOL;
import static stocker.data.fetchers.JsonConstants.INDICATORS;
import static stocker.data.fetchers.JsonConstants.RANGE;

public class YahooFinanceParser extends BaseParser {// TODO divide this class? It does two things now? SAngle responsibility!! Now it parses and creates a TradingPeriod?
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

    public YahooFinanceParser(String jsonString) {
        super(jsonString);
        this.symbol = "null";
        this.timestampList = new ArrayList<>();
        this.volumeList = new ArrayList<>();
        this.openList = new ArrayList<>();
        this.closeList = new ArrayList<>();
        this.lowList = new ArrayList<>();
        this.highList = new ArrayList<>();
        this.interval = "null"; // TODO use null instead of "null"
        this.range = "null";
    }

    @Override
    protected void initParsedObject() {
        logger.info("Initializing parsed object - Number of timestamps: {}", timestampList.size());
        logger.info("Initializing parsed object - Number of volumes: {}", volumeList.size());
        logger.info("Initializing parsed object - Number of close prices: {}", closeList.size());

        tradingPeriod = createTradingPeriod();
    }

    private Stock createStock() {
        return new Stock(symbol, createTradingPeriod());
    }

    /**
     * takes the lists of close, open, timestamp etc. and creates a trading period of candlesticks.
     */
    private TradingPeriod createTradingPeriod() {
        List<Candlestick> candlestickList = new ArrayList<>();
        IntStream.range(0, openList.size()).forEach(i -> {
            candlestickList.add(new Candlestick(openList.get(i),closeList.get(i), lowList.get(i),
                    highList.get(i), volumeList.get(i), timestampList.get(i), interval));
        });

        return new TradingPeriod(candlestickList, range, interval);
    }

    /** TODO
     * @bug only windows? Sometimes a double in this method is intepreted/read as having a comma instead of a period, e.g. 156,6 instead of 156.6.
     */
    @Override
    protected void handleNumberToken() throws IOException {
        switch (currentKey) { // which is the current json object todo improve
            case TIMESTAMP -> timestampList.add(jsonReader.nextLong());
            case OPEN -> openList.add(
                    Double.valueOf(decimalFormat.format(jsonReader.nextDouble()))); // TODO why is decimalFormat necessary here? or is it?
            case CLOSE -> closeList.add(
                    Double.valueOf(decimalFormat.format(jsonReader.nextDouble())));
            case LOW -> lowList.add(
                    Double.valueOf(decimalFormat.format(jsonReader.nextDouble())));
            case HIGH -> highList.add(
                    Double.valueOf(decimalFormat.format(jsonReader.nextDouble())));
            case VOLUME -> volumeList.add(jsonReader.nextLong());
            default -> logger.debug(
                    "default case in YahooFinanceParser::handleNumberToken() Key: {} Value: {}", currentKey, jsonReader.nextString());
        }
    }

    @Override
    protected void handleStringToken() throws IOException {
        final String currentString = jsonReader.nextString();
        logger.debug(currentString);

        if (DATA_GRANULARITY.equals(currentKey)) {
            interval = currentString;
        } else if (RANGE.equals(currentKey)) {
            range = currentKey;
        }
    }

    @Override
    protected void handleBooleanToken() throws IOException {
        final Boolean currentBoolean = jsonReader.nextBoolean();
        logger.debug(currentBoolean.toString());
    }

    @Override
    protected void handleNameToken() throws IOException {
        final String currentName = jsonReader.nextName();
        updateCurrentAndPreviousKeys(currentName);
        logger.debug(currentName);

        switch (currentName) {
            case INDICATORS -> {
                logger.info("Processing indicators section in JSON");
            }
            case SYMBOL -> this.symbol = currentName;
        }
    }

    @Override
    protected void handleNullToken() throws IOException {
        jsonReader.nextNull();
        logger.debug("null");
    }

    public TradingPeriod getTradingPeriod() {
        return tradingPeriod;
    }
}
