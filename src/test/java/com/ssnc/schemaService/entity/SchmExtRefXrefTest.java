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
    void testSetExtRefId_StoresAsProvided() {
        xref.setExtRefId("xyz789");
        assertEquals("xyz789", xref.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithGuid_StoresAsProvided() {
        String guid = "a1b2c3d4-e5f6-7890-abcd-ef1234567890";
        xref.setExtRefId(guid);
        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", xref.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithIntegerId_StoresAsProvided() {
        xref.setExtRefId("9876543210");
        assertEquals("9876543210", xref.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithNull_StoresNull() {
        xref.setExtRefId(null);
        assertNull(xref.getExtRefId());
    }

    @Test
    void testSetExtRefId_WithMixedCase_StoresAsProvided() {
        xref.setExtRefId("MiXeD-CaSe-456");
        assertEquals("MiXeD-CaSe-456", xref.getExtRefId());
    }
}
