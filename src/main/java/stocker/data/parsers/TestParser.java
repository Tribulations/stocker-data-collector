package stocker.data.parsers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TestParser extends BaseParser {
    private static final Logger logger = LoggerFactory.getLogger(TestParser.class);

    public TestParser(final String jsonString) {
        super(jsonString);
    }

    @Override
    protected void initParsedObject() {

    }

    @Override
    protected void handleNullToken() throws IOException {
        jsonReader.nextNull();
        logger.debug("null");
    }

    @Override
    protected void handleNumberToken() throws IOException {
        final String currentNumber = jsonReader.nextString(); // change to correct number data type in subclass
        logger.debug(currentNumber);
    }

    @Override
    protected void handleStringToken() throws IOException {
        final String currentString = jsonReader.nextString();
        logger.debug(currentString);
    }

    @Override
    protected void handleBooleanToken() throws IOException {
        final String currentBoolean = jsonReader.nextString();
        logger.debug(currentBoolean);
    }

    @Override
    protected void handleNameToken() throws IOException {
        final String currentName = jsonReader.nextName();
        updateCurrentAndPreviousKeys(currentName);
        logger.debug(currentName);
    }
}
