package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchemaImportResponseTest {

    @Test
    void testSuccessConstructor() {
        SchemaDto schema = new SchemaDto();
        schema.setName("TestSchema");

        SchemaImportResponse response = new SchemaImportResponse(schema);

        assertTrue(response.isSuccess());
        assertEquals("TestSchema", response.getSchemaName());
        assertEquals(schema, response.getSchema());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testErrorConstructor() {
        String schemaName = "TestSchema";
        String errorMessage = "Import failed";

        SchemaImportResponse response = new SchemaImportResponse(schemaName, errorMessage);

        assertFalse(response.isSuccess());
        assertEquals(schemaName, response.getSchemaName());
        assertNull(response.getSchema());
        assertEquals(errorMessage, response.getErrorMessage());
    }

    @Test
    void testDefaultConstructor() {
        SchemaImportResponse response = new SchemaImportResponse();

        assertFalse(response.isSuccess());
        assertNull(response.getSchemaName());
        assertNull(response.getSchema());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testAllArgsConstructor() {
        SchemaDto schema = new SchemaDto();
        schema.setName("TestSchema");

        SchemaImportResponse response = new SchemaImportResponse(true, "TestSchema", schema, null);

        assertTrue(response.isSuccess());
        assertEquals("TestSchema", response.getSchemaName());
        assertEquals(schema, response.getSchema());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testSettersAndGetters() {
        SchemaImportResponse response = new SchemaImportResponse();

        response.setSuccess(true);
        assertTrue(response.isSuccess());

        response.setSchemaName("TestSchema");
        assertEquals("TestSchema", response.getSchemaName());

        SchemaDto schema = new SchemaDto();
        response.setSchema(schema);
        assertEquals(schema, response.getSchema());

        response.setErrorMessage("Test error");
        assertEquals("Test error", response.getErrorMessage());
    }

    @Test
    void testSuccessScenario() {
        SchemaDto schema = new SchemaDto();
        schema.setName("SuccessfulSchema");

        SchemaImportResponse response = new SchemaImportResponse(schema);

        assertTrue(response.isSuccess());
        assertNotNull(response.getSchema());
        assertNotNull(response.getSchemaName());
        assertNull(response.getErrorMessage());
    }

    @Test
    void testErrorScenario() {
        SchemaImportResponse response = new SchemaImportResponse("FailedSchema", "Validation error");

        assertFalse(response.isSuccess());
        assertEquals("FailedSchema", response.getSchemaName());
        assertNull(response.getSchema());
        assertEquals("Validation error", response.getErrorMessage());
    }
}
