package com.example.producer.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderBatchRequest(@NotEmpty(message = "orders list cannot be empty") List<@Valid OrderRequest> orders) {
}
