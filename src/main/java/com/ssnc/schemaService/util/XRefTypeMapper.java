package com.ssnc.schemaService.util;

import com.ssnc.schemaService.entity.XRefType;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class to map string representations to XRefType enum values
 */
public class XRefTypeMapper {

    private static final Map<String, XRefType> stringToEnumMap = new HashMap<>();
    private static final Map<XRefType, String> enumToStringMap = new HashMap<>();

    static {
        // Map incoming string values to enum
        stringToEnumMap.put("Process", XRefType.ProcessModel);
        stringToEnumMap.put("Automation", XRefType.AutomationService);
        stringToEnumMap.put("PresentationFlow", XRefType.PresentationFlow);
        stringToEnumMap.put("Sampling", XRefType.SamplingService);
        stringToEnumMap.put("UXBForm", XRefType.UXB_Form);
        stringToEnumMap.put("UXBApp", XRefType.UXB_App);

        // Reverse mapping for output
        enumToStringMap.put(XRefType.ProcessModel, "Process");
        enumToStringMap.put(XRefType.AutomationService, "Automation");
        enumToStringMap.put(XRefType.PresentationFlow, "PresentationFlow");
        enumToStringMap.put(XRefType.SamplingService, "Sampling");
        enumToStringMap.put(XRefType.UXB_Form, "UXBForm");
        enumToStringMap.put(XRefType.UXB_App, "UXBApp");
    }

    /**
     * Convert string to XRefType enum
     * @param value String value (Process, Automation, PresentationFlow, Sampling, UXBForm, UXBApp)
     * @return Corresponding XRefType enum value
     * @throws IllegalArgumentException if value is not recognized
     */
    public static XRefType toEnum(String value) {
        if (value == null) {
            return null;
        }
        XRefType type = stringToEnumMap.get(value);
        if (type == null) {
            throw new IllegalArgumentException("Invalid reference type: " + value +
                    ". Valid values are: Process, Automation, PresentationFlow, Sampling, UXBForm, UXBApp");
        }
        return type;
    }

    /**
     * Convert XRefType enum to string
     * @param type XRefType enum value
     * @return String representation
     */
    public static String toString(XRefType type) {
        if (type == null) {
            return null;
        }
        return enumToStringMap.get(type);
    }
}