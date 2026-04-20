package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.PagedResponse;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.dto.SchemaVersionDto;
import com.ssnc.schemaService.dto.SchemaWithVersionDto;
import com.ssnc.schemaService.service.SchemaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SchemaControllerTest {

    @Mock
    private SchemaService schemaService;

    @InjectMocks
    private SchemaController schemaController;

    private UUID testSchemaId;
    private String testNamespace;
    private SchemaDto testSchemaDto;
    private SchemaVersionDto testVersionDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testSchemaId = UUID.randomUUID();
        testNamespace = "testNamespace";

        testSchemaDto = new SchemaDto();
        testSchemaDto.setId(testSchemaId);
        testSchemaDto.setName("Test Schema");
        testSchemaDto.setDescription("Test Description");
        testSchemaDto.setCreatedByUser("testUser");
        testSchemaDto.setModifiedByUser("testUser");
        testSchemaDto.setCreateDateTime(LocalDateTime.now());
        testSchemaDto.setModifiedDateTime(LocalDateTime.now());

        testVersionDto = new SchemaVersionDto();
        testVersionDto.setVersionNumber(1);
        testVersionDto.setIsDraft(false);
        testVersionDto.setCreatedByUser("testUser");
        testVersionDto.setModifiedByUser("testUser");
    }

    @Test
    void testGetSchemas() {
        Pageable pageable = PageRequest.of(0, 20);
        List<SchemaDto> schemas = Arrays.asList(testSchemaDto);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "none", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<PagedResponse<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, null, "none", false, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(schemas, response.getBody().getContent());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals(1, response.getBody().getTotalPages());
        assertEquals(0, response.getBody().getCurrentPage());
        assertEquals(20, response.getBody().getPageSize());
        assertFalse(response.getBody().isHasNext());
        assertFalse(response.getBody().isHasPrevious());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, null, "none", pageable);
    }

    @Test
    void testGetSchemasWithFilters() {
        Pageable pageable = PageRequest.of(0, 20);
        List<SchemaDto> schemas = Arrays.asList(testSchemaDto);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, "testSchema", "FormData", "group1", "user1", null, "nameAsc", "none", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<PagedResponse<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, "testSchema", "FormData", "group1", "user1", null, "nameAsc", "none", false, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(schemas, response.getBody().getContent());
        verify(schemaService).getSchemas(testNamespace, "testSchema", "FormData", "group1", "user1", null, "nameAsc", "none", pageable);
    }

    @Test
    void testGetSchemasWithSort() {
        Pageable pageable = PageRequest.of(0, 20);
        List<SchemaDto> schemas = Arrays.asList(testSchemaDto);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, null, null, null, null, null, "versionUpdateDesc", "latest", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<PagedResponse<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, "versionUpdateDesc", "latest", false, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(schemas, response.getBody().getContent());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, "versionUpdateDesc", "latest", pageable);
    }

    @Test
    void testCreateSchema_Success() throws IOException {
        when(schemaService.createSchema(eq(testNamespace), any(SchemaDto.class), eq("content")))
                .thenReturn(testSchemaDto);

        ResponseEntity<?> response = schemaController.createSchema(
                testNamespace, testSchemaDto, "content");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testSchemaDto, response.getBody());
        verify(schemaService).createSchema(testNamespace, testSchemaDto, "content");
    }

    @Test
    void testCreateSchema_IOException() throws IOException {
        when(schemaService.createSchema(eq(testNamespace), any(SchemaDto.class), anyString()))
                .thenThrow(new IOException("Test exception"));

        ResponseEntity<?> response = schemaController.createSchema(
                testNamespace, testSchemaDto, "content");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ErrorMessages.SCHEMA_CREATION_FAILED, response.getBody());
    }

    @Test
    void testGetSchemaById_Found() {
        Pageable pageable = PageRequest.of(0, 20);
        SchemaWithVersionDto schemaWithVersion = new SchemaWithVersionDto();
        schemaWithVersion.setSchema(testSchemaDto);
        List<SchemaWithVersionDto> result = Collections.singletonList(schemaWithVersion);
        Page<SchemaWithVersionDto> expectedPage = new PageImpl<>(result, pageable, result.size());

        when(schemaService.getSchemasById(testNamespace, testSchemaId, null, null, pageable))
                .thenReturn(expectedPage);

        ResponseEntity<List<SchemaWithVersionDto>> response = schemaController.getSchemaById(
                testNamespace, testSchemaId.toString(), null, null, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(result, response.getBody());
    }

    @Test
    void testGetSchemaById_NotFound() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<SchemaWithVersionDto> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(schemaService.getSchemasById(testNamespace, testSchemaId, null, null, pageable))
                .thenReturn(emptyPage);

        ResponseEntity<List<SchemaWithVersionDto>> response = schemaController.getSchemaById(
                testNamespace, testSchemaId.toString(), null, null, pageable);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateSchema() {
        when(schemaService.updateSchema(testNamespace, testSchemaId, testSchemaDto))
                .thenReturn(testSchemaDto);

        ResponseEntity<SchemaDto> response = schemaController.updateSchema(
                testNamespace, testSchemaId.toString(), testSchemaDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testSchemaDto, response.getBody());
        verify(schemaService).updateSchema(testNamespace, testSchemaId, testSchemaDto);
    }

    @Test
    void testGetSchemaVersion_Found() {
        when(schemaService.getSchemaVersion(testNamespace, testSchemaId, 1))
                .thenReturn(Optional.of(testVersionDto));

        ResponseEntity<SchemaVersionDto> response = schemaController.getSchemaVersion(
                testNamespace, testSchemaId.toString(), "1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testVersionDto, response.getBody());
    }

    @Test
    void testGetSchemaVersion_NotFound() {
        when(schemaService.getSchemaVersion(testNamespace, testSchemaId, 1))
                .thenReturn(Optional.empty());

        ResponseEntity<SchemaVersionDto> response = schemaController.getSchemaVersion(
                testNamespace, testSchemaId.toString(), "1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testPublishSchemaVersion_Success() {
        doNothing().when(schemaService).publishSchemaVersion(testNamespace, testSchemaId, 1);

        ResponseEntity<Void> response = schemaController.publishSchemaVersion(
                testNamespace, testSchemaId.toString(), "1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(schemaService).publishSchemaVersion(testNamespace, testSchemaId, 1);
    }

    @Test
    void testPublishSchemaVersion_NotFound() {
        doThrow(new IllegalArgumentException("Version not found"))
                .when(schemaService).publishSchemaVersion(testNamespace, testSchemaId, 1);

        ResponseEntity<Void> response = schemaController.publishSchemaVersion(
                testNamespace, testSchemaId.toString(), "1");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUnPublishSchemaVersion() {
        doNothing().when(schemaService).unPublishSchemaVersion(testNamespace, testSchemaId);

        ResponseEntity<Void> response = schemaController.unPublishSchemaVersion(
                testNamespace, testSchemaId.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(schemaService).unPublishSchemaVersion(testNamespace, testSchemaId);
    }

    @Test
    void testUnPublishSchemaVersion_SchemaInUse() {
        doThrow(new IllegalStateException("Schema cannot be unpublished as it is in use by external references"))
                .when(schemaService).unPublishSchemaVersion(testNamespace, testSchemaId);

        assertThrows(IllegalStateException.class, () ->
                schemaController.unPublishSchemaVersion(testNamespace, testSchemaId.toString())
        );

        verify(schemaService).unPublishSchemaVersion(testNamespace, testSchemaId);
    }

    @Test
    void testGetPublishedVersion_Found() {
        when(schemaService.getPublishedVersion(testNamespace, testSchemaId))
                .thenReturn(Optional.of(testVersionDto));

        ResponseEntity<SchemaVersionDto> response = schemaController.getPublishedVersion(
                testNamespace, testSchemaId.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testVersionDto, response.getBody());
    }

    @Test
    void testGetDraftVersion_Found() {
        when(schemaService.getDraftVersion(testNamespace, testSchemaId))
                .thenReturn(Optional.of(testVersionDto));

        ResponseEntity<SchemaVersionDto> response = schemaController.getDraftVersion(
                testNamespace, testSchemaId.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testVersionDto, response.getBody());
    }

    @Test
    void testGetPublishedContent_Found() {
        String expectedContent = "published content";
        when(schemaService.getPublishedContent(testNamespace, testSchemaId))
                .thenReturn(Optional.of(expectedContent));

        ResponseEntity<String> response = schemaController.getPublishedContent(
                testNamespace, testSchemaId.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedContent, response.getBody());
    }

    @Test
    void testGetVersionContent_Found() {
        String expectedContent = "version content";
        when(schemaService.getVersionContent(testNamespace, testSchemaId, 1))
                .thenReturn(Optional.of(expectedContent));

        ResponseEntity<String> response = schemaController.getVersionContent(
                testNamespace, testSchemaId.toString(), 1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedContent, response.getBody());
    }

    @Test
    void testGetDraftContent_Found() {
        String expectedContent = "draft content";
        when(schemaService.getDraftContent(testNamespace, testSchemaId))
                .thenReturn(Optional.of(expectedContent));

        ResponseEntity<String> response = schemaController.getDraftContent(
                testNamespace, testSchemaId.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedContent, response.getBody());
    }

    @Test
    void testUpdateDraftContent() {
        String content = "updated draft content";
        when(schemaService.updateDraftContent(testNamespace, testSchemaId, content))
                .thenReturn(testVersionDto);

        ResponseEntity<SchemaVersionDto> response = schemaController.updateDraftContent(
                testNamespace, testSchemaId.toString(), content);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testVersionDto, response.getBody());
        verify(schemaService).updateDraftContent(testNamespace, testSchemaId, content);
    }

    @Test
    void testLockSchema() {
        doNothing().when(schemaService).lockSchema(testNamespace, testSchemaId);

        ResponseEntity<Void> response = schemaController.lockSchema(
                testNamespace, testSchemaId.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(schemaService).lockSchema(testNamespace, testSchemaId);
    }

    @Test
    void testUnlockSchema() {
        doNothing().when(schemaService).unlockSchema(testNamespace, testSchemaId);

        ResponseEntity<Void> response = schemaController.unlockSchema(
                testNamespace, testSchemaId.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(schemaService).unlockSchema(testNamespace, testSchemaId);
    }

    @Test
    void testGetSchemas_WithVersionDraft() {
        Pageable pageable = PageRequest.of(0, 20);
        SchemaDto schema1 = new SchemaDto();
        schema1.setId(UUID.randomUUID());
        schema1.setName("Schema 1");
        schema1.setDraft(1);

        // Set version object for draft
        SchemaVersionDto draftVersion = new SchemaVersionDto();
        draftVersion.setVersionNumber(1);
        draftVersion.setIsDraft(true);
        draftVersion.setModifiedByUser("testUser");
        schema1.setVersion(draftVersion);

        List<SchemaDto> schemas = Arrays.asList(schema1);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "draft", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<PagedResponse<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, null, "draft", false, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(schemas, response.getBody().getContent());
        assertEquals(1, response.getBody().getContent().size());
        assertNotNull(response.getBody().getContent().get(0).getVersion());
        assertTrue(response.getBody().getContent().get(0).getVersion().getIsDraft());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, null, "draft", pageable);
    }

    @Test
    void testGetSchemas_WithVersionPublished() {
        Pageable pageable = PageRequest.of(0, 20);
        SchemaDto schema1 = new SchemaDto();
        schema1.setId(UUID.randomUUID());
        schema1.setName("Schema 1");
        schema1.setPublished(2);

        // Set version object for published
        SchemaVersionDto publishedVersion = new SchemaVersionDto();
        publishedVersion.setVersionNumber(2);
        publishedVersion.setIsDraft(false);
        publishedVersion.setModifiedByUser("testUser");
        schema1.setVersion(publishedVersion);

        List<SchemaDto> schemas = Arrays.asList(schema1);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "published", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<PagedResponse<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, null, "published", false, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(schemas, response.getBody().getContent());
        assertEquals(1, response.getBody().getContent().size());
        assertNotNull(response.getBody().getContent().get(0).getVersion());
        assertFalse(response.getBody().getContent().get(0).getVersion().getIsDraft());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, null, "published", pageable);
    }

    @Test
    void testGetSchemas_WithVersionLatest() {
        Pageable pageable = PageRequest.of(0, 20);
        SchemaDto schema1 = new SchemaDto();
        schema1.setId(UUID.randomUUID());
        schema1.setName("Schema 1");
        schema1.setDraft(2);
        schema1.setPublished(1);

        // Set version object for latest (draft takes precedence)
        SchemaVersionDto latestVersion = new SchemaVersionDto();
        latestVersion.setVersionNumber(2);
        latestVersion.setIsDraft(true);
        latestVersion.setModifiedByUser("testUser");
        schema1.setVersion(latestVersion);

        List<SchemaDto> schemas = Arrays.asList(schema1);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "latest", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<PagedResponse<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, null, "latest", false, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(schemas, response.getBody().getContent());
        assertEquals(1, response.getBody().getContent().size());
        assertNotNull(response.getBody().getContent().get(0).getVersion());
        assertEquals(2, response.getBody().getContent().get(0).getVersion().getVersionNumber());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, null, "latest", pageable);
    }

    @Test
    void testGetSchemas_WithModifiedByUserFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        List<SchemaDto> schemas = Arrays.asList(testSchemaDto);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, null, null, null, "john.doe", null, null, "none", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<PagedResponse<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, "john.doe", null, null, "none", false, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(schemas, response.getBody().getContent());
        verify(schemaService).getSchemas(testNamespace, null, null, null, "john.doe", null, null, "none", pageable);
    }

    @Test
    void testGetSchemas_WithVersionModifiedByUserFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        List<SchemaDto> schemas = Arrays.asList(testSchemaDto);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, null, null, null, null, "jane.smith", null, "none", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<PagedResponse<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, "jane.smith", null, "none", false, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(schemas, response.getBody().getContent());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, "jane.smith", null, "none", pageable);
    }

    @Test
    void testGetSchemaExternalReferences_Success() {
        Pageable pageable = PageRequest.of(0, 20);
        String extRefId1 = "REF-123456";
        String extRefId2 = "REF-789012";

        ExtRefDto extRef1 = new ExtRefDto();
        extRef1.setExtRefId(extRefId1);
        extRef1.setExtRefName("External Reference 1");
        extRef1.setExtRefType("PROCESS");
        extRef1.setExtRefVersion("1.0");

        ExtRefDto extRef2 = new ExtRefDto();
        extRef2.setExtRefId(extRefId2);
        extRef2.setExtRefName("External Reference 2");
        extRef2.setExtRefType("AUTOMATION");
        extRef2.setExtRefVersion("2.0");

        List<ExtRefDto> extRefs = Arrays.asList(extRef1, extRef2);
        Page<ExtRefDto> expectedPage = new PageImpl<>(extRefs, pageable, extRefs.size());

        when(schemaService.getExternalReferencesBySchemaId(testNamespace, testSchemaId, pageable))
                .thenReturn(expectedPage);

        ResponseEntity<List<ExtRefDto>> response = schemaController.getSchemaExternalReferences(
                testNamespace, testSchemaId.toString(), pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(extRefs, response.getBody());
        assertEquals(2, response.getBody().size());
        verify(schemaService).getExternalReferencesBySchemaId(testNamespace, testSchemaId, pageable);
    }

    @Test
    void testGetSchemaExternalReferences_EmptyList() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<ExtRefDto> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(schemaService.getExternalReferencesBySchemaId(testNamespace, testSchemaId, pageable))
                .thenReturn(emptyPage);

        ResponseEntity<List<ExtRefDto>> response = schemaController.getSchemaExternalReferences(
                testNamespace, testSchemaId.toString(), pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(0, response.getBody().size());
        verify(schemaService).getExternalReferencesBySchemaId(testNamespace, testSchemaId, pageable);
    }

    @Test
    void testImportSchema_Success() throws IOException {
        String content = "test schema content";
        when(schemaService.importSchema(eq(testNamespace), any(SchemaDto.class), eq(content)))
                .thenReturn(testSchemaDto);

        ResponseEntity<?> response = schemaController.importSchema(
                testNamespace, testSchemaDto, content);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testSchemaDto, response.getBody());
        verify(schemaService).importSchema(testNamespace, testSchemaDto, content);
    }

    @Test
    void testImportSchema_DuplicateName_ReturnsBadRequest() throws IOException {
        String content = "test schema content";
        when(schemaService.importSchema(eq(testNamespace), any(SchemaDto.class), eq(content)))
                .thenThrow(new IllegalArgumentException(ErrorMessages.SCHEMA_IMPORT_EXISTS));

        ResponseEntity<?> response = schemaController.importSchema(
                testNamespace, testSchemaDto, content);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(ErrorMessages.SCHEMA_IMPORT_EXISTS, response.getBody());
        verify(schemaService).importSchema(testNamespace, testSchemaDto, content);
    }

    @Test
    void testImportSchema_MissingContent_ReturnsBadRequest() throws IOException {
        String content = "test content";
        when(schemaService.importSchema(eq(testNamespace), any(SchemaDto.class), eq(content)))
                .thenThrow(new IllegalArgumentException("Content is required for schema import"));

        ResponseEntity<?> response = schemaController.importSchema(
                testNamespace, testSchemaDto, content);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Content is required for schema import", response.getBody());
        verify(schemaService).importSchema(testNamespace, testSchemaDto, content);
    }

    @Test
    void testImportSchema_StateException_ReturnsConflict() throws IOException {
        String content = "test schema content";
        when(schemaService.importSchema(eq(testNamespace), any(SchemaDto.class), eq(content)))
                .thenThrow(new IllegalStateException("Failed to create initial version during import"));

        ResponseEntity<?> response = schemaController.importSchema(
                testNamespace, testSchemaDto, content);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Failed to create initial version during import", response.getBody());
        verify(schemaService).importSchema(testNamespace, testSchemaDto, content);
    }

    @Test
    void testImportSchema_IOException_ReturnsInternalServerError() throws IOException {
        String content = "test schema content";
        when(schemaService.importSchema(eq(testNamespace), any(SchemaDto.class), eq(content)))
                .thenThrow(new IOException("Test IO exception"));

        ResponseEntity<?> response = schemaController.importSchema(
                testNamespace, testSchemaDto, content);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals(ErrorMessages.SCHEMA_CREATION_FAILED, response.getBody());
        verify(schemaService).importSchema(testNamespace, testSchemaDto, content);
    }

    @Test
    void testImportSchemas_BulkImport_ReturnsOk() {
        com.ssnc.schemaService.dto.SchemaImportRequest request1 = new com.ssnc.schemaService.dto.SchemaImportRequest();
        request1.setSchema(testSchemaDto);
        request1.setContent("content1");

        SchemaDto schema2 = new SchemaDto();
        schema2.setName("Schema2");
        com.ssnc.schemaService.dto.SchemaImportRequest request2 = new com.ssnc.schemaService.dto.SchemaImportRequest();
        request2.setSchema(schema2);
        request2.setContent("content2");

        List<com.ssnc.schemaService.dto.SchemaImportRequest> requests = Arrays.asList(request1, request2);

        com.ssnc.schemaService.dto.SchemaImportResponse response1 = new com.ssnc.schemaService.dto.SchemaImportResponse(testSchemaDto);
        com.ssnc.schemaService.dto.SchemaImportResponse response2 = new com.ssnc.schemaService.dto.SchemaImportResponse(schema2);
        List<com.ssnc.schemaService.dto.SchemaImportResponse> expectedResponses = Arrays.asList(response1, response2);

        when(schemaService.importSchemas(testNamespace, requests)).thenReturn(expectedResponses);

        ResponseEntity<List<com.ssnc.schemaService.dto.SchemaImportResponse>> response =
                schemaController.importSchemas(testNamespace, requests);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().get(0).isSuccess());
        assertTrue(response.getBody().get(1).isSuccess());
        verify(schemaService).importSchemas(testNamespace, requests);
    }

    @Test
    void testImportSchemas_BulkImportWithFailures_ReturnsOk() {
        com.ssnc.schemaService.dto.SchemaImportRequest request1 = new com.ssnc.schemaService.dto.SchemaImportRequest();
        request1.setSchema(testSchemaDto);
        request1.setContent("content1");

        SchemaDto schema2 = new SchemaDto();
        schema2.setName("Schema2");
        com.ssnc.schemaService.dto.SchemaImportRequest request2 = new com.ssnc.schemaService.dto.SchemaImportRequest();
        request2.setSchema(schema2);
        request2.setContent("content2");

        List<com.ssnc.schemaService.dto.SchemaImportRequest> requests = Arrays.asList(request1, request2);

        com.ssnc.schemaService.dto.SchemaImportResponse response1 = new com.ssnc.schemaService.dto.SchemaImportResponse(testSchemaDto);
        com.ssnc.schemaService.dto.SchemaImportResponse response2 = new com.ssnc.schemaService.dto.SchemaImportResponse("Schema2", "Schema already exists");
        List<com.ssnc.schemaService.dto.SchemaImportResponse> expectedResponses = Arrays.asList(response1, response2);

        when(schemaService.importSchemas(testNamespace, requests)).thenReturn(expectedResponses);

        ResponseEntity<List<com.ssnc.schemaService.dto.SchemaImportResponse>> response =
                schemaController.importSchemas(testNamespace, requests);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().get(0).isSuccess());
        assertFalse(response.getBody().get(1).isSuccess());
        assertEquals("Schema already exists", response.getBody().get(1).getErrorMessage());
        verify(schemaService).importSchemas(testNamespace, requests);
    }

    @Test
    void testExportSchema_Success_ReturnsOk() {
        com.ssnc.schemaService.dto.SchemaExportDto exportDto = new com.ssnc.schemaService.dto.SchemaExportDto();
        exportDto.setName("Test Schema");
        exportDto.setDescription("Test Description");
        exportDto.setSchemaType("JSON");
        exportDto.setContentType("application/json");
        exportDto.setSchmGroup("group1");
        exportDto.setContent("test content");

        when(schemaService.exportSchema(testNamespace, testSchemaId)).thenReturn(exportDto);

        ResponseEntity<?> response = schemaController.exportSchema(testNamespace, testSchemaId.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(exportDto, response.getBody());
        verify(schemaService).exportSchema(testNamespace, testSchemaId);
    }

    @Test
    void testExportSchema_SchemaNotFound_ReturnsNotFound() {
        when(schemaService.exportSchema(testNamespace, testSchemaId))
                .thenThrow(new IllegalArgumentException("Schema not found"));

        ResponseEntity<?> response = schemaController.exportSchema(testNamespace, testSchemaId.toString());

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Schema not found", response.getBody());
        verify(schemaService).exportSchema(testNamespace, testSchemaId);
    }

    @Test
    void testExportSchema_NoPublishedVersion_ReturnsBadRequest() {
        when(schemaService.exportSchema(testNamespace, testSchemaId))
                .thenThrow(new IllegalStateException("Schema does not have a published version"));

        ResponseEntity<?> response = schemaController.exportSchema(testNamespace, testSchemaId.toString());

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Schema does not have a published version", response.getBody());
        verify(schemaService).exportSchema(testNamespace, testSchemaId);
    }

    @Test
    void testExportSchemas_BulkExport_ReturnsOk() {
        UUID schema2Id = UUID.randomUUID();

        com.ssnc.schemaService.dto.SchemaExportRequest request1 = new com.ssnc.schemaService.dto.SchemaExportRequest();
        request1.setSchmId(testSchemaId);

        com.ssnc.schemaService.dto.SchemaExportRequest request2 = new com.ssnc.schemaService.dto.SchemaExportRequest();
        request2.setName("Schema2");

        List<com.ssnc.schemaService.dto.SchemaExportRequest> requests = Arrays.asList(request1, request2);

        com.ssnc.schemaService.dto.SchemaExportDto exportDto1 = new com.ssnc.schemaService.dto.SchemaExportDto();
        exportDto1.setName("Test Schema");
        exportDto1.setContent("content1");

        com.ssnc.schemaService.dto.SchemaExportDto exportDto2 = new com.ssnc.schemaService.dto.SchemaExportDto();
        exportDto2.setName("Schema2");
        exportDto2.setContent("content2");

        com.ssnc.schemaService.dto.SchemaExportResponse response1 = new com.ssnc.schemaService.dto.SchemaExportResponse(exportDto1);
        com.ssnc.schemaService.dto.SchemaExportResponse response2 = new com.ssnc.schemaService.dto.SchemaExportResponse(exportDto2);
        List<com.ssnc.schemaService.dto.SchemaExportResponse> expectedResponses = Arrays.asList(response1, response2);

        when(schemaService.exportSchemas(testNamespace, requests)).thenReturn(expectedResponses);

        ResponseEntity<List<com.ssnc.schemaService.dto.SchemaExportResponse>> response =
                schemaController.exportSchemas(testNamespace, requests);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().get(0).isSuccess());
        assertTrue(response.getBody().get(1).isSuccess());
        verify(schemaService).exportSchemas(testNamespace, requests);
    }

    @Test
    void testExportSchemas_BulkExportWithFailures_ReturnsOk() {
        com.ssnc.schemaService.dto.SchemaExportRequest request1 = new com.ssnc.schemaService.dto.SchemaExportRequest();
        request1.setSchmId(testSchemaId);

        com.ssnc.schemaService.dto.SchemaExportRequest request2 = new com.ssnc.schemaService.dto.SchemaExportRequest();
        request2.setName("NonExistentSchema");

        List<com.ssnc.schemaService.dto.SchemaExportRequest> requests = Arrays.asList(request1, request2);

        com.ssnc.schemaService.dto.SchemaExportDto exportDto1 = new com.ssnc.schemaService.dto.SchemaExportDto();
        exportDto1.setName("Test Schema");
        exportDto1.setContent("content1");

        com.ssnc.schemaService.dto.SchemaExportResponse response1 = new com.ssnc.schemaService.dto.SchemaExportResponse(exportDto1);
        com.ssnc.schemaService.dto.SchemaExportResponse response2 = new com.ssnc.schemaService.dto.SchemaExportResponse("NonExistentSchema", "Not Found: Schema not found");
        List<com.ssnc.schemaService.dto.SchemaExportResponse> expectedResponses = Arrays.asList(response1, response2);

        when(schemaService.exportSchemas(testNamespace, requests)).thenReturn(expectedResponses);

        ResponseEntity<List<com.ssnc.schemaService.dto.SchemaExportResponse>> response =
                schemaController.exportSchemas(testNamespace, requests);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().get(0).isSuccess());
        assertFalse(response.getBody().get(1).isSuccess());
        assertEquals("Not Found: Schema not found", response.getBody().get(1).getErrorMessage());
        verify(schemaService).exportSchemas(testNamespace, requests);
    }

    @Test
    void testImportSchemas_ValidationError_NullSchema() {
        com.ssnc.schemaService.dto.SchemaImportRequest request = new com.ssnc.schemaService.dto.SchemaImportRequest();
        request.setSchema(null);
        request.setContent("content");

        com.ssnc.schemaService.dto.SchemaImportResponse errorResponse =
            new com.ssnc.schemaService.dto.SchemaImportResponse(null, "Bad Request: Schema information is required");

        when(schemaService.importSchemas(eq(testNamespace), anyList()))
            .thenReturn(Arrays.asList(errorResponse));

        ResponseEntity<List<com.ssnc.schemaService.dto.SchemaImportResponse>> response =
                schemaController.importSchemas(testNamespace, Arrays.asList(request));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertFalse(response.getBody().get(0).isSuccess());
        assertNull(response.getBody().get(0).getSchemaName());
    }

    @Test
    void testImportSchemas_ValidationError_NullSchemaName() {
        SchemaDto schemaWithoutName = new SchemaDto();
        schemaWithoutName.setName(null);

        com.ssnc.schemaService.dto.SchemaImportRequest request = new com.ssnc.schemaService.dto.SchemaImportRequest();
        request.setSchema(schemaWithoutName);
        request.setContent("content");

        com.ssnc.schemaService.dto.SchemaImportResponse errorResponse =
            new com.ssnc.schemaService.dto.SchemaImportResponse(null, "Bad Request: Schema name is required");

        when(schemaService.importSchemas(eq(testNamespace), anyList()))
            .thenReturn(Arrays.asList(errorResponse));

        ResponseEntity<List<com.ssnc.schemaService.dto.SchemaImportResponse>> response =
                schemaController.importSchemas(testNamespace, Arrays.asList(request));

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertFalse(response.getBody().get(0).isSuccess());
        assertTrue(response.getBody().get(0).getErrorMessage().contains("Schema name is required"));
    }

    @Test
    void testGetSchemas_Unpaged() {
        SchemaDto schema1 = new SchemaDto();
        schema1.setId(UUID.randomUUID());
        schema1.setName("Schema 1");

        SchemaDto schema2 = new SchemaDto();
        schema2.setId(UUID.randomUUID());
        schema2.setName("Schema 2");

        SchemaDto schema3 = new SchemaDto();
        schema3.setId(UUID.randomUUID());
        schema3.setName("Schema 3");

        List<SchemaDto> allSchemas = Arrays.asList(schema1, schema2, schema3);
        Page<SchemaDto> unpagedPage = new PageImpl<>(allSchemas, Pageable.unpaged(), allSchemas.size());

        when(schemaService.getSchemas(eq(testNamespace), isNull(), isNull(), isNull(), isNull(), isNull(),
                isNull(), eq("none"), eq(Pageable.unpaged())))
                .thenReturn(unpagedPage);

        Pageable pageable = PageRequest.of(0, 20);
        ResponseEntity<PagedResponse<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, null, "none", true, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().getContent().size());
        assertEquals(3, response.getBody().getTotalElements());
        assertEquals(allSchemas, response.getBody().getContent());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, null, "none", Pageable.unpaged());
    }

    @Test
    void testGetSchemas_UnpagedWithFilters() {
        SchemaDto schema1 = new SchemaDto();
        schema1.setId(UUID.randomUUID());
        schema1.setName("Filtered Schema 1");

        SchemaDto schema2 = new SchemaDto();
        schema2.setId(UUID.randomUUID());
        schema2.setName("Filtered Schema 2");

        List<SchemaDto> filteredSchemas = Arrays.asList(schema1, schema2);
        Page<SchemaDto> unpagedPage = new PageImpl<>(filteredSchemas, Pageable.unpaged(), filteredSchemas.size());

        when(schemaService.getSchemas(eq(testNamespace), eq("Filtered"), eq("FormData"), eq("group1"),
                isNull(), isNull(), isNull(), eq("none"), eq(Pageable.unpaged())))
                .thenReturn(unpagedPage);

        Pageable pageable = PageRequest.of(0, 20);
        ResponseEntity<PagedResponse<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, "Filtered", "FormData", "group1", null, null, null, "none", true, pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals(2, response.getBody().getTotalElements());
        verify(schemaService).getSchemas(testNamespace, "Filtered", "FormData", "group1", null, null, null, "none", Pageable.unpaged());
    }
}
