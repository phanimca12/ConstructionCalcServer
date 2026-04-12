package com.ssnc.schemaService.entity;

import com.ssnc.schemaService.constants.ErrorMessages;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtRefTypeTest {

    @Test
    void testEnumValues() {
        ExtRefType[] values = ExtRefType.values();
        assertEquals(5, values.length);
        assertEquals(ExtRefType.PROCESS, values[0]);
        assertEquals(ExtRefType.AUTOMATION, values[1]);
        assertEquals(ExtRefType.PRESENTATION_FLOW, values[2]);
        assertEquals(ExtRefType.SAMPLING, values[3]);
        assertEquals(ExtRefType.UX_BUILDER, values[4]);
    }

    @Test
    void testFromString_Process() {
        ExtRefType result = ExtRefType.fromString("Process");
        assertEquals(ExtRefType.PROCESS, result);
    }

    @Test
    void testFromString_Automation() {
        ExtRefType result = ExtRefType.fromString("Automation");
        assertEquals(ExtRefType.AUTOMATION, result);
    }

    @Test
    void testFromString_PresentationFlow() {
        ExtRefType result = ExtRefType.fromString("PresentationFlow");
        assertEquals(ExtRefType.PRESENTATION_FLOW, result);
    }

    @Test
    void testFromString_Sampling() {
        ExtRefType result = ExtRefType.fromString("Sampling");
        assertEquals(ExtRefType.SAMPLING, result);
    }

    @Test
    void testFromString_UXBuilder() {
        ExtRefType result = ExtRefType.fromString("UXBuilder");
        assertEquals(ExtRefType.UX_BUILDER, result);
    }

    @Test
    void testFromString_CaseInsensitive_Process() {
        ExtRefType result = ExtRefType.fromString("process");
        assertEquals(ExtRefType.PROCESS, result);
    }

    @Test
    void testFromString_CaseInsensitive_Automation() {
        ExtRefType result = ExtRefType.fromString("AUTOMATION");
        assertEquals(ExtRefType.AUTOMATION, result);
    }

    @Test
    void testFromString_CaseInsensitive_PresentationFlow() {
        ExtRefType result = ExtRefType.fromString("presentationflow");
        assertEquals(ExtRefType.PRESENTATION_FLOW, result);
    }

    @Test
    void testFromString_CaseInsensitive_Sampling() {
        ExtRefType result = ExtRefType.fromString("SaMpLiNg");
        assertEquals(ExtRefType.SAMPLING, result);
    }

    @Test
    void testFromString_CaseInsensitive_UXBuilder() {
        ExtRefType result = ExtRefType.fromString("uxbuilder");
        assertEquals(ExtRefType.UX_BUILDER, result);
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
        assertEquals(String.format(ErrorMessages.EXTERNAL_REFERENCE_INVALID_TYPE, "InvalidType"), exception.getMessage());
    }

    @Test
    void testFromString_EmptyString() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ExtRefType.fromString("")
        );
        assertEquals(String.format(ErrorMessages.EXTERNAL_REFERENCE_INVALID_TYPE, ""), exception.getMessage());
    }

    @Test
    void testFromString_InvalidCase() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ExtRefType.fromString("INVALID_TYPE")
        );
        assertEquals(String.format(ErrorMessages.EXTERNAL_REFERENCE_INVALID_TYPE, "INVALID_TYPE"), exception.getMessage());
    }

    @Test
    void testEnumName_Process() {
        assertEquals("PROCESS", ExtRefType.PROCESS.name());
    }

    @Test
    void testEnumName_Automation() {
        assertEquals("AUTOMATION", ExtRefType.AUTOMATION.name());
    }

    @Test
    void testEnumName_PresentationFlow() {
        assertEquals("PRESENTATION_FLOW", ExtRefType.PRESENTATION_FLOW.name());
    }

    @Test
    void testEnumName_Sampling() {
        assertEquals("SAMPLING", ExtRefType.SAMPLING.name());
    }

    @Test
    void testEnumName_UXBuilder() {
        assertEquals("UX_BUILDER", ExtRefType.UX_BUILDER.name());
    }

    @Test
    void testValueOf_Process() {
        assertEquals(ExtRefType.PROCESS, ExtRefType.valueOf("PROCESS"));
    }

    @Test
    void testValueOf_Automation() {
        assertEquals(ExtRefType.AUTOMATION, ExtRefType.valueOf("AUTOMATION"));
    }

    @Test
    void testValueOf_PresentationFlow() {
        assertEquals(ExtRefType.PRESENTATION_FLOW, ExtRefType.valueOf("PRESENTATION_FLOW"));
    }

    @Test
    void testValueOf_Sampling() {
        assertEquals(ExtRefType.SAMPLING, ExtRefType.valueOf("SAMPLING"));
    }

    @Test
    void testValueOf_UXBuilder() {
        assertEquals(ExtRefType.UX_BUILDER, ExtRefType.valueOf("UX_BUILDER"));
    }

    @Test
    void testValueOf_InvalidType() {
        assertThrows(IllegalArgumentException.class, () -> ExtRefType.valueOf("InvalidType"));
    }

    @Test
    void testOrdinal_Process() {
        assertEquals(0, ExtRefType.PROCESS.ordinal());
    }

    @Test
    void testOrdinal_Automation() {
        assertEquals(1, ExtRefType.AUTOMATION.ordinal());
    }

    @Test
    void testOrdinal_PresentationFlow() {
        assertEquals(2, ExtRefType.PRESENTATION_FLOW.ordinal());
    }

    @Test
    void testOrdinal_Sampling() {
        assertEquals(3, ExtRefType.SAMPLING.ordinal());
    }

    @Test
    void testOrdinal_UXBuilder() {
        assertEquals(4, ExtRefType.UX_BUILDER.ordinal());
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
        String original = "AUTOMATION";
        ExtRefType enumValue = ExtRefType.fromString(original);
        String converted = enumValue.name();
        assertEquals(original, converted);
    }
}
