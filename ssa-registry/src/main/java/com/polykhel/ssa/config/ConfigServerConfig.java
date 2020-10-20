package com.polykhel.ssa.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Properties for the config server configured in the {@code application.yml} file.
 */
@ConfigurationProperties(prefix = "spring.cloud.config.server")
@Getter
public class ConfigServerConfig {

    private final List<Map<String, Object>> composite = new ArrayList<>();
}
