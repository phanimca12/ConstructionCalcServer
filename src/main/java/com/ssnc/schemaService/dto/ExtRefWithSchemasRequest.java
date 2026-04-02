package com.ssnc.schemaService.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ExtRefWithSchemasRequest {
    /**
     * List of schema references. Can be empty but not null.
     * Validation is performed at service layer.
     */
    private List<SchemaReference> schemas;

    @Data
    public static class SchemaReference {
        /**
         * Schema ID. Validation is performed at service layer.
         */
        private UUID schmId;

        private String schmName;
    }
}
