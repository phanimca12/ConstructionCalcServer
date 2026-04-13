package com.ssnc.schemaService.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "SCHM_EXT_REF_XREF", schema = "SCHMDB")
@Getter
@Setter
public class SchmExtRefXref {

    @Id
    @GeneratedValue
    @Column(name = "XREF_ID", nullable = false)
    private UUID xrefId;

    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "SCHM_ID", nullable = false)
    private UUID schmId;

    @Column(name = "EXT_REF_ID", nullable = false, length = 64)
    private String extRefId;

    @Column(name = "EXT_REF_VERSION", nullable = false, length = 6)
    private String extRefVersion;

    @Column(name = "CREATED_BY", length = 256)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "CREATED_DATETIME", updatable = false)
    private LocalDateTime createdDatetime;

    /**
     * ORM mapping relationship - DO NOT ACCESS directly.
     * Accessing this field triggers lazy-loading and causes N+1 query problems.
     * Use tenantId field instead for filtering/queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TENANT_ID", insertable = false, updatable = false)
    @ToString.Exclude
    private Tenant tenant;

    /**
     * ORM mapping relationship - DO NOT ACCESS directly.
     * Accessing this field triggers lazy-loading and causes N+1 query problems.
     * Use schmId field instead for filtering/queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHM_ID", insertable = false, updatable = false)
    @ToString.Exclude
    private Schm schm;

    /**
     * ORM mapping relationship - DO NOT ACCESS directly.
     * Accessing this field triggers lazy-loading and causes N+1 query problems.
     * Use extRefId and extRefVersion fields instead for filtering/queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "EXT_REF_ID", referencedColumnName = "EXT_REF_ID", insertable = false, updatable = false),
        @JoinColumn(name = "EXT_REF_VERSION", referencedColumnName = "EXT_REF_VERSION", insertable = false, updatable = false)
    })
    @ToString.Exclude
    private ExtRef extRef;
}
