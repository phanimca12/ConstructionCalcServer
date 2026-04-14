package com.ssnc.schemaService.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SchemaExportDto {
    private String name;
    private String description;
    private String schemaType;
    private String contentType;

    @JsonProperty("group")
    private String schmGroup;

    private String content;
}
