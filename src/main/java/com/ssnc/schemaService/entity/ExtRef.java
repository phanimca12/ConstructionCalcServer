package com.ssnc.schemaService.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
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
    @Setter(AccessLevel.NONE)  // Prevent Lombok from generating setter - we have a custom one
    private String extRefId;

    /**
     * Custom setter to ensure ext_ref_id is always stored in uppercase.
     * SECURITY: This setter is critical for data integrity with the database CHECK constraint.
     */
    public void setExtRefId(String extRefId) {
        this.extRefId = (extRefId != null) ? extRefId.toUpperCase() : null;
    }

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TENANT_ID", insertable = false, updatable = false)
    private Tenant tenant;
}
