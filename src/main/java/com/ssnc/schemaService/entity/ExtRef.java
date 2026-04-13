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

    @EmbeddedId
    private ExtRefId id;

    @Column(name = "TENANT_ID", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "EXT_REF_NAME", length = 256)
    private String extRefName;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "EXT_REF_TYPE", nullable = false)
    private ExtRefType extRefType;

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

    // Convenience methods for accessing composite key fields
    public String getExtRefId() {
        return id != null ? id.getExtRefId() : null;
    }

    public String getExtRefVersion() {
        return id != null ? id.getExtRefVersion() : null;
    }

    /**
     * Set the composite key. BOTH extRefId and extRefVersion are required.
     * Do not use individual setters - they can create partial keys that violate NOT NULL constraints.
     *
     * @param extRefId - The external reference ID (required, NOT NULL)
     * @param extRefVersion - The version (required, NOT NULL)
     */
    public void setId(String extRefId, String extRefVersion) {
        if (extRefId == null || extRefVersion == null) {
            throw new IllegalArgumentException("Both extRefId and extRefVersion are required (NOT NULL)");
        }
        this.id = new ExtRefId(extRefId, extRefVersion);
    }
}
