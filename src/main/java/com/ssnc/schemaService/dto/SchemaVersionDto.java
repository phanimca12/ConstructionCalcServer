package com.ssnc.schemaService.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SchemaVersionDto {
    private Integer versionNumber;
    private String content;
    private Boolean isDraft;
    private String createdByUser;
    private LocalDateTime createDateTime;
    private String modifedByUser;
    private LocalDateTime modifiedDateTime;
}
