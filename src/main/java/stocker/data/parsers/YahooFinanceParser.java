package stocker.data.parsers;

import stocker.representation.Candlestick;
import stocker.representation.Stock;
import stocker.representation.TradingPeriod;
import stocker.support.StockAppLogger;
import stocker.support.Utils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static stocker.data.fetchers.wJson.JsonConstants.TIMESTAMP;
import static stocker.data.fetchers.wJson.JsonConstants.OPEN;
import static stocker.data.fetchers.wJson.JsonConstants.CLOSE;
import static stocker.data.fetchers.wJson.JsonConstants.LOW;
import static stocker.data.fetchers.wJson.JsonConstants.HIGH;
import static stocker.data.fetchers.wJson.JsonConstants.VOLUME;
import static stocker.data.fetchers.wJson.JsonConstants.DATA_GRANULARITY;
import static stocker.data.fetchers.wJson.JsonConstants.SYMBOL;
import static stocker.data.fetchers.wJson.JsonConstants.INDICATORS;
import static stocker.data.fetchers.wJson.JsonConstants.RANGE;

public class YahooFinanceParser extends BaseParser {// TODO divide this class? It does two things now? SAngle responsibility!! Now it parses and creates a TradingPeriod?
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
        StockAppLogger.INSTANCE.logInfo(String.format("Number of timestamps: %s - %s::%s",
                timestampList.size(), getClass().getCanonicalName(), Utils.getMethodName()));
        StockAppLogger.INSTANCE.logInfo(String.format("Number of volumes: %s - %s::%s",
                volumeList.size(), getClass().getCanonicalName(), Utils.getMethodName()));
        StockAppLogger.INSTANCE.logInfo(String.format("Number of close: %s - %s::%s",
                closeList.size(), getClass().getCanonicalName(), Utils.getMethodName()));

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
            default -> StockAppLogger.INSTANCE.logDebug(
                    "default case in YahooFinanceParser::handleNumberToken() " + jsonReader.nextString());
        }
    }

    @Override
    protected void handleStringToken() throws IOException {
        final String currentString = jsonReader.nextString();
        StockAppLogger.INSTANCE.logDebug(currentString);

        if (DATA_GRANULARITY.equals(currentKey)) {
            interval = currentString;
        } else if (RANGE.equals(currentKey)) {
            range = currentKey;
        }
    }

    @Override
    protected void handleBooleanToken() throws IOException {
        final Boolean currentBoolean = jsonReader.nextBoolean();
        StockAppLogger.INSTANCE.logDebug(currentBoolean.toString());
    }

    @Override
    protected void handleNameToken() throws IOException {
        final String currentName = jsonReader.nextName();
        updateCurrentAndPreviousKeys(currentName);
        StockAppLogger.INSTANCE.logDebug(currentName);

        switch (currentName) {
            case INDICATORS -> {
                StockAppLogger.INSTANCE.logInfo(
                        "Inside indicators in switch statement - "
                                + getClass().getCanonicalName() + "::"
                                + Utils.getMethodName());
            }
            case SYMBOL -> this.symbol = currentName;
        }
    }

    @Override
    protected void handleNullToken() throws IOException {
        jsonReader.nextNull();
        StockAppLogger.INSTANCE.logDebug("null");
    }

    public TradingPeriod getTradingPeriod() {
        return tradingPeriod;
    }
}
