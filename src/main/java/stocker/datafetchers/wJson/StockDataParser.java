package stocker.datafetchers.wJson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import stocker.stock.Candlestick;
import stocker.stock.Stock;
import stocker.stock.TradingPeriod;
import stocker.support.StockAppLogger;
import stocker.support.Utils;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;


/**
 * TODO add doc.
 */
public final class StockDataParser {
    private int beginObjectCounter = 0; // todo remove debug
    private int endObjectCounter = 0; // todo remove debug???

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private String symbol = null;
    private String currentKey = null;
    private String previousKey = null;
    private List<Long> timestampList;
    private List<Long> volumeList;
    private List<Double> openList;
    private List<Double> closeList;
    private List<Double> lowList;
    private List<Double> highList;

    public final static StockDataParser INSTANCE = new StockDataParser();

    // todo is an empty constructor needed when only the default constructor is used?
    private StockDataParser() {

    }

    /**
     * take care of updating the keys used to keep track of the current objects that are being parsed
     *
     * @param key the current key name of the current object that is being traversed
     */
    private void setKeys(String key) {
        this.previousKey = currentKey;
        this.currentKey = key;
    }

    /**
     * just throws exception and that's bad. TODO
     * @param fileName
     * @return
     * @throws Exception
     */
    private String readFileAsString(String fileName) throws Exception {
        String data = "";
        data = new String(Files.readAllBytes(Paths.get(fileName)));
        return data;
    }

    /**
     * TODO add doc.
     * method used for debugging
     * @throws Exception
     */
//    public void parseStockData() throws Exception {
//        final String jsonString = readFileAsString("src/main/resourcs/simple.json");
//        StockAppLogger.INSTANCE.logDebug(jsonString);
//
//        // begin parsing
//        handleObject(new JsonReader(new StringReader(jsonString)));
//
//        StockAppLogger.INSTANCE.logDebug(String.format("BEGIN_OBJECT CoOUNTER: %s", beginObjectCounter));
////        System.out.println(String.format("BEGIN_OBJECT COUNTER: %s", beginObjectCounter));
//        System.out.println(String.format("END_OBJECT COUNTER: %s", endObjectCounter));
//    }

