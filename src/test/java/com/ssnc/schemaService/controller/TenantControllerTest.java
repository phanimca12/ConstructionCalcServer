package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.dto.TenantDto;
import com.ssnc.schemaService.service.TenantService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TenantControllerTest {

    @Mock
    private TenantService tenantService;

    @InjectMocks
    private TenantController tenantController;

    private TenantDto testTenantDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testTenantDto = new TenantDto();
        testTenantDto.setName("testTenant");
        testTenantDto.setCreatedByUser("testUser");
        testTenantDto.setCreateDateTime(LocalDateTime.now());
        testTenantDto.setModifiedByUser("testUser");
        testTenantDto.setModifiedDateTime(LocalDateTime.now());
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
