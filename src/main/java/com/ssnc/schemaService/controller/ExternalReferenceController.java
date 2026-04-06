package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.dto.ErrorResponse;
import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.ExtRefResponse;
import com.ssnc.schemaService.dto.ExtRefWithSchemasRequest;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.service.ExternalReferenceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/extRef/{nameSpace}")
@Validated
public class ExternalReferenceController {

    @Autowired
    private ExternalReferenceService externalReferenceService;

    /**
     * GET /extRef/{nameSpace}
     * Get external references with optional type filter
     *
     * @param nameSpace - Namespace filter
     * @param type - Optional external reference type (Process, Automation, PresentationFlow, Sampling, UXBuilder)
     * @return List of external references or error response
     */
    @GetMapping
    public ResponseEntity<?> getExternalReferences(
            @PathVariable("nameSpace") @Size(max = 32, message = "Namespace must not exceed 32 characters") String nameSpace,
            @RequestParam(required = false) @Size(max = 64, message = "Type must not exceed 64 characters") String type) {

        try {
            List<ExtRefDto> extRefs = externalReferenceService.getExternalReferences(nameSpace, type);
            return ResponseEntity.ok(extRefs);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * GET /extRef/{nameSpace}/type/{extRefType}/id/{extRefId}/version/{extRefVersion}/schemas
     * Get schemas by external reference
     *
     * @param nameSpace - Namespace filter
     * @param extRefType - External reference type (Process, Automation, PresentationFlow, Sampling, UXBuilder)
     * @param extRefId - External reference ID (supports both GUID and integer values, max 64 chars)
     * @param extRefVersion - External reference version (max 64 chars)
     * @return List of schemas associated with the external reference or error response
     */
    @GetMapping("/type/{extRefType}/id/{extRefId}/version/{extRefVersion}/schemas")
    public ResponseEntity<?> getSchemasByExternalReference(
            @PathVariable("nameSpace") @Size(max = 32, message = "Namespace must not exceed 32 characters") String nameSpace,
            @PathVariable("extRefType") @Size(max = 64, message = "External reference type must not exceed 64 characters") String extRefType,
            @PathVariable("extRefId") @Size(max = 64, message = "External reference ID must not exceed 64 characters") String extRefId,
            @PathVariable("extRefVersion") @Size(max = 64, message = "External reference version must not exceed 64 characters") String extRefVersion) {

        try {
            List<SchemaDto> schemas = externalReferenceService.getSchemasByExternalReference(
                    nameSpace, extRefType, extRefId, extRefVersion);
            return ResponseEntity.ok(schemas);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * PUT /extRef/{nameSpace}/type/{extRefType}/name/{extRefName}/id/{extRefId}/version/{extRefVersion}/schemas
     * Create an external reference with associated schemas.
     *
     * IMMUTABILITY: Once created, an external reference version is IMMUTABLE. You cannot change
     * the name, type, or schema associations for an existing version. To make changes, create
     * a new version.
     *
     * This endpoint is idempotent - if the exact same data is sent multiple times, it will only
     * create once and return a success message for subsequent identical requests.
     *
     * @param nameSpace - Namespace filter (max 32 chars)
     * @param extRefType - External reference type (Process, Automation, PresentationFlow, Sampling, UXBuilder, max 64 chars)
     * @param extRefName - External reference name (max 256 chars)
     * @param extRefId - External reference ID (supports both GUID and integer values, max 64 chars)
     * @param extRefVersion - External reference version (max 64 chars)
     * @param request - REQUIRED request body containing list of schemas to associate (can be empty array)
     * @return Response containing the external reference, a message, and an update flag or error response
     * @throws IllegalArgumentException if trying to modify an existing version (400 Bad Request)
     */
    @PutMapping("/type/{extRefType}/name/{extRefName}/id/{extRefId}/version/{extRefVersion}/schemas")
    public ResponseEntity<?> createOrUpdateExternalReference(
            @PathVariable("nameSpace") @Size(max = 32, message = "Namespace must not exceed 32 characters") String nameSpace,
            @PathVariable("extRefType") @Size(max = 64, message = "External reference type must not exceed 64 characters") String extRefType,
            @PathVariable("extRefName") @Size(max = 256, message = "External reference name must not exceed 256 characters") String extRefName,
            @PathVariable("extRefId") @Size(max = 64, message = "External reference ID must not exceed 64 characters") String extRefId,
            @PathVariable("extRefVersion") @Size(max = 64, message = "External reference version must not exceed 64 characters") String extRefVersion,
            @RequestBody @Valid ExtRefWithSchemasRequest request) {

        try {
            ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                    nameSpace, extRefType, extRefName, extRefId, extRefVersion, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
