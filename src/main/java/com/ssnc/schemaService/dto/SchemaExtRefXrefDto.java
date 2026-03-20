package com.ssnc.schemaService.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SchemaExtRefXrefDto {
    private UUID schemaId;
    private String schemaName;
    private UUID extRefId;
    private String extRefName;
    private String extRefType;
    private String createdByUser;
    private LocalDateTime createDateTime;
}
