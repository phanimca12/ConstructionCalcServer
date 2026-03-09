package com.ssnc.schemaService.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NameSpaceDto {
    private String id;
    private String name;
    private String description;
    private String createdByUser;
    private LocalDateTime createDateTime;
    private String modifedByUser;
    private LocalDateTime modifiedDateTime;
}
