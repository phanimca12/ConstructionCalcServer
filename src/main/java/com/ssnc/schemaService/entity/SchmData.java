package com.ssnc.schemaService.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SCHM_DATA", schema = "SCHMDB")
public class SchmData {

    @EmbeddedId
    private SchmDataId id;

    @Lob
    @Column(name = "SCHM_DATA")
    private String schmData;

    @Column(name = "SCHM_VERSION_NAME", length = 64)
    private String schmVersionName;

    @Column(name = "CREATED_DATETIME")
    private LocalDateTime createdDatetime;

    @Column(name = "CREATED_BY", length = 256)
    private String createdBy;

    @Column(name = "UPDATED_DATETIME")
    private LocalDateTime updatedDatetime;

    @Column(name = "UPDATED_BY", length = 256)
    private String updatedBy;

    // getters & setters


    public SchmDataId getId() {
        return id;
    }

    public void setId(SchmDataId id) {
        this.id = id;
    }

    public String getSchmData() {
        return schmData;
    }

    public void setSchmData(String schmData) {
        this.schmData = schmData;
    }

    public String getSchmVersionName() {
        return schmVersionName;
    }

    public void setSchmVersionName(String schmVersionName) {
        this.schmVersionName = schmVersionName;
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
}
