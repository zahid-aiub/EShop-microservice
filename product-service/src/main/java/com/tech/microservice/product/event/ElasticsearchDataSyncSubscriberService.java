package com.tech.microservice.product.event;

import com.tech.microservice.product.model.ProductES;
import com.tech.microservice.product.repository.elastic.ProductSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ElasticsearchDataSyncSubscriberService {

    @Autowired
    private ProductSearchRepository productSearchRepository;

    Logger logger = LoggerFactory.getLogger(ElasticsearchDataSyncSubscriberService.class);

    @KafkaListener(topics = "elastic-sync")
    public void listen(ProductCreatedEvent productCreatedEvent) {
        logger.info("Got Message from order-placed topic {}", productCreatedEvent);

        ProductES productES = new ProductES(
                productCreatedEvent.id(),
                productCreatedEvent.name(),
                productCreatedEvent.description(),
                productCreatedEvent.skuCode(),
                productCreatedEvent.price()
        );

        productSearchRepository.save(productES);
        logger.info("::::::: Product data sync to Kafka topic elastic-sync: {}", productCreatedEvent);
    }

}
