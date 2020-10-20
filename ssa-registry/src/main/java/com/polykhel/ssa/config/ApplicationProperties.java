package com.polykhel.ssa.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Application properties configured in the {@code application.yml} file.
 */
@Getter
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final OAuth2 oauth2 = new OAuth2();

    @Getter
    @Setter
    public static class OAuth2 {
        private String principalAttribute;
        private String authoritiesAttribute;
    }
}
