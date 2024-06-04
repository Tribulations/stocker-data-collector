package stocker.data.parsers;

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
public  abstract class BaseParser {
    protected String currentKey = null;
    protected String previousKey = null;

    protected final JsonReader jsonReader;
    protected JsonToken jsonToken;

    protected BaseParser(final String jsonString) {
        StockAppLogger.INSTANCE.logDebug(jsonString);
        this.jsonReader = new JsonReader(new StringReader(jsonString));
    }

    /**
     * take care of updating the keys used to keep track of the current objects that are being parsed
     *
     * @param key the current key name of the current object that is being traversed
     */
    protected void setKeys(String key) {
        this.previousKey = currentKey;
        this.currentKey = key;
    }

    /**
     * Loops over and prints all data contained in a Json string.
     * This method can be used initially to check a json formatte string and then modifying this method in
     * subclasses to fit the clients needs.
     */
    public void parse() {
        // begin parsing
        handleObject();
        initParsedObject();
    }

    protected abstract void initParsedObject();

    /**
     * TODO add doc.
     */
    private void handleObject() {
        try {
            if (jsonReader.peek().equals(JsonToken.END_ARRAY)) {
                jsonReader.endArray();
            } else if (jsonReader.peek().equals(JsonToken.BEGIN_OBJECT)) {
                jsonReader.beginObject();
            }

            // iterate over the object?
            while (jsonReader.hasNext() || !jsonReader.peek().equals(JsonToken.END_DOCUMENT)) {
                jsonToken = jsonReader.peek();
                switch (jsonToken) {
                    case BEGIN_ARRAY -> handleArray();
                    case END_OBJECT -> jsonReader.endObject();
                    default -> handleNonArrayToken();
                }
            }
        } catch (IOException e) {
            StockAppLogger.INSTANCE.logInfo(e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * TODO  add doc.
     * @throws IOException
     */
    private void handleArray() throws IOException {
        jsonReader.beginArray();
        while (true) {
            jsonToken = jsonReader.peek();
            switch (jsonToken) {
                case END_ARRAY -> {
                    jsonReader.endArray();
                    return;
                }
                case BEGIN_OBJECT -> handleObject();
                case END_OBJECT -> {
                    jsonReader.endObject();
                    return;
                }
                case END_DOCUMENT -> {
                    return;
                }
                default -> handleNonArrayToken();
            }
        }
    }

    /**
     * todo add doc.
     * @throws IOException
     */
    private void handleNonArrayToken() throws IOException {
        switch (jsonToken) {
            case NAME -> handleNameToken();
            case STRING -> handleStringToken();
            case NUMBER -> handleNumberToken();
            case NULL -> handleNullToken();
            case BOOLEAN -> handleBooleanToken();
            default -> handleObject();
        }
    }

    protected abstract void handleNumberToken() throws IOException;

    protected abstract void handleStringToken() throws IOException;

    protected abstract void handleNameToken() throws IOException;

    protected abstract void handleNullToken() throws IOException;

    protected abstract void handleBooleanToken() throws IOException;
}

