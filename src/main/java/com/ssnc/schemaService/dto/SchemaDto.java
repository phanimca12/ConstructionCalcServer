package com.ssnc.schemaService.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SchemaDto {
    private UUID id;
    private String name;
    private String description;
    private String schemaType;
    private String contentType;
    private String lockBy;
    private String schmGroup;

    private String published;
    private String draft;

    private String createdByUser;
    private LocalDateTime createDateTime;
    private String modifiedByUser;
    private LocalDateTime modifiedDateTime;
}
