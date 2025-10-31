package com.tech.microservice.product.event.publisher;

import com.tech.microservice.product.event.ProductCreatedEvent;
import com.tech.microservice.product.event.subscriber.ElasticsearchDataSyncSubscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class ElasticsearchDataSyncPublisher {

    Logger logger = LoggerFactory.getLogger(ElasticsearchDataSyncSubscriber.class);

    private static final String TOPIC = "elastic-sync";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    public void sendProductCreatedEvent(ProductCreatedEvent event) {
        try {
            CompletableFuture<SendResult<String, Object>> future =
                    kafkaTemplate.send(TOPIC, event);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    logger.info("Sent product created event for product ID: {} with offset: {}",
                            event.id(), result.getRecordMetadata().offset());
                } else {
                    logger.error("Unable to send product created event for product ID: {}. Error: {}",
                            event.id(), ex.getMessage());
                }
            });
        } catch (Exception e) {
            logger.error("Error sending product created event for product ID: {}. Error: {}",
                    event.id(), e.getMessage());
        }
    }
}
