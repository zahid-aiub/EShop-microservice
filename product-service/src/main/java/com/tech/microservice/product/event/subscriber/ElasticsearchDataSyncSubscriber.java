package com.tech.microservice.product.event.subscriber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tech.microservice.product.event.ProductCreatedEvent;
import com.tech.microservice.product.event.ProductDeletedEvent;
import com.tech.microservice.product.event.ProductUpdatedEvent;
import com.tech.microservice.product.model.ProductES;
import com.tech.microservice.product.repository.ProductSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ElasticsearchDataSyncSubscriber {

    private static final String TOPIC = "elastic-sync";

    @Autowired
    private ProductSearchRepository productSearchRepository;

    Logger logger = LoggerFactory.getLogger(ElasticsearchDataSyncSubscriber.class);

    @KafkaListener(topics = TOPIC)
    public void handleProductEvents(String message) {
        try {
            JsonNode jsonNode = new ObjectMapper().readTree(message);
            String eventType = jsonNode.get("eventType").asText();
            JsonNode payload = jsonNode.get("payload");

            switch (eventType) {
                case "ProductCreatedEvent" ->
                        handleProductCreated(new ObjectMapper().treeToValue(payload, ProductCreatedEvent.class));
                case "ProductUpdatedEvent" ->
                        handleProductUpdated(new ObjectMapper().treeToValue(payload, ProductUpdatedEvent.class));
                case "ProductDeletedEvent" ->
                        handleProductDeleted(new ObjectMapper().treeToValue(payload, ProductDeletedEvent.class));
                default -> logger.warn("Unknown event type received: {}", eventType);
            }

        } catch (Exception e) {
            logger.error("Failed to process event. Error: {}", e.getMessage());
        }
    }

    private void handleProductCreated(ProductCreatedEvent event) {
        logger.info("Received ProductCreatedEvent for product ID: {}", event.id());

        ProductES productES = new ProductES(
                event.id(),
                event.name(),
                event.description(),
                event.skuCode(),
                event.price()
        );

        productSearchRepository.save(productES);
        logger.info("Product created in Elasticsearch. Product ID: {}", event.id());
    }

    private void handleProductUpdated(ProductUpdatedEvent event) {
        logger.info("Received ProductUpdatedEvent for product ID: {}", event.id());

        ProductES productES = new ProductES(
                event.id(),
                event.name(),
                event.description(),
                event.skuCode(),
                event.price()
        );

        productSearchRepository.save(productES);
        logger.info("Product updated in Elasticsearch. Product ID: {}", event.id());
    }

    private void handleProductDeleted(ProductDeletedEvent event) {
        logger.info("Received ProductDeletedEvent for product ID: {}", event.id());

        productSearchRepository.deleteById(event.id());
        logger.info("Product deleted from Elasticsearch. Product ID: {}", event.id());
    }
}