package com.tech.microservice.product.service;

import com.tech.microservice.product.dto.ProductRequest;
import com.tech.microservice.product.dto.ProductResponse;
import com.tech.microservice.product.event.ProductCreatedEvent;
import com.tech.microservice.product.event.ProductDeletedEvent;
import com.tech.microservice.product.event.ProductUpdatedEvent;
import com.tech.microservice.product.event.publisher.ElasticsearchDataSyncPublisher;
import com.tech.microservice.product.model.Product;
import com.tech.microservice.product.model.ProductES;
import com.tech.microservice.product.repository.elastic.ProductSearchRepository;
import com.tech.microservice.product.repository.mongo.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        publisher.sendProductCreatedEvent(productCreatedEvent);
        log.info("ProductCreatedEvent sent to Kafka for product ID: {}", product.getId());

        return convertToResponse(product);
    }

    public ProductResponse updateProduct(String id, ProductRequest productRequest) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isEmpty()) {
            throw new RuntimeException("Product not found with id: " + id);
        }

        Product product = existingProduct.get();

        Product updatedProduct = product.toBuilder()
                .name(productRequest.name())
                .description(productRequest.description())
                .skuCode(productRequest.skuCode())
                .price(productRequest.price())
                .build();

        Product savedProduct = productRepository.save(updatedProduct);
        log.info("Product updated successfully. ID: {}", id);

        ProductUpdatedEvent productUpdatedEvent = new ProductUpdatedEvent(
                savedProduct.getId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getSkuCode(),
                savedProduct.getPrice()
        );

        publisher.sendProductUpdatedEvent(productUpdatedEvent);
        log.info("ProductUpdatedEvent sent to Kafka for product ID: {}", id);

        return convertToResponse(savedProduct);
    }

    public void deleteProduct(String id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found with id: " + id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully. ID: {}", id);

        ProductDeletedEvent productDeletedEvent = new ProductDeletedEvent(id);
        publisher.sendProductDeletedEvent(productDeletedEvent);
        log.info("ProductDeletedEvent sent to Kafka for product ID: {}", id);
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll()
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<ProductResponse> fullTextSearch(String text) {
        return productSearchRepository.fullTextSearch(text)
                .stream()
                .map(this::convertToResponse)
                .toList();
    }

//    // Bulk sync existing data
//    public void syncAllProductsToElasticsearch() {
//        List<Product> allProducts = productRepository.findAll();
//        List<ProductES> esProducts = allProducts.stream()
//                .map(ProductES::new)
//                .toList();
//
//        productSearchRepository.saveAll(esProducts);
//        log.info("Bulk sync completed for {} products", esProducts.size());
//    }

    // Bulk sync existing data - Complete synchronization
    public void syncAllProductsToElasticsearch() {
        try {
            log.info("Starting complete sync between MongoDB and Elasticsearch...");

            // Step 1: Get all products from MongoDB
            List<Product> mongoProducts = productRepository.findAll();
            log.info("Found {} products in MongoDB", mongoProducts.size());

            // Step 2: Get all product IDs from Elasticsearch
            List<String> esProductIds = getAllProductIdsFromElasticsearch();
            log.info("Found {} products in Elasticsearch", esProductIds.size());

            // Step 3: Get MongoDB product IDs
            List<String> mongoProductIds = mongoProducts.stream()
                    .map(Product::getId)
                    .toList();

            // Step 4: Find products to delete from Elasticsearch
            List<String> productsToDelete = esProductIds.stream()
                    .filter(esId -> !mongoProductIds.contains(esId))
                    .toList();

            // Step 5: Delete orphaned products from Elasticsearch
            if (!productsToDelete.isEmpty()) {
                productSearchRepository.deleteAllById(productsToDelete);
                log.info("Deleted {} orphaned products from Elasticsearch", productsToDelete.size());
            }

            // Step 6: Convert and save all MongoDB products to Elasticsearch
            List<ProductES> esProducts = mongoProducts.stream()
                    .map(ProductES::new)
                    .toList();

            productSearchRepository.saveAll(esProducts);
            log.info("Upserted {} products to Elasticsearch", esProducts.size());

            log.info("Complete sync completed successfully. MongoDB ↔ Elasticsearch are now in sync");

        } catch (Exception e) {
            log.error("Error during bulk sync: {}", e.getMessage(), e);
            throw new RuntimeException("Bulk sync failed", e);
        }
    }

    // Helper method to get all product IDs from Elasticsearch
    private List<String> getAllProductIdsFromElasticsearch() {
        try {
            Iterable<ProductES> productIterable = productSearchRepository.findAll();
            List<String> productIds = new ArrayList<>();

            for (ProductES product : productIterable) {
                productIds.add(product.getId());
            }

            return productIds;
        } catch (Exception e) {
            log.warn("Error fetching product IDs from Elasticsearch: {}", e.getMessage());
            return List.of(); // Return empty list if Elasticsearch is not available
        }
    }

    private ProductResponse convertToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSkuCode(),
                product.getPrice()
        );
    }

    private ProductResponse convertToResponse(ProductES product) {
        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getSkuCode(),
                product.getPrice()
        );
    }
}