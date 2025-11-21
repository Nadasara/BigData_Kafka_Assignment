package com.example.consumer.model;

public record AggregateResult(ProductAggregate product, ProductAggregate global) {
}
