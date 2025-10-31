package com.tech.microservice.product.repository.elastic;

import com.tech.microservice.product.model.ProductES;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface ProductSearchRepository extends ElasticsearchRepository<ProductES, String> {

    List<ProductES> findByNameContainingIgnoreCase(String name);
    List<ProductES> findByDescriptionContainingIgnoreCase(String description);

    @Query("""
        {
          "multi_match": {
            "query": "?0",
            "fields": ["name", "description", "skuCode"],
            "fuzziness": "AUTO",
            "operator": "or"
          }
        }
        """)
    List<ProductES> fullTextSearch(String text);

}
