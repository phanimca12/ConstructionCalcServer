package com.ssnc.schemaService.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class NameSpaceDto {
    private UUID nmspcId;
    private UUID tenantId;
    private String name;
    private String description;
    private String createdByUser;
    private LocalDateTime createDateTime;
    private String modifiedByUser;
    private LocalDateTime modifiedDateTime;
}
