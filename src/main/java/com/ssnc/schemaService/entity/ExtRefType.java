package com.ssnc.schemaService.entity;

public enum ExtRefType {
    Process,
    Automation,
    PresentationFlow,
    Sampling,
    UXBuilder;

    public static ExtRefType fromString(String value) {
        if (value == null) {
            return null;
        }
        for (ExtRefType type : ExtRefType.values()) {
            if (type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid ExtRefType: " + value);
    }
}
