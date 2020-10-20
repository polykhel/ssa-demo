package com.polykhel.ssa.client;

import com.polykhel.ssa.security.oauth2.AuthorizationHeaderUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;

import java.util.Optional;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class TokenRelayRequestInterceptor implements RequestInterceptor {

    private final AuthorizationHeaderUtil authorizationHeaderUtil;

    public TokenRelayRequestInterceptor(AuthorizationHeaderUtil authorizationHeaderUtil) {
        super();
        this.authorizationHeaderUtil = authorizationHeaderUtil;
    }

    @Override
    public void apply(RequestTemplate template) {
        Optional<String> authorizationHeader = authorizationHeaderUtil.getAuthorizationHeader();
        authorizationHeader.ifPresent(s -> template.header(AUTHORIZATION, s));
    }
}
