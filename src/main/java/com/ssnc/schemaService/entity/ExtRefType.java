package com.ssnc.schemaService.entity;

import com.ssnc.schemaService.constants.ErrorMessages;

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
        throw new IllegalArgumentException(String.format(ErrorMessages.EXTERNAL_REFERENCE_INVALID_TYPE, value));
    }
}
