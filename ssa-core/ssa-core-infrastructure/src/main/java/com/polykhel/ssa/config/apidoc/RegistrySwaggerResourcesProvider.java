package com.polykhel.ssa.config.apidoc;

import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import springfox.documentation.swagger.web.SwaggerResource;
import springfox.documentation.swagger.web.SwaggerResourcesProvider;

import java.util.ArrayList;
import java.util.List;

import static com.polykhel.ssa.utils.constants.ProfileConstants.SPRING_PROFILE_SWAGGER;

/**
 * Retrieves all registered microservices Swagger resources.
 */
@Component
@Primary
@Profile(SPRING_PROFILE_SWAGGER)
public class RegistrySwaggerResourcesProvider implements SwaggerResourcesProvider {

    private final RouteLocator routeLocator;

    public RegistrySwaggerResourcesProvider(RouteLocator routeLocator) {
        this.routeLocator = routeLocator;
    }

    @Override
    public List<SwaggerResource> get() {
        List<SwaggerResource> resources = new ArrayList<>();

        // Add the registry swagger resource that correspond to the registry's own swagger doc
        resources.add(swaggerResource("ssa-registry", "/v2/api-docs"));

        // Add the registered microservices swagger docs as additional swagger resources
        List<Route> routes = routeLocator.getRoutes();
        routes.forEach(
            route ->
                resources.add(
                    swaggerResource(route.getId(), route.getFullPath().replace("**", "v2/api-docs"))));

        return resources;
    }

    private SwaggerResource swaggerResource(String name, String location) {
        SwaggerResource swaggerResource = new SwaggerResource();
        swaggerResource.setName(name);
        swaggerResource.setLocation(location);
        swaggerResource.setSwaggerVersion("2.0");
        return swaggerResource;
    }
}
