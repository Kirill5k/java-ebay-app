package io.kirill.ebayapp;

import io.kirill.ebayapp.common.configs.CexConfig;
import io.kirill.ebayapp.common.configs.EbayConfig;
import io.kirill.ebayapp.common.configs.TelegramConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({EbayConfig.class, CexConfig.class, TelegramConfig.class})
public class EbayApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbayApplication.class, args);
	}

	@Bean
	RouterFunction<ServerResponse> homeRoute() {
		return route(GET("/"), req -> ok().body(Mono.just("Hello, World!"), String.class));
	}
}
