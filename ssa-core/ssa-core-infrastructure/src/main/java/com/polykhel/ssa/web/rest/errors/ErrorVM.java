package com.polykhel.ssa.web.rest.errors;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * View Model for transferring error message with a list of field errors.
 */
@AllArgsConstructor
@Getter
public class ErrorVM implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String message;
    private final String description;

    private List<FieldErrorVM> fieldErrors;

    public ErrorVM(String message) {
        this(message, null);
    }

    public ErrorVM(String message, String description) {
        this.message = message;
        this.description = description;
    }

    public void add(String objectName, String field, String message) {
        if (fieldErrors == null) {
            fieldErrors = new ArrayList<>();
        }
        fieldErrors.add(new FieldErrorVM(objectName, field, message));
    }
}
