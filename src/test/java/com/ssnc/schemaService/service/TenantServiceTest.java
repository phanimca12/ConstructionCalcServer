package com.ssnc.schemaService.service;

import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.dto.TenantDto;
import com.ssnc.schemaService.entity.Tenant;
import com.ssnc.schemaService.repo.TenantRepository;
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
class TenantServiceTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private JwtClaimsContext jwtClaimsContext;

    @InjectMocks
    private TenantService tenantService;

    private String testTenantName;
    private String testUserId;
    private Tenant testTenant;
    private TenantDto testTenantDto;

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

        testTenantDto = new TenantDto();
        testTenantDto.setName(testTenantName);
        testTenantDto.setCreatedByUser(testUserId);
        testTenantDto.setModifiedByUser(testUserId);

        // Mock JwtClaimsContext to return test user
        when(jwtClaimsContext.getUserId()).thenReturn(testUserId);
        when(jwtClaimsContext.getTenant()).thenReturn(testTenantName);
        when(jwtClaimsContext.isPopulated()).thenReturn(true);
    }

    @Test
    void testCreateTenantsFromContext_Success() throws Exception {
        when(tenantRepository.findByTenantName(testTenantName)).thenReturn(Optional.empty());
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        List<Tenant> result = tenantService.createTenantsFromContext();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTenantName, result.get(0).getTenantName());
        verify(jwtClaimsContext, atLeastOnce()).getUserId();
        verify(tenantRepository).save(argThat(tenant ->
                testUserId.equals(tenant.getCreatedBy()) &&
                testUserId.equals(tenant.getUpdatedBy()) &&
                testTenantName.equals(tenant.getTenantName())
        ));
    }

    @Test
    void testCreateTenantsFromContext_TenantAlreadyExists() throws Exception {
        when(tenantRepository.findByTenantName(testTenantName)).thenReturn(Optional.of(testTenant));

        List<Tenant> result = tenantService.createTenantsFromContext();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void testCreateTenantsFromContext_NullTenantName() throws Exception {
        when(jwtClaimsContext.getTenant()).thenReturn(null);

        List<Tenant> result = tenantService.createTenantsFromContext();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void testCreateTenantsFromContext_EmptyTenantName() throws Exception {
        when(jwtClaimsContext.getTenant()).thenReturn("");

        List<Tenant> result = tenantService.createTenantsFromContext();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void testCreateTenantsFromContext_NullUserId_UsesSystem() throws Exception {
        when(jwtClaimsContext.getUserId()).thenReturn(null);
        when(tenantRepository.findByTenantName(testTenantName)).thenReturn(Optional.empty());
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        List<Tenant> result = tenantService.createTenantsFromContext();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(tenantRepository).save(argThat(tenant ->
                AppConstants.SYSTEM_USER.equals(tenant.getCreatedBy()) &&
                AppConstants.SYSTEM_USER.equals(tenant.getUpdatedBy())
        ));
    }

    @Test
    void testCreateTenantsFromContext_JwtContextNotPopulated_ThrowsException() {
        when(jwtClaimsContext.isPopulated()).thenReturn(false);

        assertThrows(IllegalStateException.class, () ->
                tenantService.createTenantsFromContext()
        );

        verify(tenantRepository, never()).save(any(Tenant.class));
    }

    @Test
    void testCreateTenant_SetsUserFromJwtContext() {
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        TenantDto result = tenantService.createTenant(testTenantDto);

        assertNotNull(result);
        assertEquals(testTenantName, result.getName());
        verify(jwtClaimsContext, atLeastOnce()).getUserId();
        verify(tenantRepository).save(argThat(tenant ->
                testUserId.equals(tenant.getCreatedBy()) &&
                testUserId.equals(tenant.getUpdatedBy())
        ));
    }

    @Test
    void testCreateTenant_NullJwtContext_UsesSystemUser() {
        when(jwtClaimsContext.getUserId()).thenReturn(null);
        when(tenantRepository.save(any(Tenant.class))).thenReturn(testTenant);

        TenantDto result = tenantService.createTenant(testTenantDto);

        assertNotNull(result);
        verify(tenantRepository).save(argThat(tenant ->
                AppConstants.SYSTEM_USER.equals(tenant.getCreatedBy()) &&
                AppConstants.SYSTEM_USER.equals(tenant.getUpdatedBy())
        ));
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