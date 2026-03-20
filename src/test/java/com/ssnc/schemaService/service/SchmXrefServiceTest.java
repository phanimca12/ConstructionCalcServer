package com.ssnc.schemaService.service;

import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmXref;
import com.ssnc.schemaService.entity.XRefType;
import com.ssnc.schemaService.repo.SchmRepository;
import com.ssnc.schemaService.repo.SchmXrefRepository;
import com.ssnc.schemaService.tenant.NamespaceFilterManager;
import com.ssnc.shared.security.JwtClaimsContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchmXrefServiceTest {

    @Mock
    private SchmXrefRepository schmXrefRepository;

    @Mock
    private SchmRepository schmRepository;

    @Mock
    private NamespaceFilterManager namespaceFilterManager;

    @Mock
    private JwtClaimsContext jwtClaimsContext;

    @InjectMocks
    private SchmXrefService schmXrefService;

    private String testNamespace;
    private String testUserId;
    private SchmXref testXref;
    private Schm testSchm;
    private UUID testSchmId;
    private UUID testXrefId;

    @BeforeEach
    void setUp() {
        testNamespace = "testNamespace";
        testUserId = "testUser";
        testSchmId = UUID.randomUUID();
        testXrefId = UUID.randomUUID();

        testXref = new SchmXref();
        testXref.setXrefId(testXrefId);
        testXref.setSchmId(testSchmId);
        testXref.setSchmName("Test Schema");
        testXref.setSchmType("JSON");
        testXref.setNmspName(testNamespace);
        testXref.setRefType(XRefType.AutomationService);
        testXref.setRefName("TestAutomation");
        testXref.setRefVersion("1.0");
        testXref.setCreatedBy(testUserId);
        testXref.setUpdatedBy(testUserId);
        testXref.setCreatedDatetime(LocalDateTime.now());
        testXref.setUpdatedDatetime(LocalDateTime.now());

        testSchm = new Schm();
        testSchm.setSchmId(testSchmId);
        testSchm.setSchmName("Test Schema");
        testSchm.setSchmDesc("Test Description");
        testSchm.setSchemaType("JSON");
        testSchm.setContentType("application/json");
        testSchm.setCreatedBy(testUserId);
        testSchm.setUpdatedBy(testUserId);
        testSchm.setCreatedDatetime(LocalDateTime.now());
        testSchm.setUpdatedDatetime(LocalDateTime.now());

        when(jwtClaimsContext.getUserId()).thenReturn(testUserId);
    }

    @Test
    void testGetExtRefs_WithoutTypeFilter() {
        List<SchmXref> xrefs = Arrays.asList(testXref);
        when(schmXrefRepository.findByNmspName(testNamespace)).thenReturn(xrefs);

        List<ExtRefDto> result = schmXrefService.getExtRefs(testNamespace, null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testXref.getRefName(), result.get(0).getExtRefName());
        verify(namespaceFilterManager).enableIfPresent(testNamespace);
        verify(schmXrefRepository).findByNmspName(testNamespace);
    }

    @Test
    void testGetExtRefs_WithTypeFilter() {
        List<SchmXref> xrefs = Arrays.asList(testXref);
        when(schmXrefRepository.findByNmspNameAndRefType(testNamespace, XRefType.AutomationService)).thenReturn(xrefs);

        List<ExtRefDto> result = schmXrefService.getExtRefs(testNamespace, "Automation");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Automation", result.get(0).getExtRefType());
        verify(schmXrefRepository).findByNmspNameAndRefType(testNamespace, XRefType.AutomationService);
    }

    @Test
    void testGetExtRefs_EmptyResult() {
        when(schmXrefRepository.findByNmspName(testNamespace)).thenReturn(Collections.emptyList());

        List<ExtRefDto> result = schmXrefService.getExtRefs(testNamespace, null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSchemasForExtRef() {
        List<SchmXref> xrefs = Arrays.asList(testXref);
        when(schmXrefRepository.findByNmspNameAndRefTypeAndRefName(testNamespace, XRefType.AutomationService, "TestAutomation"))
                .thenReturn(xrefs);
        when(schmRepository.findById(testSchmId)).thenReturn(Optional.of(testSchm));

        List<SchemaDto> result = schmXrefService.getSchemasForExtRef(testNamespace, "Automation", "TestAutomation", null);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSchm.getSchmName(), result.get(0).getName());
        verify(namespaceFilterManager).enableIfPresent(testNamespace);
        verify(schmXrefRepository).findByNmspNameAndRefTypeAndRefName(testNamespace, XRefType.AutomationService, "TestAutomation");
        verify(schmRepository).findById(testSchmId);
    }

    @Test
    void testGetSchemasForExtRef_EmptyResult() {
        when(schmXrefRepository.findByNmspNameAndRefTypeAndRefName(testNamespace, XRefType.AutomationService, "TestAutomation"))
                .thenReturn(Collections.emptyList());

        List<SchemaDto> result = schmXrefService.getSchemasForExtRef(testNamespace, "Automation", "TestAutomation", null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSchemasForExtRef_SchemaNotFound() {
        List<SchmXref> xrefs = Arrays.asList(testXref);
        when(schmXrefRepository.findByNmspNameAndRefTypeAndRefName(testNamespace, XRefType.AutomationService, "TestAutomation"))
                .thenReturn(xrefs);
        when(schmRepository.findById(testSchmId)).thenReturn(Optional.empty());

        List<SchemaDto> result = schmXrefService.getSchemasForExtRef(testNamespace, "Automation", "TestAutomation", null);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSchemasForExtRef_WithVersion() {
        List<SchmXref> xrefs = Arrays.asList(testXref);
        when(schmXrefRepository.findByNmspNameAndRefTypeAndRefNameAndRefVersion(testNamespace, XRefType.AutomationService, "TestAutomation", "1.0"))
                .thenReturn(xrefs);
        when(schmRepository.findById(testSchmId)).thenReturn(Optional.of(testSchm));

        List<SchemaDto> result = schmXrefService.getSchemasForExtRef(testNamespace, "Automation", "TestAutomation", "1.0");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSchm.getSchmName(), result.get(0).getName());
        verify(schmXrefRepository).findByNmspNameAndRefTypeAndRefNameAndRefVersion(testNamespace, XRefType.AutomationService, "TestAutomation", "1.0");
    }

    @Test
    void testAssociateSchemasWithExtRef() {
        SchemaDto schemaDto = new SchemaDto();
        schemaDto.setId(testSchmId);
        schemaDto.setName("Test Schema");
        schemaDto.setSchemaType("JSON");
        List<SchemaDto> schemas = Arrays.asList(schemaDto);

        when(schmXrefRepository.findByNmspNameAndRefTypeAndRefName(testNamespace, XRefType.AutomationService, "TestAutomation"))
                .thenReturn(Collections.emptyList());
        when(schmXrefRepository.save(any(SchmXref.class))).thenReturn(testXref);

        List<SchemaDto> result = schmXrefService.associateSchemasWithExtRef(
                testNamespace, "Automation", "TestAutomation", "1.0", schemas);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(namespaceFilterManager).enableIfPresent(testNamespace);
        verify(schmXrefRepository).save(argThat(xref ->
                testSchmId.equals(xref.getSchmId()) &&
                "Test Schema".equals(xref.getSchmName()) &&
                XRefType.AutomationService.equals(xref.getRefType()) &&
                "TestAutomation".equals(xref.getRefName()) &&
                "1.0".equals(xref.getRefVersion()) &&
                testUserId.equals(xref.getCreatedBy()) &&
                testUserId.equals(xref.getUpdatedBy())
        ));
    }

    @Test
    void testAssociateSchemasWithExtRef_ReplacesExisting() {
        SchemaDto schemaDto = new SchemaDto();
        schemaDto.setId(testSchmId);
        schemaDto.setName("Test Schema");
        schemaDto.setSchemaType("JSON");
        List<SchemaDto> schemas = Arrays.asList(schemaDto);

        List<SchmXref> existingXrefs = Arrays.asList(testXref);
        when(schmXrefRepository.findByNmspNameAndRefTypeAndRefNameAndRefVersion(testNamespace, XRefType.AutomationService, "TestAutomation", "1.0"))
                .thenReturn(existingXrefs);
        when(schmXrefRepository.save(any(SchmXref.class))).thenReturn(testXref);

        List<SchemaDto> result = schmXrefService.associateSchemasWithExtRef(
                testNamespace, "Automation", "TestAutomation", "1.0", schemas);

        assertNotNull(result);
        verify(schmXrefRepository).delete(testXref);
        verify(schmXrefRepository).save(any(SchmXref.class));
    }

    @Test
    void testAssociateSchemasWithExtRef_WithNullJwtContext_UsesSYSTEMUser() {
        when(jwtClaimsContext.getUserId()).thenReturn(null);

        SchemaDto schemaDto = new SchemaDto();
        schemaDto.setId(testSchmId);
        schemaDto.setName("Test Schema");
        schemaDto.setSchemaType("JSON");
        List<SchemaDto> schemas = Arrays.asList(schemaDto);

        when(schmXrefRepository.findByNmspNameAndRefTypeAndRefName(testNamespace, XRefType.AutomationService, "TestAutomation"))
                .thenReturn(Collections.emptyList());
        when(schmXrefRepository.save(any(SchmXref.class))).thenReturn(testXref);

        List<SchemaDto> result = schmXrefService.associateSchemasWithExtRef(
                testNamespace, "Automation", "TestAutomation", null, schemas);

        assertNotNull(result);
        verify(schmXrefRepository).save(argThat(xref ->
                "system".equals(xref.getCreatedBy()) &&
                "system".equals(xref.getUpdatedBy())
        ));
    }

    @Test
    void testGetExtRefsForSchema() {
        List<SchmXref> xrefs = Arrays.asList(testXref);
        when(schmXrefRepository.findBySchmId(testSchmId)).thenReturn(xrefs);

        List<ExtRefDto> result = schmXrefService.getExtRefsForSchema(testNamespace, testSchmId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testXref.getRefName(), result.get(0).getExtRefName());
        verify(namespaceFilterManager).enableIfPresent(testNamespace);
        verify(schmXrefRepository).findBySchmId(testSchmId);
    }

    @Test
    void testGetExtRefsForSchema_EmptyResult() {
        when(schmXrefRepository.findBySchmId(testSchmId)).thenReturn(Collections.emptyList());

        List<ExtRefDto> result = schmXrefService.getExtRefsForSchema(testNamespace, testSchmId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
