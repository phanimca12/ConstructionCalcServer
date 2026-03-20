package com.ssnc.schemaService.service;

import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.dto.SchemaVersionDto;
import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmData;
import com.ssnc.schemaService.entity.SchmDataId;
import com.ssnc.schemaService.repo.SchmDataRepository;
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

        List<SchemaDto> result = schemaService.getSchemas(testNamespace, null, null, null, false);

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
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        schemaService.unPublishSchemaVersion(testNamespace, testSchmId);

        verify(schmRepository).save(argThat(schm -> schm.getPublishVersion() == null));
    }

    @Test
    void testUnPublishSchemaVersion_SchemaNotFound_ThrowsException() {
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                schemaService.unPublishSchemaVersion(testNamespace, testSchmId)
        );
    }

    @Test
    void testLockSchema_Success() {
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        schemaService.lockSchema(testNamespace, testSchmId);

        verify(schmRepository).save(argThat(schm ->
                testUserId.equals(schm.getLockBy())
        ));
        verify(jwtClaimsContext, atLeastOnce()).getUserId();
    }

    @Test
    void testLockSchema_SchemaNotFound_ThrowsException() {
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                schemaService.lockSchema(testNamespace, testSchmId)
        );
    }

    @Test
    void testLockSchema_WithNullJwtContext_UsesSYSTEMUser() {
        when(jwtClaimsContext.getUserId()).thenReturn(null);
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        schemaService.lockSchema(testNamespace, testSchmId);

        verify(schmRepository).save(argThat(schm ->
                AppConstants.SYSTEM_USER.equals(schm.getLockBy())
        ));
    }

    @Test
    void testUnlockSchema_Success() {
        testSchm.setLockBy(testUserId);
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.of(testSchm));
        when(schmRepository.save(any(Schm.class))).thenReturn(testSchm);

        schemaService.unlockSchema(testNamespace, testSchmId);

        verify(schmRepository).save(argThat(schm -> schm.getLockBy() == null));
    }

    @Test
    void testUnlockSchema_SchemaNotFound_ThrowsException() {
        when(schmRepository.findBySchmId(testSchmId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () ->
                schemaService.unlockSchema(testNamespace, testSchmId)
        );
    }
}