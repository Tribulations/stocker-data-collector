package stocker.data.parsers;

import com.google.gson.JsonParseException;
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
 * @author Joakim Colloz
 * @version 1.0
 * @since 1.1
 */
public abstract class BaseParser implements AutoCloseable {
    private static final Logger logger = LoggerFactory.getLogger(BaseParser.class);
    protected String currentKey = null;
    protected String previousKey = null;
    protected final JsonReader jsonReader;
    protected JsonToken jsonToken;

    /**
     * Constructor that initializes the JsonReader with the provided JSON string.
     *
     * @param jsonString the JSON string to be parsed
     * @throws IllegalArgumentException if jsonString is null or empty
     */
    protected BaseParser(final String jsonString) {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON string cannot be null or empty");
        }

        logger.debug("Initializing BaseParser with JSON string of {} characters", jsonString.length());
        this.jsonReader = new JsonReader(new StringReader(jsonString));
        logger.debug("JsonReader initialized successfully");
    }

    /**
     * Updates the keys used to keep track of the current and previous objects being parsed.
     *
     * @param key the current key name of the current object being traversed
     */
    protected void updateJsonKeyHistory(String key) {
        this.previousKey = currentKey;
        this.currentKey = key;
        logger.trace("Updated keys: previous='{}', current='{}'", previousKey, currentKey);
    }

    /**
     * Parses the JSON string.
     * This method initiates the parsing process by calling the {@link #traverserJsonObject()} method
     * and then invokes {@link #finalizeParsingResult()} which should be implemented by subclasses.
     *
     * @throws JsonParseException if parsing fails due to malformed JSON or I/O errors
     */
    public void parse() throws JsonParseException {
        logger.info("Starting JSON parsing");
        try {
            traverserJsonObject();
            finalizeParsingResult();
            logger.info("JSON parsing completed successfully");
        } catch (Exception e) {
            logger.error("Failed to parse JSON: {}", e.getMessage(), e);
            throw new JsonParseException("JSON parsing failed", e);
        }
    }

    /**
     * Closes the JsonReader resource.
     */
    @Override
    public void close() {
        try {
            if (jsonReader != null) {
                jsonReader.close();
                logger.debug("JsonReader closed successfully");
            }
        } catch (IOException e) {
            logger.warn("Error closing JsonReader: {}", e.getMessage(), e);
        }
    }

    /**
     * Finalizes the parsing result by creating the target object from collected data.
     * This method is called after JSON traversal is complete.
     */
    protected abstract void finalizeParsingResult();

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
     * including the management of key names using the {@link #updateJsonKeyHistory(String)} method.
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
     * This method processes JSON objects and delegates to {@link #traverseJsonArray()} for arrays.
     *
     * @throws IOException if an I/O error occurs during JSON processing
     */
    private void traverserJsonObject() throws IOException {
        logger.debug("Starting to handle JSON object");

        try {
            if (jsonReader.peek().equals(JsonToken.END_ARRAY)) {
                jsonReader.endArray();
                logger.trace("Ended array");
            } else if (jsonReader.peek().equals(JsonToken.BEGIN_OBJECT)) {
                jsonReader.beginObject();
                logger.trace("Started object");
            }

            // Iterate over the object
            while (jsonReader.hasNext() || !jsonReader.peek().equals(JsonToken.END_DOCUMENT)) {
                jsonToken = jsonReader.peek();
                logger.trace("Processing token: {}", jsonToken);

                try {
                    switch (jsonToken) {
                        case BEGIN_ARRAY -> {
                            logger.debug("Handling array at key: {}", currentKey);
                            traverseJsonArray();
                        }
                        case END_OBJECT -> {
                            jsonReader.endObject();
                            logger.trace("Ended object");
                        }
                        default -> processValueToken();
                    }
                } catch (IOException e) {
                    logger.error("Error processing token {} at key '{}': {}", jsonToken, currentKey, e.getMessage(), e);
                    throw new IOException("Failed to process JSON token: " + jsonToken + " at key: " + currentKey, e);
                }
            }

            logger.debug("Completed handling JSON object");

        } catch (IOException e) {
            logger.error("Error handling JSON object structure: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Handles JSON arrays by iterating over their elements.
     * This method processes nested arrays and delegates to {@link #traverserJsonObject()} for objects within arrays.
     *
     * @throws IOException if an I/O error occurs
     */
    private void traverseJsonArray() throws IOException {
        logger.debug("Starting to handle JSON array at key: {}", currentKey);

        try {
            jsonReader.beginArray();
            int elementCount = 0;

            while (true) {
                jsonToken = jsonReader.peek();
                logger.trace("Processing array element {}, token: {}", elementCount, jsonToken);

                try {
                    switch (jsonToken) {
                        case END_ARRAY -> {
                            jsonReader.endArray();
                            logger.debug("Completed array with {} elements at key: {}", elementCount, currentKey);
                            return;
                        }
                        case BEGIN_OBJECT -> {
                            traverserJsonObject();
                            elementCount++;
                        }
                        case END_OBJECT -> {
                            jsonReader.endObject();
                            logger.trace("Ended object in array");
                            return;
                        }
                        case END_DOCUMENT -> {
                            logger.debug("Reached end of document while processing array");
                            return;
                        }
                        default -> {
                            processValueToken();
                            elementCount++;
                        }
                    }
                } catch (IOException e) {
                    logger.error("Error processing array element {} at key '{}': {}", elementCount, currentKey, e.getMessage(), e);
                    throw new IOException("Failed to process array element " + elementCount + " at key: " + currentKey, e);
                }
            }
        } catch (IOException e) {
            logger.error("Error handling JSON array at key '{}': {}", currentKey, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Handles non-array JSON tokens such as strings, numbers, booleans, and nulls.
     *
     * <p>The NAME token is used to manage key names in JSON objects, and the handling of this token
     * involves updating the current and previous key names using the {@link #updateJsonKeyHistory(String)} method.
     *
     * @throws IOException if an I/O error occurs
     */
    private void processValueToken() throws IOException {
        logger.trace("Handling non-array token: {} at key: {}", jsonToken, currentKey);

        try {
            switch (jsonToken) {
                case NAME -> {
                    logger.trace("Processing NAME token");
                    handleNameToken();
                }
                case STRING -> {
                    logger.trace("Processing STRING token at key: {}", currentKey);
                    handleStringToken();
                }
                case NUMBER -> {
                    logger.trace("Processing NUMBER token at key: {}", currentKey);
                    handleNumberToken();
                }
                case NULL -> {
                    logger.trace("Processing NULL token at key: {}", currentKey);
                    handleNullToken();
                }
                case BOOLEAN -> {
                    logger.trace("Processing BOOLEAN token at key: {}", currentKey);
                    handleBooleanToken();
                }
                default -> {
                    logger.debug("Delegating token {} to handleObject", jsonToken);
                    traverserJsonObject();
                }
            }
        } catch (IOException e) {
            logger.error("Error handling token {} at key '{}': {}", jsonToken, currentKey, e.getMessage(), e);
            throw new IOException("Failed to handle " + jsonToken + " token at key: " + currentKey, e);
        }
    }
}
