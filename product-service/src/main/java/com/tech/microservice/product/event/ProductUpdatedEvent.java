package com.tech.microservice.product.event;

import java.math.BigDecimal;

public record ProductUpdatedEvent(
        String id,
        String name,
        String description,
        String skuCode,
        BigDecimal price
) {}