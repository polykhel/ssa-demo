package com.polykhel.ssa.config;

import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.zalando.problem.ProblemModule;
import org.zalando.problem.violations.ConstraintViolationProblemModule;

@Configuration
public class JacksonConfig {

    /**
     * Support for JavaTime classes.
     */
    @Bean
    public JavaTimeModule javaTimeModule() {
        return new JavaTimeModule();
    }

    /**
     * Support for JDK 8 classes like Optional.
     */
    @Bean
    public Jdk8Module jdk8Module() {
        return new Jdk8Module();
    }

    /**
     * Support for dynamic bytecode generation for standard Jackson POJO serializers and
     * deserializers, eliminating majority of remaining data binding overhead.
     */
    @Bean
    public AfterburnerModule afterburnerModule() {
        return new AfterburnerModule();
    }

    /**
     * Support for serialization/deserialization of RFC7807 Problem.
     */
    @Bean
    public ProblemModule problemModule() {
        return new ProblemModule();
    }

    /**
     * Support for serialization/deserialization of ConstraintViolationProblem.
     */
    @Bean
    public ConstraintViolationProblemModule constraintViolationProblemModule() {
        return new ConstraintViolationProblemModule();
    }
}
