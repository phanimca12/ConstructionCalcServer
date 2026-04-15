package com.ssnc.schemaService.tenant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TenantContextTest {

    @AfterEach
    void tearDown() {
        // Clean up after each test
        TenantContext.clear();
    }

    @Test
    void testSetAndGetTenantName() {
        String tenantName = "testTenant";
        TenantContext.setTenantName(tenantName);

        assertEquals(tenantName, TenantContext.getTenantName());
    }

    @Test
    void testGetTenantName_WhenNotSet() {
        assertNull(TenantContext.getTenantName());
    }

    @Test
    void testClearTenantName() {
        TenantContext.setTenantName("testTenant");
        assertNotNull(TenantContext.getTenantName());

        TenantContext.clear();
        assertNull(TenantContext.getTenantName());
    }

    @Test
    void testThreadLocalIsolation() throws InterruptedException {
        TenantContext.setTenantName("mainThread");

        Thread thread = new Thread(() -> {
            // This should be null in the new thread
            assertNull(TenantContext.getTenantName());

            // Set a different value in this thread
            TenantContext.setTenantName("childThread");
            assertEquals("childThread", TenantContext.getTenantName());

            TenantContext.clear();
        });

        thread.start();
        thread.join();

        // Main thread should still have its original value
        assertEquals("mainThread", TenantContext.getTenantName());
    }

    @Test
    void testOverwriteTenantName() {
        TenantContext.setTenantName("tenant1");
        assertEquals("tenant1", TenantContext.getTenantName());

        TenantContext.setTenantName("tenant2");
        assertEquals("tenant2", TenantContext.getTenantName());
    }

    @Test
    void testSetNullTenantName() {
        TenantContext.setTenantName("testTenant");
        assertNotNull(TenantContext.getTenantName());

        TenantContext.setTenantName(null);
        assertNull(TenantContext.getTenantName());
    }

    @Test
    void testMultipleClearCalls() {
        TenantContext.setTenantName("testTenant");
        TenantContext.clear();
        TenantContext.clear(); // Should not throw exception

        assertNull(TenantContext.getTenantName());
    }

    @Test
    void testEmptyStringTenantName() {
        TenantContext.setTenantName("");
        assertEquals("", TenantContext.getTenantName());
    }
}
