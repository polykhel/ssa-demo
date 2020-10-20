package com.polykhel.ssa.web.rest;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.config.ConfigurationManager;
import com.netflix.discovery.shared.Application;
import com.netflix.discovery.shared.Pair;
import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import com.netflix.eureka.registry.PeerAwareInstanceRegistryImpl;
import com.netflix.eureka.resources.StatusResource;
import com.netflix.eureka.util.StatusInfo;
import com.polykhel.ssa.web.rest.vm.EurekaVM;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

/**
 * Controller for viewing Eureka data.
 */
@RestController
@RequestMapping("/api/eureka")
@Slf4j
public class EurekaResource {

    /**
     * GET  /eureka/applications : get Eureka applications information
     */
    @GetMapping("/applications")
    public ResponseEntity<EurekaVM> eureka() {
        EurekaVM eurekaVM = new EurekaVM();
        eurekaVM.setApplications(getApplications());
        return ResponseEntity.ok(eurekaVM);
    }


    /**
     * GET  /eureka/lastn : get Eureka registrations
     */
    @GetMapping("/eureka/lastn")
    public ResponseEntity<Map<String, Map<Long, String>>> lastn() {
        Map<String, Map<Long, String>> lastn = new HashMap<>();
        PeerAwareInstanceRegistryImpl registry = (PeerAwareInstanceRegistryImpl) getRegistry();
        Map<Long, String> canceledMap = registry.getLastNCanceledInstances()
            .stream().collect(toMap(Pair::first, Pair::second));
        lastn.put("canceled", canceledMap);
        Map<Long, String> registeredMap = registry.getLastNRegisteredInstances()
            .stream().collect(toMap(Pair::first, Pair::second));
        lastn.put("registered", registeredMap);
        return ResponseEntity.ok(lastn);
    }


    /**
     * GET  /eureka/replicas : get Eureka replicas
     */
    @GetMapping("/eureka/replicas")
    public ResponseEntity<List<String>> replicas() {
        List<String> replicas = new ArrayList<>();
        getServerContext().getPeerEurekaNodes().getPeerNodesView().forEach(
            node -> {
                try {
                    // The URL is parsed in order to remove login/password information
                    URI uri = new URI(node.getServiceUrl());
                    replicas.add(uri.getHost() + ":" + uri.getPort());
                } catch (URISyntaxException e) {
                    log.warn("Could not parse peer Eureka node URL: {}", e.getMessage());
                }
            }
        );
        return ResponseEntity.ok(replicas);
    }

    /**
     * GET  /eureka/status : get Eureka status
     */
    @GetMapping("/eureka/status")
    public ResponseEntity<EurekaVM> eurekaStatus() {
        EurekaVM eurekaVM = new EurekaVM();
        eurekaVM.setStatus(getEurekaStatus());
        return ResponseEntity.ok(eurekaVM);
    }

    private List<Map<String, Object>> getApplications() {
        List<Application> sortedApplications = getRegistry().getSortedApplications();
        List<Map<String, Object>> apps = new ArrayList<>();
        for (Application app : sortedApplications) {
            HashMap<String, Object> appData = new LinkedHashMap<>();
            apps.add(appData);
            appData.put("name", app.getName());
            List<Map<String, Object>> instances = new ArrayList<>();
            for (InstanceInfo info : app.getInstances()) {
                Map<String, Object> instance = new HashMap<>();
                instance.put("instanceId", info.getInstanceId());
                instance.put("homePageUrl", info.getHomePageUrl());
                instance.put("healthCheckUrl", info.getHealthCheckUrl());
                instance.put("statusPageUrl", info.getStatusPageUrl());
                instance.put("status", info.getStatus().name());
                instance.put("metadata", info.getMetadata());
                instances.add(instance);
            }
            appData.put("instances", instances);
        }
        return apps;
    }

    private Map<String, Object> getEurekaStatus() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("time", new Date());
        stats.put("currentTime", StatusResource.getCurrentTimeAsString());
        stats.put("upTime", StatusInfo.getUpTime());
        stats.put("environment", ConfigurationManager.getDeploymentContext()
            .getDeploymentEnvironment());
        stats.put("datacenter", ConfigurationManager.getDeploymentContext()
            .getDeploymentDatacenter());

        PeerAwareInstanceRegistry registry = getRegistry();

        stats.put("isBelowRenewThreshold", registry.isBelowRenewThresold() == 1);

        populateInstanceInfo(stats);

        return stats;
    }

    private void populateInstanceInfo(Map<String, Object> model) {
        StatusInfo statusInfo;
        try {
            statusInfo = new StatusResource().getStatusInfo();
        } catch (Exception e) {
            log.error(e.getMessage());
            statusInfo = StatusInfo.Builder.newBuilder().isHealthy(false).build();
        }
        if (ObjectUtils.allNotNull(statusInfo, statusInfo.getGeneralStats())) {
            model.put("generalStats", statusInfo.getGeneralStats());
        }
        if (ObjectUtils.allNotNull(statusInfo, statusInfo.getInstanceInfo())) {
            InstanceInfo instanceInfo = statusInfo.getInstanceInfo();
            Map<String, String> instanceMap = new HashMap<>();
            instanceMap.put("ipAddr", instanceInfo.getIPAddr());
            instanceMap.put("status", instanceInfo.getStatus().toString());
            model.put("instanceInfo", instanceMap);
        }
    }


    private PeerAwareInstanceRegistry getRegistry() {
        return getServerContext().getRegistry();
    }

    private EurekaServerContext getServerContext() {
        return EurekaServerContextHolder.getInstance().getServerContext();
    }
}
