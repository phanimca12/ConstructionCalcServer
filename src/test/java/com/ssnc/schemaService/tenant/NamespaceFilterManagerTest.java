package com.ssnc.schemaService.tenant;

import com.ssnc.schemaService.entity.Tenant;
import com.ssnc.schemaService.repo.TenantRepository;
import com.ssnc.schemaService.service.NameSpaceService;
import jakarta.persistence.EntityManager;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NamespaceFilterManagerTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Session session;

    @Mock
    private Filter filter;

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private NameSpaceService nameSpaceService;

    private NamespaceFilterManager namespaceFilterManager;

    private String testTenantName;
    private String testNamespace;
    private UUID testTenantId;
    private UUID testNmspcId;
    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        testTenantName = "testTenant";
        testNamespace = "testNamespace";
        testTenantId = UUID.randomUUID();
        testNmspcId = UUID.randomUUID();

        testTenant = new Tenant();
        testTenant.setTenantId(testTenantId);
        testTenant.setTenantName(testTenantName);

        TenantContext.setTenantName(testTenantName);

        // Create instance and manually inject dependencies
        namespaceFilterManager = new NamespaceFilterManager(tenantRepository, nameSpaceService);
        ReflectionTestUtils.setField(namespaceFilterManager, "entityManager", entityManager);

        // Mock Hibernate Session and Filter
        when(entityManager.unwrap(Session.class)).thenReturn(session);
        when(session.enableFilter("namespaceFilter")).thenReturn(filter);
        when(filter.setParameter(anyString(), any())).thenReturn(filter);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testEnableIfPresent_NamespaceExists_EnablesFilter() {
        // Arrange
        when(tenantRepository.findByTenantName(testTenantName)).thenReturn(Optional.of(testTenant));
        when(nameSpaceService.ensureNamespaceExists(testTenantId, testNamespace)).thenReturn(testNmspcId);

        // Act
        namespaceFilterManager.enableIfPresent(testNamespace);

        // Assert
        verify(tenantRepository).findByTenantName(testTenantName);
        verify(nameSpaceService).ensureNamespaceExists(testTenantId, testNamespace);
        verify(filter).setParameter("namespaceId", testNmspcId);
    }

    @Test
    void testEnableIfPresent_NamespaceDoesNotExist_CreatesNewNamespace() {
        // Arrange
        when(tenantRepository.findByTenantName(testTenantName)).thenReturn(Optional.of(testTenant));
        when(nameSpaceService.ensureNamespaceExists(testTenantId, testNamespace)).thenReturn(testNmspcId);

        // Act
        namespaceFilterManager.enableIfPresent(testNamespace);

        // Assert
        verify(nameSpaceService).ensureNamespaceExists(testTenantId, testNamespace);
        verify(filter).setParameter("namespaceId", testNmspcId);
    }

    @Test
    void testEnableIfPresent_CacheHit_NoDuplicateServiceCall() {
        // Arrange
        when(tenantRepository.findByTenantName(testTenantName)).thenReturn(Optional.of(testTenant));
        when(nameSpaceService.ensureNamespaceExists(testTenantId, testNamespace)).thenReturn(testNmspcId);

        // Act - First call to populate cache
        namespaceFilterManager.enableIfPresent(testNamespace);

        // Act - Second call should use cache
        namespaceFilterManager.enableIfPresent(testNamespace);

        // Assert - Service called only once (cache hit on second call)
        verify(nameSpaceService, times(1)).ensureNamespaceExists(testTenantId, testNamespace);
        verify(filter, times(2)).setParameter("namespaceId", testNmspcId);
    }


    @Test
    void testEnableIfPresent_NullNamespace_DoesNothing() {
        // Act
        namespaceFilterManager.enableIfPresent(null);

        // Assert
        verify(tenantRepository, never()).findByTenantName(any());
        verify(nameSpaceService, never()).ensureNamespaceExists(any(), any());
        verify(entityManager, never()).unwrap(any());
    }

    @Test
    void testEnableIfPresent_TenantNotFound_ThrowsException() {
        // Arrange
        when(tenantRepository.findByTenantName(testTenantName)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                namespaceFilterManager.enableIfPresent(testNamespace)
        );

        assertTrue(exception.getMessage().contains("Tenant not found"));
        verify(nameSpaceService, never()).ensureNamespaceExists(any(), any());
    }

    @Test
    void testEnableIfPresent_ServiceThrowsException_PropagatesException() {
        // Arrange
        when(tenantRepository.findByTenantName(testTenantName)).thenReturn(Optional.of(testTenant));
        when(nameSpaceService.ensureNamespaceExists(testTenantId, testNamespace))
                .thenThrow(new IllegalStateException("Namespace operation failed"));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                namespaceFilterManager.enableIfPresent(testNamespace)
        );

        assertTrue(exception.getMessage().contains("Namespace operation failed"));
    }

    @Test
    void testEnableIfPresent_DifferentTenants_SameNamespace_UsesSeparateCache() {
        // Arrange
        String tenant2Name = "tenant2";
        UUID tenant2Id = UUID.randomUUID();
        UUID nmspc2Id = UUID.randomUUID();

        Tenant tenant2 = new Tenant();
        tenant2.setTenantId(tenant2Id);
        tenant2.setTenantName(tenant2Name);

        // First tenant
        when(tenantRepository.findByTenantName(testTenantName)).thenReturn(Optional.of(testTenant));
        when(nameSpaceService.ensureNamespaceExists(testTenantId, testNamespace)).thenReturn(testNmspcId);

        // Act - First tenant
        namespaceFilterManager.enableIfPresent(testNamespace);

        // Setup for second tenant
        TenantContext.setTenantName(tenant2Name);
        when(tenantRepository.findByTenantName(tenant2Name)).thenReturn(Optional.of(tenant2));
        when(nameSpaceService.ensureNamespaceExists(tenant2Id, testNamespace)).thenReturn(nmspc2Id);

        // Act - Second tenant
        namespaceFilterManager.enableIfPresent(testNamespace);

        // Assert - service called for both tenants (separate cache keys)
        verify(nameSpaceService).ensureNamespaceExists(testTenantId, testNamespace);
        verify(nameSpaceService).ensureNamespaceExists(tenant2Id, testNamespace);
        verify(filter, times(2)).setParameter(anyString(), any());
    }
}
