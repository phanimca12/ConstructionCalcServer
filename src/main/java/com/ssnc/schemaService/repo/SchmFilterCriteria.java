package com.ssnc.schemaService.repo;

import java.time.LocalDateTime;
import java.util.UUID;

public class SchmFilterCriteria {

    private UUID schmId;
    private String schemaName;
    private String lockBy;
    private Integer publishVersion;
    // getters & setters


    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public Integer getPublishVersion() {
        return publishVersion;
    }

    public void setPublishVersion(Integer publishVersion) {
        this.publishVersion = publishVersion;
    }

    public UUID getSchmId() {
        return schmId;
    }

    public void setSchmId(UUID schmId) {
        this.schmId = schmId;
    }

    public String getLockBy() {
        return lockBy;
    }

    public void setLockBy(String lockBy) {
        this.lockBy = lockBy;
    }
}
