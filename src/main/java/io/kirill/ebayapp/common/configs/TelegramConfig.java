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
@ConfigurationProperties(prefix="app.telegram")
public class TelegramConfig {
    private final String baseUrl;
    private final String messagePath;
    private final String channelId;
}
