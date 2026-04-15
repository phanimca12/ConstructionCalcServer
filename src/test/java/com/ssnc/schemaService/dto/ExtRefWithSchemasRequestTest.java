package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExtRefWithSchemasRequestTest {

    @Test
    void testSettersAndGetters() {
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();

        List<ExtRefWithSchemasRequest.SchemaReference> schemas = new ArrayList<>();
        request.setSchemas(schemas);
        assertEquals(schemas, request.getSchemas());
    }

    @Test
    void testNullValues() {
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();

        assertNull(request.getSchemas());
    }

    @Test
    void testWithSchemas() {
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();

        List<ExtRefWithSchemasRequest.SchemaReference> schemas = new ArrayList<>();

        ExtRefWithSchemasRequest.SchemaReference ref1 = new ExtRefWithSchemasRequest.SchemaReference();
        ref1.setSchmId(UUID.randomUUID());
        schemas.add(ref1);

        ExtRefWithSchemasRequest.SchemaReference ref2 = new ExtRefWithSchemasRequest.SchemaReference();
        ref2.setSchmId(UUID.randomUUID());
        schemas.add(ref2);

        request.setSchemas(schemas);

        assertEquals(2, request.getSchemas().size());
    }

    @Test
    void testSchemaReference() {
        ExtRefWithSchemasRequest.SchemaReference ref = new ExtRefWithSchemasRequest.SchemaReference();

        UUID schmId = UUID.randomUUID();
        ref.setSchmId(schmId);
        assertEquals(schmId, ref.getSchmId());
    }

    @Test
    void testSchemaReferenceNullValue() {
        ExtRefWithSchemasRequest.SchemaReference ref = new ExtRefWithSchemasRequest.SchemaReference();

        assertNull(ref.getSchmId());
    }

    @Test
    void testEmptySchemasList() {
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        request.setSchemas(new ArrayList<>());

        assertNotNull(request.getSchemas());
        assertTrue(request.getSchemas().isEmpty());
    }
}
