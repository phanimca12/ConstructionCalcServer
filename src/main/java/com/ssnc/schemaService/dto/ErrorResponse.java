package com.ssnc.schemaService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Standard error response structure for API error handling
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
    /**
     * HTTP status code
     */
    private int status;

    /**
     * Error message
     */
    private String error;

    /**
     * Detailed error message
     */
    private String message;

    /**
     * Timestamp of the error
     */
    private LocalDateTime timestamp;

    /**
     * Convenience constructor with current timestamp
     */
    public ErrorResponse(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
