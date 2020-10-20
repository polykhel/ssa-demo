package com.polykhel.ssa.security.uaa;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import static com.polykhel.ssa.utils.constants.ProfileConstants.PROFILE_UAA;
import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@Profile(PROFILE_UAA)
public class UaaAuthorizationHeaderFilter extends ZuulFilter {
    private final UaaAuthorizationHeaderUtil authorizationHeaderUtil;

    public UaaAuthorizationHeaderFilter(UaaAuthorizationHeaderUtil authorizationHeaderUtil) {
        this.authorizationHeaderUtil = authorizationHeaderUtil;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        RequestContext ctx = RequestContext.getCurrentContext();
        return ctx.getRequest().getRequestURI().startsWith("/services");
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        // Add specific authorization headers for UAA
        ctx.addZuulRequestHeader(AUTHORIZATION, authorizationHeaderUtil.getAuthorizationHeader());
        return null;
    }
}
