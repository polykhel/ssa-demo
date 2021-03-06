package com.polykhel.ssa.service.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;

import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Extends a ZuulRoute to add the instance status ("UP", "DOWN", etc...)
 */
public class ZuulRouteDTO extends ZuulProperties.ZuulRoute {

    @Getter
    @Setter
    private String status;

    public ZuulRouteDTO(
        String id,
        String path,
        String serviceId,
        String url,
        boolean stripPrefix,
        Boolean retryable,
        @NotNull Set<String> sensitiveHeaders,
        String status) {
        super(id, path, serviceId, url, stripPrefix, retryable, sensitiveHeaders);
        this.status = status;
    }

    public ZuulRouteDTO(String path, String location, String status) {
        super(path, location);
        this.status = status;
    }

    public ZuulRouteDTO(String status) {
        super();
        this.status = status;
    }
}
