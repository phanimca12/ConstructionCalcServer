package com.ssnc.schemaService.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "SCHM_XREF", schema = "SCHMDB")
@Data
public class SchmXref {

    @Id
    @GeneratedValue
    @Column(name = "XREF_ID", nullable = false)
    private UUID xrefId;

    @Column(name = "SCHM_ID", nullable = false)
    private UUID schmId;

    @Column(name = "SCHM_NAME", length = 256)
    private String schmName;

    @Column(name = "SCHM_TYPE", length = 64)
    private String schmType;

    @Column(name = "NMSPC_NAME", length = 32)
    private String nmspName;

    @Enumerated(EnumType.STRING)
    @Column(name = "REF_TYPE", length = 64)
    private XRefType refType;

    @Column(name = "REF_VERSION", length = 64)
    private String refVersion;

    @Column(name = "REF_NAME", length = 256)
    private String refName;

    @Column(name = "REF_GUID")
    private UUID refGuid;

    @Column(name = "REF_GUID_CHAR", length = 256)
    private String refGuidChar;

    @CreationTimestamp
    @Column(name = "CREATED_DATETIME", updatable = false)
    private LocalDateTime createdDatetime;

    @UpdateTimestamp
    @Column(name = "UPDATED_DATETIME")
    private LocalDateTime updatedDatetime;

    @Column(name = "CREATED_BY", length = 256)
    private String createdBy;

    @Column(name = "UPDATED_BY", length = 256)
    private String updatedBy;
}