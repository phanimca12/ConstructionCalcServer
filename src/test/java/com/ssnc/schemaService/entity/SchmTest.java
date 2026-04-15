package com.ssnc.schemaService.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SchmTest {

    @Test
    void testSettersAndGetters() {
        Schm schm = new Schm();

        UUID schmId = UUID.randomUUID();
        schm.setSchmId(schmId);
        assertEquals(schmId, schm.getSchmId());

        UUID tenantId = UUID.randomUUID();
        schm.setTenantId(tenantId);
        assertEquals(tenantId, schm.getTenantId());

        UUID nmspcId = UUID.randomUUID();
        schm.setNmspcId(nmspcId);
        assertEquals(nmspcId, schm.getNmspcId());

        schm.setSchmName("TestSchema");
        assertEquals("TestSchema", schm.getSchmName());

        schm.setSchmDesc("Test Description");
        assertEquals("Test Description", schm.getSchmDesc());

        schm.setSchemaType("JSON");
        assertEquals("JSON", schm.getSchemaType());

        schm.setContentType("application/json");
        assertEquals("application/json", schm.getContentType());

        schm.setSchmGroup("TestGroup");
        assertEquals("TestGroup", schm.getSchmGroup());

        schm.setPublishVersion(1);
        assertEquals(1, schm.getPublishVersion());

        schm.setLockBy("testUser");
        assertEquals("testUser", schm.getLockBy());

        schm.setCreatedBy("creator");
        assertEquals("creator", schm.getCreatedBy());

        schm.setUpdatedBy("updater");
        assertEquals("updater", schm.getUpdatedBy());

        LocalDateTime now = LocalDateTime.now();
        schm.setCreatedDatetime(now);
        assertEquals(now, schm.getCreatedDatetime());

        schm.setUpdatedDatetime(now);
        assertEquals(now, schm.getUpdatedDatetime());
    }

    @Test
    void testEqualsAndHashCode() {
        Schm schm1 = new Schm();
        UUID id = UUID.randomUUID();
        schm1.setSchmId(id);
        schm1.setSchmName("TestSchema");

        Schm schm2 = new Schm();
        schm2.setSchmId(id);
        schm2.setSchmName("TestSchema");

        assertEquals(schm1, schm2);
        assertEquals(schm1.hashCode(), schm2.hashCode());
    }

    @Test
    void testToString() {
        Schm schm = new Schm();
        schm.setSchmName("TestSchema");

        String toString = schm.toString();
        assertNotNull(toString);
        assertTrue(toString.contains("TestSchema"));
    }

    @Test
    void testNullValues() {
        Schm schm = new Schm();

        assertNull(schm.getSchmId());
        assertNull(schm.getTenantId());
        assertNull(schm.getNmspcId());
        assertNull(schm.getSchmName());
        assertNull(schm.getSchemaType());
        assertNull(schm.getPublishVersion());
    }

    @Test
    void testPublishVersionUpdate() {
        Schm schm = new Schm();

        assertNull(schm.getPublishVersion());

        schm.setPublishVersion(1);
        assertEquals(1, schm.getPublishVersion());

        schm.setPublishVersion(2);
        assertEquals(2, schm.getPublishVersion());

        schm.setPublishVersion(null);
        assertNull(schm.getPublishVersion());
    }

    @Test
    void testLockByUpdate() {
        Schm schm = new Schm();

        assertNull(schm.getLockBy());

        schm.setLockBy("user1");
        assertEquals("user1", schm.getLockBy());

        schm.setLockBy("user2");
        assertEquals("user2", schm.getLockBy());

        schm.setLockBy(null);
        assertNull(schm.getLockBy());
    }
}
