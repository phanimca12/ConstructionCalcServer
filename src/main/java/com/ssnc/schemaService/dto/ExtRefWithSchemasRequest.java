package com.ssnc.schemaService.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ExtRefWithSchemasRequest {
    /**
     * List of schema references. Can be empty but not null.
     */
    @NotNull(message = "schemas field is required")
    @Valid
    private List<SchemaReference> schemas;

    @Data
    public static class SchemaReference {
        /**
         * Schema ID is required for each schema reference
         */
        @NotNull(message = "schmId is required for each schema reference")
        private UUID schmId;

        private String schmName;
    }
}
