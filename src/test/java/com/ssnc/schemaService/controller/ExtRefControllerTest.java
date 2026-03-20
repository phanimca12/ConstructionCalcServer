package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.service.SchmXrefService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExtRefControllerTest {

    @Mock
    private SchmXrefService schmXrefService;

    @InjectMocks
    private ExtRefController extRefController;

    private String testNamespace;
    private ExtRefDto testExtRefDto;
    private SchemaDto testSchemaDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testNamespace = "testNamespace";

        testExtRefDto = new ExtRefDto();
        testExtRefDto.setExtRefId(UUID.randomUUID());
        testExtRefDto.setExtRefName("Test ExtRef");
        testExtRefDto.setExtRefType("Automation");
        testExtRefDto.setExtRefVersion("1.0");
        testExtRefDto.setCreatedByUser("testUser");
        testExtRefDto.setCreateDateTime(LocalDateTime.now());

        testSchemaDto = new SchemaDto();
        testSchemaDto.setId(UUID.randomUUID());
        testSchemaDto.setName("Test Schema");
        testSchemaDto.setDescription("Test Description");
        testSchemaDto.setSchemaType("JSON");
    }

    @Test
    void testGetExtRefs_WithoutTypeFilter() {
        List<ExtRefDto> expectedExtRefs = Arrays.asList(testExtRefDto);
        when(schmXrefService.getExtRefs(testNamespace, null))
                .thenReturn(expectedExtRefs);

        ResponseEntity<List<ExtRefDto>> response = extRefController.getExtRefs(testNamespace, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedExtRefs, response.getBody());
        verify(schmXrefService).getExtRefs(testNamespace, null);
    }

    @Test
    void testGetExtRefs_WithTypeFilter() {
        List<ExtRefDto> expectedExtRefs = Arrays.asList(testExtRefDto);
        when(schmXrefService.getExtRefs(testNamespace, "Automation"))
                .thenReturn(expectedExtRefs);

        ResponseEntity<List<ExtRefDto>> response = extRefController.getExtRefs(testNamespace, "Automation");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedExtRefs, response.getBody());
        verify(schmXrefService).getExtRefs(testNamespace, "Automation");
    }

    @Test
    void testGetExtRefs_EmptyResult() {
        when(schmXrefService.getExtRefs(testNamespace, null))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<ExtRefDto>> response = extRefController.getExtRefs(testNamespace, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetSchemasForExtRef() {
        List<SchemaDto> expectedSchemas = Arrays.asList(testSchemaDto);
        when(schmXrefService.getSchemasForExtRef(testNamespace, "Automation", "TestAutomation", null))
                .thenReturn(expectedSchemas);

        ResponseEntity<List<SchemaDto>> response = extRefController.getSchemasForExtRef(
                testNamespace, "Automation", "TestAutomation");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSchemas, response.getBody());
        verify(schmXrefService).getSchemasForExtRef(testNamespace, "Automation", "TestAutomation", null);
    }

    @Test
    void testGetSchemasForExtRef_EmptyResult() {
        when(schmXrefService.getSchemasForExtRef(testNamespace, "Automation", "TestAutomation", null))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<SchemaDto>> response = extRefController.getSchemasForExtRef(
                testNamespace, "Automation", "TestAutomation");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testAssociateSchemasWithExtRef() {
        List<SchemaDto> schemas = Arrays.asList(testSchemaDto);
        when(schmXrefService.associateSchemasWithExtRef(testNamespace, "Automation", "TestAutomation", null, schemas))
                .thenReturn(schemas);

        ResponseEntity<List<SchemaDto>> response = extRefController.associateSchemasWithExtRef(
                testNamespace, "Automation", "TestAutomation", schemas);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(schemas, response.getBody());
        verify(schmXrefService).associateSchemasWithExtRef(testNamespace, "Automation", "TestAutomation", null, schemas);
    }

    @Test
    void testAssociateSchemasWithExtRef_EmptyList() {
        List<SchemaDto> emptyList = Collections.emptyList();
        when(schmXrefService.associateSchemasWithExtRef(testNamespace, "Automation", "TestAutomation", null, emptyList))
                .thenReturn(emptyList);

        ResponseEntity<List<SchemaDto>> response = extRefController.associateSchemasWithExtRef(
                testNamespace, "Automation", "TestAutomation", emptyList);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
        verify(schmXrefService).associateSchemasWithExtRef(testNamespace, "Automation", "TestAutomation", null, emptyList);
    }

    @Test
    void testGetSchemasForExtRef_WithVersion() {
        List<SchemaDto> expectedSchemas = Arrays.asList(testSchemaDto);
        when(schmXrefService.getSchemasForExtRef(testNamespace, "Automation", "TestAutomation", "1.0"))
                .thenReturn(expectedSchemas);

        ResponseEntity<List<SchemaDto>> response = extRefController.getSchemasForExtRefWithVersion(
                testNamespace, "Automation", "TestAutomation", "1.0");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedSchemas, response.getBody());
        verify(schmXrefService).getSchemasForExtRef(testNamespace, "Automation", "TestAutomation", "1.0");
    }

    @Test
    void testAssociateSchemasWithExtRef_WithVersion() {
        List<SchemaDto> schemas = Arrays.asList(testSchemaDto);
        when(schmXrefService.associateSchemasWithExtRef(testNamespace, "Automation", "TestAutomation", "1.0", schemas))
                .thenReturn(schemas);

        ResponseEntity<List<SchemaDto>> response = extRefController.associateSchemasWithExtRefWithVersion(
                testNamespace, "Automation", "TestAutomation", "1.0", schemas);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(schemas, response.getBody());
        verify(schmXrefService).associateSchemasWithExtRef(testNamespace, "Automation", "TestAutomation", "1.0", schemas);
    }
}
