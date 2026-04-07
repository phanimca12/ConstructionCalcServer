package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.ExtRefDto;
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

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, null, "none", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(schemas, response.getBody());
        assertEquals(1, response.getBody().size());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, null, "none", pageable);
    }

    @Test
    void testGetSchemasWithFilters() {
        Pageable pageable = PageRequest.of(0, 20);
        List<SchemaDto> schemas = Arrays.asList(testSchemaDto);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, "testSchema", "FormData", "group1", "user1", null, "nameAsc", "none", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, "testSchema", "FormData", "group1", "user1", null, "nameAsc", "none", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(schemas, response.getBody());
        verify(schemaService).getSchemas(testNamespace, "testSchema", "FormData", "group1", "user1", null, "nameAsc", "none", pageable);
    }

    @Test
    void testGetSchemasWithSort() {
        Pageable pageable = PageRequest.of(0, 20);
        List<SchemaDto> schemas = Arrays.asList(testSchemaDto);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, null, null, null, null, null, "versionUpdateDesc", "latest", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, "versionUpdateDesc", "latest", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(schemas, response.getBody());
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
        schema1.setDraft("1");

        List<SchemaDto> schemas = Arrays.asList(schema1);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "draft", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, null, "draft", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(schemas, response.getBody());
        assertEquals(1, response.getBody().size());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, null, "draft", pageable);
    }

    @Test
    void testGetSchemas_WithVersionPublished() {
        Pageable pageable = PageRequest.of(0, 20);
        SchemaDto schema1 = new SchemaDto();
        schema1.setId(UUID.randomUUID());
        schema1.setName("Schema 1");
        schema1.setPublished("2");

        List<SchemaDto> schemas = Arrays.asList(schema1);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "published", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, null, "published", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(schemas, response.getBody());
        assertEquals(1, response.getBody().size());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, null, "published", pageable);
    }

    @Test
    void testGetSchemas_WithVersionLatest() {
        Pageable pageable = PageRequest.of(0, 20);
        SchemaDto schema1 = new SchemaDto();
        schema1.setId(UUID.randomUUID());
        schema1.setName("Schema 1");
        schema1.setDraft("2");
        schema1.setPublished("1");

        List<SchemaDto> schemas = Arrays.asList(schema1);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "latest", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, null, "latest", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(schemas, response.getBody());
        assertEquals(1, response.getBody().size());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, null, "latest", pageable);
    }

    @Test
    void testGetSchemas_WithModifiedByUserFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        List<SchemaDto> schemas = Arrays.asList(testSchemaDto);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, null, null, null, "john.doe", null, null, "none", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, "john.doe", null, null, "none", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(schemas, response.getBody());
        verify(schemaService).getSchemas(testNamespace, null, null, null, "john.doe", null, null, "none", pageable);
    }

    @Test
    void testGetSchemas_WithVersionModifiedByUserFilter() {
        Pageable pageable = PageRequest.of(0, 20);
        List<SchemaDto> schemas = Arrays.asList(testSchemaDto);
        Page<SchemaDto> expectedPage = new PageImpl<>(schemas, pageable, schemas.size());

        when(schemaService.getSchemas(testNamespace, null, null, null, null, "jane.smith", null, "none", pageable))
                .thenReturn(expectedPage);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, "jane.smith", null, "none", pageable);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(schemas, response.getBody());
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
        extRef1.setExtRefType("API");
        extRef1.setExtRefVersion("1.0");

        ExtRefDto extRef2 = new ExtRefDto();
        extRef2.setExtRefId(extRefId2);
        extRef2.setExtRefName("External Reference 2");
        extRef2.setExtRefType("DATABASE");
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
}
