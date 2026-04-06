package com.ssnc.schemaService.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
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

    @Column(name = "CREATED_BY", length = 256)
    private String createdBy;

    @CreationTimestamp
    @Column(name = "CREATED_DATETIME", updatable = false)
    private LocalDateTime createdDatetime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TENANT_ID", insertable = false, updatable = false)
    private Tenant tenant;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SCHM_ID", insertable = false, updatable = false)
    private Schm schm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EXT_REF_ID", insertable = false, updatable = false)
    private ExtRef extRef;
}
