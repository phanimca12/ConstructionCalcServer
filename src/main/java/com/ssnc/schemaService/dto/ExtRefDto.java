package com.ssnc.schemaService.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ExtRefDto {
    private UUID extRefId;
    private String extRefName;
    private String extRefType;
    private String extRefVersion;
    private String createdByUser;
    private LocalDateTime createDateTime;
    private String modifiedByUser;
    private LocalDateTime modifiedDateTime;
}
