package com.ssnc.schemaService.service;

import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.ExtRefResponse;
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
import java.util.ArrayList;
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

            ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, emptyRequest);

            assertNotNull(response);
            assertTrue(response.isUpdated());
            assertEquals(ErrorMessages.EXTERNAL_REFERENCE_CREATED_SUCCESS, response.getMessage());
            assertNotNull(response.getExtRef());
            assertEquals(testExtRefId, response.getExtRef().getExtRefId());
            assertEquals("Test Process", response.getExtRef().getExtRefName());
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
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId)).thenReturn(Arrays.asList());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType, "Updated Process", testExtRefId, testExtRefVersion, emptyRequest);

            assertNotNull(response);
            assertTrue(response.isUpdated());
            assertEquals(ErrorMessages.EXTERNAL_REFERENCE_UPDATED_SUCCESS, response.getMessage());
            assertNotNull(response.getExtRef());
            assertEquals(testExtRefId, response.getExtRef().getExtRefId());
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
        // Use deterministic UUIDs to verify lock ordering
        UUID schmId1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID schmId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        ExtRefWithSchemasRequest.SchemaReference schemaRef1 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef1.setSchmId(schmId1);
        schemaRef1.setSchmName("Schema 1");

        ExtRefWithSchemasRequest.SchemaReference schemaRef2 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef2.setSchmId(schmId2);
        schemaRef2.setSchmName("Schema 2");

        // Add in reverse order to verify sorting happens
        request.setSchemas(Arrays.asList(schemaRef2, schemaRef1));

        Schm schm1 = new Schm();
        schm1.setSchmId(schmId1);
        schm1.setSchmName("Schema 1");
        schm1.setPublishVersion(1); // Published

        Schm schm2 = new Schm();
        schm2.setSchmId(schmId2);
        schm2.setSchmName("Schema 2");
        schm2.setPublishVersion(1); // Published

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.empty());
            when(extRefRepository.findByExtRefNameAndExtRefTypeAndExtRefVersion(anyString(), anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId)).thenReturn(Arrays.asList());
            when(schmExtRefXrefRepository.save(any(SchmExtRefXref.class))).thenAnswer(i -> i.getArguments()[0]);
            // Mock schema validation with locking
            when(schmRepository.findWithLockBySchmId(schmId1)).thenReturn(Optional.of(schm1));
            when(schmRepository.findWithLockBySchmId(schmId2)).thenReturn(Optional.of(schm2));
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, request);

            assertNotNull(response);
            assertTrue(response.isUpdated());
            assertNotNull(response.getExtRef());
            assertEquals(testExtRefId, response.getExtRef().getExtRefId());

            // Verify schemas are locked in SORTED order (1, 2), not request order (2, 1)
            org.mockito.InOrder inOrder = inOrder(schmRepository);
            inOrder.verify(schmRepository).findWithLockBySchmId(schmId1);  // First (smaller UUID)
            inOrder.verify(schmRepository).findWithLockBySchmId(schmId2);  // Second (larger UUID)

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
            when(schmRepository.findWithLockBySchmId(nonExistentSchmId)).thenReturn(Optional.empty());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, request)
            );

            assertTrue(exception.getMessage().contains("not found"));
            verify(schmRepository).findWithLockBySchmId(nonExistentSchmId);
            verify(schmExtRefXrefRepository, never()).save(any(SchmExtRefXref.class));
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

                ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                        testNamespace, type, "Test " + type, extRef.getExtRefId(), "1.0.0", emptyRequest);

                assertNotNull(response);
                assertTrue(response.isUpdated());
                assertNotNull(response.getExtRef());
                assertEquals(type, response.getExtRef().getExtRefType());
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

    @Test
    void testCreateOrUpdateExternalReference_WithUnpublishedSchema_ThrowsException() {
        // Test for race condition fix: cannot create reference to unpublished schema
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        ExtRefWithSchemasRequest.SchemaReference schemaRef = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef.setSchmId(testSchmId);
        schemaRef.setSchmName("Unpublished Schema");
        request.setSchemas(Arrays.asList(schemaRef));

        // Schema exists but is NOT published
        testSchm.setPublishVersion(null);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId)).thenReturn(Arrays.asList());
            when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, request)
            );

            assertTrue(exception.getMessage().contains("unpublished"));
            assertTrue(exception.getMessage().contains("must be published"));
            verify(schmRepository).findWithLockBySchmId(testSchmId);
            verify(schmExtRefXrefRepository, never()).save(any(SchmExtRefXref.class));
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_UsesPessimisticLocking_PreventingRaceCondition() {
        // This test verifies pessimistic locking prevents the race condition where:
        // - Thread A unpublishes a schema
        // - Thread B creates a reference to that schema
        // With locking, Thread B will block until Thread A completes
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        ExtRefWithSchemasRequest.SchemaReference schemaRef = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef.setSchmId(testSchmId);
        request.setSchemas(Arrays.asList(schemaRef));

        testSchm.setPublishVersion(1); // Published

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId)).thenReturn(Arrays.asList());
            when(schmExtRefXrefRepository.save(any(SchmExtRefXref.class))).thenAnswer(i -> i.getArguments()[0]);
            when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, request);

            // Verify findWithLockBySchmId is called, NOT findBySchmId
            // This ensures the pessimistic write lock is acquired
            verify(schmRepository).findWithLockBySchmId(testSchmId);
            verify(schmRepository, never()).findBySchmId(testSchmId);
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_Idempotent_NoChange() {
        // Test idempotency: same data sent multiple times should not update
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        ExtRefWithSchemasRequest.SchemaReference schemaRef = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef.setSchmId(testSchmId);
        schemaRef.setSchmName("Schema 1");
        request.setSchemas(Arrays.asList(schemaRef));

        // Existing external reference with same name, type, and version
        testExtRef.setExtRefName("Test Process");
        testExtRef.setExtRefType(testExtRefType);
        testExtRef.setExtRefVersion(testExtRefVersion);

        // Existing schema association (same as request)
        SchmExtRefXref existingXref = new SchmExtRefXref();
        existingXref.setXrefId(UUID.randomUUID());
        existingXref.setExtRefId(testExtRefId);
        existingXref.setSchmId(testSchmId);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.of(testExtRef));
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId))
                    .thenReturn(Arrays.asList(existingXref));
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType, "Test Process", testExtRefId, testExtRefVersion, request);

            assertNotNull(response);
            assertFalse(response.isUpdated()); // No update occurred
            assertEquals(ErrorMessages.EXTERNAL_REFERENCE_UP_TO_DATE, response.getMessage());
            assertNotNull(response.getExtRef());
            assertEquals(testExtRefId, response.getExtRef().getExtRefId());

            // Verify no save or delete operations were performed
            verify(extRefRepository, never()).save(any(ExtRef.class));
            verify(schmExtRefXrefRepository, never()).save(any(SchmExtRefXref.class));
            verify(schmExtRefXrefRepository, never()).deleteAll(anyList());
            verify(schmRepository, never()).findWithLockBySchmId(any(UUID.class));
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_ExistingVersion_DifferentName_ThrowsError() {
        // IMMUTABILITY: Cannot change name for existing version
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        request.setSchemas(Arrays.asList());

        // Existing external reference with different name
        testExtRef.setExtRefName("Old Name");
        testExtRef.setExtRefType(testExtRefType);
        testExtRef.setExtRefVersion(testExtRefVersion);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.of(testExtRef));
            when(extRefRepository.findByExtRefNameAndExtRefTypeAndExtRefVersion(anyString(), anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId)).thenReturn(Arrays.asList());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            // Should throw error - cannot change name for existing version
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType, "New Name", testExtRefId, testExtRefVersion, request)
            );

            assertTrue(exception.getMessage().contains("immutable"));
            assertTrue(exception.getMessage().contains(testExtRefVersion));

            // Verify no save operations were performed
            verify(extRefRepository, never()).save(any(ExtRef.class));
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_ExistingVersion_DifferentSchemas_ThrowsError() {
        // IMMUTABILITY: Cannot change schemas for existing version
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        UUID newSchmId = UUID.randomUUID();
        ExtRefWithSchemasRequest.SchemaReference schemaRef = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef.setSchmId(newSchmId);
        schemaRef.setSchmName("New Schema");
        request.setSchemas(Arrays.asList(schemaRef));

        // Existing external reference with same metadata
        testExtRef.setExtRefName("Test Process");
        testExtRef.setExtRefType(testExtRefType);
        testExtRef.setExtRefVersion(testExtRefVersion);

        // Existing schema association is different (testSchmId vs newSchmId)
        SchmExtRefXref existingXref = new SchmExtRefXref();
        existingXref.setXrefId(UUID.randomUUID());
        existingXref.setExtRefId(testExtRefId);
        existingXref.setSchmId(testSchmId); // Different from request

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.of(testExtRef));
            when(extRefRepository.findByExtRefNameAndExtRefTypeAndExtRefVersion(anyString(), anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId))
                    .thenReturn(Arrays.asList(existingXref));
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            // Should throw error - cannot change schemas for existing version
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType, "Test Process", testExtRefId, testExtRefVersion, request)
            );

            assertTrue(exception.getMessage().contains("immutable"));
            assertTrue(exception.getMessage().contains(testExtRefVersion));
            assertTrue(exception.getMessage().contains(testSchmId.toString()));
            assertTrue(exception.getMessage().contains(newSchmId.toString()));

            // Verify no save or delete operations were performed
            verify(extRefRepository, never()).save(any(ExtRef.class));
            verify(schmExtRefXrefRepository, never()).deleteAll(anyList());
            verify(schmExtRefXrefRepository, never()).save(any(SchmExtRefXref.class));
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_WithNullSchemaId_ThrowsException() {
        // Test that null schema ID in request is rejected BEFORE any database operations
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        ExtRefWithSchemasRequest.SchemaReference schemaRef = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef.setSchmId(null); // Null schema ID
        schemaRef.setSchmName("Test Schema");
        request.setSchemas(Arrays.asList(schemaRef));

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, request)
            );

            assertTrue(exception.getMessage().contains(ErrorMessages.SCHEMA_ID_CANNOT_BE_NULL));

            // CRITICAL: Verify validation happens BEFORE any database operations
            // No database reads should occur
            verify(extRefRepository, never()).findById(any());
            verify(schmExtRefXrefRepository, never()).findByExtRefId(any());

            // No database writes should occur
            verify(extRefRepository, never()).save(any(ExtRef.class));
            verify(schmExtRefXrefRepository, never()).save(any());
            verify(schmExtRefXrefRepository, never()).deleteAll(any());
            verify(schmRepository, never()).findWithLockBySchmId(any());
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_WithNullContextUser_UsesSystemUser() {
        // Test that null jwtClaimsContext falls back to SYSTEM_USER
        ExtRefWithSchemasRequest emptyRequest = new ExtRefWithSchemasRequest();
        emptyRequest.setSchemas(Arrays.asList());

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            // Simulate null jwtClaimsContext
            when(jwtClaimsContext.getUserId()).thenReturn(null);

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenAnswer(invocation -> {
                ExtRef saved = invocation.getArgument(0);
                // Verify SYSTEM_USER was used
                assertEquals("system", saved.getCreatedBy());
                assertEquals("system", saved.getUpdatedBy());
                return saved;
            });
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId)).thenReturn(Arrays.asList());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, emptyRequest);

            assertNotNull(response);
            assertTrue(response.isUpdated());
            verify(extRefRepository).save(any(ExtRef.class));
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_ValidationBeforeChanges_NoPartialUpdates() {
        // This test verifies that ALL validations happen BEFORE any changes are made
        // If validation fails, NO changes should be committed (fail-fast principle)
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();

        // Request wants to:
        // - Keep testSchmId (already exists)
        // - Remove schmId2 (currently associated)
        // - Add invalidSchmId (will fail validation - not found)
        UUID invalidSchmId = UUID.randomUUID();
        ExtRefWithSchemasRequest.SchemaReference schemaRef1 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef1.setSchmId(testSchmId);
        schemaRef1.setSchmName("Schema 1");

        ExtRefWithSchemasRequest.SchemaReference schemaRef2 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef2.setSchmId(invalidSchmId);
        schemaRef2.setSchmName("Invalid Schema");

        request.setSchemas(Arrays.asList(schemaRef1, schemaRef2));

        // Existing has testSchmId and schmId2
        UUID schmId2 = UUID.randomUUID();
        SchmExtRefXref existingXref1 = new SchmExtRefXref();
        existingXref1.setSchmId(testSchmId);

        SchmExtRefXref existingXref2 = new SchmExtRefXref();
        existingXref2.setSchmId(schmId2); // This would be deleted if validation passed

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.of(testExtRef));
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId))
                    .thenReturn(Arrays.asList(existingXref1, existingXref2));

            // testSchmId already exists, so won't be validated
            // invalidSchmId will fail validation (not found)
            when(schmRepository.findWithLockBySchmId(invalidSchmId)).thenReturn(Optional.empty());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            // Execute - should throw exception during validation
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType, "Test Process", testExtRefId, testExtRefVersion, request)
            );

            assertTrue(exception.getMessage().contains("not found"));

            // CRITICAL: Verify NO deletions occurred before validation failed
            // If deleteAll was called, it would mean partial update happened
            verify(schmExtRefXrefRepository, never()).deleteAll(anyList());
            verify(schmExtRefXrefRepository, never()).save(any(SchmExtRefXref.class));

            // Validation happened first
            verify(schmRepository).findWithLockBySchmId(invalidSchmId);
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_UniqueConstraintViolation_ThrowsClearError() {
        // Test that unique constraint (tenant_name, name, type, version) is validated
        // and provides clear error message instead of database constraint violation
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        request.setSchemas(Arrays.asList());

        // Existing external reference with same name, type, version but DIFFERENT ID
        UUID existingExtRefId = UUID.randomUUID();
        ExtRef existingExtRef = new ExtRef();
        existingExtRef.setExtRefId(existingExtRefId);
        existingExtRef.setTenantName("client1Id");
        existingExtRef.setExtRefName("Test Process");
        existingExtRef.setExtRefType(testExtRefType);
        existingExtRef.setExtRefVersion(testExtRefVersion);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            // No record with testExtRefId (new ID)
            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.empty());

            // But a record EXISTS with same name/type/version and different ID
            when(extRefRepository.findByExtRefNameAndExtRefTypeAndExtRefVersion(
                    "Test Process", testExtRefType, testExtRefVersion))
                    .thenReturn(Optional.of(existingExtRef));

            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            // Execute - should throw clear validation error
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType, "Test Process", testExtRefId, testExtRefVersion, request)
            );

            // Verify clear error message
            assertTrue(exception.getMessage().contains("already exists with ID"));
            assertTrue(exception.getMessage().contains(existingExtRefId.toString()));
            assertTrue(exception.getMessage().contains("Test Process"));
            assertTrue(exception.getMessage().contains(testExtRefType));
            assertTrue(exception.getMessage().contains(testExtRefVersion));

            // Verify no save operations occurred
            verify(extRefRepository, never()).save(any(ExtRef.class));
            verify(schmExtRefXrefRepository, never()).save(any());
            verify(schmExtRefXrefRepository, never()).deleteAll(any());
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_SameIdDifferentVersion_ThrowsError() {
        // IMMUTABILITY: Cannot change version for existing extRefId
        // Each version needs its own extRefId (primary key)
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        request.setSchemas(Arrays.asList());

        // Existing external reference with same ID but different version
        testExtRef.setExtRefName("Old Name");
        testExtRef.setExtRefType(testExtRefType);
        testExtRef.setExtRefVersion("1.0.0");

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.of(testExtRef));
            when(extRefRepository.findByExtRefNameAndExtRefTypeAndExtRefVersion(
                    "New Name", testExtRefType, "2.0.0"))
                    .thenReturn(Optional.empty());
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId)).thenReturn(Arrays.asList());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            // Should throw error - cannot change version for existing extRefId
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType, "New Name", testExtRefId, "2.0.0", request)
            );

            assertTrue(exception.getMessage().contains("immutable"));
            verify(extRefRepository, never()).save(any(ExtRef.class));
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_MixedPublishedAndUnpublished_OnlyFailsOnUnpublished() {
        // Test that if multiple schemas are provided and one is unpublished, the operation fails
        // UUIDs are created in specific order to ensure deterministic locking sequence
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();

        // Create UUIDs in deterministic order so published comes first alphabetically
        UUID publishedSchmId = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID unpublishedSchmId = UUID.fromString("22222222-2222-2222-2222-222222222222");

        ExtRefWithSchemasRequest.SchemaReference schemaRef1 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef1.setSchmId(publishedSchmId);
        schemaRef1.setSchmName("Published Schema");

        ExtRefWithSchemasRequest.SchemaReference schemaRef2 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef2.setSchmId(unpublishedSchmId);
        schemaRef2.setSchmName("Unpublished Schema");

        // Add in reverse order to test that sorting happens
        request.setSchemas(Arrays.asList(schemaRef2, schemaRef1));

        Schm publishedSchm = new Schm();
        publishedSchm.setSchmId(publishedSchmId);
        publishedSchm.setPublishVersion(1); // Published

        Schm unpublishedSchm = new Schm();
        unpublishedSchm.setSchmId(unpublishedSchmId);
        unpublishedSchm.setPublishVersion(null); // NOT published

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.empty());
            when(extRefRepository.findByExtRefNameAndExtRefTypeAndExtRefVersion(anyString(), anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId)).thenReturn(Arrays.asList());
            when(schmRepository.findWithLockBySchmId(publishedSchmId)).thenReturn(Optional.of(publishedSchm));
            when(schmRepository.findWithLockBySchmId(unpublishedSchmId)).thenReturn(Optional.of(unpublishedSchm));
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType, "New Process", testExtRefId, testExtRefVersion, request)
            );

            assertTrue(exception.getMessage().contains("unpublished"));

            // With sorted locking, published schema (UUID 1111...) is locked FIRST, then unpublished (UUID 2222...)
            // Published schema should pass validation, unpublished should fail
            org.mockito.InOrder inOrder = inOrder(schmRepository);
            inOrder.verify(schmRepository).findWithLockBySchmId(publishedSchmId);  // First (smaller UUID)
            inOrder.verify(schmRepository).findWithLockBySchmId(unpublishedSchmId);  // Second (larger UUID) - fails here

            // No xref should be saved because validation failed
            verify(schmExtRefXrefRepository, never()).save(any(SchmExtRefXref.class));
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_LocksInSortedOrder_PreventingDeadlock() {
        // Test that schemas are locked in sorted UUID order to prevent deadlock
        // Deadlock scenario without sorting:
        //   Request A: locks [UUID-222, UUID-111] (reverse order)
        //   Request B: locks [UUID-111, UUID-222] (forward order)
        //   Result: A holds 222 waiting for 111, B holds 111 waiting for 222 = DEADLOCK
        //
        // With sorting: both requests lock [UUID-111, UUID-222] = no deadlock

        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();

        // Create UUIDs that would cause deadlock if not sorted
        UUID uuid1 = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID uuid2 = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UUID uuid3 = UUID.fromString("33333333-3333-3333-3333-333333333333");

        // Request them in REVERSE order to test sorting
        ExtRefWithSchemasRequest.SchemaReference schemaRef3 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef3.setSchmId(uuid3);
        schemaRef3.setSchmName("Schema 3");

        ExtRefWithSchemasRequest.SchemaReference schemaRef2 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef2.setSchmId(uuid2);
        schemaRef2.setSchmName("Schema 2");

        ExtRefWithSchemasRequest.SchemaReference schemaRef1 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef1.setSchmId(uuid1);
        schemaRef1.setSchmName("Schema 1");

        // Add in reverse order: 3, 2, 1
        request.setSchemas(Arrays.asList(schemaRef3, schemaRef2, schemaRef1));

        Schm schm1 = new Schm();
        schm1.setSchmId(uuid1);
        schm1.setPublishVersion(1);

        Schm schm2 = new Schm();
        schm2.setSchmId(uuid2);
        schm2.setPublishVersion(1);

        Schm schm3 = new Schm();
        schm3.setSchmId(uuid3);
        schm3.setPublishVersion(1);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(testExtRefId)).thenReturn(Optional.empty());
            when(extRefRepository.findByExtRefNameAndExtRefTypeAndExtRefVersion(anyString(), anyString(), anyString()))
                    .thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefId(testExtRefId)).thenReturn(Arrays.asList());
            when(schmExtRefXrefRepository.save(any(SchmExtRefXref.class))).thenAnswer(i -> i.getArguments()[0]);

            // Mock the repository to return schemas - locks should be acquired in SORTED order (1, 2, 3)
            when(schmRepository.findWithLockBySchmId(uuid1)).thenReturn(Optional.of(schm1));
            when(schmRepository.findWithLockBySchmId(uuid2)).thenReturn(Optional.of(schm2));
            when(schmRepository.findWithLockBySchmId(uuid3)).thenReturn(Optional.of(schm3));

            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType, "Test Process", testExtRefId, testExtRefVersion, request);

            assertNotNull(response);
            assertTrue(response.isUpdated());
            assertEquals(ErrorMessages.EXTERNAL_REFERENCE_CREATED_SUCCESS, response.getMessage());

            // Verify that locks were acquired in SORTED order (1, 2, 3), not request order (3, 2, 1)
            // This prevents deadlock by ensuring consistent lock ordering across all requests
            org.mockito.InOrder inOrder = inOrder(schmRepository);
            inOrder.verify(schmRepository).findWithLockBySchmId(uuid1); // FIRST (smallest UUID)
            inOrder.verify(schmRepository).findWithLockBySchmId(uuid2); // SECOND
            inOrder.verify(schmRepository).findWithLockBySchmId(uuid3); // THIRD (largest UUID)

            // Verify all schemas were saved
            verify(schmExtRefXrefRepository, times(3)).save(any(SchmExtRefXref.class));
        }
    }
}
