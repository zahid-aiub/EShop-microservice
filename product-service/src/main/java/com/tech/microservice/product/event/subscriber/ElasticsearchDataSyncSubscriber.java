package com.tech.microservice.product.event.subscriber;

import com.tech.microservice.product.event.ProductCreatedEvent;
import com.tech.microservice.product.model.ProductES;
import com.tech.microservice.product.repository.elastic.ProductSearchRepository;
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
    public void consumeProductCreatedEvent(ProductCreatedEvent event) {
        try {
            logger.info("Received ProductCreatedEvent for product ID: {}", event.id());

            ProductES productES = new ProductES(
                    event.id(),
                    event.name(),
                    event.description(),
                    event.skuCode(),
                    event.price()
            );

            productSearchRepository.save(productES);
            logger.info("Product synced to Elasticsearch from Kafka event. Product ID: {}", event.id());

        } catch (Exception e) {
            logger.error("Failed to sync product to Elasticsearch for ID: {}. Error: {}",
                    event.id(), e.getMessage());
        }
    }

}
