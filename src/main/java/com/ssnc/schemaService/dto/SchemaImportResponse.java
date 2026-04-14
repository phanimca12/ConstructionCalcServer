package com.ssnc.schemaService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaImportResponse {
    private boolean success;
    private String schemaName;
    private SchemaDto schema;
    private String errorMessage;

    // Success constructor
    public SchemaImportResponse(SchemaDto schema) {
        this.success = true;
        this.schemaName = schema.getName();
        this.schema = schema;
        this.errorMessage = null;
    }

    // Error constructor
    public SchemaImportResponse(String schemaName, String errorMessage) {
        this.success = false;
        this.schemaName = schemaName;
        this.schema = null;
        this.errorMessage = errorMessage;
    }
}
