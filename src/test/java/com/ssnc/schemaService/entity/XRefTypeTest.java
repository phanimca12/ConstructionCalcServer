package com.ssnc.schemaService.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class XRefTypeTest {

    @Test
    void testEnumValues() {
        XRefType[] values = XRefType.values();
        assertEquals(6, values.length);

        assertEquals(XRefType.ProcessModel, values[0]);
        assertEquals(XRefType.AutomationService, values[1]);
        assertEquals(XRefType.PresentationFlow, values[2]);
        assertEquals(XRefType.SamplingService, values[3]);
        assertEquals(XRefType.UXB_Form, values[4]);
        assertEquals(XRefType.UXB_App, values[5]);
    }

    @Test
    void testFromInt_ProcessModel() {
        XRefType result = XRefType.fromInt(0);
        assertEquals(XRefType.ProcessModel, result);
    }

    @Test
    void testFromInt_AutomationService() {
        XRefType result = XRefType.fromInt(1);
        assertEquals(XRefType.AutomationService, result);
    }

    @Test
    void testFromInt_PresentationFlow() {
        XRefType result = XRefType.fromInt(2);
        assertEquals(XRefType.PresentationFlow, result);
    }

    @Test
    void testFromInt_SamplingService() {
        XRefType result = XRefType.fromInt(3);
        assertEquals(XRefType.SamplingService, result);
    }

    @Test
    void testFromInt_UXBForm() {
        XRefType result = XRefType.fromInt(4);
        assertEquals(XRefType.UXB_Form, result);
    }

    @Test
    void testFromInt_UXBApp() {
        XRefType result = XRefType.fromInt(5);
        assertEquals(XRefType.UXB_App, result);
    }

    @Test
    void testFromInt_InvalidValue() {
        XRefType result = XRefType.fromInt(999);
        assertNull(result);
    }

    @Test
    void testFromInt_NegativeValue() {
        XRefType result = XRefType.fromInt(-1);
        assertNull(result);
    }

    @Test
    void testOrdinal_ProcessModel() {
        assertEquals(0, XRefType.ProcessModel.ordinal());
    }

    @Test
    void testOrdinal_AutomationService() {
        assertEquals(1, XRefType.AutomationService.ordinal());
    }

    @Test
    void testOrdinal_PresentationFlow() {
        assertEquals(2, XRefType.PresentationFlow.ordinal());
    }

    @Test
    void testOrdinal_SamplingService() {
        assertEquals(3, XRefType.SamplingService.ordinal());
    }

    @Test
    void testOrdinal_UXBForm() {
        assertEquals(4, XRefType.UXB_Form.ordinal());
    }

    @Test
    void testOrdinal_UXBApp() {
        assertEquals(5, XRefType.UXB_App.ordinal());
    }

    @Test
    void testBidirectionalMapping() {
        // Test that fromInt and ordinal are inverses
        for (XRefType type : XRefType.values()) {
            int ordinal = type.ordinal();
            XRefType retrieved = XRefType.fromInt(ordinal);
            assertEquals(type, retrieved,
                "Bidirectional mapping failed for " + type + " with ordinal " + ordinal);
        }
    }

    @Test
    void testValueOf() {
        assertEquals(XRefType.ProcessModel, XRefType.valueOf("ProcessModel"));
        assertEquals(XRefType.AutomationService, XRefType.valueOf("AutomationService"));
        assertEquals(XRefType.PresentationFlow, XRefType.valueOf("PresentationFlow"));
        assertEquals(XRefType.SamplingService, XRefType.valueOf("SamplingService"));
        assertEquals(XRefType.UXB_Form, XRefType.valueOf("UXB_Form"));
        assertEquals(XRefType.UXB_App, XRefType.valueOf("UXB_App"));
    }

    @Test
    void testValueOf_InvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> XRefType.valueOf("InvalidType"));
    }

    @Test
    void testName() {
        assertEquals("ProcessModel", XRefType.ProcessModel.name());
        assertEquals("AutomationService", XRefType.AutomationService.name());
        assertEquals("PresentationFlow", XRefType.PresentationFlow.name());
        assertEquals("SamplingService", XRefType.SamplingService.name());
        assertEquals("UXB_Form", XRefType.UXB_Form.name());
        assertEquals("UXB_App", XRefType.UXB_App.name());
    }
}