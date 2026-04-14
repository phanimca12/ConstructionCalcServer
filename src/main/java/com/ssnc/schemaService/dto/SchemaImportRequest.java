package com.ssnc.schemaService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SchemaImportRequest {
    @NotNull(message = "Schema information is required")
    @Valid
    private SchemaDto schema;

    @NotNull(message = "Content is required")
    private String content;
}
