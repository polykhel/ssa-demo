package com.polykhel.ssa.web.rest.vm;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * View Model object for representing Eureka applications list.
 */
@Getter
@Setter
public class EurekaVM {

    private List<Map<String, Object>> applications;

    private Map<String, Object> status;

}
