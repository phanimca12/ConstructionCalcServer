package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.dto.SchmXrefDto;
import com.ssnc.schemaService.service.SchmXrefService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SchemaXrefControllerTest {

    @Mock
    private SchmXrefService schmXrefService;

    @InjectMocks
    private SchemaXrefController schemaXrefController;

    private UUID testXrefId;
    private UUID testSchmId;
    private UUID testRefGuid;
    private SchmXrefDto testXrefDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testXrefId = UUID.randomUUID();
        testSchmId = UUID.randomUUID();
        testRefGuid = UUID.randomUUID();

        testXrefDto = new SchmXrefDto();
        testXrefDto.setXrefId(testXrefId);
        testXrefDto.setSchmId(testSchmId);
        testXrefDto.setSchmName("Test Schema");
        testXrefDto.setSchmType("data-object");
        testXrefDto.setNmspName("testNamespace");
        testXrefDto.setRefType("Process");
        testXrefDto.setRefVersion("1.0.0");
        testXrefDto.setRefName("Referenced Schema");
        testXrefDto.setRefGuid(testRefGuid);
        testXrefDto.setRefGuidChar(testRefGuid.toString());
        testXrefDto.setCreatedBy("test.user");
        testXrefDto.setCreatedDatetime(LocalDateTime.now());
        testXrefDto.setUpdatedBy("test.user");
        testXrefDto.setUpdatedDatetime(LocalDateTime.now());
    }

    @Test
    void testGetAllXrefs_NoFilters() {
        List<SchmXrefDto> expectedXrefs = Arrays.asList(testXrefDto);
        when(schmXrefService.getAllXrefs()).thenReturn(expectedXrefs);

        ResponseEntity<List<SchmXrefDto>> response = schemaXrefController.getAllXrefs(
                null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedXrefs, response.getBody());
        verify(schmXrefService).getAllXrefs();
        verify(schmXrefService, never()).getXrefsBySchmId(any());
        verify(schmXrefService, never()).getXrefsBySchmIdAndRefType(any(), any());
        verify(schmXrefService, never()).getXrefsByRefGuid(any());
    }

    @Test
    void testGetAllXrefs_FilterBySchmId() {
        List<SchmXrefDto> expectedXrefs = Arrays.asList(testXrefDto);
        when(schmXrefService.getXrefsBySchmId(testSchmId)).thenReturn(expectedXrefs);

        ResponseEntity<List<SchmXrefDto>> response = schemaXrefController.getAllXrefs(
                testSchmId, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedXrefs, response.getBody());
        verify(schmXrefService).getXrefsBySchmId(testSchmId);
        verify(schmXrefService, never()).getAllXrefs();
    }

    @Test
    void testGetAllXrefs_FilterBySchmIdAndRefType() {
        List<SchmXrefDto> expectedXrefs = Arrays.asList(testXrefDto);
        String refType = "Process";
        when(schmXrefService.getXrefsBySchmIdAndRefType(testSchmId, refType))
                .thenReturn(expectedXrefs);

        ResponseEntity<List<SchmXrefDto>> response = schemaXrefController.getAllXrefs(
                testSchmId, refType, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedXrefs, response.getBody());
        verify(schmXrefService).getXrefsBySchmIdAndRefType(testSchmId, refType);
        verify(schmXrefService, never()).getXrefsBySchmId(any());
        verify(schmXrefService, never()).getAllXrefs();
    }

    @Test
    void testGetAllXrefs_FilterByRefGuid() {
        List<SchmXrefDto> expectedXrefs = Arrays.asList(testXrefDto);
        when(schmXrefService.getXrefsByRefGuid(testRefGuid)).thenReturn(expectedXrefs);

        ResponseEntity<List<SchmXrefDto>> response = schemaXrefController.getAllXrefs(
                null, null, testRefGuid);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedXrefs, response.getBody());
        verify(schmXrefService).getXrefsByRefGuid(testRefGuid);
        verify(schmXrefService, never()).getAllXrefs();
    }

    @Test
    void testGetAllXrefs_EmptyList() {
        List<SchmXrefDto> emptyList = Collections.emptyList();
        when(schmXrefService.getAllXrefs()).thenReturn(emptyList);

        ResponseEntity<List<SchmXrefDto>> response = schemaXrefController.getAllXrefs(
                null, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetXrefById_Found() {
        when(schmXrefService.getXrefById(testXrefId)).thenReturn(Optional.of(testXrefDto));

        ResponseEntity<SchmXrefDto> response = schemaXrefController.getXrefById(testXrefId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testXrefDto, response.getBody());
        verify(schmXrefService).getXrefById(testXrefId);
    }

    @Test
    void testGetXrefById_NotFound() {
        when(schmXrefService.getXrefById(testXrefId)).thenReturn(Optional.empty());

        ResponseEntity<SchmXrefDto> response = schemaXrefController.getXrefById(testXrefId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(schmXrefService).getXrefById(testXrefId);
    }

    @Test
    void testCreateXref_Success() {
        when(schmXrefService.createXref(any(SchmXrefDto.class))).thenReturn(testXrefDto);

        ResponseEntity<SchmXrefDto> response = schemaXrefController.createXref(testXrefDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testXrefDto, response.getBody());
        verify(schmXrefService).createXref(testXrefDto);
    }

    @Test
    void testCreateXref_Exception() {
        when(schmXrefService.createXref(any(SchmXrefDto.class)))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<SchmXrefDto> response = schemaXrefController.createXref(testXrefDto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(schmXrefService).createXref(testXrefDto);
    }

    @Test
    void testUpdateXref_Success() {
        when(schmXrefService.updateXref(eq(testXrefId), any(SchmXrefDto.class)))
                .thenReturn(testXrefDto);

        ResponseEntity<SchmXrefDto> response = schemaXrefController.updateXref(
                testXrefId, testXrefDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testXrefDto, response.getBody());
        verify(schmXrefService).updateXref(testXrefId, testXrefDto);
    }

    @Test
    void testUpdateXref_NotFound() {
        when(schmXrefService.updateXref(eq(testXrefId), any(SchmXrefDto.class)))
                .thenThrow(new IllegalArgumentException("Cross-reference not found"));

        ResponseEntity<SchmXrefDto> response = schemaXrefController.updateXref(
                testXrefId, testXrefDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(schmXrefService).updateXref(testXrefId, testXrefDto);
    }

    @Test
    void testUpdateXref_Exception() {
        when(schmXrefService.updateXref(eq(testXrefId), any(SchmXrefDto.class)))
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<SchmXrefDto> response = schemaXrefController.updateXref(
                testXrefId, testXrefDto);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
        verify(schmXrefService).updateXref(testXrefId, testXrefDto);
    }

    @Test
    void testDeleteXref_Success() {
        doNothing().when(schmXrefService).deleteXref(testXrefId);

        ResponseEntity<Void> response = schemaXrefController.deleteXref(testXrefId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(schmXrefService).deleteXref(testXrefId);
    }

    @Test
    void testDeleteXref_NotFound() {
        doThrow(new IllegalArgumentException("Cross-reference not found"))
                .when(schmXrefService).deleteXref(testXrefId);

        ResponseEntity<Void> response = schemaXrefController.deleteXref(testXrefId);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(schmXrefService).deleteXref(testXrefId);
    }

    @Test
    void testDeleteXref_Exception() {
        doThrow(new RuntimeException("Database error"))
                .when(schmXrefService).deleteXref(testXrefId);

        ResponseEntity<Void> response = schemaXrefController.deleteXref(testXrefId);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(schmXrefService).deleteXref(testXrefId);
    }

    @Test
    void testGetAllXrefs_WithRefTypeButNoSchmId() {
        // Test scenario where refType is provided without schmId - should get all xrefs
        List<SchmXrefDto> expectedXrefs = Arrays.asList(testXrefDto);
        when(schmXrefService.getAllXrefs()).thenReturn(expectedXrefs);

        ResponseEntity<List<SchmXrefDto>> response = schemaXrefController.getAllXrefs(
                null, "Automation", null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedXrefs, response.getBody());
        verify(schmXrefService).getAllXrefs();
    }

    @Test
    void testGetAllXrefs_MultipleResults() {
        SchmXrefDto xref1 = new SchmXrefDto();
        xref1.setXrefId(UUID.randomUUID());
        xref1.setSchmId(testSchmId);

        SchmXrefDto xref2 = new SchmXrefDto();
        xref2.setXrefId(UUID.randomUUID());
        xref2.setSchmId(testSchmId);

        List<SchmXrefDto> expectedXrefs = Arrays.asList(xref1, xref2);
        when(schmXrefService.getXrefsBySchmId(testSchmId)).thenReturn(expectedXrefs);

        ResponseEntity<List<SchmXrefDto>> response = schemaXrefController.getAllXrefs(
                testSchmId, null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals(expectedXrefs, response.getBody());
    }

    @Test
    void testCreateXref_WithNullFields() {
        SchmXrefDto minimalDto = new SchmXrefDto();
        minimalDto.setSchmId(testSchmId);
        minimalDto.setRefGuid(testRefGuid);

        when(schmXrefService.createXref(any(SchmXrefDto.class))).thenReturn(minimalDto);

        ResponseEntity<SchmXrefDto> response = schemaXrefController.createXref(minimalDto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(schmXrefService).createXref(minimalDto);
    }

    @Test
    void testUpdateXref_ChangingAllFields() {
        SchmXrefDto updatedDto = new SchmXrefDto();
        updatedDto.setXrefId(testXrefId);
        updatedDto.setSchmId(UUID.randomUUID());
        updatedDto.setSchmName("Updated Schema");
        updatedDto.setSchmType("updated-type");
        updatedDto.setNmspName("updatedNamespace");
        updatedDto.setRefType("Automation");
        updatedDto.setRefVersion("2.0.0");
        updatedDto.setRefName("Updated Reference");
        updatedDto.setRefGuid(UUID.randomUUID());
        updatedDto.setRefGuidChar("new-guid");
        updatedDto.setUpdatedBy("updated.user");
        updatedDto.setUpdatedDatetime(LocalDateTime.now());

        when(schmXrefService.updateXref(eq(testXrefId), any(SchmXrefDto.class)))
                .thenReturn(updatedDto);

        ResponseEntity<SchmXrefDto> response = schemaXrefController.updateXref(
                testXrefId, updatedDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedDto, response.getBody());
        verify(schmXrefService).updateXref(testXrefId, updatedDto);
    }

    @Test
    void testGetAllXrefs_WithValidRefTypes() {
        // Test all valid reference types
        String[] validTypes = {"Process", "Automation", "PresentationFlow", "Sampling", "UXBForm", "UXBApp"};

        for (String refType : validTypes) {
            List<SchmXrefDto> expectedXrefs = Arrays.asList(testXrefDto);
            when(schmXrefService.getXrefsBySchmIdAndRefType(testSchmId, refType))
                    .thenReturn(expectedXrefs);

            ResponseEntity<List<SchmXrefDto>> response = schemaXrefController.getAllXrefs(
                    testSchmId, refType, null);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            verify(schmXrefService).getXrefsBySchmIdAndRefType(testSchmId, refType);
        }
    }

    @Test
    void testCreateXref_WithAllRefTypes() {
        // Test creating xrefs with each valid reference type
        String[] validTypes = {"Process", "Automation", "PresentationFlow", "Sampling", "UXBForm", "UXBApp"};

        for (String refType : validTypes) {
            SchmXrefDto dto = new SchmXrefDto();
            dto.setSchmId(testSchmId);
            dto.setRefType(refType);
            dto.setRefGuid(testRefGuid);

            when(schmXrefService.createXref(any(SchmXrefDto.class))).thenReturn(dto);

            ResponseEntity<SchmXrefDto> response = schemaXrefController.createXref(dto);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(refType, response.getBody().getRefType());
        }
    }
}