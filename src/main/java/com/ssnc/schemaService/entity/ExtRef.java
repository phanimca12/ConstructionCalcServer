package com.ssnc.schemaService.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.TenantId;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "EXT_REF", schema = "SCHMDB")
@Data
public class ExtRef {

    @Id
    @GeneratedValue
    @Column(name = "EXT_REF_ID", nullable = false)
    private UUID extRefId;

    /**
     * Hibernate 6 tenant discriminator column
     */
    @TenantId
    @Column(name = "TENANT_NAME", nullable = false, updatable = false)
    private String tenantName;

    @Column(name = "EXT_REF_NAME", length = 256)
    private String extRefName;

    @Column(name = "EXT_REF_TYPE", length = 64)
    private String extRefType;

    @Column(name = "EXT_REF_VERSION", length = 64)
    private String extRefVersion;

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
