package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.service.SchmXrefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/extRef/{nameSpace}")
public class ExtRefController {

    @Autowired
    private SchmXrefService schmXrefService;

    /**
     * GET /extRef/{nameSpace}
     * Get all external references with optional type filter
     */
    @GetMapping
    public ResponseEntity<List<ExtRefDto>> getExtRefs(
            @PathVariable("nameSpace") String nameSpace,
            @RequestParam(required = false) String type) {
        List<ExtRefDto> extRefs = schmXrefService.getExtRefs(nameSpace, type);
        return ResponseEntity.ok(extRefs);
    }

    /**
     * GET /extRef/{nameSpace}/type/{extRefType}/name/{extRefName}/schemas
     * Get schemas associated with a specific external reference (all versions)
     */
    @GetMapping("/type/{extRefType}/name/{extRefName}/schemas")
    public ResponseEntity<List<SchemaDto>> getSchemasForExtRef(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("extRefType") String extRefType,
            @PathVariable("extRefName") String extRefName) {
        List<SchemaDto> schemas = schmXrefService.getSchemasForExtRef(nameSpace, extRefType, extRefName, null);
        return ResponseEntity.ok(schemas);
    }

    /**
     * GET /extRef/{nameSpace}/type/{extRefType}/name/{extRefName}/version/{extRefVersion}/schemas
     * Get schemas associated with a specific external reference version
     */
    @GetMapping("/type/{extRefType}/name/{extRefName}/version/{extRefVersion}/schemas")
    public ResponseEntity<List<SchemaDto>> getSchemasForExtRefWithVersion(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("extRefType") String extRefType,
            @PathVariable("extRefName") String extRefName,
            @PathVariable("extRefVersion") String extRefVersion) {
        List<SchemaDto> schemas = schmXrefService.getSchemasForExtRef(nameSpace, extRefType, extRefName, extRefVersion);
        return ResponseEntity.ok(schemas);
    }

    /**
     * PUT /extRef/{nameSpace}/type/{extRefType}/name/{extRefName}/schemas
     * Associate schemas with an external reference (no specific version)
     */
    @PutMapping("/type/{extRefType}/name/{extRefName}/schemas")
    public ResponseEntity<List<SchemaDto>> associateSchemasWithExtRef(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("extRefType") String extRefType,
            @PathVariable("extRefName") String extRefName,
            @RequestBody List<SchemaDto> schemas) {
        List<SchemaDto> result = schmXrefService.associateSchemasWithExtRef(nameSpace, extRefType, extRefName, null, schemas);
        return ResponseEntity.ok(result);
    }

    /**
     * PUT /extRef/{nameSpace}/type/{extRefType}/name/{extRefName}/version/{extRefVersion}/schemas
     * Associate schemas with a specific external reference version
     */
    @PutMapping("/type/{extRefType}/name/{extRefName}/version/{extRefVersion}/schemas")
    public ResponseEntity<List<SchemaDto>> associateSchemasWithExtRefWithVersion(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("extRefType") String extRefType,
            @PathVariable("extRefName") String extRefName,
            @PathVariable("extRefVersion") String extRefVersion,
            @RequestBody List<SchemaDto> schemas) {
        List<SchemaDto> result = schmXrefService.associateSchemasWithExtRef(nameSpace, extRefType, extRefName, extRefVersion, schemas);
        return ResponseEntity.ok(result);
    }
}
