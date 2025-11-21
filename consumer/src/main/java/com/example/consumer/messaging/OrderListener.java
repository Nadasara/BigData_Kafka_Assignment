package com.example.consumer.messaging;

import com.example.avro.OrderEvent;
import com.example.consumer.model.AggregateResult;
import com.example.consumer.model.ProductAggregate;
import com.example.consumer.service.OrderAggregationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class OrderListener {

    private static final Logger log = LoggerFactory.getLogger(OrderListener.class);
    private final OrderAggregationService aggregationService;

    public OrderListener(OrderAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @KafkaListener(topics = {"${order.topic}", "${order.retry-topic}"}, containerFactory = "orderListenerContainerFactory")
    public void consume(@Payload OrderEvent event) {
        AggregateResult aggregateResult = aggregationService.updateAggregate(event);
        ProductAggregate productAggregate = aggregateResult.product();
        ProductAggregate globalAggregate = aggregateResult.global();
        log.info("Processed order {} for product {} at price {}. Product avg: {} (count {}), Global avg: {} (count {})",
            event.getOrderId(), event.getProduct(), event.getPrice(),
            productAggregate.average(), productAggregate.count(),
            globalAggregate.average(), globalAggregate.count());
    }
}
