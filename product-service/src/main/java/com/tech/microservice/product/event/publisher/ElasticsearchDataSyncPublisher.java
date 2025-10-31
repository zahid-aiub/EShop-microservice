package com.tech.microservice.product.event.publisher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tech.microservice.product.event.ProductCreatedEvent;
import com.tech.microservice.product.event.ProductDeletedEvent;
import com.tech.microservice.product.event.ProductUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ElasticsearchDataSyncPublisher {

    Logger logger = LoggerFactory.getLogger(ElasticsearchDataSyncPublisher.class);

    private static final String TOPIC = "elastic-sync";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public ElasticsearchDataSyncPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void sendProductCreatedEvent(ProductCreatedEvent event) {
        sendEvent("PRODUCT_CREATED", event.id(), event);
    }

    public void sendProductUpdatedEvent(ProductUpdatedEvent event) {
        sendEvent("PRODUCT_UPDATED", event.id(), event);
    }

    public void sendProductDeletedEvent(ProductDeletedEvent event) {
        sendEvent("PRODUCT_DELETED", event.id(), event);
    }


    private void sendEvent(String eventType, String productId, Object event) {
        try {
            Map<String, Object> message = new HashMap<>();
            message.put("eventType", event.getClass().getSimpleName());
            message.put("payload", event);

            String jsonMessage = objectMapper.writeValueAsString(message);

            CompletableFuture<SendResult<String, String>> future =
                    kafkaTemplate.send(TOPIC, jsonMessage);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Sent {} event for product ID: {} with offset: {}",
                            eventType, productId, result.getRecordMetadata().offset());
                } else {
                    logger.error("Unable to send {} event for product ID: {}. Error: {}",
                            eventType, productId, ex.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error sending {} event for product ID: {}. Error: {}",
                    eventType, productId, e.getMessage());
        }

    }
}