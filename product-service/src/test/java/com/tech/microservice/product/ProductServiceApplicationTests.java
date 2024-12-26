package com.tech.microservice.product;

import io.restassured.RestAssured;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.testcontainers.containers.MongoDBContainer;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductServiceApplicationTests {

	static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:7.0.5");

	@LocalServerPort
	private Integer port;

	@BeforeEach
	void setUp() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = port;
	}

	static {
		mongoDBContainer.start();

		/*
		* Sets the system property spring.data.mongodb.uri to point to the
		* Testcontainer's replica set URL, overriding the default MongoDB connection string.
		* */
		System.setProperty("spring.data.mongodb.uri", mongoDBContainer.getReplicaSetUrl());
	}

	@Test
	void shouldCreateProduct() {

		String body = """
				{
				  	"name" : "Macbook Pro M4 Pro",
				    "description" : "Product from Apple",
				    "price" : 2750
				}
				""";

		RestAssured.given()
				.contentType("application/json")
				.body(body)
				.when()
				.post("/api/product")
				.then()
				.statusCode(201)
				.body("id", Matchers.notNullValue())
				.body("name", Matchers.equalTo("Macbook Pro M4 Pro"))
				.body("description", Matchers.equalTo("Product from Apple"))
				.body("price", Matchers.equalTo(2750));
	}



}
