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
    void testSetExtRefId_ConvertsToUppercase() {
        extRef.setExtRefId("abc123");
        assertEquals("ABC123", extRef.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithGuid_ConvertsToUppercase() {
        String guid = "550e8400-e29b-41d4-a716-446655440000";
        extRef.setExtRefId(guid);
        assertEquals("550E8400-E29B-41D4-A716-446655440000", extRef.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithIntegerId_ConvertsToUppercase() {
        extRef.setExtRefId("1234567890");
        assertEquals("1234567890", extRef.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithMixedCase_ConvertsToUppercase() {
        extRef.setExtRefId("AbC-DeF-123");
        assertEquals("ABC-DEF-123", extRef.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithNull_StoresNull() {
        extRef.setExtRefId(null);
        assertNull(extRef.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithAlreadyUppercase_RemainsUnchanged() {
        extRef.setExtRefId("ALREADY-UPPERCASE-123");
        assertEquals("ALREADY-UPPERCASE-123", extRef.getExtRefId());
    }
}
