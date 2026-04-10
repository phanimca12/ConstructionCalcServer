package com.ssnc.schemaService.service;

import com.ssnc.schemaService.config.CacheConfigProperties;
import com.ssnc.schemaService.dto.TenantDto;
import com.ssnc.schemaService.entity.Tenant;
import com.ssnc.schemaService.repo.TenantRepository;
import com.ssnc.shared.security.JwtClaimsContext;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private JwtClaimsContext jwtClaimsContext;

    @Mock
    private CacheConfigProperties cacheConfigProperties;

    @Mock
    private CacheConfigProperties.TenantCacheConfig tenantCacheConfig;

    private TenantService tenantService;

    private String testTenantName;
    private String testUserId;
    private Tenant testTenant;

    @BeforeEach
    void setUp() {
        testTenantName = "testTenant";
        testUserId = "testUser";

        testTenant = new Tenant();
        testTenant.setTenantName(testTenantName);
        testTenant.setCreatedBy(testUserId);
        testTenant.setUpdatedBy(testUserId);
        testTenant.setCreatedDatetime(LocalDateTime.now());
        testTenant.setUpdatedDatetime(LocalDateTime.now());

        // Mock cache configuration
        when(tenantCacheConfig.getExpireAfterWriteMinutes()).thenReturn(60);
        when(tenantCacheConfig.getMaximumSize()).thenReturn(1000);
        when(cacheConfigProperties.getTenant()).thenReturn(tenantCacheConfig);

        // Instantiate service with mocked dependencies
        tenantService = new TenantService(cacheConfigProperties, tenantRepository, jwtClaimsContext);
    }

    @Test
    void testGetTenantByName_Success() {
        when(tenantRepository.findByTenantName(testTenantName)).thenReturn(Optional.of(testTenant));

        TenantDto result = tenantService.getTenantByName(testTenantName);

        assertNotNull(result);
        assertEquals(testTenantName, result.getName());
        assertEquals(testUserId, result.getCreatedByUser());
        assertEquals(testUserId, result.getModifiedByUser());
        verify(tenantRepository).findByTenantName(testTenantName);
    }

    @Test
    void testGetTenantByName_NotFound_ThrowsException() {
        when(tenantRepository.findByTenantName(testTenantName)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
                tenantService.getTenantByName(testTenantName)
        );

        verify(tenantRepository).findByTenantName(testTenantName);
    }

    @Test
    void testGetAllTenants_Success() {
        Tenant tenant2 = new Tenant();
        tenant2.setTenantName("tenant2");
        tenant2.setCreatedBy("user2");
        tenant2.setUpdatedBy("user2");

        List<Tenant> tenants = Arrays.asList(testTenant, tenant2);
        when(tenantRepository.findAll()).thenReturn(tenants);

        List<TenantDto> result = tenantService.getAllTenants();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testTenantName, result.get(0).getName());
        assertEquals("tenant2", result.get(1).getName());
        verify(tenantRepository).findAll();
    }

    @Test
    void testGetAllTenants_EmptyList() {
        when(tenantRepository.findAll()).thenReturn(Arrays.asList());

        List<TenantDto> result = tenantService.getAllTenants();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tenantRepository).findAll();
    }
}