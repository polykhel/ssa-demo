package com.polykhel.ssa.security.oauth2;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.core.Ordered;

import java.util.Optional;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class AuthorizationHeaderFilter extends ZuulFilter {

    private final AuthorizationHeaderUtil headerUtil;

    public AuthorizationHeaderFilter(AuthorizationHeaderUtil headerUtil) {
        this.headerUtil = headerUtil;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        Optional<String> authorizationHeader = headerUtil.getAuthorizationHeader();
        authorizationHeader.ifPresent(s -> ctx.addZuulRequestHeader(AUTHORIZATION, s));
        return null;
    }
}
