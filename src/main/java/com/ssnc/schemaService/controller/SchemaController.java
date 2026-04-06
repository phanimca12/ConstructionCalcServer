package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.constants.ApiConstants;
import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.dto.SchemaVersionDto;
import com.ssnc.schemaService.dto.SchemaWithVersionDto;
import com.ssnc.schemaService.service.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(ApiConstants.PATH_SCHEMAS_BASE)
public class SchemaController {

    @Autowired
    private SchemaService schemaService;

    /**
     * GET /schemas/{nameSpace}
     * Get schemas with optional filtering, sorting, and pagination
     *
     * @param nameSpace - Namespace filter
     * @param name - Optional name filter
     * @param type - Optional schema type filter
     * @param group - Optional group filter
     * @param modifiedByUser - Optional user filter
     * @param versionModifiedByUser - Optional version modified by user filter
     * @param sort - Optional sort parameter
     * @param withVersion - Optional version filter (none, draft, published)
     * @param pageable - Pagination parameters (page, size, sort)
     * @return List of schemas (without pagination metadata)
     */
    @GetMapping
    public ResponseEntity<List<SchemaDto>> getSchemas(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @RequestParam(value = ApiConstants.QUERY_PARAM_NAME, required = false) String name,
            @RequestParam(value = ApiConstants.QUERY_PARAM_TYPE, required = false) String type,
            @RequestParam(value = ApiConstants.QUERY_PARAM_GROUP, required = false) String group,
            @RequestParam(value = ApiConstants.QUERY_PARAM_MODIFIED_BY_USER, required = false) String modifiedByUser,
            @RequestParam(value = ApiConstants.QUERY_PARAM_VERSION_MODIFIED_BY_USER, required = false) String versionModifiedByUser,
            @RequestParam(value = ApiConstants.QUERY_PARAM_SORT, required = false) String sort,
            @RequestParam(value = ApiConstants.QUERY_PARAM_WITH_VERSION, required = false, defaultValue = ApiConstants.DEFAULT_WITH_VERSION) String withVersion,
            Pageable pageable) {
        Page<SchemaDto> schemas = schemaService.getSchemas(
                nameSpace, name, type, group, modifiedByUser, versionModifiedByUser, sort, withVersion, pageable);
        return ResponseEntity.ok(schemas.getContent());
    }

