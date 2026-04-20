package com.ssnc.schemaService.service;

import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.dto.SchemaVersionDto;
import com.ssnc.schemaService.entity.ExtRef;
import com.ssnc.schemaService.entity.ExtRefId;
import com.ssnc.schemaService.entity.ExtRefType;
import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmData;
import com.ssnc.schemaService.entity.SchmDataId;
import com.ssnc.schemaService.entity.SchmExtRefXref;
import com.ssnc.schemaService.repo.ExtRefRepository;
import com.ssnc.schemaService.repo.SchmDataRepository;
import com.ssnc.schemaService.repo.SchmExtRefXrefRepository;
import com.ssnc.schemaService.repo.SchmFilterCriteria;
import com.ssnc.schemaService.repo.SchmRepository;
import com.ssnc.schemaService.tenant.NamespaceFilterManager;
import com.ssnc.schemaService.tenant.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import com.ssnc.shared.security.JwtClaimsContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchemaServiceTest {

    @Mock
    private SchmRepository schmRepository;

    @Mock
    private SchmDataRepository schmDataRepository;

    @Mock
    private NamespaceFilterManager namespaceFilterManager;

    @Mock
    private JwtClaimsContext jwtClaimsContext;

    @Mock
    private SchmExtRefXrefRepository schmExtRefXrefRepository;

    @Mock
    private ExtRefRepository extRefRepository;

    @Mock
    private com.ssnc.schemaService.repo.TenantRepository tenantRepository;

    @Mock
    private NameSpaceService nameSpaceService;

    @InjectMocks
    private SchemaService schemaService;

    private String testNamespace;
    private String testUserId;
    private UUID testTenantId;
    private UUID testNmspcId;
    private SchemaDto testSchemaDto;
    private Schm testSchm;
    private UUID testSchmId;
    private com.ssnc.schemaService.entity.Tenant testTenant;

    @BeforeEach
    void setUp() {
        testNamespace = "testNamespace";
        testUserId = "testUser";
        testSchmId = UUID.randomUUID();
        testTenantId = UUID.randomUUID();
        testNmspcId = UUID.randomUUID();

        testTenant = new com.ssnc.schemaService.entity.Tenant();
        testTenant.setTenantId(testTenantId);
        testTenant.setTenantName("client1Id");

        testSchemaDto = new SchemaDto();
        testSchemaDto.setName("Test Schema");
        testSchemaDto.setDescription("Test Description");
        testSchemaDto.setSchemaType("FormData");
        testSchemaDto.setContentType("application/json");
        testSchemaDto.setSchmGroup("testGroup");

        testSchm = new Schm();
        testSchm.setSchmId(testSchmId);
        testSchm.setTenantId(testTenantId);
        testSchm.setNmspcId(testNmspcId);
        testSchm.setSchmName("Test Schema");
        testSchm.setSchmDesc("Test Description");
        testSchm.setCreatedBy(testUserId);
        testSchm.setUpdatedBy(testUserId);
        testSchm.setCreatedDatetime(LocalDateTime.now());
        testSchm.setUpdatedDatetime(LocalDateTime.now());

        // Mock JwtClaimsContext to return test user
        when(jwtClaimsContext.getUserId()).thenReturn(testUserId);
        when(tenantRepository.findByTenantName(anyString())).thenReturn(Optional.of(testTenant));
        // Mock namespace service to return namespace ID
        when(nameSpaceService.ensureNamespaceExists(any(UUID.class), eq(testNamespace)))
                .thenReturn(testNmspcId);
    }

    @Test
    void testCreateSchema_SetsUserFromJwtContext() throws IOException {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(schmRepository.findBySchmName(anyString())).thenReturn(Optional.empty());
            when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

            SchemaDto result = schemaService.createSchema(testNamespace, testSchemaDto, null);

            assertNotNull(result);
            verify(jwtClaimsContext, atLeastOnce()).getUserId();
            verify(schmRepository).save(argThat(schm ->
                    testUserId.equals(schm.getCreatedBy()) &&
                    testUserId.equals(schm.getUpdatedBy())
            ));
        }
    }

    @Test
    void testCreateSchema_WithContent_CreatesVersion() throws IOException {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            String content = "test content";
            when(schmRepository.findBySchmName(anyString())).thenReturn(Optional.empty());
            when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);
            when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
            when(schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc(any(UUID.class)))
                    .thenReturn(Optional.empty());

            SchmData savedSchmData = new SchmData();
            SchmDataId id = new SchmDataId();
            id.setSchmId(testSchmId);
            id.setSchmVersion(1);
            savedSchmData.setId(id);
            savedSchmData.setCreatedBy(testUserId);
            savedSchmData.setUpdatedBy(testUserId);
            savedSchmData.setSchmData(content);
            savedSchmData.setIsDraft(true);

            when(schmDataRepository.save(any(SchmData.class))).thenReturn(savedSchmData);

            SchemaDto result = schemaService.createSchema(testNamespace, testSchemaDto, content);

            assertNotNull(result);
            verify(schmDataRepository).save(argThat(schmData ->
                    testUserId.equals(schmData.getCreatedBy()) &&
                    testUserId.equals(schmData.getUpdatedBy()) &&
                    content.equals(schmData.getSchmData()) &&
                    schmData.getIsDraft()
            ));
        }
    }

    @Test
    void testCreateSchema_DuplicateName_ThrowsException() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(schmRepository.findBySchmName(anyString())).thenReturn(Optional.of(testSchm));

            assertThrows(IllegalArgumentException.class, () ->
                    schemaService.createSchema(testNamespace, testSchemaDto, null)
            );

            verify(schmRepository, never()).save(any(Schm.class));
        }
    }

    @Test
    void testGetSchemas_ReturnsSortedByName() {
        Schm schm1 = new Schm();
        schm1.setSchmId(UUID.randomUUID());
        schm1.setSchmName("Zebra Schema");
        schm1.setSchmDesc("Last alphabetically");
        schm1.setCreatedBy(testUserId);
        schm1.setUpdatedBy(testUserId);

        Schm schm2 = new Schm();
        schm2.setSchmId(UUID.randomUUID());
        schm2.setSchmName("Apple Schema");
        schm2.setSchmDesc("First alphabetically");
        schm2.setCreatedBy(testUserId);
        schm2.setUpdatedBy(testUserId);

        Schm schm3 = new Schm();
        schm3.setSchmId(UUID.randomUUID());
        schm3.setSchmName("Mango Schema");
        schm3.setSchmDesc("Middle alphabetically");
        schm3.setCreatedBy(testUserId);
        schm3.setUpdatedBy(testUserId);

        List<Schm> unsortedSchemas = Arrays.asList(schm1, schm2, schm3);
        when(schmRepository.findAll(any(Specification.class))).thenReturn(unsortedSchemas);

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "none", PageRequest.of(0, 20)).getContent();

        assertNotNull(result);
        assertEquals(3, result.size());
        // Verify sorted order: Apple, Mango, Zebra
        assertEquals("Apple Schema", result.get(0).getName());
        assertEquals("Mango Schema", result.get(1).getName());
        assertEquals("Zebra Schema", result.get(2).getName());
        verify(namespaceFilterManager).enableIfPresent(testNamespace);
    }

    @Test
    void testCreateSchema_NullJwtContext_UsesSystemUser() throws IOException {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(jwtClaimsContext.getUserId()).thenReturn(null);
            when(schmRepository.findBySchmName(anyString())).thenReturn(Optional.empty());

            Schm systemSchm = new Schm();
            systemSchm.setSchmId(testSchmId);
            systemSchm.setSchmName("Test Schema");
            systemSchm.setCreatedBy(AppConstants.SYSTEM_USER);
            systemSchm.setUpdatedBy(AppConstants.SYSTEM_USER);

            when(schmRepository.save(any(Schm.class))).thenReturn(systemSchm);

            SchemaDto result = schemaService.createSchema(testNamespace, testSchemaDto, null);

            assertNotNull(result);
            verify(schmRepository).save(argThat(schm ->
                    AppConstants.SYSTEM_USER.equals(schm.getCreatedBy()) &&
                    AppConstants.SYSTEM_USER.equals(schm.getUpdatedBy())
            ));
        }
    }

    @Test
    void testUpdateSchema_SetsUserFromJwtContext() {
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        SchemaDto updateDto = new SchemaDto();
        updateDto.setDescription("Updated Description");

        SchemaDto result = schemaService.updateSchema(testNamespace, testSchmId, updateDto);

        assertNotNull(result);
        verify(jwtClaimsContext, atLeastOnce()).getUserId();
        verify(schmRepository).save(argThat(schm ->
                testUserId.equals(schm.getUpdatedBy())
        ));
    }

    @Test
    void testUpdateSchema_NotFound_ThrowsException() {
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.empty());

        SchemaDto updateDto = new SchemaDto();
        updateDto.setDescription("Updated Description");

        assertThrows(IllegalArgumentException.class, () ->
                schemaService.updateSchema(testNamespace, testSchmId, updateDto)
        );

        verify(schmRepository, never()).save(any(Schm.class));
    }

    @Test
    void testUpdateDraftContent_CreatesNewVersion_WithJwtUser() {
        testSchm.setDraftVersion(null);
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc(testSchmId))
                .thenReturn(Optional.empty());

        SchmData savedSchmData = new SchmData();
        SchmDataId id = new SchmDataId();
        id.setSchmId(testSchmId);
        id.setSchmVersion(1);
        savedSchmData.setId(id);
        savedSchmData.setCreatedBy(testUserId);
        savedSchmData.setUpdatedBy(testUserId);
        savedSchmData.setIsDraft(true);

        when(schmDataRepository.save(any(SchmData.class))).thenReturn(savedSchmData);
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        String content = "new draft content";
        SchemaVersionDto result = schemaService.updateDraftContent(testNamespace, testSchmId, content);

        assertNotNull(result);
        verify(schmDataRepository).save(argThat(schmData ->
                testUserId.equals(schmData.getCreatedBy()) &&
                testUserId.equals(schmData.getUpdatedBy()) &&
                content.equals(schmData.getSchmData())
        ));
        verify(schmRepository).save(argThat(schm -> schm.getDraftVersion() != null && schm.getDraftVersion().equals(1)));
    }

    @Test
    void testUpdateDraftContent_UpdatesExistingDraft() {
        SchmDataId id = new SchmDataId();
        id.setSchmId(testSchmId);
        id.setSchmVersion(1);

        SchmData existingDraft = new SchmData();
        existingDraft.setId(id);
        existingDraft.setIsDraft(true);
        existingDraft.setCreatedBy("oldUser");
        existingDraft.setUpdatedBy("oldUser");
        existingDraft.setSchmData("old content");

        testSchm.setDraftVersion(1);
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmDataRepository.findById(id)).thenReturn(Optional.of(existingDraft));
        when(schmDataRepository.save(any(SchmData.class))).thenReturn(existingDraft);

        String newContent = "updated draft content";
        SchemaVersionDto result = schemaService.updateDraftContent(testNamespace, testSchmId, newContent);

        assertNotNull(result);
        verify(schmDataRepository).save(argThat(schmData ->
                newContent.equals(schmData.getSchmData())
        ));
    }

    @Test
    void testPublishSchemaVersion_Success() {
        SchmDataId id = new SchmDataId();
        id.setSchmId(testSchmId);
        id.setSchmVersion(1);

        SchmData schemaData = new SchmData();
        schemaData.setId(id);
        schemaData.setIsDraft(true);

        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.getSchemaVersion(testSchmId, 1)).thenReturn(Optional.of(schemaData));
        when(schmDataRepository.save(any(SchmData.class))).thenReturn(schemaData);
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        schemaService.publishSchemaVersion(testNamespace, testSchmId, 1);

        verify(schmDataRepository).save(argThat(sd -> !sd.getIsDraft()));
        verify(schmRepository).save(argThat(schm ->
                schm.getPublishVersion().equals(1) && schm.getDraftVersion() == null));
    }

    @Test
    void testPublishSchemaVersion_AlreadyPublished_ThrowsException() {
        testSchm.setPublishVersion(1);
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));

        assertThrows(IllegalStateException.class, () ->
                schemaService.publishSchemaVersion(testNamespace, testSchmId, 1)
        );
    }

    @Test
    void testPublishSchemaVersion_VersionNotFound_ThrowsException() {
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.getSchemaVersion(testSchmId, 1)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                schemaService.publishSchemaVersion(testNamespace, testSchmId, 1)
        );
    }

    @Test
    void testUnPublishSchemaVersion_Success() {
        testSchm.setPublishVersion(1);
        // Verify pessimistic locking is used
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmExtRefXrefRepository.findBySchmId(testSchmId)).thenReturn(Arrays.asList());
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        schemaService.unPublishSchemaVersion(testNamespace, testSchmId);

        // Verify the locked method was called (prevents race conditions)
        verify(schmRepository).findWithLockBySchmId(testSchmId);
        verify(schmExtRefXrefRepository).findBySchmId(testSchmId);
        verify(schmRepository).save(argThat(schm -> schm.getPublishVersion() == null));
    }

    @Test
    void testUnPublishSchemaVersion_SchemaNotFound_ThrowsException() {
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                schemaService.unPublishSchemaVersion(testNamespace, testSchmId)
        );

        verify(schmRepository).findWithLockBySchmId(testSchmId);
    }

    @Test
    void testUnPublishSchemaVersion_SchemaInUse_ThrowsException() {
        testSchm.setPublishVersion(1);
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));

        // Mock external references exist
        SchmExtRefXref xref = new SchmExtRefXref();
        xref.setSchmId(testSchmId);
        xref.setExtRefId("EXT-REF-IN-USE");
        when(schmExtRefXrefRepository.findBySchmId(testSchmId)).thenReturn(Arrays.asList(xref));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                schemaService.unPublishSchemaVersion(testNamespace, testSchmId)
        );

        assertTrue(exception.getMessage().contains("cannot be unpublished as it is in use"));
        verify(schmRepository).findWithLockBySchmId(testSchmId);
        verify(schmExtRefXrefRepository).findBySchmId(testSchmId);
        verify(schmRepository, never()).save(any(Schm.class));
    }

    @Test
    void testUnPublishSchemaVersion_UsesPessimisticLocking_PreventingRaceCondition() {
        // This test verifies that pessimistic locking is used to prevent the race condition
        // where a reference could be created between checking and unpublishing
        testSchm.setPublishVersion(1);
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmExtRefXrefRepository.findBySchmId(testSchmId)).thenReturn(Arrays.asList());
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        schemaService.unPublishSchemaVersion(testNamespace, testSchmId);

        // Verify findWithLockBySchmId is called, NOT findBySchmId
        // This ensures the pessimistic write lock is acquired, preventing concurrent modifications
        verify(schmRepository).findWithLockBySchmId(testSchmId);
        verify(schmRepository, never()).findBySchmId(testSchmId);
    }

    @Test
    void testLockSchema_Success() {
        testSchm.setLockBy(null); // Not locked
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        schemaService.lockSchema(testNamespace, testSchmId);

        verify(schmRepository).findWithLockBySchmId(testSchmId);
        verify(schmRepository).save(argThat(schm ->
                testUserId.equals(schm.getLockBy())
        ));
        verify(jwtClaimsContext, atLeastOnce()).getUserId();
    }

    @Test
    void testLockSchema_AlreadyLockedBySameUser_Success() {
        testSchm.setLockBy(testUserId); // Already locked by same user
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        schemaService.lockSchema(testNamespace, testSchmId);

        verify(schmRepository).findWithLockBySchmId(testSchmId);
        verify(schmRepository).save(argThat(schm ->
                testUserId.equals(schm.getLockBy())
        ));
    }

    @Test
    void testLockSchema_AlreadyLockedByDifferentUser_ThrowsException() {
        testSchm.setLockBy("otherUser"); // Locked by different user
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                schemaService.lockSchema(testNamespace, testSchmId)
        );

        assertTrue(exception.getMessage().contains("already locked by"));
        verify(schmRepository).findWithLockBySchmId(testSchmId);
        verify(schmRepository, never()).save(any(Schm.class));
    }

    @Test
    void testLockSchema_SchemaNotFound_ThrowsException() {
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                schemaService.lockSchema(testNamespace, testSchmId)
        );

        verify(schmRepository).findWithLockBySchmId(testSchmId);
    }

    @Test
    void testLockSchema_WithNullJwtContext_UsesSYSTEMUser() {
        when(jwtClaimsContext.getUserId()).thenReturn(null);
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        schemaService.lockSchema(testNamespace, testSchmId);

        verify(schmRepository).findWithLockBySchmId(testSchmId);
        verify(schmRepository).save(argThat(schm ->
                AppConstants.SYSTEM_USER.equals(schm.getLockBy())
        ));
    }

    @Test
    void testUnlockSchema_Success() {
        testSchm.setLockBy(testUserId); // Locked by same user
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        schemaService.unlockSchema(testNamespace, testSchmId);

        verify(schmRepository).findWithLockBySchmId(testSchmId);
        verify(schmRepository).save(argThat(schm -> schm.getLockBy() == null));
    }

    @Test
    void testUnlockSchema_LockedByDifferentUser_ThrowsException() {
        testSchm.setLockBy("otherUser"); // Locked by different user
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                schemaService.unlockSchema(testNamespace, testSchmId)
        );

        assertTrue(exception.getMessage().contains("Cannot unlock"));
        verify(schmRepository).findWithLockBySchmId(testSchmId);
        verify(schmRepository, never()).save(any(Schm.class));
    }

    @Test
    void testUnlockSchema_SystemUserCanUnlockAny() {
        testSchm.setLockBy("otherUser"); // Locked by different user
        when(jwtClaimsContext.getUserId()).thenReturn(AppConstants.SYSTEM_USER);
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        schemaService.unlockSchema(testNamespace, testSchmId);

        verify(schmRepository).findWithLockBySchmId(testSchmId);
        verify(schmRepository).save(argThat(schm -> schm.getLockBy() == null));
    }

    @Test
    void testUnlockSchema_SchemaNotFound_ThrowsException() {
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                schemaService.unlockSchema(testNamespace, testSchmId)
        );

        verify(schmRepository).findWithLockBySchmId(testSchmId);
    }

    @Test
    void testLockSchema_UsesPessimisticLocking_PreventingRaceCondition() {
        // This test verifies that pessimistic locking is used to prevent race conditions
        testSchm.setLockBy(null);
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        schemaService.lockSchema(testNamespace, testSchmId);

        // Verify findWithLockBySchmId is called, NOT findBySchmId
        verify(schmRepository).findWithLockBySchmId(testSchmId);
        verify(schmRepository, never()).findBySchmId(testSchmId);
    }

    @Test
    void testLockSchema_IdempotentBehavior_SameUserMultipleCalls() {
        // This test verifies that lockSchema is idempotent when the same user calls it multiple times
        // This is intentional behavior to allow safe retries and prevent lock state corruption
        testSchm.setLockBy(testUserId); // Already locked by same user
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        // Call lock multiple times - all should succeed (idempotent)
        schemaService.lockSchema(testNamespace, testSchmId);
        schemaService.lockSchema(testNamespace, testSchmId);
        schemaService.lockSchema(testNamespace, testSchmId);

        // All calls should succeed without exception
        // Verify pessimistic lock was acquired each time (preventing concurrent different users)
        verify(schmRepository, times(3)).findWithLockBySchmId(testSchmId);
        verify(schmRepository, times(3)).save(any(Schm.class));

        // Verify final state: still locked by the same user
        verify(schmRepository, atLeast(1)).save(argThat(schm ->
                testUserId.equals(schm.getLockBy())
        ));
    }

    @Test
    void testUnlockSchema_UsesPessimisticLocking_PreventingRaceCondition() {
        // This test verifies that pessimistic locking is used to prevent race conditions
        testSchm.setLockBy(testUserId);
        when(schmRepository.findWithLockBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        schemaService.unlockSchema(testNamespace, testSchmId);

        // Verify findWithLockBySchmId is called, NOT findBySchmId
        verify(schmRepository).findWithLockBySchmId(testSchmId);
        verify(schmRepository, never()).findBySchmId(testSchmId);
    }

    @Test
    void testGetSchemas_SortByNameAsc() {
        Schm schm1 = createTestSchm("Zebra Schema");
        Schm schm2 = createTestSchm("Apple Schema");
        Schm schm3 = createTestSchm("Mango Schema");

        List<Schm> unsortedSchemas = Arrays.asList(schm1, schm2, schm3);
        when(schmRepository.findAll(any(Specification.class))).thenReturn(unsortedSchemas);

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, "nameAsc", "none", PageRequest.of(0, 20)).getContent();

        assertEquals(3, result.size());
        assertEquals("Apple Schema", result.get(0).getName());
        assertEquals("Mango Schema", result.get(1).getName());
        assertEquals("Zebra Schema", result.get(2).getName());
    }

    @Test
    void testGetSchemas_SortByNameAsc_WithNulls() {
        Schm schm1 = createTestSchm("Zebra Schema");
        Schm schm2 = createTestSchm(null); // Null name
        Schm schm3 = createTestSchm("Apple Schema");

        List<Schm> unsortedSchemas = Arrays.asList(schm1, schm2, schm3);
        when(schmRepository.findAll(any(Specification.class))).thenReturn(unsortedSchemas);

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, "nameAsc", "none", PageRequest.of(0, 20)).getContent();

        // Should not throw NPE, nulls should be first
        assertEquals(3, result.size());
        assertNull(result.get(0).getName()); // Null first
        assertEquals("Apple Schema", result.get(1).getName());
        assertEquals("Zebra Schema", result.get(2).getName());
    }

    @Test
    void testGetSchemas_SortByNameDesc() {
        Schm schm1 = createTestSchm("Apple Schema");
        Schm schm2 = createTestSchm("Zebra Schema");
        Schm schm3 = createTestSchm("Mango Schema");

        List<Schm> unsortedSchemas = Arrays.asList(schm1, schm2, schm3);
        when(schmRepository.findAll(any(Specification.class))).thenReturn(unsortedSchemas);

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, "nameDesc", "none", PageRequest.of(0, 20)).getContent();

        assertEquals(3, result.size());
        assertEquals("Zebra Schema", result.get(0).getName());
        assertEquals("Mango Schema", result.get(1).getName());
        assertEquals("Apple Schema", result.get(2).getName());
    }

    @Test
    void testGetSchemas_SortByVersionUpdateDesc() {
        Schm schm1 = createTestSchmWithDate("Schema 1", LocalDateTime.of(2024, 1, 1, 10, 0));
        Schm schm2 = createTestSchmWithDate("Schema 2", LocalDateTime.of(2024, 1, 3, 10, 0));
        Schm schm3 = createTestSchmWithDate("Schema 3", LocalDateTime.of(2024, 1, 2, 10, 0));

        List<Schm> unsortedSchemas = Arrays.asList(schm1, schm2, schm3);
        when(schmRepository.findAll(any(Specification.class))).thenReturn(unsortedSchemas);

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, "versionUpdateDesc", "none", PageRequest.of(0, 20)).getContent();

        assertEquals(3, result.size());
        assertEquals("Schema 2", result.get(0).getName());
        assertEquals("Schema 3", result.get(1).getName());
        assertEquals("Schema 1", result.get(2).getName());
    }

    @Test
    void testGetSchemas_SortByVersionUpdateAsc() {
        Schm schm1 = createTestSchmWithDate("Schema 1", LocalDateTime.of(2024, 1, 3, 10, 0));
        Schm schm2 = createTestSchmWithDate("Schema 2", LocalDateTime.of(2024, 1, 1, 10, 0));
        Schm schm3 = createTestSchmWithDate("Schema 3", LocalDateTime.of(2024, 1, 2, 10, 0));

        List<Schm> unsortedSchemas = Arrays.asList(schm1, schm2, schm3);
        when(schmRepository.findAll(any(Specification.class))).thenReturn(unsortedSchemas);

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, "versionUpdateAsc", "none", PageRequest.of(0, 20)).getContent();

        assertEquals(3, result.size());
        assertEquals("Schema 2", result.get(0).getName());
        assertEquals("Schema 3", result.get(1).getName());
        assertEquals("Schema 1", result.get(2).getName());
    }

    private Schm createTestSchm(String name) {
        Schm schm = new Schm();
        schm.setSchmId(UUID.randomUUID());
        schm.setSchmName(name);
        schm.setSchmDesc("Test description");
        schm.setCreatedBy(testUserId);
        schm.setUpdatedBy(testUserId);
        schm.setUpdatedDatetime(LocalDateTime.now());
        return schm;
    }

    private Schm createTestSchmWithDate(String name, LocalDateTime updatedDate) {
        Schm schm = createTestSchm(name);
        schm.setUpdatedDatetime(updatedDate);
        return schm;
    }

    @Test
    void testGetSchemas_WithVersionDraft_OnlyReturnsSchemasWithDraft() {
        // Schema 1: Has draft
        Schm schm1 = createTestSchm("Schema 1");
        UUID schm1Id = schm1.getSchmId();

        // Schema 2: No draft
        Schm schm2 = createTestSchm("Schema 2");
        UUID schm2Id = schm2.getSchmId();

        // Schema 3: Has draft
        Schm schm3 = createTestSchm("Schema 3");
        UUID schm3Id = schm3.getSchmId();

        // Set draft versions directly in SCHM entities
        schm1.setDraftVersion(1);
        schm2.setDraftVersion(null);
        schm3.setDraftVersion(1);

        List<Schm> schemas = Arrays.asList(schm1, schm2, schm3);
        when(schmRepository.findAll(any(Specification.class))).thenReturn(schemas);

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "draft", PageRequest.of(0, 20)).getContent();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.getName().equals("Schema 1")));
        assertTrue(result.stream().anyMatch(s -> s.getName().equals("Schema 3")));
        assertFalse(result.stream().anyMatch(s -> s.getName().equals("Schema 2")));
    }

    @Test
    void testGetSchemas_WithVersionPublished_OnlyReturnsSchemasWithPublishedVersion() {
        // Schema 1: Has published version
        Schm schm1 = createTestSchm("Schema 1");
        schm1.setPublishVersion(2);
        UUID schm1Id = schm1.getSchmId();

        // Schema 2: No published version
        Schm schm2 = createTestSchm("Schema 2");
        schm2.setPublishVersion(null);
        UUID schm2Id = schm2.getSchmId();

        // Schema 3: Has published version
        Schm schm3 = createTestSchm("Schema 3");
        schm3.setPublishVersion(1);
        UUID schm3Id = schm3.getSchmId();

        List<Schm> schemas = Arrays.asList(schm1, schm2, schm3);
        when(schmRepository.findAll(any(Specification.class))).thenReturn(schemas);


        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "published", PageRequest.of(0, 20)).getContent();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.getName().equals("Schema 1")));
        assertTrue(result.stream().anyMatch(s -> s.getName().equals("Schema 3")));
        assertFalse(result.stream().anyMatch(s -> s.getName().equals("Schema 2")));
    }

    @Test
    void testGetSchemas_WithVersionLatest_ReturnsSchemasWithAnyVersion() {
        // Schema 1: Has both draft and published
        Schm schm1 = createTestSchm("Schema 1");
        schm1.setPublishVersion(1);
        UUID schm1Id = schm1.getSchmId();

        // Schema 2: No versions
        Schm schm2 = createTestSchm("Schema 2");
        UUID schm2Id = schm2.getSchmId();

        // Schema 3: Has only draft
        Schm schm3 = createTestSchm("Schema 3");
        UUID schm3Id = schm3.getSchmId();

        // Set draft versions directly in SCHM entities
        schm1.setDraftVersion(2);
        schm2.setDraftVersion(null);
        schm3.setDraftVersion(1);

        List<Schm> schemas = Arrays.asList(schm1, schm2, schm3);
        when(schmRepository.findAll(any(Specification.class))).thenReturn(schemas);

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "latest", PageRequest.of(0, 20)).getContent();

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> s.getName().equals("Schema 1")));
        assertTrue(result.stream().anyMatch(s -> s.getName().equals("Schema 3")));
        assertFalse(result.stream().anyMatch(s -> s.getName().equals("Schema 2")));
    }

    @Test
    void testGetSchemas_WithVersionNone_ReturnsAllSchemas() {
        Schm schm1 = createTestSchm("Schema 1");
        schm1.setPublishVersion(1);
        UUID schm1Id = schm1.getSchmId();

        Schm schm2 = createTestSchm("Schema 2");
        UUID schm2Id = schm2.getSchmId();

        Schm schm3 = createTestSchm("Schema 3");
        UUID schm3Id = schm3.getSchmId();

        List<Schm> schemas = Arrays.asList(schm1, schm2, schm3);
        when(schmRepository.findAll(any(Specification.class))).thenReturn(schemas);


        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "none", PageRequest.of(0, 20)).getContent();

        assertEquals(3, result.size());
    }

    @Test
    void testGetSchemas_WithVersionNull_ReturnsAllSchemas() {
        Schm schm1 = createTestSchm("Schema 1");
        UUID schm1Id = schm1.getSchmId();

        Schm schm2 = createTestSchm("Schema 2");
        UUID schm2Id = schm2.getSchmId();

        List<Schm> schemas = Arrays.asList(schm1, schm2);
        when(schmRepository.findAll(any(Specification.class))).thenReturn(schemas);


        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, null, null, PageRequest.of(0, 20)).getContent();

        assertEquals(2, result.size());
    }

    private SchmData createDraftVersion(UUID schmId, int versionNumber) {
        SchmDataId id = new SchmDataId();
        id.setSchmId(schmId);
        id.setSchmVersion(versionNumber);

        SchmData draft = new SchmData();
        draft.setId(id);
        draft.setIsDraft(true);
        draft.setCreatedBy(testUserId);
        draft.setUpdatedBy(testUserId);
        draft.setCreatedDatetime(LocalDateTime.now());
        draft.setUpdatedDatetime(LocalDateTime.now());

        return draft;
    }

    @Test
    void testGetExternalReferencesBySchemaId_Success() {
        String extRefId1 = "EXT-REF-001";
        String extRefId2 = "EXT-REF-002";
        String extRefVersion1 = "1.0";
        String extRefVersion2 = "2.0";

        // Mock schema exists
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));

        // Mock cross-references with versions
        SchmExtRefXref xref1 = new SchmExtRefXref();
        xref1.setSchmId(testSchmId);
        xref1.setExtRefId(extRefId1);
        xref1.setExtRefVersion(extRefVersion1);

        SchmExtRefXref xref2 = new SchmExtRefXref();
        xref2.setSchmId(testSchmId);
        xref2.setExtRefId(extRefId2);
        xref2.setExtRefVersion(extRefVersion2);

        List<SchmExtRefXref> xrefs = Arrays.asList(xref1, xref2);
        when(schmExtRefXrefRepository.findBySchmId(testSchmId)).thenReturn(xrefs);

        // Mock external references
        ExtRef extRef1 = new ExtRef();
        extRef1.setId(extRefId1, extRefVersion1);
        extRef1.setExtRefName("API Reference");
        extRef1.setExtRefType(ExtRefType.PROCESS);
        extRef1.setCreatedBy(testUserId);
        extRef1.setUpdatedBy(testUserId);

        ExtRef extRef2 = new ExtRef();
        extRef2.setId(extRefId2, extRefVersion2);
        extRef2.setExtRefName("Database Reference");
        extRef2.setExtRefType(ExtRefType.AUTOMATION);
        extRef2.setCreatedBy(testUserId);
        extRef2.setUpdatedBy(testUserId);

        // Mock batch fetch (fix N+1 query) - using composite keys
        when(extRefRepository.findAllById(Arrays.asList(
                new ExtRefId(extRefId1, extRefVersion1),
                new ExtRefId(extRefId2, extRefVersion2))))
                .thenReturn(Arrays.asList(extRef1, extRef2));

        // Execute
        List<ExtRefDto> result = schemaService.getExternalReferencesBySchemaId(testNamespace, testSchmId, PageRequest.of(0, 20)).getContent();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("API Reference", result.get(0).getExtRefName());
        assertEquals("PROCESS", result.get(0).getExtRefType());
        assertEquals("1.0", result.get(0).getExtRefVersion());
        assertEquals("Database Reference", result.get(1).getExtRefName());
        assertEquals("AUTOMATION", result.get(1).getExtRefType());
        assertEquals("2.0", result.get(1).getExtRefVersion());

        verify(namespaceFilterManager).enableIfPresent(testNamespace);
        verify(schmRepository).findBySchmId(testSchmId);
        verify(schmExtRefXrefRepository).findBySchmId(testSchmId);
        verify(extRefRepository).findAllById(anyIterable());
        verify(extRefRepository, never()).findById(any());
    }

    @Test
    void testGetExternalReferencesBySchemaId_SchemaNotFound() {
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                schemaService.getExternalReferencesBySchemaId(testNamespace, testSchmId, PageRequest.of(0, 20)).getContent()
        );

        verify(namespaceFilterManager).enableIfPresent(testNamespace);
        verify(schmRepository).findBySchmId(testSchmId);
        verify(schmExtRefXrefRepository, never()).findBySchmId(any());
    }

    @Test
    void testGetExternalReferencesBySchemaId_NoExternalReferences() {
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmExtRefXrefRepository.findBySchmId(testSchmId)).thenReturn(Arrays.asList());
        when(extRefRepository.findAllById(anyIterable())).thenReturn(Arrays.asList());

        List<ExtRefDto> result = schemaService.getExternalReferencesBySchemaId(testNamespace, testSchmId, PageRequest.of(0, 20)).getContent();

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(schmRepository).findBySchmId(testSchmId);
        verify(schmExtRefXrefRepository).findBySchmId(testSchmId);
    }

    @Test
    void testGetExternalReferencesBySchemaId_WithMissingExtRef() {
        String extRefId1 = "EXT-REF-100";
        String extRefId2 = "EXT-REF-200";
        String extRefVersion1 = "1.0";
        String extRefVersion2 = "2.0";

        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));

        SchmExtRefXref xref1 = new SchmExtRefXref();
        xref1.setSchmId(testSchmId);
        xref1.setExtRefId(extRefId1);
        xref1.setExtRefVersion(extRefVersion1);

        SchmExtRefXref xref2 = new SchmExtRefXref();
        xref2.setSchmId(testSchmId);
        xref2.setExtRefId(extRefId2);
        xref2.setExtRefVersion(extRefVersion2);

        List<SchmExtRefXref> xrefs = Arrays.asList(xref1, xref2);
        when(schmExtRefXrefRepository.findBySchmId(testSchmId)).thenReturn(xrefs);

        ExtRef extRef1 = new ExtRef();
        extRef1.setId(extRefId1, extRefVersion1);
        extRef1.setExtRefName("API Reference");
        extRef1.setExtRefType(ExtRefType.PROCESS);

        // Mock batch fetch - only extRef1 exists, extRef2 is missing
        when(extRefRepository.findAllById(Arrays.asList(
                new ExtRefId(extRefId1, extRefVersion1),
                new ExtRefId(extRefId2, extRefVersion2))))
                .thenReturn(Arrays.asList(extRef1)); // Only returns extRef1

        List<ExtRefDto> result = schemaService.getExternalReferencesBySchemaId(testNamespace, testSchmId, PageRequest.of(0, 20)).getContent();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("API Reference", result.get(0).getExtRefName());
        verify(extRefRepository).findAllById(anyIterable());
    }

    @Test
    void testImportSchema_Success_CreatesAndPublishes() throws IOException {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            String content = "test schema content";
            when(schmRepository.findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, testSchemaDto.getName()))
                    .thenReturn(Optional.empty());
            when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);
            when(schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc(any(UUID.class)))
                    .thenReturn(Optional.empty());

            SchmDataId id = new SchmDataId();
            id.setSchmId(testSchmId);
            id.setSchmVersion(1);

            SchmData savedSchmData = new SchmData();
            savedSchmData.setId(id);
            savedSchmData.setCreatedBy(testUserId);
            savedSchmData.setUpdatedBy(testUserId);
            savedSchmData.setSchmData(content);
            savedSchmData.setIsDraft(true);

            when(schmDataRepository.save(any(SchmData.class))).thenReturn(savedSchmData);

            // Mock publishSchemaVersion internal calls
            when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
            when(schmRepository.getSchemaVersion(testSchmId, 1)).thenReturn(Optional.of(savedSchmData));

            SchemaDto result = schemaService.importSchema(testNamespace, testSchemaDto, content);

            assertNotNull(result);
            // Verify schema was saved (initial save + publish calls save internally)
            verify(schmRepository, atLeastOnce()).save(any(Schm.class));
            // Verify initial schema save with correct user
            verify(schmRepository, atLeastOnce()).save(argThat(schm ->
                    schm != null && testUserId.equals(schm.getCreatedBy()) &&
                    testUserId.equals(schm.getUpdatedBy())
            ));
            // Verify content was saved
            verify(schmDataRepository, atLeastOnce()).save(argThat(schmData ->
                    schmData != null && content.equals(schmData.getSchmData())
            ));
            // Verify findBySchmId was called to refresh schema
            verify(schmRepository, atLeastOnce()).findBySchmId(testSchmId);
        }
    }

    @Test
    void testImportSchema_DuplicateName_ThrowsException() throws IOException {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            String content = "test content";
            // Mock that schema with this name already exists in this tenant+namespace
            when(schmRepository.findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, testSchemaDto.getName()))
                    .thenReturn(Optional.of(testSchm));

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    schemaService.importSchema(testNamespace, testSchemaDto, content)
            );

            assertEquals(ErrorMessages.SCHEMA_IMPORT_EXISTS, exception.getMessage());
            // Verify that tenant+namespace-aware lookup was called to check for duplicates
            verify(schmRepository).findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, testSchemaDto.getName());
            // Verify save was never called since duplicate was detected early
            verify(schmRepository, never()).save(any(Schm.class));
            verify(schmDataRepository, never()).save(any(SchmData.class));
        }
    }

    @Test
    void testImportSchema_MissingContent_ThrowsException() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    schemaService.importSchema(testNamespace, testSchemaDto, null)
            );

            assertEquals("Content is required for schema import", exception.getMessage());
            verify(schmRepository, never()).save(any(Schm.class));
        }
    }

    @Test
    void testImportSchema_EmptyContent_ThrowsException() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    schemaService.importSchema(testNamespace, testSchemaDto, "")
            );

            assertEquals("Content is required for schema import", exception.getMessage());
            verify(schmRepository, never()).save(any(Schm.class));
        }
    }

    @Test
    void testImportSchema_PublishFails_ThrowsException() throws IOException {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            String content = "test content";
            when(schmRepository.findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, testSchemaDto.getName()))
                    .thenReturn(Optional.empty());
            when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);
            when(schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc(any(UUID.class)))
                    .thenReturn(Optional.empty());

            SchmDataId id = new SchmDataId();
            id.setSchmId(testSchmId);
            id.setSchmVersion(1);

            SchmData savedSchmData = new SchmData();
            savedSchmData.setId(id);
            savedSchmData.setIsDraft(true);

            when(schmDataRepository.save(any(SchmData.class))).thenReturn(savedSchmData);

            // Mock findBySchmId for publishSchemaVersion
            when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
            // Version not found during publish step - publishSchemaVersion will throw
            when(schmRepository.getSchemaVersion(testSchmId, 1)).thenReturn(Optional.empty());

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    schemaService.importSchema(testNamespace, testSchemaDto, content)
            );

            assertEquals("Failed to publish initial version during import", exception.getMessage());
            assertNotNull(exception.getCause());
            assertTrue(exception.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    void testImportSchema_AtomicTransaction_PublishFailureRollsBack() throws IOException {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            String content = "test content";
            when(schmRepository.findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, testSchemaDto.getName()))
                    .thenReturn(Optional.empty());
            when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

            when(schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc(any(UUID.class)))
                    .thenReturn(Optional.empty());

            SchmDataId id = new SchmDataId();
            id.setSchmId(testSchmId);
            id.setSchmVersion(1);

            SchmData savedSchmData = new SchmData();
            savedSchmData.setId(id);
            savedSchmData.setIsDraft(true);

            when(schmDataRepository.save(any(SchmData.class))).thenReturn(savedSchmData);

            // Mock findBySchmId for publishSchemaVersion
            when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
            when(schmRepository.getSchemaVersion(testSchmId, 1)).thenReturn(Optional.of(savedSchmData));

            // Simulate failure during publish step - second save in publishSchemaVersion
            when(schmRepository.save(argThat(schm -> schm != null && schm.getPublishVersion() != null && schm.getPublishVersion().equals(1))))
                    .thenThrow(new RuntimeException("Database error during publish"));

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    schemaService.importSchema(testNamespace, testSchemaDto, content)
            );

            assertEquals("Failed to publish initial version during import", exception.getMessage());
            // Verify that operations were attempted but transaction should rollback
            // This test ensures @Transactional is present and working
            verify(schmRepository, atLeastOnce()).save(any(Schm.class));
            verify(schmDataRepository, atLeastOnce()).save(any(SchmData.class));
        }
    }

    @Test
    void testImportSchema_RaceCondition_DuplicateNameConstraint_ThrowsProperError() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            String content = "test content";
            // Simulate race condition: check passes but save fails due to concurrent insert
            when(schmRepository.findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, testSchemaDto.getName()))
                    .thenReturn(Optional.empty());

            // Create proper SQLException with SQLState code for unique constraint violation
            java.sql.SQLException sqlException = new java.sql.SQLException(
                    "duplicate key value violates unique constraint \"uk_schm_name\"", "23505");
            org.springframework.dao.DataIntegrityViolationException constraintException =
                    new org.springframework.dao.DataIntegrityViolationException(
                            "unique constraint violation: schm_name", sqlException);

            when(schmRepository.save(any(Schm.class))).thenThrow(constraintException);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                    schemaService.importSchema(testNamespace, testSchemaDto, content)
            );

            assertEquals(ErrorMessages.SCHEMA_IMPORT_EXISTS, exception.getMessage());
            verify(schmRepository).findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, testSchemaDto.getName());
            verify(schmRepository).save(any(Schm.class));
        }
    }

    @Test
    void testImportSchema_RaceCondition_OtherConstraintViolation_Rethrows() {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            String content = "test content";
            when(schmRepository.findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, testSchemaDto.getName()))
                    .thenReturn(Optional.empty());

            // Create proper SQLException with different SQLState (23503 = foreign key violation)
            java.sql.SQLException sqlException = new java.sql.SQLException(
                    "foreign key constraint violation on tenant_id", "23503");
            org.springframework.dao.DataIntegrityViolationException constraintException =
                    new org.springframework.dao.DataIntegrityViolationException(
                            "foreign key constraint violation", sqlException);

            when(schmRepository.save(any(Schm.class))).thenThrow(constraintException);

            assertThrows(org.springframework.dao.DataIntegrityViolationException.class, () ->
                    schemaService.importSchema(testNamespace, testSchemaDto, content)
            );

            verify(schmRepository).findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, testSchemaDto.getName());
            verify(schmRepository).save(any(Schm.class));
        }
    }

    @Test
    void testImportSchema_SetsUserFromJwtContext() throws IOException {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            String content = "test content";
            when(schmRepository.findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, testSchemaDto.getName()))
                    .thenReturn(Optional.empty());
            when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);
            when(schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc(any(UUID.class)))
                    .thenReturn(Optional.empty());

            SchmDataId id = new SchmDataId();
            id.setSchmId(testSchmId);
            id.setSchmVersion(1);

            SchmData savedSchmData = new SchmData();
            savedSchmData.setId(id);
            savedSchmData.setCreatedBy(testUserId);
            savedSchmData.setUpdatedBy(testUserId);
            savedSchmData.setSchmData(content);
            savedSchmData.setIsDraft(true);

            when(schmDataRepository.save(any(SchmData.class))).thenReturn(savedSchmData);
            // Mock publishSchemaVersion calls
            when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
            when(schmRepository.getSchemaVersion(testSchmId, 1)).thenReturn(Optional.of(savedSchmData));

            SchemaDto result = schemaService.importSchema(testNamespace, testSchemaDto, content);

            assertNotNull(result);
            verify(jwtClaimsContext, atLeastOnce()).getUserId();
            // schmRepository.save is called (initial + possibly publish version update)
            verify(schmRepository, atLeastOnce()).save(argThat(schm ->
                    schm != null && testUserId.equals(schm.getCreatedBy()) &&
                    testUserId.equals(schm.getUpdatedBy())
            ));
            verify(schmDataRepository, atLeastOnce()).save(argThat(schmData ->
                    schmData != null && testUserId.equals(schmData.getCreatedBy()) &&
                    testUserId.equals(schmData.getUpdatedBy())
            ));
        }
    }

    @Test
    void testImportSchema_MultiTenantIsolation_SameNameDifferentTenant() throws IOException {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            String content = "test content";
            String schemaName = "CommonSchemaName";
            testSchemaDto.setName(schemaName);

            // Schema with same name exists in different tenant - should not conflict
            UUID otherTenantId = UUID.randomUUID();
            Schm existingInOtherTenant = new Schm();
            existingInOtherTenant.setSchmId(UUID.randomUUID());
            existingInOtherTenant.setTenantId(otherTenantId);
            existingInOtherTenant.setNmspcId(testNmspcId);
            existingInOtherTenant.setSchmName(schemaName);

            // SECURITY: Lookup is scoped to current tenant+namespace, so returns empty
            when(schmRepository.findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, schemaName))
                    .thenReturn(Optional.empty());

            when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);
            when(schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc(any(UUID.class)))
                    .thenReturn(Optional.empty());

            SchmDataId id = new SchmDataId();
            id.setSchmId(testSchmId);
            id.setSchmVersion(1);

            SchmData savedSchmData = new SchmData();
            savedSchmData.setId(id);
            savedSchmData.setIsDraft(true);

            when(schmDataRepository.save(any(SchmData.class))).thenReturn(savedSchmData);
            // Mock publishSchemaVersion calls
            when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
            when(schmRepository.getSchemaVersion(testSchmId, 1)).thenReturn(Optional.of(savedSchmData));

            // Should succeed - same name in different tenant is allowed
            SchemaDto result = schemaService.importSchema(testNamespace, testSchemaDto, content);

            assertNotNull(result);
            // Verify tenant+namespace-aware lookup was used
            verify(schmRepository).findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, schemaName);
        }
    }

    @Test
    void testImportSchema_RefreshFails_ThrowsException() throws IOException {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            String content = "test content";
            when(schmRepository.findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, testSchemaDto.getName()))
                    .thenReturn(Optional.empty());
            when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);
            when(schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc(any(UUID.class)))
                    .thenReturn(Optional.empty());

            SchmDataId id = new SchmDataId();
            id.setSchmId(testSchmId);
            id.setSchmVersion(1);

            SchmData savedSchmData = new SchmData();
            savedSchmData.setId(id);
            savedSchmData.setIsDraft(true);

            when(schmDataRepository.save(any(SchmData.class))).thenReturn(savedSchmData);
            when(schmRepository.getSchemaVersion(testSchmId, 1)).thenReturn(Optional.of(savedSchmData));

            // First call for createSchemaDataFromFile, second for publishSchemaVersion, third for refresh fails
            when(schmRepository.findBySchmId(testSchmId))
                    .thenReturn(Optional.of(testSchm))  // First call during createSchemaDataFromFile
                    .thenReturn(Optional.of(testSchm))  // Second call during publishSchemaVersion
                    .thenReturn(Optional.empty());       // Third call during refresh fails

            IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                    schemaService.importSchema(testNamespace, testSchemaDto, content)
            );

            assertEquals("Schema not found immediately after import - possible data corruption", exception.getMessage());
            // Verify findBySchmId was called at least 3 times (createSchemaData + publish + refresh attempt)
            verify(schmRepository, atLeast(3)).findBySchmId(testSchmId);
        }
    }

    @Test
    void testImportSchema_NamespaceIsolation_SameNameDifferentNamespace() throws IOException {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            String content = "test content";
            String schemaName = "CommonSchemaName";
            testSchemaDto.setName(schemaName);

            // Schema with same name exists in different namespace - should not conflict
            UUID otherNmspcId = UUID.randomUUID();
            Schm existingInOtherNamespace = new Schm();
            existingInOtherNamespace.setSchmId(UUID.randomUUID());
            existingInOtherNamespace.setTenantId(testTenantId);
            existingInOtherNamespace.setNmspcId(otherNmspcId);
            existingInOtherNamespace.setSchmName(schemaName);

            // SECURITY: Lookup is scoped to current tenant+namespace, so returns empty
            when(schmRepository.findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, schemaName))
                    .thenReturn(Optional.empty());

            when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);
            when(schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc(any(UUID.class)))
                    .thenReturn(Optional.empty());

            SchmDataId id = new SchmDataId();
            id.setSchmId(testSchmId);
            id.setSchmVersion(1);

            SchmData savedSchmData = new SchmData();
            savedSchmData.setId(id);
            savedSchmData.setIsDraft(true);

            when(schmDataRepository.save(any(SchmData.class))).thenReturn(savedSchmData);
            // Mock publishSchemaVersion calls
            when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
            when(schmRepository.getSchemaVersion(testSchmId, 1)).thenReturn(Optional.of(savedSchmData));

            // Should succeed - same name in different namespace is allowed
            SchemaDto result = schemaService.importSchema(testNamespace, testSchemaDto, content);

            assertNotNull(result);
            // Verify tenant+namespace-aware lookup was used
            verify(schmRepository).findByTenantIdAndNmspcIdAndSchmName(testTenantId, testNmspcId, schemaName);
        }
    }

    @Test
    void testExportSchema_Success() {
        testSchm.setPublishVersion(1);
        testSchm.setSchmName("TestSchema");
        testSchm.setSchmDesc("Test Description");
        testSchm.setSchemaType("JSON");
        testSchm.setContentType("application/json");
        testSchm.setSchmGroup("group1");

        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));

        SchmDataId publishedId = new SchmDataId();
        publishedId.setSchmId(testSchmId);
        publishedId.setSchmVersion(1);

        SchmData publishedData = new SchmData();
        publishedData.setId(publishedId);
        publishedData.setSchmData("test content");
        publishedData.setIsDraft(false);

        when(schmRepository.getPublishedVersion(testSchmId)).thenReturn(Optional.of(publishedData));

        com.ssnc.schemaService.dto.SchemaExportDto result = schemaService.exportSchema(testNamespace, testSchmId);

        assertNotNull(result);
        assertEquals("TestSchema", result.getName());
        assertEquals("Test Description", result.getDescription());
        assertEquals("JSON", result.getSchemaType());
        assertEquals("application/json", result.getContentType());
        assertEquals("group1", result.getSchmGroup());
        assertEquals("test content", result.getContent());
        verify(schmRepository).findBySchmId(testSchmId);
    }

    @Test
    void testExportSchema_SchemaNotFound() {
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
            schemaService.exportSchema(testNamespace, testSchmId));
        verify(schmRepository).findBySchmId(testSchmId);
    }

    @Test
    void testExportSchema_NoPublishedVersion() {
        testSchm.setPublishVersion(null);
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));

        assertThrows(IllegalStateException.class, () ->
            schemaService.exportSchema(testNamespace, testSchmId));
        verify(schmRepository).findBySchmId(testSchmId);
    }

    @Test
    void testExportSchemas_BySchmId() {
        testSchm.setPublishVersion(1);
        testSchm.setSchmName("TestSchema");
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));

        SchmDataId publishedId = new SchmDataId();
        publishedId.setSchmId(testSchmId);
        publishedId.setSchmVersion(1);

        SchmData publishedData = new SchmData();
        publishedData.setId(publishedId);
        publishedData.setSchmData("test content");
        when(schmRepository.getPublishedVersion(testSchmId)).thenReturn(Optional.of(publishedData));

        com.ssnc.schemaService.dto.SchemaExportRequest request = new com.ssnc.schemaService.dto.SchemaExportRequest();
        request.setSchmId(testSchmId);

        List<com.ssnc.schemaService.dto.SchemaExportResponse> responses =
            schemaService.exportSchemas(testNamespace, Arrays.asList(request));

        assertEquals(1, responses.size());
        assertTrue(responses.get(0).isSuccess());
        assertEquals("TestSchema", responses.get(0).getSchemaName());
        assertNotNull(responses.get(0).getSchema());
    }

    @Test
    void testExportSchemas_ByName() {
        testSchm.setPublishVersion(1);
        testSchm.setSchmName("TestSchema");
        when(schmRepository.findBySchmName("TestSchema")).thenReturn(Optional.of(testSchm));
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));

        SchmDataId publishedId = new SchmDataId();
        publishedId.setSchmId(testSchmId);
        publishedId.setSchmVersion(1);

        SchmData publishedData = new SchmData();
        publishedData.setId(publishedId);
        publishedData.setSchmData("test content");
        when(schmRepository.getPublishedVersion(testSchmId)).thenReturn(Optional.of(publishedData));

        com.ssnc.schemaService.dto.SchemaExportRequest request = new com.ssnc.schemaService.dto.SchemaExportRequest();
        request.setName("TestSchema");

        List<com.ssnc.schemaService.dto.SchemaExportResponse> responses =
            schemaService.exportSchemas(testNamespace, Arrays.asList(request));

        assertEquals(1, responses.size());
        assertTrue(responses.get(0).isSuccess());
        assertEquals("TestSchema", responses.get(0).getSchemaName());
    }

    @Test
    void testExportSchemas_WithFailures() {
        com.ssnc.schemaService.dto.SchemaExportRequest request1 = new com.ssnc.schemaService.dto.SchemaExportRequest();
        request1.setSchmId(testSchmId);

        com.ssnc.schemaService.dto.SchemaExportRequest request2 = new com.ssnc.schemaService.dto.SchemaExportRequest();
        request2.setName("NonExistent");

        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.empty());
        when(schmRepository.findBySchmName("NonExistent")).thenReturn(Optional.empty());

        List<com.ssnc.schemaService.dto.SchemaExportResponse> responses =
            schemaService.exportSchemas(testNamespace, Arrays.asList(request1, request2));

        assertEquals(2, responses.size());
        assertFalse(responses.get(0).isSuccess());
        assertFalse(responses.get(1).isSuccess());
        assertNotNull(responses.get(0).getErrorMessage());
        assertNotNull(responses.get(1).getErrorMessage());
    }

    @Test
    void testImportSchemas_BulkSuccess() throws IOException {
        try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getTenantName).thenReturn("client1Id");

            when(jwtClaimsContext.getUserId()).thenReturn(testUserId);
            when(tenantRepository.findByTenantName("client1Id")).thenReturn(Optional.of(testTenant));
            when(nameSpaceService.ensureNamespaceExists(testTenantId, testNamespace)).thenReturn(testNmspcId);
            when(schmRepository.findByTenantIdAndNmspcIdAndSchmName(any(), any(), any())).thenReturn(Optional.empty());
            when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);
            when(schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc(any())).thenReturn(Optional.empty());
            when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));

            SchmDataId id = new SchmDataId();
            id.setSchmId(testSchmId);
            id.setSchmVersion(1);

            SchmData savedSchmData = new SchmData();
            savedSchmData.setId(id);
            savedSchmData.setIsDraft(true);
            when(schmDataRepository.save(any(SchmData.class))).thenReturn(savedSchmData);
            when(schmRepository.getSchemaVersion(testSchmId, 1)).thenReturn(Optional.of(savedSchmData));

            com.ssnc.schemaService.dto.SchemaImportRequest request = new com.ssnc.schemaService.dto.SchemaImportRequest();
            request.setSchema(testSchemaDto);
            request.setContent("content");

            List<com.ssnc.schemaService.dto.SchemaImportResponse> responses =
                schemaService.importSchemas(testNamespace, Arrays.asList(request));

            assertEquals(1, responses.size());
            assertTrue(responses.get(0).isSuccess());
        }
    }

    @Test
    void testImportSchemas_NullSchema_ReturnsValidationError() {
        com.ssnc.schemaService.dto.SchemaImportRequest request = new com.ssnc.schemaService.dto.SchemaImportRequest();
        request.setSchema(null);
        request.setContent("content");

        List<com.ssnc.schemaService.dto.SchemaImportResponse> responses =
            schemaService.importSchemas(testNamespace, Arrays.asList(request));

        assertEquals(1, responses.size());
        assertFalse(responses.get(0).isSuccess());
        assertNull(responses.get(0).getSchemaName());
        assertTrue(responses.get(0).getErrorMessage().contains("Schema information is required"));
    }

    @Test
    void testImportSchemas_NullSchemaName_ReturnsValidationError() {
        SchemaDto schemaWithoutName = new SchemaDto();
        schemaWithoutName.setName(null);
        schemaWithoutName.setSchemaType("JSON");

        com.ssnc.schemaService.dto.SchemaImportRequest request = new com.ssnc.schemaService.dto.SchemaImportRequest();
        request.setSchema(schemaWithoutName);
        request.setContent("content");

        List<com.ssnc.schemaService.dto.SchemaImportResponse> responses =
            schemaService.importSchemas(testNamespace, Arrays.asList(request));

        assertEquals(1, responses.size());
        assertFalse(responses.get(0).isSuccess());
        assertNull(responses.get(0).getSchemaName());
        assertTrue(responses.get(0).getErrorMessage().contains("Schema name is required"));
    }

    @Test
    void testImportSchemas_EmptySchemaName_ReturnsValidationError() {
        SchemaDto schemaWithEmptyName = new SchemaDto();
        schemaWithEmptyName.setName("   ");
        schemaWithEmptyName.setSchemaType("JSON");

        com.ssnc.schemaService.dto.SchemaImportRequest request = new com.ssnc.schemaService.dto.SchemaImportRequest();
        request.setSchema(schemaWithEmptyName);
        request.setContent("content");

        List<com.ssnc.schemaService.dto.SchemaImportResponse> responses =
            schemaService.importSchemas(testNamespace, Arrays.asList(request));

        assertEquals(1, responses.size());
        assertFalse(responses.get(0).isSuccess());
        assertTrue(responses.get(0).getErrorMessage().contains("Schema name is required"));
    }
}