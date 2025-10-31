package com.tech.microservice.product.event;

import java.math.BigDecimal;

public record ProductCreatedEvent(
        String id,
        String name,
        String description,
        String skuCode,
        BigDecimal price
) {}