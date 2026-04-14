package com.ssnc.schemaService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchemaExportResponse {
    private boolean success;
    private String schemaName;
    private SchemaExportDto schema;
    private String errorMessage;

    // Success constructor
    public SchemaExportResponse(SchemaExportDto schema) {
        this.success = true;
        this.schemaName = schema.getName();
        this.schema = schema;
        this.errorMessage = null;
    }

    // Error constructor
    public SchemaExportResponse(String schemaName, String errorMessage) {
        this.success = false;
        this.schemaName = schemaName;
        this.schema = null;
        this.errorMessage = errorMessage;
    }
}
