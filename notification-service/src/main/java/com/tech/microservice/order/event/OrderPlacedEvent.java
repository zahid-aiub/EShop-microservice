package com.tech.microservice.order.event;

public class OrderPlacedEvent {

    private String orderNumber;
    private String email;

    public OrderPlacedEvent() {}

    public OrderPlacedEvent(String orderNumber, String email) {
        this.orderNumber = orderNumber;
        this.email = email;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getEmail() {
        return email;
    }
}
