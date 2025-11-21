package com.example.producer.service;

import com.example.avro.OrderEvent;
import com.example.producer.api.dto.OrderRequest;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final KafkaTemplate<String, OrderEvent> kafkaTemplate;
    private final String orderTopic;

    public OrderService(KafkaTemplate<String, OrderEvent> kafkaTemplate,
                        @Value("${order.topic}") String orderTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderTopic = orderTopic;
    }

    public void sendOrder(OrderRequest request) {
        OrderEvent payload = buildEvent(request);
        kafkaTemplate.send(orderTopic, payload.getOrderId().toString(), payload)
            .whenComplete(this::logResult);
    }

    public int sendOrders(List<OrderRequest> requests) {
        if (requests == null || requests.isEmpty()) {
            return 0;
        }
        requests.forEach(this::sendOrder);
        return requests.size();
    }

    private OrderEvent buildEvent(OrderRequest request) {
        return OrderEvent.newBuilder()
                .setOrderId(request.orderId())
                .setProduct(request.product())
                .setPrice(request.price().doubleValue())
            .setCreatedAt(Instant.now())
                .build();
    }

    private void logResult(SendResult<String, OrderEvent> result, Throwable throwable) {
        if (throwable != null) {
            String key = result != null && result.getProducerRecord() != null
                    ? result.getProducerRecord().key()
                    : "unknown";
            log.error("Failed to publish order {}", key, throwable);
        } else if (result != null) {
            log.info("Published order {} to partition {} offset {}", result.getProducerRecord().key(),
                    result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
        }
    }
}
