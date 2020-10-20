package com.polykhel.ssa.security.uaa;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import static com.polykhel.ssa.utils.constants.ProfileConstants.PROFILE_UAA;

@Configuration
@Profile(PROFILE_UAA)
public class UaaConfig {
    public static final String CLIENT_REGISTRATION_ID = "uaa";

    private final ClientRegistrationRepository clientRegistrationRepository;
    private final RestTemplateBuilder restTemplateBuilder;

    public UaaConfig(
        ClientRegistrationRepository clientRegistrationRepository,
        RestTemplateBuilder restTemplateBuilder) {
        this.clientRegistrationRepository = clientRegistrationRepository;
        this.restTemplateBuilder = restTemplateBuilder;
    }

    @Bean
    @LoadBalanced
    public RestTemplate uaaRestTemplate() {
        ClientRegistration clientRegistration =
            clientRegistrationRepository.findByRegistrationId(CLIENT_REGISTRATION_ID);
        if (clientRegistration == null) {
            throw new IllegalArgumentException(
                "Invalid Client Registration with Id: " + CLIENT_REGISTRATION_ID);
        }

        return restTemplateBuilder
            .messageConverters(
                new FormHttpMessageConverter(), new OAuth2AccessTokenResponseHttpMessageConverter())
            .errorHandler(new OAuth2ErrorResponseErrorHandler())
            .basicAuthentication(clientRegistration.getClientId(), clientRegistration.getClientSecret())
            .build();
    }
}
