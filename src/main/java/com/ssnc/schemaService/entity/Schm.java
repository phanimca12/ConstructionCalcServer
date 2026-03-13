package com.ssnc.schemaService.entity;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "SCHM", schema = "SCHMDB")
@FilterDef(
        name = "namespaceFilter",
        parameters = @ParamDef(name = "namespace", type = String.class)
)
@Filter(
        name = "namespaceFilter",
        condition = "NMSPC_NAME = :namespace"
)
@Data
public class Schm {

    @Id
    @GeneratedValue
    @Column(name = "SCHM_ID", nullable = false)
    private UUID schmId;

    /**
     * Hibernate 6 tenant discriminator column
     */
    @TenantId
    @Column(name = "TENANT_NAME", nullable = false, updatable = false)
    private String tenantName;

    @Column(name = "NMSPC_NAME", nullable = false)
    private String namespace;

    @Column(name = "SCHM_NAME", length = 256)
    private String schmName;

    @Column(name = "SCHM_DESC", length = 4000)
    private String schmDesc;

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

    @Column(name = "LOCK_BY", length = 256)
    private String lockBy;

    @Column(name = "\"GROUP\"", length = 256)
    private String group;

    @Column(name = "SCHM_TYPE", length = 64)
    private String schemaType;

    @Column(name = "CONTENT_TYPE", length = 128)
    private String contentType;

    @Column(name = "PUBLISH_VERSION")
    private Integer publishVersion;

    @OneToMany( fetch = FetchType.LAZY , cascade = CascadeType.PERSIST)
     @JoinColumn(name = "SCHM_ID")
    private List<SchmData> versions = new ArrayList<>();

}
