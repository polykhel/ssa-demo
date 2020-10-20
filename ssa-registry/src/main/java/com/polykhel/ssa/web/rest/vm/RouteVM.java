package com.polykhel.ssa.web.rest.vm;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.cloud.client.ServiceInstance;

import java.util.List;

/**
 * View Model that stores a route managed by the Registry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteVM {

    private String path;

    private String prefix;

    private String serviceId;

    private String appName;

    private String status;

    private List<ServiceInstance> serviceInstances;
}
