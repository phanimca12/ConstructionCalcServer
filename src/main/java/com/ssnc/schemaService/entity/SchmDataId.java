package com.ssnc.schemaService.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
public class SchmDataId implements Serializable {

    @Column(name = "SCHM_ID", nullable = false)
    private UUID schmId;

    @Column(name = "SCHM_VERSION", nullable = false)
    private Integer schmVersion;

    // equals & hashCode (mandatory)


    public UUID getSchmId() {
        return schmId;
    }

    public void setSchmId(UUID schmId) {
        this.schmId = schmId;
    }

    public Integer getSchmVersion() {
        return schmVersion;
    }

    public void setSchmVersion(Integer schmVersion) {
        this.schmVersion = schmVersion;
    }
}
