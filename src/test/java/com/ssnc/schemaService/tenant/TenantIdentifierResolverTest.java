package com.ssnc.schemaService.tenant;

import com.ssnc.schemaService.constants.AppConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantIdentifierResolverTest {

    private TenantIdentifierResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new TenantIdentifierResolver();
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void testResolveCurrentTenantIdentifier_WithTenantSet() {
        String expectedTenant = "testTenant";
        TenantContext.setTenantName(expectedTenant);

        String result = resolver.resolveCurrentTenantIdentifier();

        assertEquals(expectedTenant, result);
    }

    @Test
    void testResolveCurrentTenantIdentifier_WithoutTenantSet() {
        TenantContext.clear();

        String result = resolver.resolveCurrentTenantIdentifier();

        assertEquals(AppConstants.DEFAULT_TENANT_ID, result);
    }

    @Test
    void testResolveCurrentTenantIdentifier_AfterClear() {
        TenantContext.setTenantName("testTenant");
        TenantContext.clear();

        String result = resolver.resolveCurrentTenantIdentifier();

        assertEquals(AppConstants.DEFAULT_TENANT_ID, result);
    }

    @Test
    void testValidateExistingCurrentSessions() {
        assertTrue(resolver.validateExistingCurrentSessions());
    }

    @Test
    void testValidateExistingCurrentSessions_AlwaysReturnsTrue() {
        // Should always return true regardless of tenant context
        TenantContext.setTenantName("tenant1");
        assertTrue(resolver.validateExistingCurrentSessions());

        TenantContext.clear();
        assertTrue(resolver.validateExistingCurrentSessions());

        TenantContext.setTenantName("tenant2");
        assertTrue(resolver.validateExistingCurrentSessions());
    }

    @Test
    void testResolveCurrentTenantIdentifier_WithEmptyString() {
        TenantContext.setTenantName("");

        String result = resolver.resolveCurrentTenantIdentifier();

        assertEquals("", result);
    }

    @Test
    void testResolveCurrentTenantIdentifier_WithNullAfterSet() {
        TenantContext.setTenantName("testTenant");
        TenantContext.setTenantName(null);

        String result = resolver.resolveCurrentTenantIdentifier();

        assertEquals(AppConstants.DEFAULT_TENANT_ID, result);
    }

    @Test
    void testMultipleResolverInstances() {
        TenantIdentifierResolver resolver1 = new TenantIdentifierResolver();
        TenantIdentifierResolver resolver2 = new TenantIdentifierResolver();

        TenantContext.setTenantName("sharedTenant");

        assertEquals("sharedTenant", resolver1.resolveCurrentTenantIdentifier());
        assertEquals("sharedTenant", resolver2.resolveCurrentTenantIdentifier());
    }
}
