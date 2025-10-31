package com.tech.microservice.product.service;

import com.tech.microservice.product.dto.ProductRequest;
import com.tech.microservice.product.dto.ProductResponse;
import com.tech.microservice.product.event.ProductCreatedEvent;
import com.tech.microservice.product.event.publisher.ElasticsearchDataSyncPublisher;
import com.tech.microservice.product.model.Product;
import com.tech.microservice.product.model.ProductES;
import com.tech.microservice.product.repository.elastic.ProductSearchRepository;
import com.tech.microservice.product.repository.mongo.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductSearchRepository productSearchRepository;

    @Autowired
    private ElasticsearchDataSyncPublisher publisher;

    public ProductResponse createProduct(ProductRequest productRequest) {
        Product product = Product.builder()
                .name(productRequest.name())
                .description(productRequest.description())
                .skuCode(productRequest.skuCode())
                .price(productRequest.price())
                .build();
        productRepository.save(product);
        log.info("Product created successfully");

        ProductCreatedEvent productCreatedEvent = new ProductCreatedEvent(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSkuCode(),
                product.getPrice()
        );
        log.info("Start - Sending ProductCreatedEvent {} to Kafka topic elastic-sync", productCreatedEvent);
        publisher.sendProductCreatedEvent(productCreatedEvent);
        log.info("End - Sending ProductCreatedEvent {} to Kafka topic elastic-sync", productCreatedEvent);
        return new ProductResponse(product.getId(), product.getName(), product.getDescription(),
                product.getSkuCode(),
                product.getPrice());
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(product -> new ProductResponse(product.getId(), product.getName(), product.getDescription(),
                        product.getSkuCode(),
                        product.getPrice()))
                .toList();
    }

    public List<ProductResponse> fullTextSearch(String text) {

        return productSearchRepository.fullTextSearch(text)
                .stream()
                .map(product -> new ProductResponse(product.getId(), product.getName(), product.getDescription(),
                        product.getSkuCode(),
                        product.getPrice()))
                .toList();
    }

    // Bulk sync existing data
    public void syncAllProductsToElasticsearch() {
        List<Product> allProducts = productRepository.findAll();
        List<ProductES> esProducts = allProducts.stream()
                .map(ProductES::new)
                .toList();

        productSearchRepository.saveAll(esProducts);
    }


}