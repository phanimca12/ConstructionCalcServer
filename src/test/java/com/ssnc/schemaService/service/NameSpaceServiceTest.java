package com.ssnc.schemaService.service;

import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.dto.NameSpaceDto;
import com.ssnc.schemaService.entity.Nmspc;
import com.ssnc.schemaService.repo.NameSpaceRepository;
import com.ssnc.shared.security.JwtClaimsContext;
import jakarta.persistence.EntityNotFoundException;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NameSpaceServiceTest {

    @Mock
    private NameSpaceRepository nameSpaceRepository;

    @Mock
    private JwtClaimsContext jwtClaimsContext;

    @InjectMocks
    private NameSpaceService nameSpaceService;

    private String testNamespaceName;
    private String testUserId;
    private Nmspc testNmspc;
    private NameSpaceDto testNameSpaceDto;

    @BeforeEach
    void setUp() {
        testNamespaceName = "testNamespace";
        testUserId = "testUser";

        testNmspc = new Nmspc();
        testNmspc.setNmspcName(testNamespaceName);
        testNmspc.setDescription("Test Namespace Description");
        testNmspc.setCreatedBy(testUserId);
        testNmspc.setUpdatedBy(testUserId);
        testNmspc.setCreatedDatetime(LocalDateTime.now());
        testNmspc.setUpdatedDatetime(LocalDateTime.now());

        testNameSpaceDto = new NameSpaceDto();
        testNameSpaceDto.setName(testNamespaceName);
        testNameSpaceDto.setDescription("Test Namespace Description");
        testNameSpaceDto.setCreatedByUser(testUserId);
        testNameSpaceDto.setModifiedByUser(testUserId);

        // Mock JwtClaimsContext to return test user
        when(jwtClaimsContext.getUserId()).thenReturn(testUserId);
    }

    @Test
    void testCreateNameSpace_SetsUserFromJwtContext() {
        when(nameSpaceRepository.save(any(Nmspc.class))).thenReturn(testNmspc);

        NameSpaceDto result = nameSpaceService.createNameSpace(testNameSpaceDto);

        assertNotNull(result);
        assertEquals(testNamespaceName, result.getName());
        assertEquals("Test Namespace Description", result.getDescription());
        verify(jwtClaimsContext, atLeastOnce()).getUserId();
        verify(nameSpaceRepository).save(argThat(nmspc ->
                testUserId.equals(nmspc.getCreatedBy()) &&
                testUserId.equals(nmspc.getUpdatedBy()) &&
                testNamespaceName.equals(nmspc.getNmspcName())
        ));
    }

    @Test
    void testCreateNameSpace_NullJwtContext_UsesSystemUser() {
        when(jwtClaimsContext.getUserId()).thenReturn(null);
        when(nameSpaceRepository.save(any(Nmspc.class))).thenReturn(testNmspc);

        NameSpaceDto result = nameSpaceService.createNameSpace(testNameSpaceDto);

        assertNotNull(result);
        verify(nameSpaceRepository).save(argThat(nmspc ->
                AppConstants.SYSTEM_USER.equals(nmspc.getCreatedBy()) &&
                AppConstants.SYSTEM_USER.equals(nmspc.getUpdatedBy())
        ));
    }

    @Test
    void testCreateNameSpace_WithNullDescription() {
        testNameSpaceDto.setDescription(null);
        testNmspc.setDescription(null);

        when(nameSpaceRepository.save(any(Nmspc.class))).thenReturn(testNmspc);

        NameSpaceDto result = nameSpaceService.createNameSpace(testNameSpaceDto);

        assertNotNull(result);
        assertNull(result.getDescription());
        verify(nameSpaceRepository).save(argThat(nmspc ->
                nmspc.getDescription() == null &&
                testUserId.equals(nmspc.getCreatedBy())
        ));
    }

    @Test
    void testCreateNameSpace_WithEmptyDescription() {
        testNameSpaceDto.setDescription("");
        testNmspc.setDescription("");

        when(nameSpaceRepository.save(any(Nmspc.class))).thenReturn(testNmspc);

        NameSpaceDto result = nameSpaceService.createNameSpace(testNameSpaceDto);

        assertNotNull(result);
        assertEquals("", result.getDescription());
        verify(nameSpaceRepository).save(any(Nmspc.class));
    }

    @Test
    void testGetNameSpaceByName_Success() {
        when(nameSpaceRepository.findBynmspcName(testNamespaceName)).thenReturn(Optional.of(testNmspc));

        NameSpaceDto result = nameSpaceService.getNameSpaceByName(testNamespaceName);

        assertNotNull(result);
        assertEquals(testNamespaceName, result.getName());
        assertEquals("Test Namespace Description", result.getDescription());
        assertEquals(testUserId, result.getCreatedByUser());
        assertEquals(testUserId, result.getModifiedByUser());
        verify(nameSpaceRepository).findBynmspcName(testNamespaceName);
    }

    @Test
    void testGetNameSpaceByName_NotFound_ThrowsException() {
        when(nameSpaceRepository.findBynmspcName(testNamespaceName)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                nameSpaceService.getNameSpaceByName(testNamespaceName)
        );

        verify(nameSpaceRepository).findBynmspcName(testNamespaceName);
    }

    @Test
    void testGetAllNameSpaces_Success() {
        Nmspc namespace2 = new Nmspc();
        namespace2.setNmspcName("namespace2");
        namespace2.setDescription("Second namespace");
        namespace2.setCreatedBy("user2");
        namespace2.setUpdatedBy("user2");

        List<Nmspc> namespaces = Arrays.asList(testNmspc, namespace2);
        when(nameSpaceRepository.findAll()).thenReturn(namespaces);

        List<NameSpaceDto> result = nameSpaceService.getAllNameSpaces();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testNamespaceName, result.get(0).getName());
        assertEquals("namespace2", result.get(1).getName());
        verify(nameSpaceRepository).findAll();
    }

    @Test
    void testGetAllNameSpaces_EmptyList() {
        when(nameSpaceRepository.findAll()).thenReturn(Arrays.asList());

        List<NameSpaceDto> result = nameSpaceService.getAllNameSpaces();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(nameSpaceRepository).findAll();
    }

    @Test
    void testMapToNameSpaceDto_AllFieldsPopulated() {
        Nmspc nmspc = new Nmspc();
        nmspc.setNmspcName("mappedNamespace");
        nmspc.setDescription("Mapped Description");
        nmspc.setCreatedBy("mappedUser");
        nmspc.setUpdatedBy("mappedUser");
        nmspc.setCreatedDatetime(LocalDateTime.now());
        nmspc.setUpdatedDatetime(LocalDateTime.now());

        when(nameSpaceRepository.findBynmspcName("mappedNamespace")).thenReturn(Optional.of(nmspc));

        NameSpaceDto result = nameSpaceService.getNameSpaceByName("mappedNamespace");

        assertNotNull(result);
        assertEquals("mappedNamespace", result.getId());
        assertEquals("mappedNamespace", result.getName());
        assertEquals("Mapped Description", result.getDescription());
        assertEquals("mappedUser", result.getCreatedByUser());
        assertEquals("mappedUser", result.getModifiedByUser());
        assertNotNull(result.getCreateDateTime());
        assertNotNull(result.getModifiedDateTime());
    }
}