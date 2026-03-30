package com.ssnc.schemaService.service;

import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.ExtRefWithSchemasRequest;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.entity.ExtRef;
import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmExtRefXref;
import com.ssnc.schemaService.repo.ExtRefRepository;
import com.ssnc.schemaService.repo.SchmExtRefXrefRepository;
import com.ssnc.schemaService.repo.SchmRepository;
import com.ssnc.schemaService.tenant.NamespaceFilterManager;
import com.ssnc.schemaService.tenant.TenantContext;
import com.ssnc.shared.security.JwtClaimsContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class ExternalReferenceServiceTest {

    @Mock
    private ExtRefRepository extRefRepository;

    @Mock
    private SchmExtRefXrefRepository schmExtRefXrefRepository;

    @Mock
    private SchmRepository schmRepository;

    @Mock
    private NamespaceFilterManager namespaceFilterManager;

    @Mock
    private JwtClaimsContext jwtClaimsContext;

    @InjectMocks
    private ExternalReferenceService externalReferenceService;

    private String testNamespace;
    private UUID testExtRefId;
    private UUID testSchmId;
    private String testExtRefType;
    private String testExtRefVersion;
    private ExtRef testExtRef;
    private Schm testSchm;
    private SchmExtRefXref testXref;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testNamespace = "testNamespace";
        testExtRefId = UUID.randomUUID();
        testSchmId = UUID.randomUUID();
        testExtRefType = "Process";
        testExtRefVersion = "1.0.0";

        testExtRef = new ExtRef();
        testExtRef.setExtRefId(testExtRefId);
        testExtRef.setExtRefName("Test Process");
        testExtRef.setExtRefType(testExtRefType);
        testExtRef.setExtRefVersion(testExtRefVersion);
        testExtRef.setTenantName("client1Id");
        testExtRef.setCreatedBy("testUser");
        testExtRef.setUpdatedBy("testUser");
        testExtRef.setCreatedDatetime(LocalDateTime.now());
        testExtRef.setUpdatedDatetime(LocalDateTime.now());

        testSchm = new Schm();
        testSchm.setSchmId(testSchmId);
        testSchm.setSchmName("Test Schema");
        testSchm.setSchmDesc("Test Description");
        testSchm.setSchemaType("JSON");
        testSchm.setCreatedBy("testUser");
        testSchm.setUpdatedBy("testUser");

        testXref = new SchmExtRefXref();
        testXref.setXrefId(UUID.randomUUID());
        testXref.setSchmId(testSchmId);
        testXref.setExtRefId(testExtRefId);
        testXref.setCreatedBy("testUser");

        when(jwtClaimsContext.getUserId()).thenReturn("testUser");
    }

    @Test
    void testGetExternalReferences_NoFilter() {
        List<ExtRef> extRefs = Arrays.asList(testExtRef);
        when(extRefRepository.findAll()).thenReturn(extRefs);
        doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

        List<ExtRefDto> result = externalReferenceService.getExternalReferences(testNamespace, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testExtRefId, result.get(0).getExtRefId());
        assertEquals("Test Process", result.get(0).getExtRefName());
        verify(namespaceFilterManager).enableIfPresent(testNamespace);
        verify(extRefRepository).findAll();
    }

    @Test
    void testGetExternalReferences_WithTypeFilter() {
        List<ExtRef> extRefs = Arrays.asList(testExtRef);
        when(extRefRepository.findByExtRefType(testExtRefType)).thenReturn(extRefs);
        doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

        List<ExtRefDto> result = externalReferenceService.getExternalReferences(testNamespace, testExtRefType);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testExtRefType, result.get(0).getExtRefType());
        verify(namespaceFilterManager).enableIfPresent(testNamespace);
        verify(extRefRepository).findByExtRefType(testExtRefType);
    }

    @Test
    void testGetExternalReferences_InvalidType() {
        doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

        assertThrows(IllegalArgumentException.class, () -> {
            externalReferenceService.getExternalReferences(testNamespace, "InvalidType");
        });

        verify(namespaceFilterManager).enableIfPresent(testNamespace);
    }

    @Test
    void testGetExternalReferences_AllValidTypes() {
        String[] validTypes = {"Process", "Automation", "PresentationFlow", "Sampling", "UXBuilder"};

        for (String type : validTypes) {
            ExtRef extRef = new ExtRef();
            extRef.setExtRefType(type);
            List<ExtRef> extRefs = Arrays.asList(extRef);
            when(extRefRepository.findByExtRefType(type)).thenReturn(extRefs);
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            List<ExtRefDto> result = externalReferenceService.getExternalReferences(testNamespace, type);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(type, result.get(0).getExtRefType());
        }
    }

    @Test
    void testGetSchemasByExternalReference_Success() {
        List<SchmExtRefXref> xrefs = Arrays.asList(testXref);
        when(schmExtRefXrefRepository.findByExtRefExtRefTypeAndExtRefExtRefIdAndExtRefExtRefVersion(
                testExtRefType, testExtRefId, testExtRefVersion)).thenReturn(xrefs);
        // Mock batch fetch instead of individual findBySchmId
        when(schmRepository.findAllById(Arrays.asList(testSchmId))).thenReturn(Arrays.asList(testSchm));
        doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

        List<SchemaDto> result = externalReferenceService.getSchemasByExternalReference(
                testNamespace, testExtRefType, testExtRefId, testExtRefVersion);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSchmId, result.get(0).getId());
        assertEquals("Test Schema", result.get(0).getName());
        verify(namespaceFilterManager).enableIfPresent(testNamespace);
        verify(schmExtRefXrefRepository).findByExtRefExtRefTypeAndExtRefExtRefIdAndExtRefExtRefVersion(
                testExtRefType, testExtRefId, testExtRefVersion);
        verify(schmRepository).findAllById(anyList());
        verify(schmRepository, never()).findBySchmId(any());
    }

    @Test
    void testGetSchemasByExternalReference_NoSchemas() {
        when(schmExtRefXrefRepository.findByExtRefExtRefTypeAndExtRefExtRefIdAndExtRefExtRefVersion(
                testExtRefType, testExtRefId, testExtRefVersion)).thenReturn(Arrays.asList());
        doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

        List<SchemaDto> result = externalReferenceService.getSchemasByExternalReference(
                testNamespace, testExtRefType, testExtRefId, testExtRefVersion);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(namespaceFilterManager).enableIfPresent(testNamespace);
    }

    @Test
    void testGetSchemasByExternalReference_InvalidType() {
        doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

        assertThrows(IllegalArgumentException.class, () -> {
            externalReferenceService.getSchemasByExternalReference(
                    testNamespace, "InvalidType", testExtRefId, testExtRefVersion);
        });

        verify(namespaceFilterManager).enableIfPresent(testNamespace);
    }

    @Test
    void testGetSchemasByExternalReference_SchemaNotFound() {
        List<SchmExtRefXref> xrefs = Arrays.asList(testXref);
        when(schmExtRefXrefRepository.findByExtRefExtRefTypeAndExtRefExtRefIdAndExtRefExtRefVersion(
                testExtRefType, testExtRefId, testExtRefVersion)).thenReturn(xrefs);
        // Mock batch fetch returns empty list (schema not found)
        when(schmRepository.findAllById(Arrays.asList(testSchmId))).thenReturn(Arrays.asList());
        doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

        List<SchemaDto> result = externalReferenceService.getSchemasByExternalReference(
                testNamespace, testExtRefType, testExtRefId, testExtRefVersion);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(schmRepository).findAllById(anyList());
    }

    @Test
    void testCreateOrUpdateExternalReference_Create() {
        ExtRefWithSchemasRequest emptyRequest = new ExtRefWithSchemasRequest();
        emptyRequest.setSchemas(Arrays.asList());

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId)).thenReturn(Arrays.asList());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            ExtRefDto result = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, emptyRequest);

            assertNotNull(result);
            assertEquals(testExtRefId, result.getExtRefId());
            assertEquals("Test Process", result.getExtRefName());
            verify(namespaceFilterManager).enableIfPresent(testNamespace);
            verify(extRefRepository).findById(testExtRefId);
            verify(extRefRepository).save(any(ExtRef.class));
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_Update() {
        ExtRefWithSchemasRequest emptyRequest = new ExtRefWithSchemasRequest();
        emptyRequest.setSchemas(Arrays.asList());

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.of(testExtRef));
            testExtRef.setExtRefName("Updated Process");
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId)).thenReturn(Arrays.asList());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            ExtRefDto result = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType, "Updated Process", testExtRefId, testExtRefVersion, emptyRequest);

            assertNotNull(result);
            assertEquals(testExtRefId, result.getExtRefId());
            verify(namespaceFilterManager).enableIfPresent(testNamespace);
            verify(extRefRepository).findById(testExtRefId);
            verify(extRefRepository).save(any(ExtRef.class));
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_InvalidType() {
        ExtRefWithSchemasRequest emptyRequest = new ExtRefWithSchemasRequest();
        emptyRequest.setSchemas(Arrays.asList());

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            assertThrows(IllegalArgumentException.class, () -> {
                externalReferenceService.createOrUpdateExternalReference(
                        testNamespace, "InvalidType", "Test", testExtRefId, testExtRefVersion, emptyRequest);
            });

            verify(namespaceFilterManager).enableIfPresent(testNamespace);
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_WithSchemas() {
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        ExtRefWithSchemasRequest.SchemaReference schemaRef1 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef1.setSchmId(testSchmId);
        schemaRef1.setSchmName("Schema 1");

        UUID schmId2 = UUID.randomUUID();
        ExtRefWithSchemasRequest.SchemaReference schemaRef2 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef2.setSchmId(schmId2);
        schemaRef2.setSchmName("Schema 2");

        request.setSchemas(Arrays.asList(schemaRef1, schemaRef2));

        Schm schm2 = new Schm();
        schm2.setSchmId(schmId2);
        schm2.setSchmName("Schema 2");

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId)).thenReturn(Arrays.asList());
            when(schmExtRefXrefRepository.save(any(SchmExtRefXref.class))).thenAnswer(i -> i.getArguments()[0]);
            // Mock schema validation
            when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
            when(schmRepository.findBySchmId(schmId2)).thenReturn(Optional.of(schm2));
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            ExtRefDto result = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, request);

            assertNotNull(result);
            assertEquals(testExtRefId, result.getExtRefId());
            verify(schmRepository, times(2)).findBySchmId(any(UUID.class));
            verify(schmExtRefXrefRepository, times(2)).save(any(SchmExtRefXref.class));
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_WithNonExistentSchema_ThrowsException() {
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        ExtRefWithSchemasRequest.SchemaReference schemaRef = new ExtRefWithSchemasRequest.SchemaReference();
        UUID nonExistentSchmId = UUID.randomUUID();
        schemaRef.setSchmId(nonExistentSchmId);
        schemaRef.setSchmName("Non-existent Schema");
        request.setSchemas(Arrays.asList(schemaRef));

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId)).thenReturn(Arrays.asList());
            // Schema doesn't exist
            when(schmRepository.findBySchmId(nonExistentSchmId)).thenReturn(Optional.empty());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, request)
            );

            assertTrue(exception.getMessage().contains("not found"));
            verify(schmRepository).findBySchmId(nonExistentSchmId);
            verify(schmExtRefXrefRepository, never()).save(any(SchmExtRefXref.class));
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_UpdateWithSchemas_DifferentialUpdate() {
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();

        // Request has testSchmId and schmId3 (new)
        ExtRefWithSchemasRequest.SchemaReference schemaRef1 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef1.setSchmId(testSchmId);
        schemaRef1.setSchmName("Schema 1");

        UUID schmId3 = UUID.randomUUID();
        ExtRefWithSchemasRequest.SchemaReference schemaRef3 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef3.setSchmId(schmId3);
        schemaRef3.setSchmName("Schema 3");

        request.setSchemas(Arrays.asList(schemaRef1, schemaRef3));

        // Existing has testSchmId (keep) and schmId2 (remove)
        SchmExtRefXref existingXref1 = new SchmExtRefXref();
        existingXref1.setXrefId(UUID.randomUUID());
        existingXref1.setExtRefId(testExtRefId);
        existingXref1.setSchmId(testSchmId); // This one stays

        UUID schmId2 = UUID.randomUUID();
        SchmExtRefXref existingXref2 = new SchmExtRefXref();
        existingXref2.setXrefId(UUID.randomUUID());
        existingXref2.setExtRefId(testExtRefId);
        existingXref2.setSchmId(schmId2); // This one gets removed

        Schm schm3 = new Schm();
        schm3.setSchmId(schmId3);
        schm3.setSchmName("Schema 3");

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.of(testExtRef));
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId))
                    .thenReturn(Arrays.asList(existingXref1, existingXref2));
            doNothing().when(schmExtRefXrefRepository).deleteAll(anyList());
            when(schmExtRefXrefRepository.save(any(SchmExtRefXref.class))).thenAnswer(i -> i.getArguments()[0]);
            // Mock schema validation - testSchmId already exists, add schmId3
            when(schmRepository.findBySchmId(schmId3)).thenReturn(Optional.of(schm3));
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            ExtRefDto result = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType, "Updated Process", testExtRefId, testExtRefVersion, request);

            assertNotNull(result);

            // Should delete only existingXref2 (schmId2 not in request)
            verify(schmExtRefXrefRepository).deleteAll(argThat(list ->
                    list.size() == 1 && ((SchmExtRefXref) list.get(0)).getSchmId().equals(schmId2)
            ));

            // Should add only schmId3 (testSchmId already exists)
            verify(schmRepository, times(1)).findBySchmId(schmId3);
            verify(schmExtRefXrefRepository, times(1)).save(any(SchmExtRefXref.class));
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_AllTypes() {
        String[] validTypes = {"Process", "Automation", "PresentationFlow", "Sampling", "UXBuilder"};

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            for (String type : validTypes) {
                ExtRefWithSchemasRequest emptyRequest = new ExtRefWithSchemasRequest();
                emptyRequest.setSchemas(Arrays.asList());

                ExtRef extRef = new ExtRef();
                extRef.setExtRefId(UUID.randomUUID());
                extRef.setExtRefType(type);
                extRef.setExtRefName("Test " + type);

                when(extRefRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
                when(extRefRepository.save(any(ExtRef.class))).thenReturn(extRef);
                when(schmExtRefXrefRepository.findByExtRefId(any(UUID.class))).thenReturn(Arrays.asList());
                doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

                ExtRefDto result = externalReferenceService.createOrUpdateExternalReference(
                        testNamespace, type, "Test " + type, extRef.getExtRefId(), "1.0.0", emptyRequest);

                assertNotNull(result);
                assertEquals(type, result.getExtRefType());
            }
        }
    }

    @Test
    void testGetExternalReferences_EmptyResult() {
        when(extRefRepository.findAll()).thenReturn(Arrays.asList());
        doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

        List<ExtRefDto> result = externalReferenceService.getExternalReferences(testNamespace, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(extRefRepository).findAll();
    }

    @Test
    void testMapToDto_AllFields() {
        List<ExtRef> extRefs = Arrays.asList(testExtRef);
        when(extRefRepository.findAll()).thenReturn(extRefs);
        doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

        List<ExtRefDto> result = externalReferenceService.getExternalReferences(testNamespace, null);

        ExtRefDto dto = result.get(0);
        assertEquals(testExtRef.getExtRefId(), dto.getExtRefId());
        assertEquals(testExtRef.getTenantName(), dto.getTenantName());
        assertEquals(testExtRef.getExtRefName(), dto.getExtRefName());
        assertEquals(testExtRef.getExtRefType(), dto.getExtRefType());
        assertEquals(testExtRef.getExtRefVersion(), dto.getExtRefVersion());
        assertEquals(testExtRef.getCreatedDatetime(), dto.getCreatedDatetime());
        assertEquals(testExtRef.getUpdatedDatetime(), dto.getUpdatedDatetime());
        assertEquals(testExtRef.getCreatedBy(), dto.getCreatedBy());
        assertEquals(testExtRef.getUpdatedBy(), dto.getUpdatedBy());
    }
}
