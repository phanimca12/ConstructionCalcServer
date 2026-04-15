package com.ssnc.schemaService.config;

import com.ssnc.schemaService.constants.AppConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BooleanToYNConverterTest {

    private BooleanToYNConverter converter;

    @BeforeEach
    void setUp() {
        converter = new BooleanToYNConverter();
    }

    @Test
    void testConvertToDatabaseColumn_True() {
        String result = converter.convertToDatabaseColumn(true);
        assertEquals(AppConstants.BOOLEAN_YES, result);
    }

    @Test
    void testConvertToDatabaseColumn_False() {
        String result = converter.convertToDatabaseColumn(false);
        assertEquals(AppConstants.BOOLEAN_NO, result);
    }

    @Test
    void testConvertToDatabaseColumn_Null() {
        String result = converter.convertToDatabaseColumn(null);
        assertNull(result);
    }

    @Test
    void testConvertToEntityAttribute_Yes() {
        Boolean result = converter.convertToEntityAttribute("Y");
        assertTrue(result);
    }

    @Test
    void testConvertToEntityAttribute_YesLowercase() {
        Boolean result = converter.convertToEntityAttribute("y");
        assertTrue(result);
    }

    @Test
    void testConvertToEntityAttribute_No() {
        Boolean result = converter.convertToEntityAttribute("N");
        assertFalse(result);
    }

    @Test
    void testConvertToEntityAttribute_NoLowercase() {
        Boolean result = converter.convertToEntityAttribute("n");
        assertFalse(result);
    }

    @Test
    void testConvertToEntityAttribute_Null() {
        Boolean result = converter.convertToEntityAttribute(null);
        assertNull(result);
    }

    @Test
    void testConvertToEntityAttribute_InvalidValue() {
        Boolean result = converter.convertToEntityAttribute("INVALID");
        assertFalse(result);
    }

    @Test
    void testConvertToEntityAttribute_EmptyString() {
        Boolean result = converter.convertToEntityAttribute("");
        assertFalse(result);
    }

    @Test
    void testRoundTrip_True() {
        String dbValue = converter.convertToDatabaseColumn(true);
        Boolean entityValue = converter.convertToEntityAttribute(dbValue);
        assertTrue(entityValue);
    }

    @Test
    void testRoundTrip_False() {
        String dbValue = converter.convertToDatabaseColumn(false);
        Boolean entityValue = converter.convertToEntityAttribute(dbValue);
        assertFalse(entityValue);
    }

    @Test
    void testRoundTrip_Null() {
        String dbValue = converter.convertToDatabaseColumn(null);
        Boolean entityValue = converter.convertToEntityAttribute(dbValue);
        assertNull(entityValue);
    }
}
