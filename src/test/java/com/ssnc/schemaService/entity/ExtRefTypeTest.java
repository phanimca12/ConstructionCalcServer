package com.ssnc.schemaService.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtRefTypeTest {

    @Test
    void testEnumValues() {
        ExtRefType[] values = ExtRefType.values();
        assertEquals(5, values.length);
        assertEquals(ExtRefType.Process, values[0]);
        assertEquals(ExtRefType.Automation, values[1]);
        assertEquals(ExtRefType.PresentationFlow, values[2]);
        assertEquals(ExtRefType.Sampling, values[3]);
        assertEquals(ExtRefType.UXBuilder, values[4]);
    }

    @Test
    void testFromString_Process() {
        ExtRefType result = ExtRefType.fromString("Process");
        assertEquals(ExtRefType.Process, result);
    }

    @Test
    void testFromString_Automation() {
        ExtRefType result = ExtRefType.fromString("Automation");
        assertEquals(ExtRefType.Automation, result);
    }

    @Test
    void testFromString_PresentationFlow() {
        ExtRefType result = ExtRefType.fromString("PresentationFlow");
        assertEquals(ExtRefType.PresentationFlow, result);
    }

    @Test
    void testFromString_Sampling() {
        ExtRefType result = ExtRefType.fromString("Sampling");
        assertEquals(ExtRefType.Sampling, result);
    }

    @Test
    void testFromString_UXBuilder() {
        ExtRefType result = ExtRefType.fromString("UXBuilder");
        assertEquals(ExtRefType.UXBuilder, result);
    }

    @Test
    void testFromString_CaseInsensitive_Process() {
        ExtRefType result = ExtRefType.fromString("process");
        assertEquals(ExtRefType.Process, result);
    }

    @Test
    void testFromString_CaseInsensitive_Automation() {
        ExtRefType result = ExtRefType.fromString("AUTOMATION");
        assertEquals(ExtRefType.Automation, result);
    }

    @Test
    void testFromString_CaseInsensitive_PresentationFlow() {
        ExtRefType result = ExtRefType.fromString("presentationflow");
        assertEquals(ExtRefType.PresentationFlow, result);
    }

    @Test
    void testFromString_CaseInsensitive_Sampling() {
        ExtRefType result = ExtRefType.fromString("SaMpLiNg");
        assertEquals(ExtRefType.Sampling, result);
    }

    @Test
    void testFromString_CaseInsensitive_UXBuilder() {
        ExtRefType result = ExtRefType.fromString("uxbuilder");
        assertEquals(ExtRefType.UXBuilder, result);
    }

    @Test
    void testFromString_Null() {
        ExtRefType result = ExtRefType.fromString(null);
        assertNull(result);
    }

    @Test
    void testFromString_InvalidType() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ExtRefType.fromString("InvalidType")
        );
        assertEquals("Invalid ExtRefType: InvalidType", exception.getMessage());
    }

    @Test
    void testFromString_EmptyString() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ExtRefType.fromString("")
        );
        assertEquals("Invalid ExtRefType: ", exception.getMessage());
    }

    @Test
    void testFromString_InvalidCase() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ExtRefType.fromString("INVALID_TYPE")
        );
        assertEquals("Invalid ExtRefType: INVALID_TYPE", exception.getMessage());
    }

    @Test
    void testEnumName_Process() {
        assertEquals("Process", ExtRefType.Process.name());
    }

    @Test
    void testEnumName_Automation() {
        assertEquals("Automation", ExtRefType.Automation.name());
    }

    @Test
    void testEnumName_PresentationFlow() {
        assertEquals("PresentationFlow", ExtRefType.PresentationFlow.name());
    }

    @Test
    void testEnumName_Sampling() {
        assertEquals("Sampling", ExtRefType.Sampling.name());
    }

    @Test
    void testEnumName_UXBuilder() {
        assertEquals("UXBuilder", ExtRefType.UXBuilder.name());
    }

    @Test
    void testValueOf_Process() {
        assertEquals(ExtRefType.Process, ExtRefType.valueOf("Process"));
    }

    @Test
    void testValueOf_Automation() {
        assertEquals(ExtRefType.Automation, ExtRefType.valueOf("Automation"));
    }

    @Test
    void testValueOf_PresentationFlow() {
        assertEquals(ExtRefType.PresentationFlow, ExtRefType.valueOf("PresentationFlow"));
    }

    @Test
    void testValueOf_Sampling() {
        assertEquals(ExtRefType.Sampling, ExtRefType.valueOf("Sampling"));
    }

    @Test
    void testValueOf_UXBuilder() {
        assertEquals(ExtRefType.UXBuilder, ExtRefType.valueOf("UXBuilder"));
    }

    @Test
    void testValueOf_InvalidType() {
        assertThrows(IllegalArgumentException.class, () -> ExtRefType.valueOf("InvalidType"));
    }

    @Test
    void testOrdinal_Process() {
        assertEquals(0, ExtRefType.Process.ordinal());
    }

    @Test
    void testOrdinal_Automation() {
        assertEquals(1, ExtRefType.Automation.ordinal());
    }

    @Test
    void testOrdinal_PresentationFlow() {
        assertEquals(2, ExtRefType.PresentationFlow.ordinal());
    }

    @Test
    void testOrdinal_Sampling() {
        assertEquals(3, ExtRefType.Sampling.ordinal());
    }

    @Test
    void testOrdinal_UXBuilder() {
        assertEquals(4, ExtRefType.UXBuilder.ordinal());
    }

    @Test
    void testFromString_AllValues() {
        for (ExtRefType type : ExtRefType.values()) {
            ExtRefType result = ExtRefType.fromString(type.name());
            assertEquals(type, result);
        }
    }

    @Test
    void testFromString_RoundTrip() {
        String original = "Automation";
        ExtRefType enumValue = ExtRefType.fromString(original);
        String converted = enumValue.name();
        assertEquals(original, converted);
    }
}
