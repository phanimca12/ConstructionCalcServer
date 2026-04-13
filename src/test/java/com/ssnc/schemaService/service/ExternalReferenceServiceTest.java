package com.ssnc.schemaService.service;

import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.ExtRefResponse;
import com.ssnc.schemaService.dto.ExtRefWithSchemasRequest;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.entity.ExtRef;
import com.ssnc.schemaService.entity.ExtRefId;
import com.ssnc.schemaService.entity.ExtRefType;
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

    @Mock
    private com.ssnc.schemaService.repo.TenantRepository tenantRepository;

    @Mock
    private com.ssnc.schemaService.repo.NameSpaceRepository nameSpaceRepository;

    @InjectMocks
    private ExternalReferenceService externalReferenceService;

    private String testNamespace;
    private String testExtRefId;
    private UUID testSchmId;
    private UUID testTenantId;
    private UUID testNmspcId;
    private ExtRefType testExtRefType;
    private String testExtRefVersion;
    private ExtRef testExtRef;
    private Schm testSchm;
    private SchmExtRefXref testXref;
    private com.ssnc.schemaService.entity.Tenant testTenant;
    private com.ssnc.schemaService.entity.Nmspc testNmspc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testNamespace = "testNamespace";
        testExtRefId = "TEST-EXT-REF-123";
        testSchmId = UUID.randomUUID();
        testTenantId = UUID.randomUUID();
        testNmspcId = UUID.randomUUID();
        testExtRefType = ExtRefType.PROCESS;
        testExtRefVersion = "1.0.0";

        testTenant = new com.ssnc.schemaService.entity.Tenant();
        testTenant.setTenantId(testTenantId);
        testTenant.setTenantName("client1Id");

        testNmspc = new com.ssnc.schemaService.entity.Nmspc();
        testNmspc.setNmspcId(testNmspcId);
        testNmspc.setNmspcName(testNamespace);

        testExtRef = new ExtRef();
        testExtRef.setId(testExtRefId, testExtRefVersion); // Set composite key atomically
        testExtRef.setExtRefName("Test Process");
        testExtRef.setExtRefType(testExtRefType);
        testExtRef.setTenantId(testTenantId);
        testExtRef.setCreatedBy("testUser");
        testExtRef.setUpdatedBy("testUser");
        testExtRef.setCreatedDatetime(LocalDateTime.now());
        testExtRef.setUpdatedDatetime(LocalDateTime.now());

        testSchm = new Schm();
        testSchm.setSchmId(testSchmId);
        testSchm.setTenantId(testTenantId);
        testSchm.setNmspcId(testNmspcId);
        testSchm.setSchmName("Test Schema");
        testSchm.setSchmDesc("Test Description");
        testSchm.setSchemaType("JSON");
        testSchm.setCreatedBy("testUser");
        testSchm.setUpdatedBy("testUser");

        testXref = new SchmExtRefXref();
        testXref.setXrefId(UUID.randomUUID());
        testXref.setTenantId(testTenantId);
        testXref.setSchmId(testSchmId);
        testXref.setExtRefId(testExtRefId);
        testXref.setExtRefVersion(testExtRefVersion);
        testXref.setCreatedBy("testUser");

        when(jwtClaimsContext.getUserId()).thenReturn("testUser");
        when(tenantRepository.findByTenantName("client1Id")).thenReturn(Optional.of(testTenant));
        when(nameSpaceRepository.findByNmspcName(testNamespace)).thenReturn(Optional.of(testNmspc));
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

        List<ExtRefDto> result = externalReferenceService.getExternalReferences(testNamespace, testExtRefType.name());

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testExtRefType.name(), result.get(0).getExtRefType());
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
        ExtRefType[] validTypes = {ExtRefType.PROCESS, ExtRefType.AUTOMATION, ExtRefType.PRESENTATION_FLOW, ExtRefType.SAMPLING, ExtRefType.UX_BUILDER};

        for (ExtRefType type : validTypes) {
            ExtRef extRef = new ExtRef();
            extRef.setExtRefType(type);
            List<ExtRef> extRefs = Arrays.asList(extRef);
            when(extRefRepository.findByExtRefType(type)).thenReturn(extRefs);
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            List<ExtRefDto> result = externalReferenceService.getExternalReferences(testNamespace, type.name());

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(type.name(), result.get(0).getExtRefType());
        }
    }

    @Test
    void testGetSchemasByExternalReference_Success() {
        List<SchmExtRefXref> xrefs = Arrays.asList(testXref);
        when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion)).thenReturn(xrefs);
        // Mock batch fetch instead of individual findBySchmId
        when(schmRepository.findAllById(Arrays.asList(testSchmId))).thenReturn(Arrays.asList(testSchm));
        doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

        List<SchemaDto> result = externalReferenceService.getSchemasByExternalReference(
                testNamespace, testExtRefType.name(), testExtRefId, testExtRefVersion);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSchmId, result.get(0).getId());
        assertEquals("Test Schema", result.get(0).getName());
        verify(namespaceFilterManager).enableIfPresent(testNamespace);
        verify(schmExtRefXrefRepository).findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion);
        verify(schmRepository).findAllById(anyList());
        verify(schmRepository, never()).findBySchmId(any());
    }

    @Test
    void testGetSchemasByExternalReference_NoSchemas() {
        when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion)).thenReturn(Arrays.asList());
        doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

        List<SchemaDto> result = externalReferenceService.getSchemasByExternalReference(
                testNamespace, testExtRefType.name(), testExtRefId, testExtRefVersion);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(namespaceFilterManager).enableIfPresent(testNamespace);
        // PERFORMANCE: Verify no unnecessary database call when no schemas to fetch
        verify(schmRepository, never()).findAllById(anyList());
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
        when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion)).thenReturn(xrefs);
        // Mock batch fetch returns empty list (schema not found)
        when(schmRepository.findAllById(Arrays.asList(testSchmId))).thenReturn(Arrays.asList());
        doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

        List<SchemaDto> result = externalReferenceService.getSchemasByExternalReference(
                testNamespace, testExtRefType.name(), testExtRefId, testExtRefVersion);

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

            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion)).thenReturn(Arrays.asList());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType.name(), "New Process", testExtRefId, testExtRefVersion, emptyRequest);

            assertNotNull(response);
            assertTrue(response.isUpdated());
            assertEquals(ErrorMessages.EXTERNAL_REFERENCE_CREATED_SUCCESS, response.getMessage());
            assertNotNull(response.getExtRef());
            assertEquals(testExtRefId, response.getExtRef().getExtRefId());
            assertEquals("Test Process", response.getExtRef().getExtRefName());
            verify(namespaceFilterManager).enableIfPresent(testNamespace);
            verify(extRefRepository).findById(new ExtRefId(testExtRefId, testExtRefVersion));
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

            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.empty());
            when(extRefRepository.findByTenantIdAndExtRefNameAndExtRefTypeAndId_ExtRefVersion(any(UUID.class), anyString(), any(ExtRefType.class), anyString()))
                    .thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion)).thenReturn(Arrays.asList());
            when(schmExtRefXrefRepository.saveAll(anyList())).thenAnswer(i -> i.getArguments()[0]);
            // Mock schema validation with locking
            when(schmRepository.findWithLockBySchmId(schmId1)).thenReturn(Optional.of(schm1));
            when(schmRepository.findWithLockBySchmId(schmId2)).thenReturn(Optional.of(schm2));
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType.name(), "New Process", testExtRefId, testExtRefVersion, request);

            assertNotNull(response);
            assertTrue(response.isUpdated());
            assertNotNull(response.getExtRef());
            assertEquals(testExtRefId, response.getExtRef().getExtRefId());

            // Verify schemas are locked in SORTED order (1, 2), not request order (2, 1)
            org.mockito.InOrder inOrder = inOrder(schmRepository);
            inOrder.verify(schmRepository).findWithLockBySchmId(schmId1);  // First (smaller UUID)
            inOrder.verify(schmRepository).findWithLockBySchmId(schmId2);  // Second (larger UUID)

            // Verify batch save was called once with correct number of items
            verify(schmExtRefXrefRepository, times(1)).saveAll(argThat(list ->
                list instanceof java.util.Collection && ((java.util.Collection<?>)list).size() == 2));
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

            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion)).thenReturn(Arrays.asList());
            // Schema doesn't exist
            when(schmRepository.findWithLockBySchmId(nonExistentSchmId)).thenReturn(Optional.empty());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType.name(), "New Process", testExtRefId, testExtRefVersion, request)
            );

            assertTrue(exception.getMessage().contains("not found"));
            verify(schmRepository).findWithLockBySchmId(nonExistentSchmId);
            // SECURITY: Verify NO saves occurred when validation failed
            verify(extRefRepository, never()).save(any(ExtRef.class));
            verify(schmExtRefXrefRepository, never()).save(any(SchmExtRefXref.class));
            verify(schmExtRefXrefRepository, never()).saveAll(anyList());
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_AllTypes() {
        ExtRefType[] validTypes = {ExtRefType.PROCESS, ExtRefType.AUTOMATION, ExtRefType.PRESENTATION_FLOW, ExtRefType.SAMPLING, ExtRefType.UX_BUILDER};

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            for (ExtRefType type : validTypes) {
                ExtRefWithSchemasRequest emptyRequest = new ExtRefWithSchemasRequest();
                emptyRequest.setSchemas(Arrays.asList());

                ExtRef extRef = new ExtRef();
                extRef.setId("TEST-" + type.name() + "-ID", "1.0.0"); // Set composite key atomically
                extRef.setExtRefType(type);
                extRef.setExtRefName("Test " + type.name());

                when(extRefRepository.findById(any(ExtRefId.class))).thenReturn(Optional.empty());
                when(extRefRepository.save(any(ExtRef.class))).thenReturn(extRef);
                when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(any(String.class), any(String.class))).thenReturn(Arrays.asList());
                doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

                ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                        testNamespace, type.name(), "Test " + type.name(), extRef.getExtRefId(), "1.0.0", emptyRequest);

                assertNotNull(response);
                assertTrue(response.isUpdated());
                assertNotNull(response.getExtRef());
                assertEquals(type.name(), response.getExtRef().getExtRefType());
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
        assertEquals(testExtRef.getExtRefName(), dto.getExtRefName());
        assertEquals(testExtRef.getExtRefType().name(), dto.getExtRefType());
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

            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion)).thenReturn(Arrays.asList());
            when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType.name(), "New Process", testExtRefId, testExtRefVersion, request)
            );

            assertTrue(exception.getMessage().contains("unpublished"));
            assertTrue(exception.getMessage().contains("must be published"));
            verify(schmRepository).findWithLockBySchmId(testSchmId);
            // SECURITY: Verify NO saves occurred when validation failed
            verify(extRefRepository, never()).save(any(ExtRef.class));
            verify(schmExtRefXrefRepository, never()).save(any(SchmExtRefXref.class));
            verify(schmExtRefXrefRepository, never()).saveAll(anyList());
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

            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion)).thenReturn(Arrays.asList());
            when(schmExtRefXrefRepository.saveAll(anyList())).thenAnswer(i -> i.getArguments()[0]);
            when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType.name(), "New Process", testExtRefId, testExtRefVersion, request);

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
        testExtRef.setId(testExtRefId, testExtRefVersion);
        testExtRef.setExtRefName("Test Process");
        testExtRef.setExtRefType(testExtRefType);

        // Existing schema association (same as request)
        SchmExtRefXref existingXref = new SchmExtRefXref();
        existingXref.setXrefId(UUID.randomUUID());
        existingXref.setExtRefId(testExtRefId);
        existingXref.setSchmId(testSchmId);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.of(testExtRef));
            when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion))
                    .thenReturn(Arrays.asList(existingXref));
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType.name(), "Test Process", testExtRefId, testExtRefVersion, request);

            assertNotNull(response);
            assertFalse(response.isUpdated()); // No update occurred
            assertEquals(ErrorMessages.EXTERNAL_REFERENCE_UP_TO_DATE, response.getMessage());
            assertNotNull(response.getExtRef());
            assertEquals(testExtRefId, response.getExtRef().getExtRefId());

            // Verify no save or delete operations were performed
            verify(extRefRepository, never()).save(any(ExtRef.class));
            verify(schmExtRefXrefRepository, never()).save(any(SchmExtRefXref.class));
            verify(schmExtRefXrefRepository, never()).saveAll(anyList());
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
        testExtRef.setId(testExtRefId, testExtRefVersion);
        testExtRef.setExtRefName("Old Name");
        testExtRef.setExtRefType(testExtRefType);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.of(testExtRef));
            when(extRefRepository.findByTenantIdAndExtRefNameAndExtRefTypeAndId_ExtRefVersion(any(UUID.class), anyString(), any(ExtRefType.class), anyString()))
                    .thenReturn(Optional.empty());
            when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion)).thenReturn(Arrays.asList());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            // Should throw error - cannot change name for existing version
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType.name(), "New Name", testExtRefId, testExtRefVersion, request)
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
        testExtRef.setId(testExtRefId, testExtRefVersion);
        testExtRef.setExtRefName("Test Process");
        testExtRef.setExtRefType(testExtRefType);

        // Existing schema association is different (testSchmId vs newSchmId)
        SchmExtRefXref existingXref = new SchmExtRefXref();
        existingXref.setXrefId(UUID.randomUUID());
        existingXref.setExtRefId(testExtRefId);
        existingXref.setSchmId(testSchmId); // Different from request

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.of(testExtRef));
            when(extRefRepository.findByTenantIdAndExtRefNameAndExtRefTypeAndId_ExtRefVersion(any(UUID.class), anyString(), any(ExtRefType.class), anyString()))
                    .thenReturn(Optional.empty());
            when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion))
                    .thenReturn(Arrays.asList(existingXref));
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            // Should throw error - cannot change schemas for existing version
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType.name(), "Test Process", testExtRefId, testExtRefVersion, request)
            );

            assertTrue(exception.getMessage().contains("immutable"));
            assertTrue(exception.getMessage().contains(testExtRefVersion));
            assertTrue(exception.getMessage().contains(testSchmId.toString()));
            assertTrue(exception.getMessage().contains(newSchmId.toString()));

            // Verify no save or delete operations were performed
            verify(extRefRepository, never()).save(any(ExtRef.class));
            verify(schmExtRefXrefRepository, never()).save(any(SchmExtRefXref.class));
            verify(schmExtRefXrefRepository, never()).saveAll(anyList());
            verify(schmExtRefXrefRepository, never()).deleteAll(anyList());
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
                            testNamespace, testExtRefType.name(), "New Process", testExtRefId, testExtRefVersion, request)
            );

            assertTrue(exception.getMessage().contains(ErrorMessages.SCHEMA_ID_CANNOT_BE_NULL));

            // CRITICAL: Verify validation happens BEFORE any database operations
            // No database reads should occur
            verify(extRefRepository, never()).findById(any());
            verify(schmExtRefXrefRepository, never()).findByExtRefId(any());

            // No database writes should occur
            verify(extRefRepository, never()).save(any(ExtRef.class));
            verify(schmExtRefXrefRepository, never()).save(any());
            verify(schmExtRefXrefRepository, never()).saveAll(anyList());
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

            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenAnswer(invocation -> {
                ExtRef saved = invocation.getArgument(0);
                // Verify SYSTEM_USER was used
                assertEquals("system", saved.getCreatedBy());
                assertEquals("system", saved.getUpdatedBy());
                return saved;
            });
            when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion)).thenReturn(Arrays.asList());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType.name(), "New Process", testExtRefId, testExtRefVersion, emptyRequest);

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

        // Test CREATION scenario (not update, since updates are not allowed due to immutability)
        // Request wants to create with:
        // - testSchmId (valid)
        // - invalidSchmId (will fail validation - not found)
        UUID invalidSchmId = UUID.randomUUID();
        ExtRefWithSchemasRequest.SchemaReference schemaRef1 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef1.setSchmId(testSchmId);
        schemaRef1.setSchmName("Schema 1");

        ExtRefWithSchemasRequest.SchemaReference schemaRef2 = new ExtRefWithSchemasRequest.SchemaReference();
        schemaRef2.setSchmId(invalidSchmId);
        schemaRef2.setSchmName("Invalid Schema");

        request.setSchemas(Arrays.asList(schemaRef1, schemaRef2));

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            // External reference does NOT exist (creation scenario)
            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.empty());
            when(extRefRepository.findByTenantIdAndExtRefNameAndExtRefTypeAndId_ExtRefVersion(any(UUID.class), anyString(), any(ExtRefType.class), anyString()))
                    .thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);

            // testSchmId will be validated first (sorted order)
            // invalidSchmId will be validated second and fail (not found)
            // Since UUIDs are sorted, we need deterministic UUIDs
            UUID validSchmId = UUID.fromString("11111111-1111-1111-1111-111111111111");
            UUID invalidSchmId2 = UUID.fromString("22222222-2222-2222-2222-222222222222");

            schemaRef1.setSchmId(validSchmId);
            schemaRef2.setSchmId(invalidSchmId2);

            Schm validSchm = new Schm();
            validSchm.setSchmId(validSchmId);
            validSchm.setPublishVersion(1);

            when(schmRepository.findWithLockBySchmId(validSchmId)).thenReturn(Optional.of(validSchm));
            when(schmRepository.findWithLockBySchmId(invalidSchmId2)).thenReturn(Optional.empty());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            // Execute - should throw exception during validation of second schema
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType.name(), "Test Process", testExtRefId, testExtRefVersion, request)
            );

            assertTrue(exception.getMessage().contains("not found"));

            // CRITICAL: Verify NO saves occurred when validation failed
            // SECURITY FIX: Schema validation now happens BEFORE ExtRef save to prevent data corruption
            verify(extRefRepository, never()).save(any(ExtRef.class)); // No ExtRef saved if validation fails
            verify(schmExtRefXrefRepository, never()).save(any(SchmExtRefXref.class)); // No xrefs saved
            verify(schmExtRefXrefRepository, never()).saveAll(anyList()); // No batch saves

            // Validation happened in sorted order before any saves
            verify(schmRepository).findWithLockBySchmId(validSchmId); // First (passed)
            verify(schmRepository).findWithLockBySchmId(invalidSchmId2); // Second (failed)
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_UniqueConstraintViolation_ThrowsClearError() {
        // Test that unique constraint (tenant_name, name, type, version) is validated
        // and provides clear error message instead of database constraint violation
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        request.setSchemas(Arrays.asList());

        // Existing external reference with same name, type, version but DIFFERENT ID
        String existingExtRefId = "EXISTING-EXT-REF-999";
        ExtRef existingExtRef = new ExtRef();
        existingExtRef.setId(existingExtRefId, testExtRefVersion); // Set composite key atomically
        existingExtRef.setTenantId(testTenantId);
        existingExtRef.setExtRefName("Test Process");
        existingExtRef.setExtRefType(testExtRefType);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            // No record with testExtRefId (new ID)
            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.empty());

            // But a record EXISTS with same name/type/version and different ID
            when(extRefRepository.findByTenantIdAndExtRefNameAndExtRefTypeAndId_ExtRefVersion(
                    testTenantId, "Test Process", testExtRefType, testExtRefVersion))
                    .thenReturn(Optional.of(existingExtRef));

            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            // Execute - should throw clear validation error
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType.name(), "Test Process", testExtRefId, testExtRefVersion, request)
            );

            // Verify clear error message
            assertTrue(exception.getMessage().contains("already exists with ID"));
            assertTrue(exception.getMessage().contains(existingExtRefId.toString()));
            assertTrue(exception.getMessage().contains("Test Process"));
            assertTrue(exception.getMessage().contains(testExtRefType.name()));
            assertTrue(exception.getMessage().contains(testExtRefVersion));

            // Verify no save operations occurred
            verify(extRefRepository, never()).save(any(ExtRef.class));
            verify(schmExtRefXrefRepository, never()).save(any());
            verify(schmExtRefXrefRepository, never()).saveAll(anyList());
            verify(schmExtRefXrefRepository, never()).deleteAll(any());
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_SameIdDifferentVersion_CreatesNewRecord() {
        // COMPOSITE KEY: With composite key (ext_ref_id, ext_ref_version), you CAN have
        // multiple versions of the same ext_ref_id. This is now ALLOWED behavior.
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        request.setSchemas(Arrays.asList());

        // Existing external reference with same ID but version 1.0.0
        ExtRef existingV1 = new ExtRef();
        existingV1.setId(testExtRefId, "1.0.0"); // Set composite key atomically
        existingV1.setExtRefName("Version 1 Name");
        existingV1.setExtRefType(testExtRefType);
        existingV1.setTenantId(testTenantId);

        // New external reference with same ID but version 2.0.0
        ExtRef newV2 = new ExtRef();
        newV2.setId(testExtRefId, "2.0.0"); // Set composite key atomically
        newV2.setExtRefName("Version 2 Name");
        newV2.setExtRefType(testExtRefType);
        newV2.setTenantId(testTenantId);

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            // Version 1.0.0 exists
            when(extRefRepository.findById(new ExtRefId(testExtRefId, "1.0.0"))).thenReturn(Optional.of(existingV1));
            // Version 2.0.0 does NOT exist yet (this is what we're creating)
            when(extRefRepository.findById(new ExtRefId(testExtRefId, "2.0.0"))).thenReturn(Optional.empty());
            when(extRefRepository.findByTenantIdAndExtRefNameAndExtRefTypeAndId_ExtRefVersion(
                    testTenantId, "Version 2 Name", testExtRefType, "2.0.0"))
                    .thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(newV2);
            when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, "2.0.0")).thenReturn(Arrays.asList());
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            // Should successfully create version 2.0.0
            ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType.name(), "Version 2 Name", testExtRefId, "2.0.0", request);

            assertNotNull(response);
            assertTrue(response.isUpdated());
            assertEquals(ErrorMessages.EXTERNAL_REFERENCE_CREATED_SUCCESS, response.getMessage());
            // Verify save was called (new version created)
            verify(extRefRepository, times(1)).save(any(ExtRef.class));
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

            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.empty());
            when(extRefRepository.findByTenantIdAndExtRefNameAndExtRefTypeAndId_ExtRefVersion(any(UUID.class), anyString(), any(ExtRefType.class), anyString()))
                    .thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion)).thenReturn(Arrays.asList());
            when(schmRepository.findWithLockBySchmId(publishedSchmId)).thenReturn(Optional.of(publishedSchm));
            when(schmRepository.findWithLockBySchmId(unpublishedSchmId)).thenReturn(Optional.of(unpublishedSchm));
            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType.name(), "New Process", testExtRefId, testExtRefVersion, request)
            );

            assertTrue(exception.getMessage().contains("unpublished"));

            // With sorted locking, published schema (UUID 1111...) is locked FIRST, then unpublished (UUID 2222...)
            // Published schema should pass validation, unpublished should fail
            org.mockito.InOrder inOrder = inOrder(schmRepository);
            inOrder.verify(schmRepository).findWithLockBySchmId(publishedSchmId);  // First (smaller UUID)
            inOrder.verify(schmRepository).findWithLockBySchmId(unpublishedSchmId);  // Second (larger UUID) - fails here

            // SECURITY: No saves should occur because validation failed
            verify(extRefRepository, never()).save(any(ExtRef.class));
            verify(schmExtRefXrefRepository, never()).save(any(SchmExtRefXref.class));
            verify(schmExtRefXrefRepository, never()).saveAll(anyList());
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

            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.empty());
            when(extRefRepository.findByTenantIdAndExtRefNameAndExtRefTypeAndId_ExtRefVersion(any(UUID.class), anyString(), any(ExtRefType.class), anyString()))
                    .thenReturn(Optional.empty());
            when(extRefRepository.save(any(ExtRef.class))).thenReturn(testExtRef);
            when(schmExtRefXrefRepository.findByExtRefIdAndExtRefVersion(testExtRefId, testExtRefVersion)).thenReturn(Arrays.asList());
            when(schmExtRefXrefRepository.saveAll(anyList())).thenAnswer(i -> i.getArguments()[0]);

            // Mock the repository to return schemas - locks should be acquired in SORTED order (1, 2, 3)
            when(schmRepository.findWithLockBySchmId(uuid1)).thenReturn(Optional.of(schm1));
            when(schmRepository.findWithLockBySchmId(uuid2)).thenReturn(Optional.of(schm2));
            when(schmRepository.findWithLockBySchmId(uuid3)).thenReturn(Optional.of(schm3));

            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            ExtRefResponse response = externalReferenceService.createOrUpdateExternalReference(
                    testNamespace, testExtRefType.name(), "Test Process", testExtRefId, testExtRefVersion, request);

            assertNotNull(response);
            assertTrue(response.isUpdated());
            assertEquals(ErrorMessages.EXTERNAL_REFERENCE_CREATED_SUCCESS, response.getMessage());

            // Verify that locks were acquired in SORTED order (1, 2, 3), not request order (3, 2, 1)
            // This prevents deadlock by ensuring consistent lock ordering across all requests
            org.mockito.InOrder inOrder = inOrder(schmRepository);
            inOrder.verify(schmRepository).findWithLockBySchmId(uuid1); // FIRST (smallest UUID)
            inOrder.verify(schmRepository).findWithLockBySchmId(uuid2); // SECOND
            inOrder.verify(schmRepository).findWithLockBySchmId(uuid3); // THIRD (largest UUID)

            // Verify batch save was called once with correct number of items
            verify(schmExtRefXrefRepository, times(1)).saveAll(argThat(list ->
                list instanceof java.util.Collection && ((java.util.Collection<?>)list).size() == 3));
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_RaceCondition_HandlesUniqueConstraintViolation() {
        // RACE CONDITION: Test that DataIntegrityViolationException is caught and converted
        // to a clean IllegalArgumentException with proper error message
        // Scenario: Between check and insert, another request creates the same record
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        request.setSchemas(Arrays.asList());

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            // Check passes (no duplicate found)
            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.empty());
            when(extRefRepository.findByTenantIdAndExtRefNameAndExtRefTypeAndId_ExtRefVersion(
                    any(UUID.class), anyString(), any(ExtRefType.class), anyString()))
                    .thenReturn(Optional.empty());

            // But save fails with DataIntegrityViolationException (race condition - another request created it)
            when(extRefRepository.save(any(ExtRef.class)))
                    .thenThrow(new org.springframework.dao.DataIntegrityViolationException(
                            "Unique constraint violation: uk_ext_ref_tenant_id_name_type_version"));

            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            // Execute - should catch DataIntegrityViolationException and throw clean IllegalArgumentException
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType.name(), "Test Process", testExtRefId, testExtRefVersion, request)
            );

            // Verify clean error message (not database stack trace)
            assertTrue(exception.getMessage().contains("already exists"));
            assertTrue(exception.getMessage().contains("Test Process"));
            assertTrue(exception.getMessage().contains(testExtRefType.name()));
            assertTrue(exception.getMessage().contains(testExtRefVersion));
        }
    }

    @Test
    void testCreateOrUpdateExternalReference_RaceCondition_RethrowsNonUniqueConstraintError() {
        // Test that non-unique-constraint DataIntegrityViolationExceptions are re-thrown
        // Only unique constraint violations should be caught and converted
        ExtRefWithSchemasRequest request = new ExtRefWithSchemasRequest();
        request.setSchemas(Arrays.asList());

        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(extRefRepository.findById(new ExtRefId(testExtRefId, testExtRefVersion))).thenReturn(Optional.empty());
            when(extRefRepository.findByTenantIdAndExtRefNameAndExtRefTypeAndId_ExtRefVersion(
                    any(UUID.class), anyString(), any(ExtRefType.class), anyString()))
                    .thenReturn(Optional.empty());

            // Save fails with DataIntegrityViolationException for a DIFFERENT constraint (e.g., foreign key)
            when(extRefRepository.save(any(ExtRef.class)))
                    .thenThrow(new org.springframework.dao.DataIntegrityViolationException(
                            "Foreign key constraint violation: fk_tenant_id"));

            doNothing().when(namespaceFilterManager).enableIfPresent(testNamespace);

            // Execute - should re-throw the DataIntegrityViolationException (not convert to IllegalArgumentException)
            assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () ->
                    externalReferenceService.createOrUpdateExternalReference(
                            testNamespace, testExtRefType.name(), "Test Process", testExtRefId, testExtRefVersion, request)
            );
        }
    }
}
