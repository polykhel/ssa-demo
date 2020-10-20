package com.polykhel.ssa.config.metric;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.actuate.autoconfigure.metrics.MetricsEndpointAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@ConditionalOnClass(Timed.class)
@AutoConfigureAfter(MetricsEndpointAutoConfiguration.class)
public class MetricsEndpointConfig {

    /**
     * Metrics Endpoint
     *
     * @param meterRegistry a {@link io.micrometer.core.instrument.MeterRegistry} object.
     * @return a {@link com.polykhel.ssa.config.metric.MetricsEndpoint} object.
     */
    @Bean
    @ConditionalOnBean({MeterRegistry.class})
    @ConditionalOnMissingBean
    @ConditionalOnAvailableEndpoint
    public MetricsEndpoint metricsEndpoint(MeterRegistry meterRegistry) {
        return new MetricsEndpoint(meterRegistry);
    }
}
