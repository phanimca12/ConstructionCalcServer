package com.ssnc.schemaService.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SchmExtRefXrefTest {

    private SchmExtRefXref xref;

    @BeforeEach
    void setUp() {
        xref = new SchmExtRefXref();
    }

    @Test
    void testSetExtRefId_ConvertsToUppercase() {
        xref.setExtRefId("xyz789");
        assertEquals("XYZ789", xref.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithGuid_ConvertsToUppercase() {
        String guid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
        xref.setExtRefId(guid);
        assertEquals("A1B2C3D4-E5F6-7890-ABCD-EF1234567890", xref.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithIntegerId_ConvertsToUppercase() {
        xref.setExtRefId("9876543210");
        assertEquals("9876543210", xref.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithNull_StoresNull() {
        xref.setExtRefId(null);
        assertNull(xref.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithMixedCase_ConvertsToUppercase() {
        xref.setExtRefId("MiXeD-CaSe-456");
        assertEquals("MIXED-CASE-456", xref.getExtRefId());
    }
}
