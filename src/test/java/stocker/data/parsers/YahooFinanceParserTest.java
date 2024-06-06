package stocker.data.parsers;

import org.junit.jupiter.api.Test;
import stocker.representation.TradingPeriod;

import static org.junit.jupiter.api.Assertions.*;

class YahooFinanceParserTest {
    final static String fetchedOneDayJson = "{\"chart\":{\"result\":[{\"meta\":{\"currency\":\"SEK\","
            + "\"symbol\":\"BOL.ST\",\"exchangeName\":\"STO\",\"fullExchangeName\":\"Stockholm\","
            + "\"instrumentType\":\"EQUITY\",\"firstTradeDate\":1007539200,\"regularMarketTime\":1717601376,"
            + "\"hasPrePostMarketData\":false,\"gmtoffset\":7200,\"timezone\":\"CEST\","
            + "\"exchangeTimezoneName\":\"Europe/Stockholm\",\"regularMarketPrice\":355.0,\"fiftyTwoWeekHigh\":355.0,"
            + "\"fiftyTwoWeekLow\":349.4,\"regularMarketDayHigh\":355.0,\"regularMarketDayLow\":349.4,"
            + "\"regularMarketVolume\":1101043,\"chartPreviousClose\":353.1,\"priceHint\":2,"
            + "\"currentTradingPeriod\":{\"pre\":{\"timezone\":\"CEST\",\"start\":1717570800,\"end\":1717570800,"
            + "\"gmtoffset\":7200},\"regular\":{\"timezone\":\"CEST\",\"start\":1717570800,\"end\":1717601400,"
            + "\"gmtoffset\":7200},\"post\":{\"timezone\":\"CEST\",\"start\":1717601400,\"end\":1717601400,"
            + "\"gmtoffset\":7200}},\"dataGranularity\":\"1d\",\"range\":\"1d\",\"validRanges\":[\"1d\","
            + "\"5d\",\"1mo\",\"3mo\",\"6mo\",\"1y\",\"2y\",\"5y\",\"10y\",\"ytd\",\"max\"]},"
            + "\"timestamp\":[1717570800], \"indicators\":{\"quote\":[{\"volume\":[1122017],"
            + "\"open\":[353.1000061035156],\"low\":[349.3999938964844],\"high\":[355.0],\"close\":[355.0]}],"
            + "\"adjclose\":[{\"adjclose\":[355.0]}]}}],\"error\":null}}";

    @Test
    void shouldCreateOneDayCandleStick() {
        YahooFinanceParser parser = new YahooFinanceParser(fetchedOneDayJson);
        parser.parse();
        TradingPeriod oneDay = parser.getTradingPeriod();
        assertEquals(1, oneDay.getCandlesticks().size());
    }
}