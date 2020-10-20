package com.polykhel.ssa.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.config.server.environment.EnvironmentRepository;
import org.springframework.cloud.config.server.environment.NativeEnvironmentProperties;
import org.springframework.cloud.config.server.environment.NativeEnvironmentRepository;
import org.springframework.cloud.config.server.environment.NativeEnvironmentRepositoryFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@ConditionalOnBean(EnvironmentRepository.class)
@Profile("native")
public class NativeRepositoryConfig {

    @Bean
    public NativeEnvironmentRepository nativeEnvironmentRepository(
        NativeEnvironmentRepositoryFactory factory,
        NativeEnvironmentProperties environmentProperties) {
        return factory.build(environmentProperties);
    }
}
