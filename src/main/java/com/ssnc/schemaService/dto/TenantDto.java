package com.ssnc.schemaService.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TenantDto {
    private String name;
    private String createdByUser;
    private LocalDateTime createDateTime;
    private String modifiedByUser;
    private LocalDateTime modifiedDateTime;
}
