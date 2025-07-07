package stocker.data.parsers;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringReader;

/**
 * Abstract base class for parsing JSON strings.
 * This class provides basic functionalities for traversing and handling JSON data.
 * Subclasses should implement the abstract methods to handle specific JSON token types.
 *
 *  * @author Tribulations / Joakim Colloz
 * @version 1.0
 * @since 1.0
 */
public abstract class BaseParser {
    private static final Logger logger = LoggerFactory.getLogger(BaseParser.class);
    protected String currentKey = null;
    protected String previousKey = null;
    protected final JsonReader jsonReader;
    protected JsonToken jsonToken;

    /**
     * Constructor that initializes the JsonReader with the provided JSON string.
     *
     * @param jsonString the JSON string to be parsed
     */
    protected BaseParser(final String jsonString) { // TODO have to close the jsonReader somewhere
        logger.debug(jsonString);
        this.jsonReader = new JsonReader(new StringReader(jsonString));
    }

    /**
     * Updates the keys used to keep track of the current and previous objects being parsed.
     *
     * @param key the current key name of the current object being traversed
     */
    protected void updateCurrentAndPreviousKeys(String key) {
        this.previousKey = currentKey;
        this.currentKey = key;
    }

    /**
     * Parses the JSON string.
     * This method initiates the parsing process by calling the {@link #handleObject()} method
     * and then invokes {@link #initParsedObject()} which should be implemented by subclasses.
     *
     * If {@link #initParsedObject()} is not implemented, this method traverses and prints all data in the Json string.
     */
    public void parse() {// TODO where close jsonReader? Maybe instantiate it here and not in the constructor? Use auto-closable
        // begin parsing
        handleObject();
        initParsedObject();
    }

    /**
     * Initializes the parsed object.
     * Subclasses should implement this method to define the initialization process.
     */
    protected abstract void initParsedObject();

    /**
     * Handles JSON number tokens.
     * Subclasses should implement this method to define the handling process.
     *
     * @throws IOException if an I/O error occurs
     */
    protected abstract void handleNumberToken() throws IOException;

    /**
     * Handles JSON string tokens.
     * Subclasses should implement this method to define the handling process.
     *
     * @throws IOException if an I/O error occurs
     */
    protected abstract void handleStringToken() throws IOException;

    /**
     * Handles JSON name tokens.
     * Subclasses should implement this method to define the handling process,
     * including the management of key names using the {@link #updateCurrentAndPreviousKeys(String)} method.
     *
     * @throws IOException if an I/O error occurs
     */
    protected abstract void handleNameToken() throws IOException;

    /**
     * Handles JSON null tokens.
     * Subclasses should implement this method to define the handling process.
     *
     * @throws IOException if an I/O error occurs
     */
    protected abstract void handleNullToken() throws IOException;

    /**
     * Handles JSON boolean tokens.
     * Subclasses should implement this method to define the handling process.
     *
     * @throws IOException if an I/O error occurs
     */
    protected abstract void handleBooleanToken() throws IOException;

    /**
     * Handles JSON objects by iterating over their keys and values.
     * This method processes JSON objects and delegates to {@link #handleArray()} for arrays.
     */
    private void handleObject() {
        try {
            if (jsonReader.peek().equals(JsonToken.END_ARRAY)) {
                jsonReader.endArray();
            } else if (jsonReader.peek().equals(JsonToken.BEGIN_OBJECT)) {
                jsonReader.beginObject();
            }

            // iterate over the object
            while (jsonReader.hasNext() || !jsonReader.peek().equals(JsonToken.END_DOCUMENT)) {
                jsonToken = jsonReader.peek();
                switch (jsonToken) {
                    case BEGIN_ARRAY -> handleArray();
                    case END_OBJECT -> jsonReader.endObject();
                    default -> handleNonArrayToken();
                }
            }
        } catch (IOException e) {
            logger.error("Error handling JSON object: {}", e.getMessage(), e);
        }
    }

    /**
     * Handles JSON arrays by iterating over their elements.
     * This method processes nested arrays and delegates to `handleObject` for objects within arrays.
     *
     * @throws IOException if an I/O error occurs
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
     * Handles non-array JSON tokens such as strings, numbers, booleans, and nulls.
     *
     * <p>The NAME token is used to manage key names in JSON objects, and the handling of this token
     * involves updating the current and previous key names using the {@link #updateCurrentAndPreviousKeys(String)} method.
     *
     * @throws IOException if an I/O error occurs
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
}

