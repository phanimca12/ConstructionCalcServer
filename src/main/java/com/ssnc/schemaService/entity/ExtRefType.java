package com.ssnc.schemaService.entity;

import com.ssnc.schemaService.constants.ErrorMessages;

public enum ExtRefType {
    PROCESS,
    AUTOMATION,
    PRESENTATION_FLOW,
    SAMPLING,
    UX_BUILDER;

    public static ExtRefType fromString(String value) {
        if (value == null) {
            return null;
        }

        // Normalize input by converting to uppercase and replacing common separators
        String normalized = value.toUpperCase().replace("-", "_").replace(" ", "_");

        // Try exact match first (e.g., "PRESENTATION_FLOW")
        for (ExtRefType type : ExtRefType.values()) {
            if (type.name().equals(normalized)) {
                return type;
            }
        }

        // Try legacy format without underscores (e.g., "PresentationFlow" -> "PRESENTATIONFLOW")
        String noUnderscore = normalized.replace("_", "");
        for (ExtRefType type : ExtRefType.values()) {
            if (type.name().replace("_", "").equals(noUnderscore)) {
                return type;
            }
        }

        throw new IllegalArgumentException(String.format(ErrorMessages.EXTERNAL_REFERENCE_INVALID_TYPE, value));
    }
}
