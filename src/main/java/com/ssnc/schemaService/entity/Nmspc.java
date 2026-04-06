package com.ssnc.schemaService.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "NMSPC", schema = "SCHMDB")
@Data
public class Nmspc {

    @Id
    @GeneratedValue
    @Column(name = "NMSPC_ID", nullable = false)
    private UUID nmspcId;

    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "NMSPC_NAME", length = 32, nullable = false)
    private String nmspcName;

    @Column(name = "NMSPC_DESC", length = 4000)
    private String description;

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
