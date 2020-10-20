package com.polykhel.ssa.core.dto;

import lombok.Data;

import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@MappedSuperclass
public abstract class BaseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String createdBy;
    private LocalDate createdDate;
    private String updatedBy;
    private LocalDate updatedDate;

}
