package com.polykhel.ssa.config;

import ch.qos.logback.classic.LoggerContext;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

import static com.polykhel.ssa.config.logging.LoggingUtils.*;

/**
 * Configures the console and Logstash log appenders from the app properties.
 */
@Configuration
@RefreshScope
public class LoggingConfig {

    public LoggingConfig(
        @Value("${spring.application.name}") String appName,
        @Value("${server.port}") String serverPort,
        CoreProperties properties,
        ObjectProvider<BuildProperties> buildProperties,
        ObjectMapper mapper)
        throws JsonProcessingException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        Map<String, String> map = new HashMap<>();
        map.put("app_name", appName);
        map.put("app_port", serverPort);
        buildProperties.ifAvailable(it -> map.put("version", it.getVersion()));
        String customFields = mapper.writeValueAsString(map);

        CoreProperties.Logging loggingProperties = properties.getLogging();
        CoreProperties.Logging.Logstash logstashProperties = loggingProperties.getLogstash();

        if (loggingProperties.isUseJsonFormat()) {
            addJsonConsoleAppender(context, customFields);
        }
        if (logstashProperties.isEnabled()) {
            addLogstashTcpSocketAppender(context, customFields, logstashProperties);
        }
        if (loggingProperties.isUseJsonFormat() || logstashProperties.isEnabled()) {
            addContextListener(context, customFields, loggingProperties);
        }
        if (properties.getMetrics().getLogs().isEnabled()) {
            setMetricsMarkerLogbackFilter(context, loggingProperties.isUseJsonFormat());
        }
    }
}
