package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SchemaExportRequestTest {

    @Test
    void testSettersAndGetters() {
        SchemaExportRequest request = new SchemaExportRequest();

        UUID schmId = UUID.randomUUID();
        request.setSchmId(schmId);
        assertEquals(schmId, request.getSchmId());

        request.setName("TestSchema");
        assertEquals("TestSchema", request.getName());
    }

    @Test
    void testNullValues() {
        SchemaExportRequest request = new SchemaExportRequest();

        assertNull(request.getSchmId());
        assertNull(request.getName());
    }

    @Test
    void testWithSchmId() {
        SchemaExportRequest request = new SchemaExportRequest();
        UUID schmId = UUID.randomUUID();
        request.setSchmId(schmId);

        assertEquals(schmId, request.getSchmId());
        assertNull(request.getName());
    }

    @Test
    void testWithName() {
        SchemaExportRequest request = new SchemaExportRequest();
        request.setName("TestSchema");

        assertEquals("TestSchema", request.getName());
        assertNull(request.getSchmId());
    }

    @Test
    void testWithBoth() {
        SchemaExportRequest request = new SchemaExportRequest();
        UUID schmId = UUID.randomUUID();
        request.setSchmId(schmId);
        request.setName("TestSchema");

        assertEquals(schmId, request.getSchmId());
        assertEquals("TestSchema", request.getName());
    }
}
