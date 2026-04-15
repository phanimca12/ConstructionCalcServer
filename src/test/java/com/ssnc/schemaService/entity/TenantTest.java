package com.ssnc.schemaService.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantTest {

    @Test
    void testSettersAndGetters() {
        Tenant tenant = new Tenant();

        UUID tenantId = UUID.randomUUID();
        tenant.setTenantId(tenantId);
        assertEquals(tenantId, tenant.getTenantId());

        tenant.setTenantName("TestTenant");
        assertEquals("TestTenant", tenant.getTenantName());

        tenant.setCreatedBy("creator");
        assertEquals("creator", tenant.getCreatedBy());

        tenant.setUpdatedBy("updater");
        assertEquals("updater", tenant.getUpdatedBy());

        LocalDateTime now = LocalDateTime.now();
        tenant.setCreatedDatetime(now);
        assertEquals(now, tenant.getCreatedDatetime());

        tenant.setUpdatedDatetime(now);
        assertEquals(now, tenant.getUpdatedDatetime());
    }

    @Test
    void testEqualsAndHashCode() {
        Tenant tenant1 = new Tenant();
        UUID id = UUID.randomUUID();
        tenant1.setTenantId(id);
        tenant1.setTenantName("TestTenant");

        Tenant tenant2 = new Tenant();
        tenant2.setTenantId(id);
        tenant2.setTenantName("TestTenant");

        assertEquals(tenant1, tenant2);
        assertEquals(tenant1.hashCode(), tenant2.hashCode());
    }

    @Test
    void testToString() {
        Tenant tenant = new Tenant();
        tenant.setTenantName("TestTenant");

        String toString = tenant.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("TestTenant"));
    }

    @Test
    void testNullValues() {
        Tenant tenant = new Tenant();

        assertNull(tenant.getTenantId());
        assertNull(tenant.getTenantName());
        assertNull(tenant.getCreatedBy());
        assertNull(tenant.getUpdatedBy());
    }

    @Test
    void testTenantNameUpdate() {
        Tenant tenant = new Tenant();

        assertNull(tenant.getTenantName());

        tenant.setTenantName("Tenant1");
        assertEquals("Tenant1", tenant.getTenantName());

        tenant.setTenantName("Tenant2");
        assertEquals("Tenant2", tenant.getTenantName());
    }

    @Test
    void testTenantNameMaxLength() {
        Tenant tenant = new Tenant();

        String maxLengthName = "a".repeat(256);
        tenant.setTenantName(maxLengthName);

        assertEquals(maxLengthName, tenant.getTenantName());
        assertEquals(256, tenant.getTenantName().length());
    }
}
