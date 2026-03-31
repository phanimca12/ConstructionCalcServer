package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.ErrorResponse;
import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.ExtRefResponse;
import com.ssnc.schemaService.dto.ExtRefWithSchemasRequest;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.service.ExternalReferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExternalReferenceControllerTest {

    @Mock
    private ExternalReferenceService externalReferenceService;

    @InjectMocks
    private ExternalReferenceController externalReferenceController;

    private String testNamespace;
    private UUID testExtRefId;
    private String testExtRefType;
    private String testExtRefVersion;
    private ExtRefDto testExtRefDto;
    private SchemaDto testSchemaDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testNamespace = "testNamespace";
        testExtRefId = UUID.randomUUID();
        testExtRefType = "Process";
        testExtRefVersion = "1.0.0";

        testExtRefDto = new ExtRefDto();
        testExtRefDto.setExtRefId(testExtRefId);
        testExtRefDto.setExtRefName("Test Process");
        testExtRefDto.setExtRefType(testExtRefType);
        testExtRefDto.setExtRefVersion(testExtRefVersion);
        testExtRefDto.setCreatedBy("testUser");
        testExtRefDto.setCreatedDatetime(LocalDateTime.now());

        testSchemaDto = new SchemaDto();
        testSchemaDto.setId(UUID.randomUUID());
        testSchemaDto.setName("Test Schema");
        testSchemaDto.setDescription("Test Description");
    }

    @Test
    void testGetExternalReferences_NoFilter() {
        List<ExtRefDto> expectedRefs = Arrays.asList(testExtRefDto);
        when(externalReferenceService.getExternalReferences(testNamespace, null))
                .thenReturn(expectedRefs);

        ResponseEntity<?> response = externalReferenceController
                .getExternalReferences(testNamespace, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedRefs, response.getBody());
        verify(externalReferenceService).getExternalReferences(testNamespace, null);
    }

    @Test
    void testGetExternalReferences_WithTypeFilter() {
        List<ExtRefDto> expectedRefs = Arrays.asList(testExtRefDto);
        when(externalReferenceService.getExternalReferences(testNamespace, "Process"))
                .thenReturn(expectedRefs);

        ResponseEntity<?> response = externalReferenceController
                .getExternalReferences(testNamespace, "Process");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedRefs, response.getBody());
        verify(externalReferenceService).getExternalReferences(testNamespace, "Process");
    }

    @Test
    void testGetExternalReferences_InvalidType() {
        when(externalReferenceService.getExternalReferences(testNamespace, "InvalidType"))
                .thenThrow(new IllegalArgumentException("Invalid ExtRefType: InvalidType"));

        ResponseEntity<?> response = externalReferenceController
                .getExternalReferences(testNamespace, "InvalidType");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Invalid ExtRefType: InvalidType", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
        verify(externalReferenceService).getExternalReferences(testNamespace, "InvalidType");
    }

    @Test
    void testGetSchemasByExternalReference_Success() {
        List<SchemaDto> expectedSchemas = Arrays.asList(testSchemaDto);
        when(externalReferenceService.getSchemasByExternalReference(
                testNamespace, testExtRefType, testExtRefId, testExtRefVersion))
                .thenReturn(expectedSchemas);

        ResponseEntity<?> response = externalReferenceController
                .getSchemasByExternalReference(testNamespace, testExtRefType, testExtRefId, testExtRefVersion);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSchemas, response.getBody());
        verify(externalReferenceService).getSchemasByExternalReference(
                testNamespace, testExtRefType, testExtRefId, testExtRefVersion);
    }

    @Test
    void testGetSchemasByExternalReference_InvalidType() {
        when(externalReferenceService.getSchemasByExternalReference(
                testNamespace, "InvalidType", testExtRefId, testExtRefVersion))
                .thenThrow(new IllegalArgumentException("Invalid ExtRefType: InvalidType"));

        ResponseEntity<?> response = externalReferenceController
                .getSchemasByExternalReference(testNamespace, "InvalidType", testExtRefId, testExtRefVersion);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Invalid ExtRefType: InvalidType", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
        verify(externalReferenceService).getSchemasByExternalReference(
                testNamespace, "InvalidType", testExtRefId, testExtRefVersion);
    }

    @Test
    void testGetSchemasByExternalReference_EmptyResult() {
        when(externalReferenceService.getSchemasByExternalReference(
                testNamespace, testExtRefType, testExtRefId, testExtRefVersion))
                .thenReturn(Arrays.asList());

        ResponseEntity<?> response = externalReferenceController
                .getSchemasByExternalReference(testNamespace, testExtRefType, testExtRefId, testExtRefVersion);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((List<?>) response.getBody()).isEmpty());
        verify(externalReferenceService).getSchemasByExternalReference(
                testNamespace, testExtRefType, testExtRefId, testExtRefVersion);
    }

    @Test
    void testCreateOrUpdateExternalReference_Create() {
        ExtRefWithSchemasRequest emptyRequest = new ExtRefWithSchemasRequest();
        emptyRequest.setSchemas(Arrays.asList());

        ExtRefResponse extRefResponse = new ExtRefResponse(
                testExtRefDto, ErrorMessages.EXTERNAL_REFERENCE_CREATED_SUCCESS, true);

        when(externalReferenceService.createOrUpdateExternalReference(
                testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, emptyRequest))
                .thenReturn(extRefResponse);

        ResponseEntity<?> response = externalReferenceController
                .createOrUpdateExternalReference(
                        testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, emptyRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ExtRefResponse);
        ExtRefResponse actualResponse = (ExtRefResponse) response.getBody();
        assertEquals(testExtRefDto, actualResponse.getExtRef());
        assertEquals(ErrorMessages.EXTERNAL_REFERENCE_CREATED_SUCCESS, actualResponse.getMessage());
        assertTrue(actualResponse.isUpdated());
        verify(externalReferenceService).createOrUpdateExternalReference(
                testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, emptyRequest);
    }

    @Test
    void testCreateOrUpdateExternalReference_InvalidType() {
        ExtRefWithSchemasRequest emptyRequest = new ExtRefWithSchemasRequest();
        emptyRequest.setSchemas(Arrays.asList());

        when(externalReferenceService.createOrUpdateExternalReference(
                testNamespace, "InvalidType", "Test", testExtRefId, testExtRefVersion, emptyRequest))
                .thenThrow(new IllegalArgumentException("Invalid ExtRefType: InvalidType"));

        ResponseEntity<?> response = externalReferenceController
                .createOrUpdateExternalReference(
                        testNamespace, "InvalidType", "Test", testExtRefId, testExtRefVersion, emptyRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertEquals(400, errorResponse.getStatus());
        assertEquals("Bad Request", errorResponse.getError());
        assertEquals("Invalid ExtRefType: InvalidType", errorResponse.getMessage());
        assertNotNull(errorResponse.getTimestamp());
        verify(externalReferenceService).createOrUpdateExternalReference(
                testNamespace, "InvalidType", "Test", testExtRefId, testExtRefVersion, emptyRequest);
    }

    @Test
    void testCreateOrUpdateExternalReference_WithSchemas() {
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        ExtRefWithSchemasRequest.SchemaReference schemaRef1 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef1.setSchmId(UUID.randomUUID());
        schemaRef1.setSchmName("Schema 1");

        ExtRefWithSchemasRequest.SchemaReference schemaRef2 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef2.setSchmId(UUID.randomUUID());
        schemaRef2.setSchmName("Schema 2");

        request.setSchemas(Arrays.asList(schemaRef1, schemaRef2));

        ExtRefResponse extRefResponse = new ExtRefResponse(
                testExtRefDto, ErrorMessages.EXTERNAL_REFERENCE_CREATED_SUCCESS, true);

        when(externalReferenceService.createOrUpdateExternalReference(
                testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, request))
                .thenReturn(extRefResponse);

        ResponseEntity<?> response = externalReferenceController
                .createOrUpdateExternalReference(
                        testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ExtRefResponse);
        ExtRefResponse actualResponse = (ExtRefResponse) response.getBody();
        assertEquals(testExtRefDto, actualResponse.getExtRef());
        assertTrue(actualResponse.isUpdated());
        verify(externalReferenceService).createOrUpdateExternalReference(
                testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, request);
    }

    @Test
    void testGetExternalReferences_AllTypes() {
        String[] types = {"Process", "Automation", "PresentationFlow", "Sampling", "UXBuilder"};

        for (String type : types) {
            ExtRefDto dto = new ExtRefDto();
            dto.setExtRefType(type);
            List<ExtRefDto> expectedRefs = Arrays.asList(dto);

            when(externalReferenceService.getExternalReferences(testNamespace, type))
                    .thenReturn(expectedRefs);

            ResponseEntity<?> response = externalReferenceController
                    .getExternalReferences(testNamespace, type);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            @SuppressWarnings("unchecked")
            List<ExtRefDto> actualRefs = (List<ExtRefDto>) response.getBody();
            assertEquals(1, actualRefs.size());
            assertEquals(type, actualRefs.get(0).getExtRefType());
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_Idempotent() {
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        request.setSchemas(Arrays.asList());

        // Mock idempotent response (no changes made)
        ExtRefResponse extRefResponse = new ExtRefResponse(
                testExtRefDto,
                ErrorMessages.EXTERNAL_REFERENCE_UP_TO_DATE,
                false);

        when(externalReferenceService.createOrUpdateExternalReference(
                testNamespace, testExtRefType, "Test Process", testExtRefId, testExtRefVersion, request))
                .thenReturn(extRefResponse);

        ResponseEntity<?> response = externalReferenceController
                .createOrUpdateExternalReference(
                        testNamespace, testExtRefType, "Test Process", testExtRefId, testExtRefVersion, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof ExtRefResponse);
        ExtRefResponse actualResponse = (ExtRefResponse) response.getBody();
        assertEquals(testExtRefDto, actualResponse.getExtRef());
        assertEquals(ErrorMessages.EXTERNAL_REFERENCE_UP_TO_DATE, actualResponse.getMessage());
        assertFalse(actualResponse.isUpdated());
        verify(externalReferenceService).createOrUpdateExternalReference(
                testNamespace, testExtRefType, "Test Process", testExtRefId, testExtRefVersion, request);
    }
}
