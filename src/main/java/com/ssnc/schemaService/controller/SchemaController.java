package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.dto.SchemaVersionDto;
import com.ssnc.schemaService.dto.SchemaWithVersionDto;
import com.ssnc.schemaService.service.SchemaService;
import com.ssnc.schemaService.service.SchmXrefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/schemas/{nameSpace}")
public class SchemaController {

    @Autowired
    private SchemaService schemaService;

    @Autowired
    private SchmXrefService schmXrefService;

    /**
     * GET /schemas/{nameSpace}
     * Get schemas with optional filtering by type, group, and content type
     */
    @GetMapping
    public ResponseEntity<List<SchemaDto>> getSchemas(
            @PathVariable("nameSpace") String nameSpace,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String group,
            @RequestParam(required = false) String contentType,
            @RequestParam(required = false) boolean publishedOnly) {
        List<SchemaDto> schemas = schemaService.getSchemas(nameSpace, type, group, contentType, publishedOnly);
        return ResponseEntity.ok(schemas);
    }

    /**
     * POST /schemas/{nameSpace}
     * Create a new schema with optional multipart content
     */
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createSchema(
            @PathVariable("nameSpace") String nameSpace,
            @RequestPart(value = "schema") SchemaDto schema,
            @RequestPart(value = "content", required = false) String content) {
        try {
            SchemaDto created = schemaService.createSchema(nameSpace, schema, content);
            return ResponseEntity.status(HttpStatus.OK).body(created);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorMessages.SCHEMA_CREATION_FAILED);
        }
    }

    /**
     * GET /schemas/{nameSpace}/{id}
     * Get schema by ID with optional version filtering
     */
    @GetMapping("/{id}")
    public ResponseEntity<List<SchemaWithVersionDto>> getSchemaById(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("id") String id,
            @RequestParam(required = false) String versionNumber,
            @RequestParam(required = false) String versionName) {

        List<SchemaWithVersionDto> result = schemaService.getSchemasById(
                nameSpace,
                UUID.fromString(id),
                versionNumber,
                versionName
        );

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * PUT /schemas/{nameSpace}/{id}
     * Update an existing schema
     */
    @PutMapping("/{id}")
    public ResponseEntity<SchemaDto> updateSchema(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("id") String id,
            @RequestBody SchemaDto schemaDto) {
        SchemaDto updated = schemaService.updateSchema(nameSpace, UUID.fromString(id), schemaDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * GET /schemas/{nameSpace}/{id}/version/{versionNumber}
     * Get specific schema version
     */
    @GetMapping("/{id}/version/{versionNumber}")
    public ResponseEntity<SchemaVersionDto> getSchemaVersion(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("id") String id,
            @PathVariable("versionNumber") String versionNumber) {
        return schemaService.getSchemaVersion(nameSpace, UUID.fromString(id), Integer.parseInt(versionNumber))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /schemas/{nameSpace}/{id}/version/{versionNumber}/publish
     * Publish a specific schema version
     */
    @PutMapping("/{id}/version/{versionNumber}/publish")
    public ResponseEntity<Void> publishSchemaVersion(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("id") String id,
            @PathVariable("versionNumber") String versionNumber) {
        try {
            schemaService.publishSchemaVersion(nameSpace, UUID.fromString(id), Integer.parseInt(versionNumber));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * PUT /schemas/{nameSpace}/{id}/version/unpublish
     * Unpublish a schema by setting publish version to null
     */
    @PutMapping("/{id}/version/unpublish")
    public ResponseEntity<Void> unPublishSchemaVersion(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("id") String id) {
        schemaService.unPublishSchemaVersion(nameSpace, UUID.fromString(id));
        return ResponseEntity.ok().build();
    }

    /**
     * GET /schemas/{nameSpace}/{id}/version/published
     * Get published version of a schema
     */
    @GetMapping("/{id}/version/published")
    public ResponseEntity<SchemaVersionDto> getPublishedVersion(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("id") String id) {
        return schemaService.getPublishedVersion(nameSpace, UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /schemas/{nameSpace}/{id}/version/draft
     * Get draft version of a schema
     */
    @GetMapping("/{id}/version/draft")
    public ResponseEntity<SchemaVersionDto> getDraftVersion(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("id") String id) {
        return schemaService.getDraftVersion(nameSpace, UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /schemas/{nameSpace}/{id}/version/published/content
     * Get content of published schema version
     */
    @GetMapping(value = "/{id}/version/published/content", produces = MediaType.ALL_VALUE)
    public ResponseEntity<String> getPublishedContent(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("id") String id) {
        return schemaService.getPublishedContent(nameSpace, UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /schemas/{nameSpace}/{id}/version/{versionNumber}/content
     * Get content of a specific schema version
     */
    @GetMapping(value = "/{id}/version/{versionNumber}/content", produces = MediaType.ALL_VALUE)
    public ResponseEntity<String> getVersionContent(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("id") String id,
            @PathVariable("versionNumber") Integer versionNumber) {
        return schemaService.getVersionContent(nameSpace, UUID.fromString(id), versionNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /schemas/{nameSpace}/{id}/version/draft/content
     * Get content of draft schema version
     */
    @GetMapping(value = "/{id}/version/draft/content", produces = MediaType.ALL_VALUE)
    public ResponseEntity<String> getDraftContent(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("id") String id) {
        return schemaService.getDraftContent(nameSpace, UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /schemas/{nameSpace}/{id}/version/draft/content
     * Update content of existing draft or create new version if draft doesn't exist
     */
    @PutMapping(value = "/{id}/version/draft/content", consumes = MediaType.ALL_VALUE)
    public ResponseEntity<SchemaVersionDto> updateDraftContent(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("id") String id,
            @RequestBody String content) {
        SchemaVersionDto response = schemaService.updateDraftContent(nameSpace, UUID.fromString(id), content);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /schemas/{nameSpace}/{id}/extRefs
     * Get external references for a schema
     */
    @GetMapping("/{id}/extRefs")
    public ResponseEntity<List<ExtRefDto>> getExtRefsForSchema(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("id") String id) {
        List<ExtRefDto> extRefs = schmXrefService.getExtRefsForSchema(nameSpace, UUID.fromString(id));
        return ResponseEntity.ok(extRefs);
    }

    /**
     * PUT /schemas/{nameSpace}/{id}/lock
     * Lock a schema
     */
    @PutMapping("/{id}/lock")
    public ResponseEntity<Void> lockSchema(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("id") String id) {
        schemaService.lockSchema(nameSpace, UUID.fromString(id));
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /schemas/{nameSpace}/{id}/unLock
     * Unlock a schema
     */
    @PutMapping("/{id}/unLock")
    public ResponseEntity<Void> unlockSchema(
            @PathVariable("nameSpace") String nameSpace,
            @PathVariable("id") String id) {
        schemaService.unlockSchema(nameSpace, UUID.fromString(id));
        return ResponseEntity.ok().build();
    }
}
