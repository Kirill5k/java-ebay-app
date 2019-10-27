package io.kirill.ebayapp.common.configs;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@Getter
@Setter
@ConstructorBinding
@RequiredArgsConstructor
@ConfigurationProperties(prefix="app.ebay")
public class EbayConfig {
    private final String clientId;
    private final String clientSecret;
    private final String baseUrl;
    private final String authPath;
    private final String searchPath;
    private final String itemPath;
}
