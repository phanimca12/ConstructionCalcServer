package com.ssnc.schemaService.service;

import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.dto.SchemaVersionDto;
import com.ssnc.schemaService.entity.ExtRef;
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
import org.springframework.data.jpa.domain.Specification;
import com.ssnc.shared.security.JwtClaimsContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

    @InjectMocks
    private SchemaService schemaService;

    private String testNamespace;
    private String testUserId;
    private SchemaDto testSchemaDto;
    private Schm testSchm;
    private UUID testSchmId;

    @BeforeEach
    void setUp() {
        testNamespace = "testNamespace";
        testUserId = "testUser";
        testSchmId = UUID.randomUUID();

        testSchemaDto = new SchemaDto();
        testSchemaDto.setName("Test Schema");
        testSchemaDto.setDescription("Test Description");
        testSchemaDto.setSchemaType("FormData");
        testSchemaDto.setContentType("application/json");
        testSchemaDto.setGroup("testGroup");

        testSchm = new Schm();
        testSchm.setSchmId(testSchmId);
        testSchm.setSchmName("Test Schema");
        testSchm.setSchmDesc("Test Description");
        testSchm.setNamespace(testNamespace);
        testSchm.setCreatedBy(testUserId);
        testSchm.setUpdatedBy(testUserId);
        testSchm.setCreatedDatetime(LocalDateTime.now());
        testSchm.setUpdatedDatetime(LocalDateTime.now());

        // Mock JwtClaimsContext to return test user
        when(jwtClaimsContext.getUserId()).thenReturn(testUserId);
    }

    @Test
    void testCreateSchema_SetsUserFromJwtContext() throws IOException {
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

    @Test
    void testCreateSchema_WithContent_CreatesVersion() throws IOException {
        String content = "test content";
        when(schmRepository.findBySchmName(anyString())).thenReturn(Optional.empty());
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);
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

    @Test
    void testCreateSchema_DuplicateName_ThrowsException() {
        when(schmRepository.findBySchmName(anyString())).thenReturn(Optional.of(testSchm));

        assertThrows(IllegalArgumentException.class, () ->
                schemaService.createSchema(testNamespace, testSchemaDto, null)
        );

        verify(schmRepository, never()).save(any(Schm.class));
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
        when(schmDataRepository.findByIdSchmIdAndIsDraft(any(UUID.class), eq(true)))
                .thenReturn(Optional.empty());

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "none");

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
        when(schmDataRepository.findByIdSchmIdAndIsDraft(testSchmId, true))
                .thenReturn(Optional.empty());
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

        String content = "new draft content";
        SchemaVersionDto result = schemaService.updateDraftContent(testNamespace, testSchmId, content);

        assertNotNull(result);
        verify(schmDataRepository).save(argThat(schmData ->
                testUserId.equals(schmData.getCreatedBy()) &&
                testUserId.equals(schmData.getUpdatedBy()) &&
                content.equals(schmData.getSchmData())
        ));
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

        when(schmDataRepository.findByIdSchmIdAndIsDraft(testSchmId, true))
                .thenReturn(Optional.of(existingDraft));
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
        verify(schmRepository).save(argThat(schm -> schm.getPublishVersion().equals(1)));
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
        xref.setExtRefId(UUID.randomUUID());
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
        when(schmDataRepository.findByIdSchmIdAndIsDraft(any(UUID.class), eq(true)))
                .thenReturn(Optional.empty());

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, "nameAsc", "none");

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
        when(schmDataRepository.findByIdSchmIdAndIsDraft(any(UUID.class), eq(true)))
                .thenReturn(Optional.empty());

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, "nameAsc", "none");

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
        when(schmDataRepository.findByIdSchmIdAndIsDraft(any(UUID.class), eq(true)))
                .thenReturn(Optional.empty());

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, "nameDesc", "none");

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
        when(schmDataRepository.findByIdSchmIdAndIsDraft(any(UUID.class), eq(true)))
                .thenReturn(Optional.empty());

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, "versionUpdateDesc", "none");

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
        when(schmDataRepository.findByIdSchmIdAndIsDraft(any(UUID.class), eq(true)))
                .thenReturn(Optional.empty());

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, "versionUpdateAsc", "none");

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

        List<Schm> schemas = Arrays.asList(schm1, schm2, schm3);
        when(schmRepository.findAll(any(Specification.class))).thenReturn(schemas);

        // Mock draft versions - only schm1 and schm3 have drafts
        SchmData draft1 = createDraftVersion(schm1Id, 1);
        SchmData draft3 = createDraftVersion(schm3Id, 1);

        when(schmDataRepository.findByIdSchmIdAndIsDraft(schm1Id, true))
                .thenReturn(Optional.of(draft1));
        when(schmDataRepository.findByIdSchmIdAndIsDraft(schm2Id, true))
                .thenReturn(Optional.empty());
        when(schmDataRepository.findByIdSchmIdAndIsDraft(schm3Id, true))
                .thenReturn(Optional.of(draft3));

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "draft");

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

        when(schmDataRepository.findByIdSchmIdAndIsDraft(any(UUID.class), eq(true)))
                .thenReturn(Optional.empty());

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "published");

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

        List<Schm> schemas = Arrays.asList(schm1, schm2, schm3);
        when(schmRepository.findAll(any(Specification.class))).thenReturn(schemas);

        SchmData draft1 = createDraftVersion(schm1Id, 2);
        SchmData draft3 = createDraftVersion(schm3Id, 1);

        when(schmDataRepository.findByIdSchmIdAndIsDraft(schm1Id, true))
                .thenReturn(Optional.of(draft1));
        when(schmDataRepository.findByIdSchmIdAndIsDraft(schm2Id, true))
                .thenReturn(Optional.empty());
        when(schmDataRepository.findByIdSchmIdAndIsDraft(schm3Id, true))
                .thenReturn(Optional.of(draft3));

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "latest");

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

        when(schmDataRepository.findByIdSchmIdAndIsDraft(any(UUID.class), eq(true)))
                .thenReturn(Optional.empty());

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, null, "none");

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

        when(schmDataRepository.findByIdSchmIdAndIsDraft(any(UUID.class), eq(true)))
                .thenReturn(Optional.empty());

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, null, null, null, null);

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
        UUID extRefId1 = UUID.randomUUID();
        UUID extRefId2 = UUID.randomUUID();

        // Mock schema exists
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));

        // Mock cross-references
        SchmExtRefXref xref1 = new SchmExtRefXref();
        xref1.setSchmId(testSchmId);
        xref1.setExtRefId(extRefId1);

        SchmExtRefXref xref2 = new SchmExtRefXref();
        xref2.setSchmId(testSchmId);
        xref2.setExtRefId(extRefId2);

        List<SchmExtRefXref> xrefs = Arrays.asList(xref1, xref2);
        when(schmExtRefXrefRepository.findBySchmId(testSchmId)).thenReturn(xrefs);

        // Mock external references
        ExtRef extRef1 = new ExtRef();
        extRef1.setExtRefId(extRefId1);
        extRef1.setExtRefName("API Reference");
        extRef1.setExtRefType("API");
        extRef1.setExtRefVersion("1.0");
        extRef1.setCreatedBy(testUserId);
        extRef1.setUpdatedBy(testUserId);

        ExtRef extRef2 = new ExtRef();
        extRef2.setExtRefId(extRefId2);
        extRef2.setExtRefName("Database Reference");
        extRef2.setExtRefType("DATABASE");
        extRef2.setExtRefVersion("2.0");
        extRef2.setCreatedBy(testUserId);
        extRef2.setUpdatedBy(testUserId);

        // Mock batch fetch (fix N+1 query)
        when(extRefRepository.findAllById(Arrays.asList(extRefId1, extRefId2)))
                .thenReturn(Arrays.asList(extRef1, extRef2));

        // Execute
        List<ExtRefDto> result = schemaService.getExternalReferencesBySchemaId(testNamespace, testSchmId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("API Reference", result.get(0).getExtRefName());
        assertEquals("API", result.get(0).getExtRefType());
        assertEquals("1.0", result.get(0).getExtRefVersion());
        assertEquals("Database Reference", result.get(1).getExtRefName());
        assertEquals("DATABASE", result.get(1).getExtRefType());
        assertEquals("2.0", result.get(1).getExtRefVersion());

        verify(namespaceFilterManager).enableIfPresent(testNamespace);
        verify(schmRepository).findBySchmId(testSchmId);
        verify(schmExtRefXrefRepository).findBySchmId(testSchmId);
        verify(extRefRepository).findAllById(anyList());
        verify(extRefRepository, never()).findById(any());
    }

    @Test
    void testGetExternalReferencesBySchemaId_SchemaNotFound() {
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                schemaService.getExternalReferencesBySchemaId(testNamespace, testSchmId)
        );

        verify(namespaceFilterManager).enableIfPresent(testNamespace);
        verify(schmRepository).findBySchmId(testSchmId);
        verify(schmExtRefXrefRepository, never()).findBySchmId(any());
    }

    @Test
    void testGetExternalReferencesBySchemaId_NoExternalReferences() {
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmExtRefXrefRepository.findBySchmId(testSchmId)).thenReturn(Arrays.asList());
        when(extRefRepository.findAllById(anyList())).thenReturn(Arrays.asList());

        List<ExtRefDto> result = schemaService.getExternalReferencesBySchemaId(testNamespace, testSchmId);

        assertNotNull(result);
        assertEquals(0, result.size());
        verify(schmRepository).findBySchmId(testSchmId);
        verify(schmExtRefXrefRepository).findBySchmId(testSchmId);
    }

    @Test
    void testGetExternalReferencesBySchemaId_WithMissingExtRef() {
        UUID extRefId1 = UUID.randomUUID();
        UUID extRefId2 = UUID.randomUUID();

        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));

        SchmExtRefXref xref1 = new SchmExtRefXref();
        xref1.setSchmId(testSchmId);
        xref1.setExtRefId(extRefId1);

        SchmExtRefXref xref2 = new SchmExtRefXref();
        xref2.setSchmId(testSchmId);
        xref2.setExtRefId(extRefId2);

        List<SchmExtRefXref> xrefs = Arrays.asList(xref1, xref2);
        when(schmExtRefXrefRepository.findBySchmId(testSchmId)).thenReturn(xrefs);

        ExtRef extRef1 = new ExtRef();
        extRef1.setExtRefId(extRefId1);
        extRef1.setExtRefName("API Reference");
        extRef1.setExtRefType("API");
        extRef1.setExtRefVersion("1.0");

        // Mock batch fetch - only extRef1 exists, extRef2 is missing
        when(extRefRepository.findAllById(Arrays.asList(extRefId1, extRefId2)))
                .thenReturn(Arrays.asList(extRef1)); // Only returns extRef1

        List<ExtRefDto> result = schemaService.getExternalReferencesBySchemaId(testNamespace, testSchmId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("API Reference", result.get(0).getExtRefName());
        verify(extRefRepository).findAllById(anyList());
    }
}