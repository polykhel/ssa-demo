package com.polykhel.ssa.config.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.polykhel.ssa.config.CoreProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.PRE_TYPE;

/**
 * Zuul filter for restricting access to backend microservices endpoints.
 */
@Slf4j
public class AccessControlFilter extends ZuulFilter {

    private final RouteLocator routeLocator;

    private final CoreProperties properties;

    public AccessControlFilter(RouteLocator routeLocator, CoreProperties properties) {
        this.routeLocator = routeLocator;
        this.properties = properties;
    }

    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    /**
     * Filter requests on endpoints that are not in the list of authorized microservices endpoints.
     */
    @Override
    public boolean shouldFilter() {
        HttpServletRequest request = RequestContext.getCurrentContext().getRequest();
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();

        for (Route route : routeLocator.getRoutes()) {
            String serviceUrl = contextPath + route.getFullPath();
            String serviceName = route.getId();

            // remove the "**" at the end of the route URL
            if (requestUri.startsWith(serviceUrl.substring(0, serviceUrl.length() - 2))) {
                return !isAuthorizedRequest(serviceUrl, serviceName, requestUri);
            }
        }

        return true;
    }

    private boolean isAuthorizedRequest(String serviceUrl, String serviceName, String requestUri) {
        Map<String, List<String>> authorizedMicroservicesEndpoints =
            properties.getGateway().getAuthorizedMicroservicesEndpoints();

        List<String> authorizedEndpoints = authorizedMicroservicesEndpoints.get(serviceName);
        // If the authorized endpoints list is empty for this service, all access is allowed
        if (authorizedEndpoints == null) {
            log.debug(
                "Access Control: allowing access for {} as no access control policy has been set up for "
                    + "service: {}",
                requestUri,
                serviceName);
            return true;
        } else {
            for (String endpoint : authorizedEndpoints) {
                // remove the "**/" at the end of the route URL
                String gatewayEndpoint = serviceUrl.substring(0, serviceUrl.length() - 3) + endpoint;
                if (requestUri.startsWith(gatewayEndpoint)) {
                    log.debug(
                        "Access Control: allowing access for {} as it matches the following authorized "
                            + "microservice endpoint: {}",
                        requestUri,
                        gatewayEndpoint);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Object run() {
        RequestContext context = RequestContext.getCurrentContext();
        context.setResponseStatusCode(HttpStatus.FORBIDDEN.value());
        if (context.getResponseBody() == null && !context.getResponseGZipped()) {
            context.setSendZuulResponse(false);
        }
        log.debug(
            "Access Control: filtered unauthorized access on endpoint {}",
            context.getRequest().getRequestURI());
        return null;
    }
}
