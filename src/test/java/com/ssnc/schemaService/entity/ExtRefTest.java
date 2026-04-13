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
    void testSetId_WithValidValues() {
        extRef.setId("abc123", "1.0.0");
        assertEquals("abc123", extRef.getExtRefId());
        assertEquals("1.0.0", extRef.getExtRefVersion());
    }

    @Test
    void testSetId_WithGuid() {
        String guid = "550e8400-e29b-41d4-a716-446655440000";
        extRef.setId(guid, "2.0.0");
        assertEquals("550e8400-e29b-41d4-a716-446655440000", extRef.getExtRefId());
        assertEquals("2.0.0", extRef.getExtRefVersion());
    }

    @Test
    void testSetId_WithIntegerId() {
        extRef.setId("1234567890", "1.0");
        assertEquals("1234567890", extRef.getExtRefId());
        assertEquals("1.0", extRef.getExtRefVersion());
    }

    @Test
    void testSetId_WithMixedCase() {
        extRef.setId("AbC-DeF-123", "3.0.0");
        assertEquals("AbC-DeF-123", extRef.getExtRefId());
        assertEquals("3.0.0", extRef.getExtRefVersion());
    }

    @Test
    void testSetId_NullExtRefId_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                extRef.setId(null, "1.0.0")
        );
        assertEquals("Both extRefId and extRefVersion are required (NOT NULL)", exception.getMessage());
    }

    @Test
    void testSetId_NullVersion_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                extRef.setId("abc123", null)
        );
        assertEquals("Both extRefId and extRefVersion are required (NOT NULL)", exception.getMessage());
    }

    @Test
    void testSetId_BothNull_ThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                extRef.setId(null, null)
        );
        assertEquals("Both extRefId and extRefVersion are required (NOT NULL)", exception.getMessage());
    }

    @Test
    void testSetId_WithUppercase() {
        extRef.setId("ALREADY-UPPERCASE-123", "4.0.0");
        assertEquals("ALREADY-UPPERCASE-123", extRef.getExtRefId());
        assertEquals("4.0.0", extRef.getExtRefVersion());
    }

    @Test
    void testGettersReturnNull_WhenIdNotSet() {
        assertNull(extRef.getExtRefId());
        assertNull(extRef.getExtRefVersion());
    }

    @Test
    void testSetId_ReplacesExistingId() {
        extRef.setId("first-id", "1.0.0");
        assertEquals("first-id", extRef.getExtRefId());
        assertEquals("1.0.0", extRef.getExtRefVersion());

        extRef.setId("second-id", "2.0.0");
        assertEquals("second-id", extRef.getExtRefId());
        assertEquals("2.0.0", extRef.getExtRefVersion());
    }
}
