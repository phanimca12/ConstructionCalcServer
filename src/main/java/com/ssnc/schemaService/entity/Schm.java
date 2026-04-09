package com.ssnc.schemaService.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "SCHM", schema = "SCHMDB")
@FilterDef(
        name = "namespaceFilter",
        parameters = @ParamDef(name = "namespaceId", type = UUID.class)
)
@Filter(
        name = "namespaceFilter",
        condition = "NMSPC_ID = :namespaceId"
)
@Data
public class Schm {

    @Id
    @GeneratedValue
    @Column(name = "SCHM_ID", nullable = false)
    private UUID schmId;

    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "NMSPC_ID", nullable = false)
    private UUID nmspcId;

    @Column(name = "SCHM_NAME", length = 256)
    private String schmName;

    @Column(name = "SCHM_DESC", length = 4000)
    private String schmDesc;

    @Column(name = "SCHM_TYPE", length = 64)
    private String schemaType;

    @Column(name = "CONTENT_TYPE", length = 128)
    private String contentType;

    @Column(name = "SCHM_GROUP", length = 256)
    private String schmGroup;

    @Column(name = "PUBLISH_VERSION")
    private Integer publishVersion;

    @Column(name = "LOCK_BY", length = 256)
    private String lockBy;

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

    /**
     * ORM mapping relationship - DO NOT ACCESS directly.
     * Accessing this field triggers lazy-loading and causes N+1 query problems.
     * Use nmspcId field instead for filtering/queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "NMSPC_ID", insertable = false, updatable = false)
    @ToString.Exclude
    private Nmspc nmspc;

    /**
     * ORM mapping relationship - DO NOT ACCESS directly.
     * Accessing this field triggers lazy-loading and causes N+1 query problems.
     * Fetch versions explicitly via SchmDataRepository when needed.
     */
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "SCHM_ID")
    @ToString.Exclude
    private List<SchmData> versions = new ArrayList<>();

}
