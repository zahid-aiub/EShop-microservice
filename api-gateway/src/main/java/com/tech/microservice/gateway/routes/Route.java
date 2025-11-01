package com.tech.microservice.gateway.routes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.filter.CircuitBreakerFilterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.stream.Collectors;

import static org.springframework.cloud.gateway.server.mvc.filter.FilterFunctions.setPath;
import static org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions.route;

@Configuration
public class Route {

    private static final Logger log = LoggerFactory.getLogger(Route.class);

    @Value("${product.service.url}")
    private String productServiceUrl;
    @Value("${order.service.url}")
    private String orderServiceUrl;
    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;


    @Bean
    public RouterFunction<ServerResponse> productServiceRoute() {
        return createLoggedRoute("product_service", "/api/product", productServiceUrl);
    }

    @Bean
    public RouterFunction<ServerResponse> productServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("product_service_swagger")
                .route(RequestPredicates.path("/aggregate/product-service/v3/api-docs"),
                        HandlerFunctions.http(productServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "productServiceSwaggerCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }


    @Bean
    public RouterFunction<ServerResponse> orderServiceRoute() {
        return createLoggedRoute("order_service", "/api/order", orderServiceUrl);
    }

    @Bean
    public RouterFunction<ServerResponse> orderServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("order_service_swagger")
                .route(RequestPredicates.path("/aggregate/order-service/v3/api-docs"),
                        HandlerFunctions.http(orderServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "orderServiceSwaggerCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }


    @Bean
    public RouterFunction<ServerResponse> inventoryServiceRoute() {
        return createLoggedRoute("inventory_service", "/api/inventory", inventoryServiceUrl);
    }

    @Bean
    public RouterFunction<ServerResponse> inventoryServiceSwaggerRoute() {
        return GatewayRouterFunctions.route("inventory_service_swagger")
                .route(RequestPredicates.path("/aggregate/inventory-service/v3/api-docs"),
                        HandlerFunctions.http(inventoryServiceUrl))
                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        "inventoryServiceSwaggerCircuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .filter(setPath("/api-docs"))
                .build();
    }


    @Bean
    public RouterFunction<ServerResponse> fallbackRoute() {
        return route("fallbackRoute")
                .GET("/fallbackRoute", request ->
                        ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body("Service Unavailable, please try again later"))
                .build();
    }


    private RouterFunction<ServerResponse> createLoggedRoute(
            String routeName, String basePath, String targetServiceUrl) {

        return GatewayRouterFunctions.route(routeName)
                .route(RequestPredicates.path(basePath)
                                .or(RequestPredicates.path(basePath + "/**")),
                        HandlerFunctions.http(targetServiceUrl))

                // Request log (no body)
                .before(request -> {
                    String username = getCurrentUsername();

                    // Safe query params
                    String queryParams = request.params().isEmpty()
                            ? "{}"
                            : request.params().toString();

                    log.info("[{}] Request    || Method: {} | Path: {} | Params: {} | Headers: {}",
                            username,
                            request.method(),
                            request.path(),
                            queryParams,
                            request.headers().asHttpHeaders());

                    return request;
                })

                // Response log
                .filter((request, next) -> {
                    long start = System.currentTimeMillis();
                    String username = getCurrentUsername();

                    ServerResponse response = next.handle(request);
                    long duration = System.currentTimeMillis() - start;

                    log.info("[{}] Response   || Status: {} | Time: {} ms | Response Body: {}",
                            username,
                            response.statusCode(),
                            duration,
                            "[]");

                    return response;
                })

                .filter(CircuitBreakerFilterFunctions.circuitBreaker(
                        routeName + "_circuitBreaker",
                        URI.create("forward:/fallbackRoute")))
                .build();
    }


    private String getCurrentUsername() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            // ignore
        }
        return "anonymous";
    }
}
