package io.kirill.ebayapp;

import io.kirill.ebayapp.common.configs.CexConfig;
import io.kirill.ebayapp.common.configs.EbayConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({EbayConfig.class, CexConfig.class})
public class EbayAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(EbayAppApplication.class, args);
	}

}
