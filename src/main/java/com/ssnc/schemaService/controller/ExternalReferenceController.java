package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.ExtRefWithSchemasRequest;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.service.ExternalReferenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/extRef/{nameSpace}")
public class ExternalReferenceController {

    @Autowired
    private ExternalReferenceService externalReferenceService;

    /**
     * GET /extRef/{nameSpace}
     * Get external references with optional type filter
     *
     * @param nameSpace - Namespace filter
     * @param type - Optional external reference type (Process, Automation, PresentationFlow, Sampling, UXBuilder)
     * @return List of external references
     */
    @GetMapping
    public ResponseEntity<List<ExtRefDto>> getExternalReferences(
            @PathVariable("nameSpace") String nameSpace,
            @RequestParam(required = false) String type) {

        try {
            List<ExtRefDto> extRefs = externalReferenceService.getExternalReferences(nameSpace, type);
            return ResponseEntity.ok(extRefs);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * GET /extRef/{nameSpace}/type/{extRefType}/id/{extRefId}/version/{extRefVersion}/schemas
     * Get schemas by external reference
     *
     * @param nameSpace - Namespace filter
     * @param extRefType - External reference type (Process, Automation, PresentationFlow, Sampling, UXBuilder)
     * @param extRefId - External reference ID (UUID)
     * @param extRefVersion - External reference version
     * @return List of schemas associated with the external reference
     */
    @GetMapping("/type/{extRefType}/id/{extRefId}/version/{extRefVersion}/schemas")
    public ResponseEntity<List<SchemaDto>> getSchemasByExternalReference(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("extRefType") String extRefType,
            @PathVariable("extRefId") UUID extRefId,
            @PathVariable("extRefVersion") String extRefVersion) {

        try {
            List<SchemaDto> schemas = externalReferenceService.getSchemasByExternalReference(
                    nameSpace, extRefType, extRefId, extRefVersion);
            return ResponseEntity.ok(schemas);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * PUT /extRef/{nameSpace}/type/{extRefType}/name/{extRefName}/id/{extRefId}/version/{extRefVersion}/schemas
     * Create or update an external reference with associated schemas
     *
     * @param nameSpace - Namespace filter
     * @param extRefType - External reference type (Process, Automation, PresentationFlow, Sampling, UXBuilder)
     * @param extRefName - External reference name
     * @param extRefId - External reference ID (UUID)
     * @param extRefVersion - External reference version
     * @param request - REQUIRED request body containing list of schemas to associate (can be empty array)
     * @return Created or updated external reference
     */
    @PutMapping("/type/{extRefType}/name/{extRefName}/id/{extRefId}/version/{extRefVersion}/schemas")
    public ResponseEntity<ExtRefDto> createOrUpdateExternalReference(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("extRefType") String extRefType,
            @PathVariable("extRefName") String extRefName,
            @PathVariable("extRefId") UUID extRefId,
            @PathVariable("extRefVersion") String extRefVersion,
            @RequestBody ExtRefWithSchemasRequest request) {

        try {
            ExtRefDto extRef = externalReferenceService.createOrUpdateExternalReference(
                    nameSpace, extRefType, extRefName, extRefId, extRefVersion, request);
            return ResponseEntity.ok(extRef);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
