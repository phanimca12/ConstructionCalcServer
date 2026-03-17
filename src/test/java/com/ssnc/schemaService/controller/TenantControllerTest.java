package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.dto.TenantDto;
import com.ssnc.schemaService.entity.Tenant;
import com.ssnc.schemaService.service.TenantService;
import com.ssnc.shared.security.JwtClaimsContext;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TenantControllerTest {

    @Mock
    private TenantService tenantService;

    @Mock
    private JwtClaimsContext jwtClaimsContext;

    private TenantController tenantController;

    private TenantDto testTenantDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tenantController = new TenantController(tenantService, jwtClaimsContext);

        testTenantDto = new TenantDto();
        testTenantDto.setName("testTenant");
        testTenantDto.setCreatedByUser("testUser");
        testTenantDto.setCreateDateTime(LocalDateTime.now());
        testTenantDto.setModifiedByUser("testUser");
        testTenantDto.setModifiedDateTime(LocalDateTime.now());
    }

    @Test
    void testCreateTenant_Success() throws Exception {
        List<String> clients = Arrays.asList("client1", "client2");
        when(jwtClaimsContext.getClients()).thenReturn(clients);
        when(tenantService.createTenantsFromContext()).thenReturn(new ArrayList<>());

        ResponseEntity<?> result = tenantController.createTenant();

        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(tenantService, times(1)).createTenantsFromContext();
    }

    @Test
    void testCreateTenant_WithException() throws Exception {
        when(jwtClaimsContext.getClients()).thenReturn(Arrays.asList("client1"));
        when(tenantService.createTenantsFromContext())
                .thenThrow(new RuntimeException("Database error"));

        ResponseEntity<?> result = tenantController.createTenant();

        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
        assertEquals("Database error", result.getBody());
        verify(tenantService, times(1)).createTenantsFromContext();
    }

    @Test
    void testGetTenantByName_Success() {
        when(tenantService.getTenantByName("testTenant"))
                .thenReturn(testTenantDto);

        TenantDto result = tenantController.getTenantByName("testTenant");

        assertNotNull(result);
        assertEquals("testTenant", result.getName());
        assertEquals("testUser", result.getCreatedByUser());
        verify(tenantService).getTenantByName("testTenant");
    }

    @Test
    void testGetTenantByName_NotFound() {
        when(tenantService.getTenantByName("nonExistent"))
                .thenThrow(new EntityNotFoundException("Tenant not found"));

        assertThrows(EntityNotFoundException.class, () -> {
            tenantController.getTenantByName("nonExistent");
        });

        verify(tenantService).getTenantByName("nonExistent");
    }

    @Test
    void testGetTenantByName_WithSpecialCharacters() {
        String tenantName = "tenant-with-dashes_and_underscores";
        TenantDto specialDto = new TenantDto();
        specialDto.setName(tenantName);

        when(tenantService.getTenantByName(tenantName))
                .thenReturn(specialDto);

        TenantDto result = tenantController.getTenantByName(tenantName);

        assertNotNull(result);
        assertEquals(tenantName, result.getName());
        verify(tenantService).getTenantByName(tenantName);
    }

    @Test
    void testGetAllTenants_Success() {
        TenantDto tenant2 = new TenantDto();
        tenant2.setName("tenant2");
        tenant2.setCreatedByUser("user2");

        List<TenantDto> tenantList = Arrays.asList(testTenantDto, tenant2);

        when(tenantService.getAllTenants()).thenReturn(tenantList);

        List<TenantDto> result = tenantController.getAllTenants();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("testTenant", result.get(0).getName());
        assertEquals("tenant2", result.get(1).getName());
        verify(tenantService).getAllTenants();
    }

    @Test
    void testGetAllTenants_EmptyList() {
        when(tenantService.getAllTenants()).thenReturn(Collections.emptyList());

        List<TenantDto> result = tenantController.getAllTenants();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(tenantService).getAllTenants();
    }

    @Test
    void testGetAllTenants_SingleItem() {
        when(tenantService.getAllTenants())
                .thenReturn(Collections.singletonList(testTenantDto));

        List<TenantDto> result = tenantController.getAllTenants();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testTenant", result.get(0).getName());
        verify(tenantService).getAllTenants();
    }

    @Test
    void testGetAllTenants_MultipleItems() {
        TenantDto tenant2 = new TenantDto();
        tenant2.setName("tenant2");
        TenantDto tenant3 = new TenantDto();
        tenant3.setName("tenant3");
        TenantDto tenant4 = new TenantDto();
        tenant4.setName("tenant4");

        List<TenantDto> tenantList = Arrays.asList(testTenantDto, tenant2, tenant3, tenant4);

        when(tenantService.getAllTenants()).thenReturn(tenantList);

        List<TenantDto> result = tenantController.getAllTenants();

        assertNotNull(result);
        assertEquals(4, result.size());
        verify(tenantService).getAllTenants();
    }
}
