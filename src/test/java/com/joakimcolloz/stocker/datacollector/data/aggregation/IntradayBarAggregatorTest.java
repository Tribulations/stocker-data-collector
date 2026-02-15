package com.joakimcolloz.stocker.datacollector.data.aggregation;

import com.joakimcolloz.stocker.datacollector.model.Candlestick;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}
