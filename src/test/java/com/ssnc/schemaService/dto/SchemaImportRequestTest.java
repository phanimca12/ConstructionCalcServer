package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchemaImportRequestTest {

    @Test
    void testSettersAndGetters() {
        SchemaImportRequest request = new SchemaImportRequest();

        SchemaDto schema = new SchemaDto();
        schema.setName("TestSchema");
        request.setSchema(schema);
        assertEquals(schema, request.getSchema());

        request.setContent("schema content");
        assertEquals("schema content", request.getContent());
    }

    @Test
    void testNullValues() {
        SchemaImportRequest request = new SchemaImportRequest();

        assertNull(request.getSchema());
        assertNull(request.getContent());
    }

    @Test
    void testWithContent() {
        SchemaImportRequest request = new SchemaImportRequest();
        SchemaDto schema = new SchemaDto();
        schema.setName("TestSchema");

        request.setSchema(schema);
        request.setContent("JSON content here");

        assertNotNull(request.getSchema());
        assertNotNull(request.getContent());
        assertEquals("TestSchema", request.getSchema().getName());
        assertEquals("JSON content here", request.getContent());
    }
}
