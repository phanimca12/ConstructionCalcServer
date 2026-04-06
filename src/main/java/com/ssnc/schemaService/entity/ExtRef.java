package com.ssnc.schemaService.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "EXT_REF", schema = "SCHMDB")
@Getter
@Setter
public class ExtRef {

    @Id
    @Column(name = "EXT_REF_ID", nullable = false, length = 64)
    private String extRefId;

    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "EXT_REF_NAME", length = 256)
    private String extRefName;

    @Column(name = "EXT_REF_TYPE", length = 64)
    private String extRefType;

    @Column(name = "EXT_REF_VERSION", length = 64)
    private String extRefVersion;

    @Column(name = "CREATED_BY", length = 256)
    private String createdBy;

    @Column(name = "UPDATED_BY", length = 256)
    private String updatedBy;

    @CreationTimestamp
    @Column(name = "CREATED_DATETIME", updatable = false)
    private LocalDateTime createdDatetime;

    @UpdateTimestamp
    @Column(name = "UPDATED_DATETIME")
    private LocalDateTime updatedDatetime;

    /**
     * ORM mapping relationship - DO NOT ACCESS directly.
     * Accessing this field triggers lazy-loading and causes N+1 query problems.
     * Use tenantId field instead for filtering/queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TENANT_ID", insertable = false, updatable = false)
    @ToString.Exclude
    private Tenant tenant;
}
