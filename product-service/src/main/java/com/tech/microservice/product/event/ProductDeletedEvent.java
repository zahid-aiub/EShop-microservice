package com.tech.microservice.product.event;

public record ProductDeletedEvent(
        String id
) {}