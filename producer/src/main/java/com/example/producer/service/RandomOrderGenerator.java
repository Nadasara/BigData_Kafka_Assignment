package com.example.producer.service;

import com.example.producer.api.dto.OrderRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class RandomOrderGenerator {

    private static final List<String> DEFAULT_PRODUCTS = List.of(
            "Laptop",
            "Phone",
            "Headphones",
            "Monitor",
            "Keyboard",
            "Mouse",
            "Printer"
    );

    public OrderRequest randomOrder(String preferredProduct) {
        String product = resolveProduct(preferredProduct);
        BigDecimal price = randomPrice();
        String orderId = UUID.randomUUID().toString();
        return new OrderRequest(orderId, product, price);
    }

    public List<OrderRequest> randomOrders(int count, String preferredProduct) {
        return IntStream.range(0, count)
                .mapToObj(i -> randomOrder(preferredProduct))
                .toList();
    }

    private String resolveProduct(String preferredProduct) {
        if (StringUtils.hasText(preferredProduct)) {
            return preferredProduct;
        }
        int index = ThreadLocalRandom.current().nextInt(DEFAULT_PRODUCTS.size());
        return DEFAULT_PRODUCTS.get(index);
    }

    private BigDecimal randomPrice() {
        double value = 5 + (ThreadLocalRandom.current().nextDouble() * 995);
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
