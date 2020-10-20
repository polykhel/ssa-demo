package com.polykhel.ssa.security.uaa;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Load Balanced Resource Details
 */
@ConditionalOnMissingBean
@Slf4j
public class LoadBalancedResourceDetails extends ClientCredentialsResourceDetails {

    /**
     * Constant <code>EXCEPTION_MESSAGE="Returning an invalid URI: {}"</code>
     */
    public static final String EXCEPTION_MESSAGE = "Returning an invalid URI: {}";

    private final LoadBalancerClient loadBalancerClient;

    @Getter
    @Setter
    private String tokenServiceId;

    public LoadBalancedResourceDetails(LoadBalancerClient loadBalancerClient) {
        this.loadBalancerClient = loadBalancerClient;
    }

    public String getAccessTokenUri() {
        if (ObjectUtils.allNotNull(loadBalancerClient, tokenServiceId) && !tokenServiceId.isEmpty()) {
            try {
                return loadBalancerClient.reconstructURI(loadBalancerClient.choose(tokenServiceId), new URI(super.getAccessTokenUri())).toString();
            } catch (URISyntaxException e) {
                log.error("Returning an invalid URI: {}", e.getMessage());
                return super.getAccessTokenUri();
            }
        } else {
            return super.getAccessTokenUri();
        }
    }
}