    /**
     * TODO add doc.
     * @throws Exception
     */
    public Stock parseStockData(final String jsonString) throws Exception {
        timestampList = new ArrayList<>();
        volumeList = new ArrayList<>();
        openList = new ArrayList<>();
        closeList = new ArrayList<>();
        lowList = new ArrayList<>();
        highList = new ArrayList<>();

        StockAppLogger.INSTANCE.logDebug(jsonString);
        // begin parsing
        handleObject(new JsonReader(new StringReader(jsonString)));

        StockAppLogger.INSTANCE.logDebug(String.format("BEGIN_OBJECT COUNTER: %s", beginObjectCounter));
        StockAppLogger.INSTANCE.logDebug(String.format("END_OBJECT COUNTER: %s", endObjectCounter));

//        timestampList.forEach(t ->StockAppLogger.INSTANCE.logDebug(Candlestick.asHumanReadebleDate(t))); todo


        // info log
        StockAppLogger.INSTANCE.logInfo(String.format("Number of timestamps: %s - %s::%s",
                timestampList.size(), getClass().getCanonicalName(), Utils.getMethodName()));
        StockAppLogger.INSTANCE.logInfo(String.format("Number of volumes: %s - %s::%s",
                volumeList.size(), getClass().getCanonicalName(), Utils.getMethodName()));
        StockAppLogger.INSTANCE.logInfo(String.format("Number of close: %s - %s::%s",
                closeList.size(), getClass().getCanonicalName(), Utils.getMethodName()));

        return createStock();
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
                    highList.get(i), volumeList.get(i), timestampList.get(i)));
        });

        return new TradingPeriod(candlestickList);
    }

    /**
     * TODO add doc.
     * @param jsonReader
     * @throws IOException
     */
    private void handleObject(JsonReader jsonReader) throws IOException {
        // improve this ? TODO why do we have eto check if it is an END_ARRAY token? shouldn't be here!!
        if (jsonReader.peek().equals(JsonToken.END_ARRAY)) {
            StockAppLogger.INSTANCE.logDebug("END_ARRAY");
            jsonReader.endArray();
        } else if (jsonReader.peek().equals(JsonToken.BEGIN_OBJECT)) {
            StockAppLogger.INSTANCE.logDebug("BEGIN_OBJECT");
            beginObjectCounter++;
            jsonReader.beginObject();
        }

        // iterate over the object?
        while (jsonReader.hasNext() || !jsonReader.peek().equals(JsonToken.END_DOCUMENT)) {
            JsonToken token = jsonReader.peek();
            switch (token) {
                case BEGIN_ARRAY -> {
                    handleArray(jsonReader);
                }
                case END_OBJECT -> {
                    endObjectCounter++;
                    StockAppLogger.INSTANCE.logDebug("END_OBJECT");
                    jsonReader.endObject();
                }
                default -> handleNonArrayToken(jsonReader, token);
            }
        }
    }

    /**
     * TODO  add doc.
     * @param jsonReader
     * @throws IOException
     */
    private void handleArray(JsonReader jsonReader) throws IOException {
        StockAppLogger.INSTANCE.logDebug("BEGIN_ARRAY");

        jsonReader.beginArray();
        while (true) {
            JsonToken token = jsonReader.peek();
            switch (token) {
                case END_ARRAY -> {
                    jsonReader.endArray();
                    StockAppLogger.INSTANCE.logDebug("END_ARRAY");
                    return;
                }
                case BEGIN_OBJECT -> {
                    handleObject(jsonReader);
                }
                case END_OBJECT -> {
                    endObjectCounter++; // TodO
                    StockAppLogger.INSTANCE.logDebug("END_OBJECT");
                    jsonReader.endObject();
                    return;
                }
                case END_DOCUMENT -> {
                    return;
                }
                default -> handleNonArrayToken(jsonReader, token);
            }
        }
    }

    /**
     * todo add doc.
     * @param jsonReader
     * @param token
     * @throws IOException
     */
    private void handleNonArrayToken(JsonReader jsonReader, JsonToken token) throws IOException {
        switch (token) {
            case NAME -> {
                String name = jsonReader.nextName();
//                this.currentKey = name; // keep track of current key to be able to parse correctly?
                setKeys(name); // keep track of current key to be able to parse correctly?
                StockAppLogger.INSTANCE.logDebug(name);

                switch (name) {
                    case Constants.INDICATORS -> {
                        StockAppLogger.INSTANCE.logInfo(
                                "Inside indicators in switch statement - "
                                        + getClass().getCanonicalName() + "::"
                                        + Utils.getMethodName());
                    }
                    case Constants.SYMBOL -> this.symbol = name;
                }
            }
            case STRING -> {
                StockAppLogger.INSTANCE.logDebug(jsonReader.nextString());
            }
            case NUMBER -> {
                switch (currentKey) { // which is the current json object todo improve
                    case Constants.TIMESTAMP -> timestampList.add(jsonReader.nextLong());
                    case Constants.OPEN -> openList.add(
                            Double.valueOf(decimalFormat.format(jsonReader.nextDouble())));
                    case Constants.CLOSE -> closeList.add(
                            Double.valueOf(decimalFormat.format(jsonReader.nextDouble())));
                    case Constants.LOW -> lowList.add(
                            Double.valueOf(decimalFormat.format(jsonReader.nextDouble())));
                    case Constants.HIGH -> highList.add(
                            Double.valueOf(decimalFormat.format(jsonReader.nextDouble())));
                    case Constants.VOLUME -> volumeList.add(jsonReader.nextLong());
                    default -> StockAppLogger.INSTANCE.logDebug(jsonReader.nextString());
                }
            }
            case NULL -> {
                jsonReader.nextNull();
                StockAppLogger.INSTANCE.logDebug("null");
            }
            default -> handleObject(jsonReader);
        }
    }
}

