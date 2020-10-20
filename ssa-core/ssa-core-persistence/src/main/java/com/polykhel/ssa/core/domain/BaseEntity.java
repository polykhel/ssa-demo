package com.polykhel.ssa.core.domain;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    @CreatedBy
    @Field("created_by")
    private String createdBy;

    @CreatedDate
    @Field("created_date")
    private LocalDate createdDate;

    @LastModifiedBy
    @Field("updated_by")
    private String updatedBy;

    @LastModifiedDate
    @Field("updated_date")
    private LocalDate updatedDate;
}
