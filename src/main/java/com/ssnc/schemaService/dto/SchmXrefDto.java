package com.ssnc.schemaService.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SchmXrefDto {
    private UUID xrefId;
    private UUID schmId;
    private String schmName;
    private String schmType;
    private String nmspName;
    private String refType;
    private String refVersion;
    private String refName;
    private UUID refGuid;
    private String refGuidChar;
    private String createdBy;
    private LocalDateTime createdDatetime;
    private String updatedBy;
    private LocalDateTime updatedDatetime;
}