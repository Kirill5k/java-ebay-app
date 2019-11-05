package io.kirill.ebayapp;

import static org.hamcrest.CoreMatchers.startsWithIgnoringCase;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
				.expectHeader().contentType(MediaType.TEXT_HTML)
				.expectStatus().isOk()
				.expectBody(String.class).value(startsWithIgnoringCase("<!DOCTYPE html>"));
	}
}