    /**
     * POST /schemas/{nameSpace}
     * Create a new schema with optional multipart content
     */
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> createSchema(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @RequestPart(value = ApiConstants.REQUEST_PART_SCHEMA) SchemaDto schema,
            @RequestPart(value = ApiConstants.REQUEST_PART_CONTENT, required = false) String content) {
        try {
            SchemaDto created = schemaService.createSchema(nameSpace, schema, content);
            return ResponseEntity.status(HttpStatus.OK).body(created);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorMessages.SCHEMA_CREATION_FAILED);
        }
    }

    /**
     * GET /schemas/{nameSpace}/{id}
     * Get schema by ID with optional version filtering and pagination
     *
     * @param nameSpace - Namespace filter
     * @param id - Schema ID
     * @param versionNumber - Optional version number filter
     * @param versionName - Optional version name filter (draft, published)
     * @param pageable - Pagination parameters (page, size, sort)
     * @return List of schema versions (without pagination metadata)
     */
    @GetMapping(ApiConstants.PATH_SCHEMA_BY_ID)
    public ResponseEntity<List<SchemaWithVersionDto>> getSchemaById(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @PathVariable(ApiConstants.PARAM_ID) String id,
            @RequestParam(value = ApiConstants.QUERY_PARAM_VERSION_NUMBER, required = false) String versionNumber,
            @RequestParam(value = ApiConstants.QUERY_PARAM_VERSION_NAME, required = false) String versionName,
            Pageable pageable) {

        Page<SchemaWithVersionDto> result = schemaService.getSchemasById(
                nameSpace,
                UUID.fromString(id),
                versionNumber,
                versionName,
                pageable
        );

        if (result.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result.getContent());
    }

    /**
     * PUT /schemas/{nameSpace}/{id}
     * Update an existing schema
     */
    @PutMapping(ApiConstants.PATH_SCHEMA_BY_ID)
    public ResponseEntity<SchemaDto> updateSchema(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @PathVariable(ApiConstants.PARAM_ID) String id,
            @RequestBody SchemaDto schemaDto) {
        SchemaDto updated = schemaService.updateSchema(nameSpace, UUID.fromString(id), schemaDto);
        return ResponseEntity.ok(updated);
    }

    /**
     * GET /schemas/{nameSpace}/{id}/version/{versionNumber}
     * Get specific schema version
     */
    @GetMapping(ApiConstants.PATH_SCHEMA_VERSION)
    public ResponseEntity<SchemaVersionDto> getSchemaVersion(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @PathVariable(ApiConstants.PARAM_ID) String id,
            @PathVariable(ApiConstants.PARAM_VERSION_NUMBER) String versionNumber) {
        return schemaService.getSchemaVersion(nameSpace, UUID.fromString(id), Integer.parseInt(versionNumber))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /schemas/{nameSpace}/{id}/version/{versionNumber}/publish
     * Publish a specific schema version
     */
    @PutMapping(ApiConstants.PATH_SCHEMA_VERSION_PUBLISH)
    public ResponseEntity<Void> publishSchemaVersion(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @PathVariable(ApiConstants.PARAM_ID) String id,
            @PathVariable(ApiConstants.PARAM_VERSION_NUMBER) String versionNumber) {
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
    @PutMapping(ApiConstants.PATH_SCHEMA_VERSION_UNPUBLISH)
    public ResponseEntity<Void> unPublishSchemaVersion(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @PathVariable(ApiConstants.PARAM_ID) String id) {
        schemaService.unPublishSchemaVersion(nameSpace, UUID.fromString(id));
        return ResponseEntity.ok().build();
    }

    /**
     * GET /schemas/{nameSpace}/{id}/version/published
     * Get published version of a schema
     */
    @GetMapping(ApiConstants.PATH_SCHEMA_VERSION_PUBLISHED)
    public ResponseEntity<SchemaVersionDto> getPublishedVersion(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @PathVariable(ApiConstants.PARAM_ID) String id) {
        return schemaService.getPublishedVersion(nameSpace, UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /schemas/{nameSpace}/{id}/version/draft
     * Get draft version of a schema
     */
    @GetMapping(ApiConstants.PATH_SCHEMA_VERSION_DRAFT)
    public ResponseEntity<SchemaVersionDto> getDraftVersion(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @PathVariable(ApiConstants.PARAM_ID) String id) {
        return schemaService.getDraftVersion(nameSpace, UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /schemas/{nameSpace}/{id}/version/published/content
     * Get content of published schema version
     */
    @GetMapping(value = ApiConstants.PATH_SCHEMA_VERSION_PUBLISHED_CONTENT, produces = MediaType.ALL_VALUE)
    public ResponseEntity<String> getPublishedContent(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @PathVariable(ApiConstants.PARAM_ID) String id) {
        return schemaService.getPublishedContent(nameSpace, UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /schemas/{nameSpace}/{id}/version/{versionNumber}/content
     * Get content of a specific schema version
     */
    @GetMapping(value = ApiConstants.PATH_SCHEMA_VERSION_CONTENT, produces = MediaType.ALL_VALUE)
    public ResponseEntity<String> getVersionContent(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @PathVariable(ApiConstants.PARAM_ID) String id,
            @PathVariable(ApiConstants.PARAM_VERSION_NUMBER) Integer versionNumber) {
        return schemaService.getVersionContent(nameSpace, UUID.fromString(id), versionNumber)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /schemas/{nameSpace}/{id}/version/draft/content
     * Get content of draft schema version
     */
    @GetMapping(value = ApiConstants.PATH_SCHEMA_VERSION_DRAFT_CONTENT, produces = MediaType.ALL_VALUE)
    public ResponseEntity<String> getDraftContent(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @PathVariable(ApiConstants.PARAM_ID) String id) {
        return schemaService.getDraftContent(nameSpace, UUID.fromString(id))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * PUT /schemas/{nameSpace}/{id}/version/draft/content
     * Update content of existing draft or create new version if draft doesn't exist
     */
    @PutMapping(value = ApiConstants.PATH_SCHEMA_VERSION_DRAFT_CONTENT, consumes = MediaType.ALL_VALUE)
    public ResponseEntity<SchemaVersionDto> updateDraftContent(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @PathVariable(ApiConstants.PARAM_ID) String id,
            @RequestBody String content) {
        SchemaVersionDto response = schemaService.updateDraftContent(nameSpace, UUID.fromString(id), content);
        return ResponseEntity.ok(response);
    }

    /**
     * PUT /schemas/{nameSpace}/{id}/lock
     * Lock a schema
     */
    @PutMapping(ApiConstants.PATH_SCHEMA_LOCK)
    public ResponseEntity<Void> lockSchema(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @PathVariable(ApiConstants.PARAM_ID) String id) {
        schemaService.lockSchema(nameSpace, UUID.fromString(id));
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /schemas/{nameSpace}/{id}/unLock
     * Unlock a schema
     */
    @PutMapping(ApiConstants.PATH_SCHEMA_UNLOCK)
    public ResponseEntity<Void> unlockSchema(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @PathVariable(ApiConstants.PARAM_ID) String id) {
        schemaService.unlockSchema(nameSpace, UUID.fromString(id));
        return ResponseEntity.ok().build();
    }

    /**
     * GET /schemas/{nameSpace}/{id}/extRefs
     * Get external references for a schema with pagination
     *
     * @param nameSpace - Namespace filter
     * @param id - Schema ID
     * @param pageable - Pagination parameters (page, size, sort)
     * @return List of external references (without pagination metadata)
     */
    @GetMapping(ApiConstants.PATH_SCHEMA_EXT_REFS)
    public ResponseEntity<List<ExtRefDto>> getSchemaExternalReferences(
            @PathVariable(ApiConstants.PARAM_NAME_SPACE) String nameSpace,
            @PathVariable(ApiConstants.PARAM_ID) String id,
            Pageable pageable) {
        Page<ExtRefDto> extRefs = schemaService.getExternalReferencesBySchemaId(nameSpace, UUID.fromString(id), pageable);
        return ResponseEntity.ok(extRefs.getContent());
    }
}
