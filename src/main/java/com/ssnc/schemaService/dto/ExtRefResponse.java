package com.ssnc.schemaService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExtRefResponse {
    private ExtRefDto extRef;
    private String message;
    private boolean updated;
}
