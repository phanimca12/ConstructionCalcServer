package com.ssnc.schemaService.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NmspcTest {

    @Test
    void testSettersAndGetters() {
        Nmspc nmspc = new Nmspc();

        UUID nmspcId = UUID.randomUUID();
        nmspc.setNmspcId(nmspcId);
        assertEquals(nmspcId, nmspc.getNmspcId());

        UUID tenantId = UUID.randomUUID();
        nmspc.setTenantId(tenantId);
        assertEquals(tenantId, nmspc.getTenantId());

        nmspc.setNmspcName("TestNamespace");
        assertEquals("TestNamespace", nmspc.getNmspcName());

        nmspc.setDescription("Test Description");
        assertEquals("Test Description", nmspc.getDescription());

        nmspc.setCreatedBy("creator");
        assertEquals("creator", nmspc.getCreatedBy());

        nmspc.setUpdatedBy("updater");
        assertEquals("updater", nmspc.getUpdatedBy());

        LocalDateTime now = LocalDateTime.now();
        nmspc.setCreatedDatetime(now);
        assertEquals(now, nmspc.getCreatedDatetime());

        nmspc.setUpdatedDatetime(now);
        assertEquals(now, nmspc.getUpdatedDatetime());
    }

    @Test
    void testEqualsAndHashCode() {
        Nmspc nmspc1 = new Nmspc();
        UUID id = UUID.randomUUID();
        nmspc1.setNmspcId(id);
        nmspc1.setNmspcName("TestNamespace");

        Nmspc nmspc2 = new Nmspc();
        nmspc2.setNmspcId(id);
        nmspc2.setNmspcName("TestNamespace");

        assertEquals(nmspc1, nmspc2);
        assertEquals(nmspc1.hashCode(), nmspc2.hashCode());
    }

    @Test
    void testToString() {
        Nmspc nmspc = new Nmspc();
        nmspc.setNmspcName("TestNamespace");

        String toString = nmspc.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("TestNamespace"));
    }

    @Test
    void testNullValues() {
        Nmspc nmspc = new Nmspc();

        assertNull(nmspc.getNmspcId());
        assertNull(nmspc.getTenantId());
        assertNull(nmspc.getNmspcName());
        assertNull(nmspc.getDescription());
        assertNull(nmspc.getCreatedBy());
        assertNull(nmspc.getUpdatedBy());
    }

    @Test
    void testDescriptionUpdate() {
        Nmspc nmspc = new Nmspc();

        assertNull(nmspc.getDescription());

        nmspc.setDescription("Original description");
        assertEquals("Original description", nmspc.getDescription());

        nmspc.setDescription("Updated description");
        assertEquals("Updated description", nmspc.getDescription());

        nmspc.setDescription(null);
        assertNull(nmspc.getDescription());
    }

    @Test
    void testNamespaceNameMaxLength() {
        Nmspc nmspc = new Nmspc();

        String maxLengthName = "a".repeat(32);
        nmspc.setNmspcName(maxLengthName);

        assertEquals(maxLengthName, nmspc.getNmspcName());
        assertEquals(32, nmspc.getNmspcName().length());
    }
}
