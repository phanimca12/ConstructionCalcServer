package com.ssnc.schemaService.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleIllegalArgumentException() {
        String errorMessage = "Invalid argument provided";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.get("timestamp") instanceof LocalDateTime);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), body.get("error"));
        assertEquals(errorMessage, body.get("message"));
    }

    @Test
    void testHandleIllegalArgumentException_WithNullMessage() {
        IllegalArgumentException exception = new IllegalArgumentException();

        ResponseEntity<Object> response = exceptionHandler.handleIllegalArgumentException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertNull(body.get("message"));
    }

    @Test
    void testHandleConstraintViolationException() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        when(violation1.getMessage()).thenReturn("Field must not be null");

        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);
        when(violation2.getMessage()).thenReturn("Size must be between 1 and 64");

        violations.add(violation1);
        violations.add(violation2);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ResponseEntity<Object> response = exceptionHandler.handleConstraintViolationException(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.get("timestamp") instanceof LocalDateTime);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), body.get("error"));

        String message = (String) body.get("message");
        assertNotNull(message);
        assertTrue(message.contains("Field must not be null") || message.contains("Size must be between 1 and 64"));
    }

    @Test
    void testHandleConstraintViolationException_SingleViolation() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();

        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Name is required");

        violations.add(violation);

        ConstraintViolationException exception = new ConstraintViolationException(violations);

        ResponseEntity<Object> response = exceptionHandler.handleConstraintViolationException(exception);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Name is required", body.get("message"));
    }

    @Test
    void testHandleMethodArgumentNotValid_WithFieldErrors() throws Exception {
        MethodParameter methodParameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("schemaDto", "name", "must not be blank");
        FieldError fieldError2 = new FieldError("schemaDto", "type", "must not be null");

        when(bindingResult.getAllErrors()).thenReturn(java.util.Arrays.asList(fieldError1, fieldError2));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertTrue(body.containsKey("timestamp"));
        assertTrue(body.get("timestamp") instanceof LocalDateTime);
        assertEquals(HttpStatus.BAD_REQUEST.value(), body.get("status"));
        assertEquals(HttpStatus.BAD_REQUEST.getReasonPhrase(), body.get("error"));

        String message = (String) body.get("message");
        assertNotNull(message);
        assertTrue(message.contains("name: must not be blank"));
        assertTrue(message.contains("type: must not be null"));
    }

    @Test
    void testHandleMethodArgumentNotValid_WithObjectErrors() throws Exception {
        MethodParameter methodParameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);

        ObjectError objectError = new ObjectError("schemaDto", "Global validation failed");

        when(bindingResult.getAllErrors()).thenReturn(java.util.Collections.singletonList(objectError));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(exception);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("Global validation failed", body.get("message"));
    }

    @Test
    void testHandleMethodArgumentNotValid_MixedErrors() throws Exception {
        MethodParameter methodParameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("schemaDto", "name", "must not be blank");
        ObjectError objectError = new ObjectError("schemaDto", "At least one field must be provided");

        when(bindingResult.getAllErrors()).thenReturn(java.util.Arrays.asList(fieldError, objectError));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(exception);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);

        String message = (String) body.get("message");
        assertNotNull(message);
        assertTrue(message.contains("name: must not be blank"));
        assertTrue(message.contains("At least one field must be provided"));
    }

    @Test
    void testHandleMethodArgumentNotValid_SingleError() throws Exception {
        MethodParameter methodParameter = mock(MethodParameter.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError("schemaDto", "name", "must not be blank");

        when(bindingResult.getAllErrors()).thenReturn(java.util.Collections.singletonList(fieldError));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(exception);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertNotNull(body);
        assertEquals("name: must not be blank", body.get("message"));
    }

    @Test
    void testResponseStructureConsistency() {
        // Test that all handlers return consistent response structure
        IllegalArgumentException illegalArgEx = new IllegalArgumentException("test");
        ResponseEntity<Object> response1 = exceptionHandler.handleIllegalArgumentException(illegalArgEx);

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("test");
        violations.add(violation);
        ConstraintViolationException constraintEx = new ConstraintViolationException(violations);
        ResponseEntity<Object> response2 = exceptionHandler.handleConstraintViolationException(constraintEx);

        @SuppressWarnings("unchecked")
        Map<String, Object> body1 = (Map<String, Object>) response1.getBody();
        @SuppressWarnings("unchecked")
        Map<String, Object> body2 = (Map<String, Object>) response2.getBody();

        // All responses should have the same structure
        assertNotNull(body1);
        assertNotNull(body2);
        assertTrue(body1.containsKey("timestamp"));
        assertTrue(body1.containsKey("status"));
        assertTrue(body1.containsKey("error"));
        assertTrue(body1.containsKey("message"));

        assertTrue(body2.containsKey("timestamp"));
        assertTrue(body2.containsKey("status"));
        assertTrue(body2.containsKey("error"));
        assertTrue(body2.containsKey("message"));
    }
}
