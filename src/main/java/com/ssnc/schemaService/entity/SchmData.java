package com.ssnc.schemaService.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SCHM_DATA", schema = "SCHMDB")
@Data
public class SchmData {

    @EmbeddedId
    private SchmDataId id;

    @Lob
    @Column(name = "SCHM_DATA")
    private String schmData;

    @Column(name = "SCHM_VERSION_NAME", length = 64)
    private String schmVersionName;

    @Column(name = "IS_DRAFT", length = 1)
    private Boolean isDraft;

    @CreationTimestamp
    @Column(name = "CREATED_DATETIME", updatable = false)
    private LocalDateTime createdDatetime;

    @Column(name = "CREATED_BY", length = 256)
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "UPDATED_DATETIME")
    private LocalDateTime updatedDatetime;

    @Column(name = "UPDATED_BY", length = 256)
    private String updatedBy;

}
