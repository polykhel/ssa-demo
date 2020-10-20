package com.polykhel.ssa.security.uaa;

import com.polykhel.ssa.config.CoreProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

@Configuration
@ConditionalOnClass({ClientCredentialsResourceDetails.class, LoadBalancerClient.class})
@ConditionalOnProperty({"core.security.client-authorization.client-id"})
public class UaaAutoConfig {

    private final CoreProperties coreProperties;

    public UaaAutoConfig(CoreProperties coreProperties) {
        this.coreProperties = coreProperties;
    }

    @Bean
    public LoadBalancedResourceDetails loadBalancedResourceDetails(LoadBalancerClient loadBalancerClient) {
        LoadBalancedResourceDetails loadBalancedResourceDetails = new LoadBalancedResourceDetails(loadBalancerClient);
        CoreProperties.Security.ClientAuthorization clientAuthorization = coreProperties.getSecurity().getClientAuthorization();
        loadBalancedResourceDetails.setAccessTokenUri(clientAuthorization.getAccessTokenUri());
        loadBalancedResourceDetails.setTokenServiceId(clientAuthorization.getTokenServiceId());
        loadBalancedResourceDetails.setClientId(clientAuthorization.getClientId());
        loadBalancedResourceDetails.setClientSecret(clientAuthorization.getClientSecret());
        return loadBalancedResourceDetails;
    }
}
