package com.polykhel.ssa.web.rest.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.Map;

/**
 * View Model for sending a parameterized error message.
 */
@AllArgsConstructor
@Getter
public class ParameterizedErrorVM implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String message;
    private final Map<String, String> paramMap;

}
