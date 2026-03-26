package com.ssnc.schemaService.dto;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ExtRefWithSchemasRequest {
    private List<SchemaReference> schemas;

    @Data
    public static class SchemaReference {
        private UUID schmId;
        private String schmName;
    }
}
