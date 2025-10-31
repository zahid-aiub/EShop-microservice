package com.tech.microservice.product.controller;

import com.tech.microservice.product.dto.ProductRequest;
import com.tech.microservice.product.dto.ProductResponse;
import com.tech.microservice.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody ProductRequest productRequest) {
        return productService.createProduct(productRequest);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/test")
    @ResponseStatus(HttpStatus.OK)
    public String test() {
        return "Test";
    }

    @GetMapping("/fulltext/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ProductResponse> fullTextSearch(@RequestParam("query") String query) {
        return productService.fullTextSearch(query);
    }

    // Sync endpoint for manual trigger
    @PostMapping("/sync/elasticsearch")
    public ResponseEntity<String> syncToElasticsearch() {
        productService.syncAllProductsToElasticsearch();
        return ResponseEntity.ok("All products synced to Elasticsearch");
    }
}