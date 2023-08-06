package stocker.data.parsers;

import stocker.support.StockAppLogger;

import java.io.IOException;

public class TestParser extends BaseParser {

    public TestParser(final String jsonString) {
        super(jsonString);
    }

    @Override
    protected void initParsedObject() {

    }

    @Override
    protected void handleNullToken() throws IOException {
        jsonReader.nextNull();
        StockAppLogger.INSTANCE.logDebug("null");
    }

    @Override
    protected void handleNumberToken() throws IOException {
        final String currentNumber = jsonReader.nextString(); // change to correct number data type in subclass
        StockAppLogger.INSTANCE.logDebug(currentNumber);
    }

    @Override
    protected void handleStringToken() throws IOException {
        final String currentString = jsonReader.nextString();
        StockAppLogger.INSTANCE.logDebug(currentString);
    }

    @Override
    protected void handleNameToken() throws IOException {
        final String currentName = jsonReader.nextName();
        setKeys(currentName);
        StockAppLogger.INSTANCE.logDebug(currentName);
    }
}
