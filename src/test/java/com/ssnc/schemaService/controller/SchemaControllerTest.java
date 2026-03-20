package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.dto.SchemaVersionDto;
import com.ssnc.schemaService.dto.SchemaWithVersionDto;
import com.ssnc.schemaService.service.SchemaService;
import com.ssnc.schemaService.service.SchmXrefService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

    @Mock
    private SchmXrefService schmXrefService;

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
        List<SchemaDto> expectedSchemas = Arrays.asList(testSchemaDto);
        when(schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "none"))
                .thenReturn(expectedSchemas);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, null, "none");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSchemas, response.getBody());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, null, "none");
    }

    @Test
    void testGetSchemasWithFilters() {
        List<SchemaDto> expectedSchemas = Arrays.asList(testSchemaDto);
        when(schemaService.getSchemas(testNamespace, "testSchema", "FormData", "group1", "user1", null, "nameAsc", "none"))
                .thenReturn(expectedSchemas);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, "testSchema", "FormData", "group1", "user1", null, "nameAsc", "none");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSchemas, response.getBody());
        verify(schemaService).getSchemas(testNamespace, "testSchema", "FormData", "group1", "user1", null, "nameAsc", "none");
    }

    @Test
    void testGetSchemasWithSort() {
        List<SchemaDto> expectedSchemas = Arrays.asList(testSchemaDto);
        when(schemaService.getSchemas(testNamespace, null, null, null, null, null, "versionUpdateDesc", "latest"))
                .thenReturn(expectedSchemas);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, "versionUpdateDesc", "latest");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSchemas, response.getBody());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, "versionUpdateDesc", "latest");
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
        SchemaWithVersionDto schemaWithVersion = new SchemaWithVersionDto();
        schemaWithVersion.setSchema(testSchemaDto);
        List<SchemaWithVersionDto> expectedResult = Collections.singletonList(schemaWithVersion);

        when(schemaService.getSchemasById(testNamespace, testSchemaId, null, null))
                .thenReturn(expectedResult);

        ResponseEntity<List<SchemaWithVersionDto>> response = schemaController.getSchemaById(
                testNamespace, testSchemaId.toString(), null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResult, response.getBody());
    }

    @Test
    void testGetSchemaById_NotFound() {
        when(schemaService.getSchemasById(testNamespace, testSchemaId, null, null))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<SchemaWithVersionDto>> response = schemaController.getSchemaById(
                testNamespace, testSchemaId.toString(), null, null);

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
    void testGetExtRefsForSchema() {
        ExtRefDto extRefDto = new ExtRefDto();
        extRefDto.setExtRefId(UUID.randomUUID());
        extRefDto.setExtRefName("Test ExtRef");
        extRefDto.setExtRefType("Automation");
        List<ExtRefDto> expectedExtRefs = Arrays.asList(extRefDto);

        when(schmXrefService.getExtRefsForSchema(testNamespace, testSchemaId))
                .thenReturn(expectedExtRefs);

        ResponseEntity<List<ExtRefDto>> response = schemaController.getExtRefsForSchema(
                testNamespace, testSchemaId.toString());

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedExtRefs, response.getBody());
        verify(schmXrefService).getExtRefsForSchema(testNamespace, testSchemaId);
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
        SchemaDto schema1 = new SchemaDto();
        schema1.setId(UUID.randomUUID());
        schema1.setName("Schema 1");
        schema1.setDraft("1");

        List<SchemaDto> expectedSchemas = Arrays.asList(schema1);
        when(schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "draft"))
                .thenReturn(expectedSchemas);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, null, "draft");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSchemas, response.getBody());
        assertEquals(1, response.getBody().size());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, null, "draft");
    }

    @Test
    void testGetSchemas_WithVersionPublished() {
        SchemaDto schema1 = new SchemaDto();
        schema1.setId(UUID.randomUUID());
        schema1.setName("Schema 1");
        schema1.setPublished("2");

        List<SchemaDto> expectedSchemas = Arrays.asList(schema1);
        when(schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "published"))
                .thenReturn(expectedSchemas);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, null, "published");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSchemas, response.getBody());
        assertEquals(1, response.getBody().size());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, null, "published");
    }

    @Test
    void testGetSchemas_WithVersionLatest() {
        SchemaDto schema1 = new SchemaDto();
        schema1.setId(UUID.randomUUID());
        schema1.setName("Schema 1");
        schema1.setDraft("2");
        schema1.setPublished("1");

        List<SchemaDto> expectedSchemas = Arrays.asList(schema1);
        when(schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "latest"))
                .thenReturn(expectedSchemas);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, null, null, "latest");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSchemas, response.getBody());
        assertEquals(1, response.getBody().size());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, null, null, "latest");
    }

    @Test
    void testGetSchemas_WithModifiedByUserFilter() {
        List<SchemaDto> expectedSchemas = Arrays.asList(testSchemaDto);
        when(schemaService.getSchemas(testNamespace, null, null, null, "john.doe", null, null, "none"))
                .thenReturn(expectedSchemas);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, "john.doe", null, null, "none");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSchemas, response.getBody());
        verify(schemaService).getSchemas(testNamespace, null, null, null, "john.doe", null, null, "none");
    }

    @Test
    void testGetSchemas_WithVersionModifiedByUserFilter() {
        List<SchemaDto> expectedSchemas = Arrays.asList(testSchemaDto);
        when(schemaService.getSchemas(testNamespace, null, null, null, null, "jane.smith", null, "none"))
                .thenReturn(expectedSchemas);

        ResponseEntity<List<SchemaDto>> response = schemaController.getSchemas(
                testNamespace, null, null, null, null, "jane.smith", null, "none");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSchemas, response.getBody());
        verify(schemaService).getSchemas(testNamespace, null, null, null, null, "jane.smith", null, "none");
    }
}
