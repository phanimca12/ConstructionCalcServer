package com.ssnc.schemaService.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "SCHM_EXT_REF_XREF", schema = "SCHMDB")
@Data
public class SchmExtRefXref {

    @Id
    @GeneratedValue
    @Column(name = "XREF_ID", nullable = false)
    private UUID xrefId;

    @Column(name = "SCHM_ID", nullable = false)
    private UUID schmId;

    @Column(name = "SCHM_NAME", length = 256)
    private String schmName;

    @Column(name = "EXT_REF_ID", nullable = false)
    private UUID extRefId;

    @Column(name = "EXT_REF_NAME", length = 256)
    private String extRefName;

    @Column(name = "EXT_REF_TYPE", length = 64)
    private String extRefType;

    @CreationTimestamp
    @Column(name = "CREATED_DATETIME", updatable = false)
    private LocalDateTime createdDatetime;

    @Column(name = "CREATED_BY", length = 256)
    private String createdBy;
}
