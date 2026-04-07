package com.ssnc.schemaService.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ExtRefDto {
    private String extRefId;
    private UUID tenantId;
    private String extRefName;
    private String extRefType;
    private String extRefVersion;
    private LocalDateTime createdDatetime;
    private LocalDateTime updatedDatetime;
    private String createdBy;
    private String updatedBy;
}
