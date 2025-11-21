package com.example.consumer.service;

import com.example.avro.OrderEvent;
import com.example.consumer.model.ProductAggregate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderAggregationService {

    private static final Logger log = LoggerFactory.getLogger(OrderAggregationService.class);
    private final ConcurrentHashMap<String, ProductAggregate> aggregates = new ConcurrentHashMap<>();

    public ProductAggregate updateAggregate(OrderEvent event) {
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
        log.debug("Updated aggregate for {} -> {}", product, updated);
        return updated;
    }

    public Map<String, ProductAggregate> snapshot() {
        return Map.copyOf(aggregates);
    }
}
