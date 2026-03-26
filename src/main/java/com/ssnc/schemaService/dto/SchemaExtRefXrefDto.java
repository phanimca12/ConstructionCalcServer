package com.ssnc.schemaService.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SchemaExtRefXrefDto {
    private UUID xrefId;
    private String tenantName;
    private UUID schmId;
    private UUID extRefId;
    private LocalDateTime createdDatetime;
    private String createdBy;

    // Optional nested objects for convenience
    private String schmName;
    private String extRefName;
    private String extRefType;
    private String extRefVersion;
}
