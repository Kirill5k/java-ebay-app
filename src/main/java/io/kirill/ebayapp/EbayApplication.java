package io.kirill.ebayapp;

import io.kirill.ebayapp.common.configs.CexConfig;
import io.kirill.ebayapp.common.configs.EbayConfig;
import io.kirill.ebayapp.common.configs.TelegramConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

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
	RouterFunction<ServerResponse> homeRoute(@Value("classpath:/index.html") Resource index) {
		return route(GET("/"), req -> ok().contentType(MediaType.TEXT_HTML).bodyValue(index));
	}
}
