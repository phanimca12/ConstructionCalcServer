package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtRefResponseTest {

    @Test
    void testAllArgsConstructor() {
        ExtRefDto extRef = new ExtRefDto();
        extRef.setExtRefName("TestRef");

        ExtRefResponse response = new ExtRefResponse(extRef, "Success message", true);

        assertEquals(extRef, response.getExtRef());
        assertEquals("Success message", response.getMessage());
        assertTrue(response.isUpdated());
    }

    @Test
    void testSettersAndGetters() {
        ExtRefResponse response = new ExtRefResponse(null, null, false);

        ExtRefDto extRef = new ExtRefDto();
        extRef.setExtRefName("TestRef");
        response.setExtRef(extRef);
        assertEquals(extRef, response.getExtRef());
        assertEquals("TestRef", response.getExtRef().getExtRefName());

        response.setMessage("Test message");
        assertEquals("Test message", response.getMessage());

        response.setUpdated(true);
        assertTrue(response.isUpdated());

        response.setUpdated(false);
        assertFalse(response.isUpdated());
    }

    @Test
    void testUpdatedFlag_True() {
        ExtRefDto extRef = new ExtRefDto();
        extRef.setExtRefName("UpdatedRef");

        ExtRefResponse response = new ExtRefResponse(extRef, "Successfully updated", true);

        assertTrue(response.isUpdated());
        assertEquals("Successfully updated", response.getMessage());
    }

    @Test
    void testUpdatedFlag_False() {
        ExtRefDto extRef = new ExtRefDto();
        extRef.setExtRefName("ExistingRef");

        ExtRefResponse response = new ExtRefResponse(extRef, "Already exists", false);

        assertFalse(response.isUpdated());
        assertEquals("Already exists", response.getMessage());
    }

    @Test
    void testWithNullExtRef() {
        ExtRefResponse response = new ExtRefResponse(null, "Error message", false);

        assertNull(response.getExtRef());
        assertEquals("Error message", response.getMessage());
        assertFalse(response.isUpdated());
    }

    @Test
    void testFullResponse() {
        ExtRefDto extRef = new ExtRefDto();
        extRef.setExtRefId("ref-123");
        extRef.setExtRefName("CompleteRef");
        extRef.setExtRefType("PROCESS");
        extRef.setExtRefVersion("1.0");

        ExtRefResponse response = new ExtRefResponse(extRef, "External reference created successfully", true);

        assertNotNull(response.getExtRef());
        assertEquals("ref-123", response.getExtRef().getExtRefId());
        assertEquals("CompleteRef", response.getExtRef().getExtRefName());
        assertEquals("PROCESS", response.getExtRef().getExtRefType());
        assertEquals("1.0", response.getExtRef().getExtRefVersion());
        assertEquals("External reference created successfully", response.getMessage());
        assertTrue(response.isUpdated());
    }
}
