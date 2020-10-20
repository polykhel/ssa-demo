package com.polykhel.ssa.config.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;

import java.util.Set;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class JWTTokenRelayFilter extends ZuulFilter {

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 10000;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        @SuppressWarnings("unchecked")
        Set<String> headers = (Set<String>) context.get("ignoredHeaders");
        headers.remove(AUTHORIZATION);
        return null;
    }
}
