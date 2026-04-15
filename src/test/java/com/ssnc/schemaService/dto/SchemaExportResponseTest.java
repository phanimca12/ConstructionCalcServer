package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchemaExportResponseTest {

    @Test
    void testSuccessConstructor() {
        SchemaExportDto exportDto = new SchemaExportDto();
        exportDto.setName("TestSchema");

        SchemaExportResponse response = new SchemaExportResponse(exportDto);

        assertTrue(response.isSuccess());
        assertEquals("TestSchema", response.getSchemaName());
        assertEquals(exportDto, response.getSchema());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testErrorConstructor() {
        String schemaName = "TestSchema";
        String errorMessage = "Export failed";

        SchemaExportResponse response = new SchemaExportResponse(schemaName, errorMessage);

        assertFalse(response.isSuccess());
        assertEquals(schemaName, response.getSchemaName());
        assertNull(response.getSchema());
        assertEquals(errorMessage, response.getErrorMessage());
    }

    @Test
    void testDefaultConstructor() {
        SchemaExportResponse response = new SchemaExportResponse();

        assertFalse(response.isSuccess());
        assertNull(response.getSchemaName());
        assertNull(response.getSchema());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testAllArgsConstructor() {
        SchemaExportDto exportDto = new SchemaExportDto();
        exportDto.setName("TestSchema");

        SchemaExportResponse response = new SchemaExportResponse(true, "TestSchema", exportDto, null);

        assertTrue(response.isSuccess());
        assertEquals("TestSchema", response.getSchemaName());
        assertEquals(exportDto, response.getSchema());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testSettersAndGetters() {
        SchemaExportResponse response = new SchemaExportResponse();

        response.setSuccess(true);
        assertTrue(response.isSuccess());

        response.setSchemaName("TestSchema");
        assertEquals("TestSchema", response.getSchemaName());

        SchemaExportDto exportDto = new SchemaExportDto();
        response.setSchema(exportDto);
        assertEquals(exportDto, response.getSchema());

        response.setErrorMessage("Test error");
        assertEquals("Test error", response.getErrorMessage());
    }

    @Test
    void testSuccessScenario() {
        SchemaExportDto exportDto = new SchemaExportDto();
        exportDto.setName("SuccessfulSchema");
        exportDto.setContent("schema content");

        SchemaExportResponse response = new SchemaExportResponse(exportDto);

        assertTrue(response.isSuccess());
        assertNotNull(response.getSchema());
        assertNotNull(response.getSchemaName());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testErrorScenario() {
        SchemaExportResponse response = new SchemaExportResponse("FailedSchema", "Schema not found");

        assertFalse(response.isSuccess());
        assertEquals("FailedSchema", response.getSchemaName());
        assertNull(response.getSchema());
        assertEquals("Schema not found", response.getErrorMessage());
    }
}
