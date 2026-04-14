package com.ssnc.schemaService.dto;

import lombok.Data;

@Data
public class SchemaImportRequest {
    private SchemaDto schema;
    private String content;
}
