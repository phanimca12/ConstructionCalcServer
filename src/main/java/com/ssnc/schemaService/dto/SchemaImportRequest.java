package com.ssnc.schemaService.dto;

import com.ssnc.schemaService.constants.ErrorMessages;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SchemaImportRequest {
    @NotNull(message = "Schema information is required")
    @Valid
    private SchemaDto schema;

    @NotBlank(message = "Content is required for schema import")
    private String content;
}
