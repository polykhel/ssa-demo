package com.polykhel.ssa.service;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.shared.Application;
import com.netflix.eureka.EurekaServerContextHolder;
import com.polykhel.ssa.service.dto.ZuulRouteDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.RoutesRefreshedEvent;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Updates Zuul proxies depending on available application instances.
 */
@Service
@Slf4j
public class ZuulUpdaterService {

    private final RouteLocator routeLocator;

    private final ZuulProperties zuulProperties;

    private final ApplicationEventPublisher publisher;

    public ZuulUpdaterService(RouteLocator routeLocator, ZuulProperties zuulProperties, ApplicationEventPublisher publisher) {
        this.routeLocator = routeLocator;
        this.zuulProperties = zuulProperties;
        this.publisher = publisher;
    }

    @Scheduled(fixedDelay = 5_000)
    public void updateZuulRoutes() {
        boolean isDirty = false;

        List<Application> applications = EurekaServerContextHolder
            .getInstance().getServerContext().getRegistry().getApplications().getRegisteredApplications();

        for (Application application : applications) {
            for (InstanceInfo instanceInfo : application.getInstances()) {
                if (!instanceInfo.getStatus().equals(InstanceInfo.InstanceStatus.UP) &&
                    !instanceInfo.getStatus().equals(InstanceInfo.InstanceStatus.STARTING)) continue;
                String instanceId = instanceInfo.getInstanceId();
                String url = instanceInfo.getHomePageUrl();
                log.debug("Checking instance {} - {} ", instanceId, url);

                ZuulRouteDTO route = new ZuulRouteDTO(instanceId, "/" +
                    application.getName().toLowerCase() + "/" + instanceId + "/**",
                    null, url, zuulProperties.isStripPrefix(), zuulProperties.getRetryable(), Collections.emptySet(),
                    instanceInfo.getStatus().toString());

                if (zuulProperties.getRoutes().containsKey(instanceId)) {
                    log.debug("Instance '{}' already registered", instanceId);
                    if (!zuulProperties.getRoutes().get(instanceId).getUrl().equals(url) ||
                        !((ZuulRouteDTO) zuulProperties.getRoutes().get(instanceId)).getStatus().equals(instanceInfo.getStatus().toString())) {
                        log.debug("Updating instance '{}' with new URL: {}", instanceId, url);
                        zuulProperties.getRoutes().put(instanceId, route);
                        isDirty = true;
                    }
                }
            }
        }
        List<String> zuulRoutesToRemove = new ArrayList<>();
        for (String key : zuulProperties.getRoutes().keySet()) {
            if (applications.stream()
                .flatMap(application -> application.getInstances().stream())
                .noneMatch(instanceInfo -> instanceInfo.getId().equals(key))) {
                log.debug("Removing instance '{}'", key);
                zuulRoutesToRemove.add(key);
                isDirty = true;
            }
        }
        for (String key : zuulRoutesToRemove) {
            zuulProperties.getRoutes().remove(key);
        }
        if (isDirty) {
            log.info("Zuul routes have changed - refreshing the configuration");
            this.publisher.publishEvent(new RoutesRefreshedEvent(routeLocator));
        }
    }
}
