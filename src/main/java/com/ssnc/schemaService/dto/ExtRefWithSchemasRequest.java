package com.ssnc.schemaService.dto;

import com.ssnc.schemaService.constants.ErrorMessages;
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
    @NotNull(message = ErrorMessages.VALIDATION_SCHEMAS_FIELD_REQUIRED)
    @Valid
    private List<SchemaReference> schemas;

    @Data
    public static class SchemaReference {
        /**
         * Schema ID is required for each schema reference
         */
        @NotNull(message = ErrorMessages.VALIDATION_SCHM_ID_REQUIRED)
        private UUID schmId;

        private String schmName;
    }
}
