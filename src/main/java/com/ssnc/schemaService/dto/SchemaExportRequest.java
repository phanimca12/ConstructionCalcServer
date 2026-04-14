package com.ssnc.schemaService.dto;

import lombok.Data;
import java.util.UUID;

@Data
public class SchemaExportRequest {
    private UUID schmId;
    private String name;
}
