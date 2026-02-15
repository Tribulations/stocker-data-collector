package com.joakimcolloz.stocker.datacollector.data.aggregation;

import com.joakimcolloz.stocker.datacollector.data.parsers.YahooFinanceParser;
import com.joakimcolloz.stocker.datacollector.model.Candlestick;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class IntradayBarAggregatorTest {

    @Test
    void shouldAggregateInto15MinBuckets() {
        IntradayBarAggregator aggregator = new IntradayBarAggregator();

        long t0 = 1000L; // bucket 990 for 15m (900s) => 900
        List<Candlestick> input = List.of(
                new Candlestick(1.0, 2.0, 0.5, 1.5, 10L, t0),
                new Candlestick(1.5, 2.5, 1.0, 2.0, 20L, t0 + 300),
                new Candlestick(2.0, 3.0, 1.5, 2.5, 30L, t0 + 600),
                new Candlestick(5.0, 6.0, 4.0, 5.5, 1L, t0 + 1200)
        );

        List<Candlestick> aggregated = aggregator.aggregate(input, 900);

        assertEquals(2, aggregated.size());

        Candlestick first = aggregated.get(0);
        assertEquals(900L, first.timestamp());
        assertEquals(1.0, first.open(), 0.0001);
        assertEquals(2.5, first.close(), 0.0001);
        assertEquals(3.0, first.high(), 0.0001);
        assertEquals(0.5, first.low(), 0.0001);
        assertEquals(60L, first.volume());

        Candlestick second = aggregated.get(1);
        assertEquals(1800L, second.timestamp());
        assertEquals(5.0, second.open(), 0.0001);
        assertEquals(5.5, second.close(), 0.0001);
        assertEquals(6.0, second.high(), 0.0001);
        assertEquals(4.0, second.low(), 0.0001);
        assertEquals(1L, second.volume());
    }

    @Test
    void shouldAggregateInto30MinBuckets() {
        IntradayBarAggregator aggregator = new IntradayBarAggregator();

        long start = 3600L; // aligns exactly with 30m buckets (1800s)
        List<Candlestick> input = List.of(
                new Candlestick(10.0, 11.0, 9.5, 10.5, 1L, start),
                new Candlestick(10.5, 13.0, 10.0, 12.5, 2L, start + 300),
                new Candlestick(12.5, 14.0, 12.0, 13.5, 3L, start + 1500),
                new Candlestick(20.0, 21.0, 19.0, 20.5, 4L, start + 1800)
        );

        List<Candlestick> aggregated = aggregator.aggregate30Minutes(input);

        assertEquals(2, aggregated.size());

        Candlestick first = aggregated.get(0);
        assertEquals(3600L, first.timestamp());
        assertEquals(10.0, first.open(), 0.0001);
        assertEquals(13.5, first.close(), 0.0001);
        assertEquals(14.0, first.high(), 0.0001);
        assertEquals(9.5, first.low(), 0.0001);
        assertEquals(6L, first.volume());

        Candlestick second = aggregated.get(1);
        assertEquals(5400L, second.timestamp());
        assertEquals(20.0, second.open(), 0.0001);
        assertEquals(20.5, second.close(), 0.0001);
        assertEquals(21.0, second.high(), 0.0001);
        assertEquals(19.0, second.low(), 0.0001);
        assertEquals(4L, second.volume());
    }

    @Test
    void shouldAggregateInto1HourBuckets() {
        IntradayBarAggregator aggregator = new IntradayBarAggregator();

        long start = 7200L; // aligns with 1h buckets (3600s)
        List<Candlestick> input = List.of(
                new Candlestick(1.0, 1.2, 0.9, 1.1, 10L, start),
                new Candlestick(1.1, 1.5, 1.0, 1.4, 20L, start + 300),
                new Candlestick(1.4, 1.6, 1.3, 1.5, 30L, start + 3300),
                new Candlestick(2.0, 2.1, 1.9, 2.05, 5L, start + 3600)
        );

        List<Candlestick> aggregated = aggregator.aggregate1Hour(input);

        assertEquals(2, aggregated.size());

        Candlestick first = aggregated.get(0);
        assertEquals(7200L, first.timestamp());
        assertEquals(1.0, first.open(), 0.0001);
        assertEquals(1.5, first.close(), 0.0001);
        assertEquals(1.6, first.high(), 0.0001);
        assertEquals(0.9, first.low(), 0.0001);
        assertEquals(60L, first.volume());

        Candlestick second = aggregated.get(1);
        assertEquals(10800L, second.timestamp());
        assertEquals(2.0, second.open(), 0.0001);
        assertEquals(2.05, second.close(), 0.0001);
        assertEquals(2.1, second.high(), 0.0001);
        assertEquals(1.9, second.low(), 0.0001);
        assertEquals(5L, second.volume());
    }

    @Test
    void shouldMatchDailyFixtureWhenAggregatingSynthetic5mTo1d() {
        String json = loadTestJSON("YahooFinance-BOL.ST-1d-1d.json");

        Candlestick daily;
        try (YahooFinanceParser parser = new YahooFinanceParser()) {
            parser.setJsonString(json);
            parser.parse();
            daily = parser.getTradingPeriod().candlesticks().get(0);
        }

        long dayStart = (daily.timestamp() / 86400L) * 86400L;
        long v1 = daily.volume() / 3;
        long v2 = daily.volume() / 3;
        long v3 = daily.volume() - v1 - v2;

        List<Candlestick> synthetic5m = List.of(
                new Candlestick(daily.open(), daily.open(), daily.open(), daily.open(), v1, dayStart),
                new Candlestick(daily.open(), daily.high(), daily.low(), daily.open(), v2, dayStart + 300),
                new Candlestick(daily.open(), daily.open(), daily.open(), daily.close(), v3, dayStart + 600)
        );

        IntradayBarAggregator aggregator = new IntradayBarAggregator();
        List<Candlestick> aggregated = aggregator.aggregate(synthetic5m, 86400L);

        assertEquals(1, aggregated.size());
        Candlestick result = aggregated.get(0);

        assertEquals(daily.open(), result.open(), 0.0001);
        assertEquals(daily.close(), result.close(), 0.0001);
        assertEquals(daily.high(), result.high(), 0.0001);
        assertEquals(daily.low(), result.low(), 0.0001);
        assertEquals(daily.volume(), result.volume());
    }

    private String loadTestJSON(final String jsonFileName) {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(jsonFileName)) {
            assertNotNull(is, "Test resource " + jsonFileName + " not found in classpath");
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load JSON test resource " + jsonFileName + " from classpath", e);
        }
    }
}
