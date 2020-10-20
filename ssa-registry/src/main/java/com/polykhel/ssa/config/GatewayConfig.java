package com.polykhel.ssa.config;

import com.polykhel.ssa.config.filter.AccessControlFilter;
import com.polykhel.ssa.config.filter.SwaggerDocsFilter;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Gateway Configuration class.
 */
@Configuration
public class GatewayConfig {

    @Bean
    public SwaggerDocsFilter swaggerDocsFilter() {
        return new SwaggerDocsFilter();
    }

    @Bean
    public AccessControlFilter accessControlFilter(
        RouteLocator routeLocator, CoreProperties properties) {
        return new AccessControlFilter(routeLocator, properties);
    }
}
