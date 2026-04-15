package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void testThreeArgumentConstructor() {
        int status = 400;
        String error = "Bad Request";
        String message = "Invalid input";

        ErrorResponse response = new ErrorResponse(status, error, message);

        assertEquals(status, response.getStatus());
        assertEquals(error, response.getError());
        assertEquals(message, response.getMessage());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void testAllArgsConstructor() {
        int status = 500;
        String error = "Internal Server Error";
        String message = "System error";
        LocalDateTime timestamp = LocalDateTime.now();

        ErrorResponse response = new ErrorResponse(status, error, message, timestamp);

        assertEquals(status, response.getStatus());
        assertEquals(error, response.getError());
        assertEquals(message, response.getMessage());
        assertEquals(timestamp, response.getTimestamp());
    }

    @Test
    void testDefaultConstructor() {
        ErrorResponse response = new ErrorResponse();

        assertEquals(0, response.getStatus());
        assertNull(response.getError());
        assertNull(response.getMessage());
        assertNull(response.getTimestamp());
    }

    @Test
    void testSettersAndGetters() {
        ErrorResponse response = new ErrorResponse();

        response.setStatus(404);
        assertEquals(404, response.getStatus());

        response.setError("Not Found");
        assertEquals("Not Found", response.getError());

        response.setMessage("Resource not found");
        assertEquals("Resource not found", response.getMessage());

        LocalDateTime now = LocalDateTime.now();
        response.setTimestamp(now);
        assertEquals(now, response.getTimestamp());
    }

    @Test
    void testTimestampAutoSet() {
        LocalDateTime before = LocalDateTime.now();
        ErrorResponse response = new ErrorResponse(400, "Bad Request", "Test");
        LocalDateTime after = LocalDateTime.now();

        assertNotNull(response.getTimestamp());
        assertTrue(response.getTimestamp().isAfter(before.minusSeconds(1)));
        assertTrue(response.getTimestamp().isBefore(after.plusSeconds(1)));
    }

    @Test
    void testNullValues() {
        ErrorResponse response = new ErrorResponse(0, null, null, null);

        assertEquals(0, response.getStatus());
        assertNull(response.getError());
        assertNull(response.getMessage());
        assertNull(response.getTimestamp());
    }

    @Test
    void testEmptyStrings() {
        ErrorResponse response = new ErrorResponse(400, "", "");

        assertEquals(400, response.getStatus());
        assertEquals("", response.getError());
        assertEquals("", response.getMessage());
    }
}
