package io.kirill.ebayapp;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@SpringBootTest
class EbayApplicationTests {

	@Autowired
	RouterFunction<ServerResponse> homeRoute;

	@Test
	void contextLoads() {
	}

	@Test
	void homeRoute() {
		var client = WebTestClient
				.bindToRouterFunction(homeRoute)
				.build();

		client
				.get()
				.uri("/")
				.exchange()
				.expectStatus().isOk()
				.expectBody(String.class)
				.isEqualTo("Hello, World!");
	}
}
