package com.ssnc.schemaService.dto;

import com.ssnc.schemaService.validation.AtLeastOneNotNull;
import lombok.Data;
import java.util.UUID;

@Data
@AtLeastOneNotNull(fields = {"schmId", "name"}, message = "Either schmId or name must be provided")
public class SchemaExportRequest {
    private UUID schmId;
    private String name;
}
