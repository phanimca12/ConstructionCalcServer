package com.ssnc.schemaService.util;

import com.ssnc.schemaService.entity.XRefType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XRefTypeMapperTest {

    @Test
    void testToEnum_Process() {
        XRefType result = XRefTypeMapper.toEnum("Process");
        assertEquals(XRefType.ProcessModel, result);
    }

    @Test
    void testToEnum_Automation() {
        XRefType result = XRefTypeMapper.toEnum("Automation");
        assertEquals(XRefType.AutomationService, result);
    }

    @Test
    void testToEnum_PresentationFlow() {
        XRefType result = XRefTypeMapper.toEnum("PresentationFlow");
        assertEquals(XRefType.PresentationFlow, result);
    }

    @Test
    void testToEnum_Sampling() {
        XRefType result = XRefTypeMapper.toEnum("Sampling");
        assertEquals(XRefType.SamplingService, result);
    }

    @Test
    void testToEnum_UXBForm() {
        XRefType result = XRefTypeMapper.toEnum("UXBForm");
        assertEquals(XRefType.UXB_Form, result);
    }

    @Test
    void testToEnum_UXBApp() {
        XRefType result = XRefTypeMapper.toEnum("UXBApp");
        assertEquals(XRefType.UXB_App, result);
    }

    @Test
    void testToEnum_Null() {
        XRefType result = XRefTypeMapper.toEnum(null);
        assertNull(result);
    }

    @Test
    void testToEnum_InvalidValue() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> XRefTypeMapper.toEnum("InvalidType")
        );
        assertTrue(exception.getMessage().contains("Invalid reference type: InvalidType"));
        assertTrue(exception.getMessage().contains("Valid values are:"));
    }

    @Test
    void testToEnum_CaseSensitive() {
        // Test that the mapping is case-sensitive
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> XRefTypeMapper.toEnum("process")
        );
        assertTrue(exception.getMessage().contains("Invalid reference type: process"));
    }

    @Test
    void testToString_ProcessModel() {
        String result = XRefTypeMapper.toString(XRefType.ProcessModel);
        assertEquals("Process", result);
    }

    @Test
    void testToString_AutomationService() {
        String result = XRefTypeMapper.toString(XRefType.AutomationService);
        assertEquals("Automation", result);
    }

    @Test
    void testToString_PresentationFlow() {
        String result = XRefTypeMapper.toString(XRefType.PresentationFlow);
        assertEquals("PresentationFlow", result);
    }

    @Test
    void testToString_SamplingService() {
        String result = XRefTypeMapper.toString(XRefType.SamplingService);
        assertEquals("Sampling", result);
    }

    @Test
    void testToString_UXBForm() {
        String result = XRefTypeMapper.toString(XRefType.UXB_Form);
        assertEquals("UXBForm", result);
    }

    @Test
    void testToString_UXBApp() {
        String result = XRefTypeMapper.toString(XRefType.UXB_App);
        assertEquals("UXBApp", result);
    }

    @Test
    void testToString_Null() {
        String result = XRefTypeMapper.toString(null);
        assertNull(result);
    }

    @Test
    void testBidirectionalMapping_Process() {
        String original = "Process";
        XRefType enumValue = XRefTypeMapper.toEnum(original);
        String converted = XRefTypeMapper.toString(enumValue);
        assertEquals(original, converted);
    }

    @Test
    void testBidirectionalMapping_Automation() {
        String original = "Automation";
        XRefType enumValue = XRefTypeMapper.toEnum(original);
        String converted = XRefTypeMapper.toString(enumValue);
        assertEquals(original, converted);
    }

    @Test
    void testBidirectionalMapping_PresentationFlow() {
        String original = "PresentationFlow";
        XRefType enumValue = XRefTypeMapper.toEnum(original);
        String converted = XRefTypeMapper.toString(enumValue);
        assertEquals(original, converted);
    }

    @Test
    void testBidirectionalMapping_Sampling() {
        String original = "Sampling";
        XRefType enumValue = XRefTypeMapper.toEnum(original);
        String converted = XRefTypeMapper.toString(enumValue);
        assertEquals(original, converted);
    }

    @Test
    void testBidirectionalMapping_UXBForm() {
        String original = "UXBForm";
        XRefType enumValue = XRefTypeMapper.toEnum(original);
        String converted = XRefTypeMapper.toString(enumValue);
        assertEquals(original, converted);
    }

    @Test
    void testBidirectionalMapping_UXBApp() {
        String original = "UXBApp";
        XRefType enumValue = XRefTypeMapper.toEnum(original);
        String converted = XRefTypeMapper.toString(enumValue);
        assertEquals(original, converted);
    }

    @Test
    void testAllEnumValuesHaveMapping() {
        // Ensure all enum values have a corresponding string mapping
        for (XRefType type : XRefType.values()) {
            String stringValue = XRefTypeMapper.toString(type);
            assertNotNull(stringValue, "No string mapping for enum: " + type);

            // Verify reverse mapping works
            XRefType reverseMapped = XRefTypeMapper.toEnum(stringValue);
            assertEquals(type, reverseMapped, "Bidirectional mapping failed for: " + type);
        }
    }
}