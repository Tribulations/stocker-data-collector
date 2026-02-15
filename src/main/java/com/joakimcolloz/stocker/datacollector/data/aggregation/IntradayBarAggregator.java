package com.joakimcolloz.stocker.datacollector.data.aggregation;

import com.joakimcolloz.stocker.datacollector.model.Candlestick;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IntradayBarAggregator {

    public List<Candlestick> aggregate(List<Candlestick> candlesticks, long bucketSeconds) {
        if (candlesticks == null) {
            throw new IllegalArgumentException("Candlesticks list cannot be null");
        }
        if (candlesticks.isEmpty()) {
            return List.of();
        }
        if (bucketSeconds <= 0) {
            throw new IllegalArgumentException("bucketSeconds must be positive");
        }

        List<Candlestick> sorted = new ArrayList<>(candlesticks);
        sorted.sort(Comparator.comparingLong(Candlestick::timestamp));

        Map<Long, List<Candlestick>> buckets = new LinkedHashMap<>();
        for (Candlestick candle : sorted) {
            long bucketStart = (candle.timestamp() / bucketSeconds) * bucketSeconds;
            buckets.computeIfAbsent(bucketStart, k -> new ArrayList<>()).add(candle);
        }

        List<Candlestick> result = new ArrayList<>(buckets.size());
        for (var entry : buckets.entrySet()) {
            long bucketStart = entry.getKey();
            List<Candlestick> bucket = entry.getValue();
            if (bucket.isEmpty()) {
                continue;
            }

            Candlestick first = bucket.get(0);
            Candlestick last = bucket.get(bucket.size() - 1);

            double open = first.open();
            double close = last.close();

            double high = bucket.stream().mapToDouble(Candlestick::high).max().orElse(first.high());
            double low = bucket.stream().mapToDouble(Candlestick::low).min().orElse(first.low());
            long volume = bucket.stream().mapToLong(Candlestick::volume).sum();

            result.add(new Candlestick(open, high, low, close, volume, bucketStart));
        }

        result.sort(Comparator.comparingLong(Candlestick::timestamp));
        return result;
    }
}
