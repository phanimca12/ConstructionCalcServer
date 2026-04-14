package com.ssnc.schemaService.validation;

import com.ssnc.schemaService.dto.SchemaExportRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AtLeastOneNotNullValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidWithSchmId() {
        SchemaExportRequest request = new SchemaExportRequest();
        request.setSchmId(UUID.randomUUID());

        Set<ConstraintViolation<SchemaExportRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should be valid when schmId is provided");
    }

    @Test
    void testValidWithName() {
        SchemaExportRequest request = new SchemaExportRequest();
        request.setName("TestSchema");

        Set<ConstraintViolation<SchemaExportRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should be valid when name is provided");
    }

    @Test
    void testValidWithBoth() {
        SchemaExportRequest request = new SchemaExportRequest();
        request.setSchmId(UUID.randomUUID());
        request.setName("TestSchema");

        Set<ConstraintViolation<SchemaExportRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Should be valid when both schmId and name are provided");
    }

    @Test
    void testInvalidWithNeither() {
        SchemaExportRequest request = new SchemaExportRequest();

        Set<ConstraintViolation<SchemaExportRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Should be invalid when neither schmId nor name is provided");
        assertEquals(1, violations.size());
        assertEquals("Either schmId or name must be provided", violations.iterator().next().getMessage());
    }
}
