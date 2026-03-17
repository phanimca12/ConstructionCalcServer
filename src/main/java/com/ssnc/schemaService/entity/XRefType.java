package com.ssnc.schemaService.entity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public enum XRefType {
    ProcessModel,
    AutomationService,
    PresentationFlow,
    SamplingService,
    UXB_Form,
    UXB_App;

    private static Map<Integer, XRefType> typeMap;

    static {
        final Map<Integer, XRefType> map = new HashMap<Integer, XRefType>();
        for (final XRefType type : values())
            map.put(type.ordinal(), type);
        typeMap = Collections.unmodifiableMap(map);
    }

    public static XRefType fromInt(final int value) {
        return typeMap.get(value);
    }
}