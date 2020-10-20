package com.polykhel.ssa.config.metric;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.polykhel.ssa.config.CoreProperties;
import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.dropwizard.DropwizardConfig;
import io.micrometer.core.instrument.dropwizard.DropwizardMeterRegistry;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Console Reporter Configuration
 * <p>
 * Pass the metrics to the logs with Dropwizard Reporter implementation
 * see https://github.com/micrometer-metrics/micrometer-docs/blob/9fedeb5/src/docs/guide/console-reporter.adoc
 */
@Configuration
@ConditionalOnProperty("ssa.metrics.logs.enabled")
@Slf4j
public class LoggingMetricsExportConfig {

    private final CoreProperties coreProperties;

    public LoggingMetricsExportConfig(CoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    /**
     * Dropwizard Registry
     *
     * @return a {@link com.codahale.metrics.MetricRegistry} object.
     */
    @Bean
    public MetricRegistry dropwizardRegistry() {
        return new MetricRegistry();
    }

    @Bean
    public Slf4jReporter consoleReporter(MetricRegistry dropwizardRegistry) {
        log.info("Initializing Metrics Log reporting");
        Marker metricsMarker = MarkerFactory.getMarker("metrics");
        final Slf4jReporter reporter = Slf4jReporter.forRegistry(dropwizardRegistry)
            .outputTo(LoggerFactory.getLogger("metrics"))
            .markWith(metricsMarker)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build();
        reporter.start(coreProperties.getMetrics().getLogs().getReportFrequency(), TimeUnit.SECONDS);
        return reporter;
    }

    /**
     * <p>consoleLoggingRegistry.</p>
     *
     * @param dropwizardRegistry a {@link com.codahale.metrics.MetricRegistry} object.
     * @return a {@link io.micrometer.core.instrument.MeterRegistry} object.
     */
    @Bean
    public MeterRegistry consoleLoggingRegistry(MetricRegistry dropwizardRegistry) {
        DropwizardConfig dropwizardConfig = new DropwizardConfig() {
            @Override
            public String prefix() {
                return "console";
            }

            @Override
            public String get(String key) {
                return null;
            }
        };

        return new DropwizardMeterRegistry(dropwizardConfig, dropwizardRegistry, HierarchicalNameMapper.DEFAULT, Clock.SYSTEM) {
            @Override
            protected Double nullGaugeValue() {
                return null;
            }
        };
    }
}
