package com.ssnc.schemaService.filter;

import com.ssnc.schemaService.service.TenantService;
import com.ssnc.schemaService.tenant.TenantContext;
import com.ssnc.shared.security.JwtClaimsContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JwtTenantFilterTest {

    @Mock
    private JwtClaimsContext jwtClaimsContext;

    @Mock
    private TenantService tenantService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtTenantFilter jwtTenantFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private String testTenantName;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        testTenantName = "testTenant";
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testDoFilterInternal_TenantExists_Success() throws ServletException, IOException {
        // Arrange
        when(jwtClaimsContext.getTenant()).thenReturn(testTenantName);
        doNothing().when(tenantService).ensureTenantExists(testTenantName);

        // Act
        jwtTenantFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtClaimsContext).getTenant();
        verify(tenantService).ensureTenantExists(testTenantName);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_TenantServiceCreatesNewTenant() throws ServletException, IOException {
        // Arrange
        when(jwtClaimsContext.getTenant()).thenReturn(testTenantName);
        doNothing().when(tenantService).ensureTenantExists(testTenantName);

        // Act
        jwtTenantFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(jwtClaimsContext).getTenant();
        verify(tenantService).ensureTenantExists(testTenantName);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void testDoFilterInternal_NullJwtClaimsContext_ThrowsException() throws ServletException, IOException {
        // Arrange
        jwtTenantFilter.jwtClaimsContext = null;

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                jwtTenantFilter.doFilterInternal(request, response, filterChain)
        );

        assertEquals("Tenant name is not available in JWT context", exception.getMessage());
        verify(filterChain, never()).doFilter(any(), any());
        verify(tenantService, never()).ensureTenantExists(any());
    }

    @Test
    void testDoFilterInternal_NullTenantName_ThrowsException() throws ServletException, IOException {
        // Arrange
        when(jwtClaimsContext.getTenant()).thenReturn(null);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                jwtTenantFilter.doFilterInternal(request, response, filterChain)
        );

        assertEquals("Tenant name is not available in JWT context", exception.getMessage());
        verify(filterChain, never()).doFilter(any(), any());
        verify(tenantService, never()).ensureTenantExists(any());
    }

    @Test
    void testDoFilterInternal_EmptyTenantName_ThrowsException() throws ServletException, IOException {
        // Arrange
        when(jwtClaimsContext.getTenant()).thenReturn("");

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                jwtTenantFilter.doFilterInternal(request, response, filterChain)
        );

        assertEquals("Tenant name is not available in JWT context", exception.getMessage());
        verify(filterChain, never()).doFilter(any(), any());
        verify(tenantService, never()).ensureTenantExists(any());
    }

    @Test
    void testDoFilterInternal_TenantContextClearedAfterSuccess() throws ServletException, IOException {
        // Arrange
        when(jwtClaimsContext.getTenant()).thenReturn(testTenantName);
        doNothing().when(tenantService).ensureTenantExists(testTenantName);

        // Act
        jwtTenantFilter.doFilterInternal(request, response, filterChain);

        // Assert - TenantContext should be cleared after filter execution
        assertNull(TenantContext.getTenantName());
    }

    @Test
    void testDoFilterInternal_TenantContextClearedAfterException() throws ServletException, IOException {
        // Arrange
        when(jwtClaimsContext.getTenant()).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                jwtTenantFilter.doFilterInternal(request, response, filterChain)
        );

        // TenantContext should be cleared even after exception
        assertNull(TenantContext.getTenantName());
    }

    @Test
    void testDoFilterInternal_ExceptionDuringFilterChain_TenantContextStillCleared() throws ServletException, IOException {
        // Arrange
        when(jwtClaimsContext.getTenant()).thenReturn(testTenantName);
        doNothing().when(tenantService).ensureTenantExists(testTenantName);
        doThrow(new ServletException("Test exception")).when(filterChain).doFilter(request, response);

        // Act & Assert
        assertThrows(ServletException.class, () ->
                jwtTenantFilter.doFilterInternal(request, response, filterChain)
        );

        // TenantContext should be cleared even after exception in filterChain
        assertNull(TenantContext.getTenantName());
    }

    @Test
    void testDoFilterInternal_MultipleTenants_DelegatesCorrectly() throws ServletException, IOException {
        // Arrange
        String tenant1 = "tenant1";
        String tenant2 = "tenant2";

        // First request - tenant1
        when(jwtClaimsContext.getTenant()).thenReturn(tenant1);
        doNothing().when(tenantService).ensureTenantExists(tenant1);

        jwtTenantFilter.doFilterInternal(request, response, filterChain);

        // Verify tenant1 was ensured
        verify(tenantService).ensureTenantExists(tenant1);

        // Reset mocks for second request
        reset(tenantService, filterChain);

        // Second request - tenant2
        when(jwtClaimsContext.getTenant()).thenReturn(tenant2);
        doNothing().when(tenantService).ensureTenantExists(tenant2);

        jwtTenantFilter.doFilterInternal(request, response, filterChain);

        // Verify tenant2 was ensured
        verify(tenantService).ensureTenantExists(tenant2);
    }

    @Test
    void testDoFilterInternal_SameTenantMultipleTimes_DelegatesEachTime() throws ServletException, IOException {
        // Arrange
        when(jwtClaimsContext.getTenant()).thenReturn(testTenantName);
        doNothing().when(tenantService).ensureTenantExists(testTenantName);

        // First request
        jwtTenantFilter.doFilterInternal(request, response, filterChain);
        verify(tenantService).ensureTenantExists(testTenantName);

        // Reset mocks
        reset(tenantService, filterChain);

        // Second request
        when(jwtClaimsContext.getTenant()).thenReturn(testTenantName);
        doNothing().when(tenantService).ensureTenantExists(testTenantName);

        jwtTenantFilter.doFilterInternal(request, response, filterChain);

        // Verify service is called again (caching is done in service layer)
        verify(tenantService).ensureTenantExists(testTenantName);
    }
}
