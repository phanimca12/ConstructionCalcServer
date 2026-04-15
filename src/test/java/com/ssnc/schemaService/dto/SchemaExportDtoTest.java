package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchemaExportDtoTest {

    @Test
    void testSettersAndGetters() {
        SchemaExportDto dto = new SchemaExportDto();

        dto.setName("TestSchema");
        assertEquals("TestSchema", dto.getName());

        dto.setDescription("Test Description");
        assertEquals("Test Description", dto.getDescription());

        dto.setSchemaType("JSON");
        assertEquals("JSON", dto.getSchemaType());

        dto.setContentType("application/json");
        assertEquals("application/json", dto.getContentType());

        dto.setSchmGroup("TestGroup");
        assertEquals("TestGroup", dto.getSchmGroup());

        dto.setContent("schema content here");
        assertEquals("schema content here", dto.getContent());
    }

    @Test
    void testNullValues() {
        SchemaExportDto dto = new SchemaExportDto();

        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getSchemaType());
        assertNull(dto.getContentType());
        assertNull(dto.getSchmGroup());
        assertNull(dto.getContent());
    }

    @Test
    void testFullSchema() {
        SchemaExportDto dto = new SchemaExportDto();

        dto.setName("CompleteSchema");
        dto.setDescription("Complete schema for export");
        dto.setSchemaType("JSON");
        dto.setContentType("application/json");
        dto.setSchmGroup("Production");
        dto.setContent("{\"type\": \"object\"}");

        assertEquals("CompleteSchema", dto.getName());
        assertEquals("Complete schema for export", dto.getDescription());
        assertEquals("JSON", dto.getSchemaType());
        assertEquals("application/json", dto.getContentType());
        assertEquals("Production", dto.getSchmGroup());
        assertEquals("{\"type\": \"object\"}", dto.getContent());
    }
}
