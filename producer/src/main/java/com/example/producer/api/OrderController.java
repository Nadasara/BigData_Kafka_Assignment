package com.example.producer.api;

import com.example.producer.api.dto.OrderBatchRequest;
import com.example.producer.api.dto.OrderRequest;
import com.example.producer.api.dto.OrderResponse;
import com.example.producer.service.OrderService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/orders")
@Validated
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponse> publishOrder(@Valid @RequestBody OrderRequest request) {
        log.info("Publishing single order {}", request.orderId());
        orderService.sendOrder(request);
        return ResponseEntity.accepted().body(new OrderResponse("accepted", 1));
    }

    @PostMapping("/random")
    public ResponseEntity<OrderResponse> publishRandomOrder(@RequestParam(value = "product", required = false) String product) {
        log.info("Publishing random order for product override {}", product);
        orderService.sendRandomOrder(product);
        return ResponseEntity.accepted().body(new OrderResponse("accepted", 1));
    }

    @PostMapping(value = "/batch", params = "!count")
    public ResponseEntity<OrderResponse> publishOrders(@Valid @RequestBody OrderBatchRequest batchRequest) {
        int published = orderService.sendOrders(batchRequest.orders());
        return ResponseEntity.accepted().body(new OrderResponse("accepted", published));
    }

    @PostMapping(value = "/batch", params = "count")
    public ResponseEntity<OrderResponse> publishGeneratedOrders(
            @RequestParam("count") @Min(1) int count,
            @RequestParam(value = "product", required = false) String product) {
        log.info("Publishing {} auto-generated orders", count);
        int published = orderService.sendRandomOrders(count, product);
        return ResponseEntity.accepted().body(new OrderResponse("accepted", published));
    }
}
