package com.ssnc.schemaService.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtRefTest {

    private ExtRef extRef;

    @BeforeEach
    void setUp() {
        extRef = new ExtRef();
    }

    @Test
    void testSetExtRefId_StoresAsProvided() {
        extRef.setExtRefId("abc123");
        assertEquals("abc123", extRef.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithGuid_StoresAsProvided() {
        String guid = "550e8400-e29b-41d4-a716-446655440000";
        extRef.setExtRefId(guid);
        assertEquals("550e8400-e29b-41d4-a716-446655440000", extRef.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithIntegerId_StoresAsProvided() {
        extRef.setExtRefId("1234567890");
        assertEquals("1234567890", extRef.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithMixedCase_StoresAsProvided() {
        extRef.setExtRefId("AbC-DeF-123");
        assertEquals("AbC-DeF-123", extRef.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithNull_StoresNull() {
        extRef.setExtRefId(null);
        assertNull(extRef.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithUppercase_StoresAsProvided() {
        extRef.setExtRefId("ALREADY-UPPERCASE-123");
        assertEquals("ALREADY-UPPERCASE-123", extRef.getExtRefId());
    }
}
