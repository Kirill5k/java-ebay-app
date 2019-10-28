package io.kirill.ebayapp;

import io.kirill.ebayapp.common.configs.CexConfig;
import io.kirill.ebayapp.common.configs.EbayConfig;
import io.kirill.ebayapp.common.configs.TelegramConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties({EbayConfig.class, CexConfig.class, TelegramConfig.class})
public class EbayApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbayApplication.class, args);
	}

}
