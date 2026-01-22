package com.ssnc.schemaService.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.TenantId;

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

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

    @Column(name = "CREATED_BY", length = 256)
    private String createdBy;

    @Column(name = "UPDATED_DATETIME")
    private LocalDateTime updatedDatetime;

    @Column(name = "UPDATED_BY", length = 256)
    private String updatedBy;

    @Column(name = "LOCK_BY", length = 256)
    private String lockBy;

    @Column(name = "\"GROUP\"", length = 256)
    private String group;

    @Column(name = "PUBLISH_VERSION")
    private Integer publishVersion;

    @OneToMany(
        fetch = FetchType.LAZY
    )
    @JoinColumn(name = "SCHM_ID")
    private List<SchmData> versions = new ArrayList<>();

    // getters & setters

    public UUID getSchmId() {
        return schmId;
    }

    public void setSchmId(UUID schmId) {
        this.schmId = schmId;
    }

    public String getTenantName() {
        return tenantName;
    }

    public void setTenantName(String tenantName) {
        this.tenantName = tenantName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getSchmName() {
        return schmName;
    }

    public void setSchmName(String schmName) {
        this.schmName = schmName;
    }

    public String getSchmDesc() {
        return schmDesc;
    }

    public void setSchmDesc(String schmDesc) {
        this.schmDesc = schmDesc;
    }

    public LocalDateTime getCreatedDatetime() {
        return createdDatetime;
    }

    public void setCreatedDatetime(LocalDateTime createdDatetime) {
        this.createdDatetime = createdDatetime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedDatetime() {
        return updatedDatetime;
    }

    public void setUpdatedDatetime(LocalDateTime updatedDatetime) {
        this.updatedDatetime = updatedDatetime;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getLockBy() {
        return lockBy;
    }

    public void setLockBy(String lockBy) {
        this.lockBy = lockBy;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public Integer getPublishVersion() {
        return publishVersion;
    }

    public void setPublishVersion(Integer publishVersion) {
        this.publishVersion = publishVersion;
    }

    public List<SchmData> getVersions() {
        return versions;
    }

    public void setVersions(List<SchmData> versions) {
        this.versions = versions;
    }
}
