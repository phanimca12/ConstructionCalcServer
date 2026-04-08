package com.ssnc.schemaService.service;

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
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private UUID testNmspcId;
    private UUID testTenantId;
    private Nmspc testNmspc;

    @BeforeEach
    void setUp() {
        testNamespaceName = "testNamespace";
        testUserId = "testUser";
        testNmspcId = UUID.randomUUID();
        testTenantId = UUID.randomUUID();

        testNmspc = new Nmspc();
        testNmspc.setNmspcId(testNmspcId);
        testNmspc.setTenantId(testTenantId);
        testNmspc.setNmspcName(testNamespaceName);
        testNmspc.setDescription("Test Namespace Description");
        testNmspc.setCreatedBy(testUserId);
        testNmspc.setUpdatedBy(testUserId);
        testNmspc.setCreatedDatetime(LocalDateTime.now());
        testNmspc.setUpdatedDatetime(LocalDateTime.now());
    }

    @Test
    void testGetNameSpaceByName_Success() {
        when(nameSpaceRepository.findByNmspcName(testNamespaceName)).thenReturn(Optional.of(testNmspc));

        NameSpaceDto result = nameSpaceService.getNameSpaceByName(testNamespaceName);

        assertNotNull(result);
        assertEquals(testNamespaceName, result.getName());
        assertEquals("Test Namespace Description", result.getDescription());
        assertEquals(testUserId, result.getCreatedByUser());
        assertEquals(testUserId, result.getModifiedByUser());
        verify(nameSpaceRepository).findByNmspcName(testNamespaceName);
    }

    @Test
    void testGetNameSpaceByName_NotFound_ThrowsException() {
        when(nameSpaceRepository.findByNmspcName(testNamespaceName)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                nameSpaceService.getNameSpaceByName(testNamespaceName)
        );

        verify(nameSpaceRepository).findByNmspcName(testNamespaceName);
    }

    @Test
    void testGetAllNameSpaces_Success() {
        Nmspc namespace2 = new Nmspc();
        namespace2.setNmspcId(UUID.randomUUID());
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
        UUID mappedNmspcId = UUID.randomUUID();
        Nmspc nmspc = new Nmspc();
        nmspc.setNmspcId(mappedNmspcId);
        nmspc.setNmspcName("mappedNamespace");
        nmspc.setDescription("Mapped Description");
        nmspc.setCreatedBy("mappedUser");
        nmspc.setUpdatedBy("mappedUser");
        nmspc.setCreatedDatetime(LocalDateTime.now());
        nmspc.setUpdatedDatetime(LocalDateTime.now());

        when(nameSpaceRepository.findByNmspcName("mappedNamespace")).thenReturn(Optional.of(nmspc));

        NameSpaceDto result = nameSpaceService.getNameSpaceByName("mappedNamespace");

        assertNotNull(result);
        assertEquals(mappedNmspcId, result.getNmspcId());
        assertEquals("mappedNamespace", result.getName());
        assertEquals("Mapped Description", result.getDescription());
        assertEquals("mappedUser", result.getCreatedByUser());
        assertEquals("mappedUser", result.getModifiedByUser());
        assertNotNull(result.getCreateDateTime());
        assertNotNull(result.getModifiedDateTime());
    }

    @Test
    void testEnsureNamespaceExists_NamespaceExists_ReturnsId() {
        // Arrange
        when(nameSpaceRepository.findByTenantIdAndNmspcName(testTenantId, testNamespaceName))
                .thenReturn(Optional.of(testNmspc));

        // Act
        UUID result = nameSpaceService.ensureNamespaceExists(testTenantId, testNamespaceName);

        // Assert
        assertEquals(testNmspcId, result);
        verify(nameSpaceRepository).findByTenantIdAndNmspcName(testTenantId, testNamespaceName);
        verify(nameSpaceRepository, never()).save(any());
    }

    @Test
    void testEnsureNamespaceExists_NamespaceDoesNotExist_CreatesNew() {
        // Arrange
        when(jwtClaimsContext.getUserId()).thenReturn(testUserId);
        when(nameSpaceRepository.findByTenantIdAndNmspcName(testTenantId, testNamespaceName))
                .thenReturn(Optional.empty());
        when(nameSpaceRepository.save(any(Nmspc.class))).thenReturn(testNmspc);

        // Act
        UUID result = nameSpaceService.ensureNamespaceExists(testTenantId, testNamespaceName);

        // Assert
        assertEquals(testNmspcId, result);
        verify(nameSpaceRepository).findByTenantIdAndNmspcName(testTenantId, testNamespaceName);
        verify(nameSpaceRepository).save(any(Nmspc.class));
    }

    @Test
    void testEnsureNamespaceExists_CacheHit_NoDatabaseQuery() {
        // Arrange
        when(nameSpaceRepository.findByTenantIdAndNmspcName(testTenantId, testNamespaceName))
                .thenReturn(Optional.of(testNmspc));

        // Act - First call populates cache
        UUID result1 = nameSpaceService.ensureNamespaceExists(testTenantId, testNamespaceName);

        // Act - Second call should hit cache
        UUID result2 = nameSpaceService.ensureNamespaceExists(testTenantId, testNamespaceName);

        // Assert - Same ID returned
        assertEquals(testNmspcId, result1);
        assertEquals(testNmspcId, result2);

        // Repository called only once (cache hit on second call)
        verify(nameSpaceRepository, times(1)).findByTenantIdAndNmspcName(testTenantId, testNamespaceName);
    }

    @Test
    void testEnsureNamespaceExists_DifferentTenants_SeparateCache() {
        // Arrange
        UUID tenant2Id = UUID.randomUUID();
        UUID nmspc2Id = UUID.randomUUID();
        Nmspc nmspc2 = new Nmspc();
        nmspc2.setNmspcId(nmspc2Id);
        nmspc2.setTenantId(tenant2Id);
        nmspc2.setNmspcName(testNamespaceName);

        when(nameSpaceRepository.findByTenantIdAndNmspcName(testTenantId, testNamespaceName))
                .thenReturn(Optional.of(testNmspc));
        when(nameSpaceRepository.findByTenantIdAndNmspcName(tenant2Id, testNamespaceName))
                .thenReturn(Optional.of(nmspc2));

        // Act
        UUID result1 = nameSpaceService.ensureNamespaceExists(testTenantId, testNamespaceName);
        UUID result2 = nameSpaceService.ensureNamespaceExists(tenant2Id, testNamespaceName);

        // Assert - Different IDs for different tenants
        assertEquals(testNmspcId, result1);
        assertEquals(nmspc2Id, result2);

        // Both queries executed (separate cache keys)
        verify(nameSpaceRepository).findByTenantIdAndNmspcName(testTenantId, testNamespaceName);
        verify(nameSpaceRepository).findByTenantIdAndNmspcName(tenant2Id, testNamespaceName);
    }

    @Test
    void testEnsureNamespaceExists_ConcurrentCreation_HandlesRaceCondition() {
        // Arrange
        when(nameSpaceRepository.findByTenantIdAndNmspcName(testTenantId, testNamespaceName))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(testNmspc)); // Second call after race condition

        when(jwtClaimsContext.getUserId()).thenReturn(testUserId);
        when(nameSpaceRepository.save(any(Nmspc.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate key"));

        // Act
        UUID result = nameSpaceService.ensureNamespaceExists(testTenantId, testNamespaceName);

        // Assert
        assertEquals(testNmspcId, result);
        verify(nameSpaceRepository, times(2)).findByTenantIdAndNmspcName(testTenantId, testNamespaceName);
    }
}