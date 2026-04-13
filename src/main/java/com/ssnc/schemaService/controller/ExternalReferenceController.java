package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.constants.ApiConstants;
import com.ssnc.schemaService.constants.ErrorMessages;
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
@RequestMapping(ApiConstants.PATH_EXT_REF_BASE)
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
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) @Size(max = 32, message = ErrorMessages.VALIDATION_NAMESPACE_MAX_LENGTH) String nameSpace,
            @RequestParam(value = ApiConstants.QUERY_PARAM_TYPE, required = false) @Size(max = 64, message = ErrorMessages.VALIDATION_TYPE_MAX_LENGTH) String type) {

        try {
            List<ExtRefDto> extRefs = externalReferenceService.getExternalReferences(nameSpace, type);
            return ResponseEntity.ok(extRefs);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    ErrorMessages.HTTP_BAD_REQUEST,
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
     * @param extRefVersion - External reference version (max 6 chars)
     * @return List of schemas associated with the external reference or error response
     */
    @GetMapping(ApiConstants.PATH_EXT_REF_BY_TYPE_ID_VERSION)
    public ResponseEntity<?> getSchemasByExternalReference(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) @Size(max = 32, message = ErrorMessages.VALIDATION_NAMESPACE_MAX_LENGTH) String nameSpace,
            @PathVariable(ApiConstants.PARAM_EXT_REF_TYPE) @Size(max = 64, message = ErrorMessages.VALIDATION_EXT_REF_TYPE_MAX_LENGTH) String extRefType,
            @PathVariable(ApiConstants.PARAM_EXT_REF_ID) @Size(max = 64, message = ErrorMessages.VALIDATION_EXT_REF_ID_MAX_LENGTH) String extRefId,
            @PathVariable(ApiConstants.PARAM_EXT_REF_VERSION) @Size(max = 6, message = ErrorMessages.VALIDATION_EXT_REF_VERSION_MAX_LENGTH) String extRefVersion) {

        try {
            List<SchemaDto> schemas = externalReferenceService.getSchemasByExternalReference(
                    nameSpace, extRefType, extRefId, extRefVersion);
            return ResponseEntity.ok(schemas);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    ErrorMessages.HTTP_BAD_REQUEST,
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
     * @param extRefVersion - External reference version (max 6 chars)
     * @param request - REQUIRED request body containing list of schemas to associate (can be empty array)
     * @return Response containing the external reference, a message, and an update flag or error response
     * @throws IllegalArgumentException if trying to modify an existing version (400 Bad Request)
     */
    @PutMapping(ApiConstants.PATH_EXT_REF_CREATE)
    public ResponseEntity<?> createOrUpdateExternalReference(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) @Size(max = 32, message = ErrorMessages.VALIDATION_NAMESPACE_MAX_LENGTH) String nameSpace,
            @PathVariable(ApiConstants.PARAM_EXT_REF_TYPE) @Size(max = 64, message = ErrorMessages.VALIDATION_EXT_REF_TYPE_MAX_LENGTH) String extRefType,
            @PathVariable(ApiConstants.PARAM_EXT_REF_NAME) @Size(max = 256, message = ErrorMessages.VALIDATION_EXT_REF_NAME_MAX_LENGTH) String extRefName,
            @PathVariable(ApiConstants.PARAM_EXT_REF_ID) @Size(max = 64, message = ErrorMessages.VALIDATION_EXT_REF_ID_MAX_LENGTH) String extRefId,
            @PathVariable(ApiConstants.PARAM_EXT_REF_VERSION) @Size(max = 6, message = ErrorMessages.VALIDATION_EXT_REF_VERSION_MAX_LENGTH) String extRefVersion,
            @RequestBody @Valid ExtRefWithSchemasRequest request) {

        try {
            ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                    nameSpace, extRefType, extRefName, extRefId, extRefVersion, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    ErrorMessages.HTTP_BAD_REQUEST,
                    e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
