package com.example.producer.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record OrderRequest(
        @NotBlank(message = "orderId is required") String orderId,
        @NotBlank(message = "product is required") String product,
        @NotNull(message = "price is required") @DecimalMin(value = "0.0", inclusive = false, message = "price must be > 0") BigDecimal price
) {
}
