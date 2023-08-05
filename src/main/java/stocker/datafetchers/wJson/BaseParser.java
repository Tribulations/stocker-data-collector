package stocker.datafetchers.wJson;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import stocker.support.StockAppLogger;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author tribulations
 * @version 1.0
 * @since 1.0
 */
public class BaseParser {
    private String currentKey = null;
    private String previousKey = null;

    public BaseParser() {

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
     * Loops over and prints all data contained in a Json string.
     * This method can be used initially to check a json formatte string and then modifying this method in
     * subclasses to fit the clients needs.
     * @param jsonString the text string formatted as json
     */
    public void parse(final String jsonString) {
        StockAppLogger.INSTANCE.logDebug(jsonString);
        // begin parsing
        StringReader stringReader = new StringReader(jsonString);
        JsonReader jsonReader = new JsonReader(stringReader);

        handleObject(jsonReader);

    }

    /**
     * TODO add doc.
     * @param jsonReader
     */
    private void handleObject(JsonReader jsonReader) {
        try {
            if (jsonReader.peek().equals(JsonToken.END_ARRAY)) {
                jsonReader.endArray();
            } else if (jsonReader.peek().equals(JsonToken.BEGIN_OBJECT)) {
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
                        jsonReader.endObject();
                    }
                    default -> handleNonArrayToken(jsonReader, token);
                }
            }
        } catch (IOException e) {
            StockAppLogger.INSTANCE.logInfo(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * TODO  add doc.
     * @param jsonReader
     * @throws IOException
     */
    private void handleArray(JsonReader jsonReader) throws IOException {
        jsonReader.beginArray();
        while (true) {
            JsonToken token = jsonReader.peek();
            switch (token) {
                case END_ARRAY -> {
                    jsonReader.endArray();
                    return;
                }
                case BEGIN_OBJECT -> {
                    handleObject(jsonReader);
                }
                case END_OBJECT -> {
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
                final String currentName = jsonReader.nextName();
                setKeys(currentName);
                StockAppLogger.INSTANCE.logDebug(currentName);
            }
            case STRING -> {
                final String currentString = jsonReader.nextString();
                StockAppLogger.INSTANCE.logDebug(currentString);
            }
            case NUMBER -> {
                final String currentNumber = jsonReader.nextString(); // change to correct number data type in subclass
                StockAppLogger.INSTANCE.logDebug(currentNumber);
                }

            case NULL -> {
                jsonReader.nextNull();
                StockAppLogger.INSTANCE.logDebug("null");
            }
            default -> handleObject(jsonReader);
        }
    }
}

