package com.ssnc.schemaService.dto;

import lombok.Data;

import java.util.List;

@Data
public class SchemaWithVersionDto {
    private SchemaDto schema;
    private List<SchemaVersionDto> versions;
}
