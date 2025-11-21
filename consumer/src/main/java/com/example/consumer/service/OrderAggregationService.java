package com.example.consumer.service;

import com.example.avro.OrderEvent;
import com.example.consumer.model.AggregateResult;
import com.example.consumer.model.ProductAggregate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderAggregationService {

    private static final Logger log = LoggerFactory.getLogger(OrderAggregationService.class);
    private final ConcurrentHashMap<String, ProductAggregate> aggregates = new ConcurrentHashMap<>();
    private final LongAdder globalCount = new LongAdder();
    private final DoubleAdder globalTotal = new DoubleAdder();

    public AggregateResult updateAggregate(OrderEvent event) {
        String product = event.getProduct().toString();
        double price = event.getPrice();
        ProductAggregate updated = aggregates.compute(product, (key, existing) -> {
            if (existing == null) {
                return new ProductAggregate(1, price, price);
            }
            long newCount = existing.count() + 1;
            double newTotal = existing.total() + price;
            double average = newTotal / newCount;
            return new ProductAggregate(newCount, newTotal, average);
        });
        globalCount.increment();
        globalTotal.add(price);
        long overallCount = globalCount.sum();
        double overallTotal = globalTotal.sum();
        double overallAverage = overallTotal / overallCount;
        ProductAggregate overall = new ProductAggregate(overallCount, overallTotal, overallAverage);
        log.debug("Updated aggregate for {} -> {} | overall {}", product, updated, overall);
        return new AggregateResult(updated, overall);
    }

    public Map<String, ProductAggregate> snapshot() {
        return Map.copyOf(aggregates);
    }
}
