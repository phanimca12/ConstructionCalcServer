package com.ssnc.schemaService.entity;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SchmDataIdTest {

    @Test
    void testDefaultConstructor() {
        SchmDataId id = new SchmDataId();

        assertNull(id.getSchmId());
        assertNull(id.getSchmVersion());
    }

    @Test
    void testParameterizedConstructor() {
        UUID schmId = UUID.randomUUID();
        Integer version = 1;

        SchmDataId id = new SchmDataId(schmId, version);

        assertEquals(schmId, id.getSchmId());
        assertEquals(version, id.getSchmVersion());
    }

    @Test
    void testSettersAndGetters() {
        SchmDataId id = new SchmDataId();

        UUID schmId = UUID.randomUUID();
        id.setSchmId(schmId);
        assertEquals(schmId, id.getSchmId());

        id.setSchmVersion(2);
        assertEquals(2, id.getSchmVersion());
    }

    @Test
    void testSetSchmId() {
        SchmDataId id = new SchmDataId();
        UUID schmId = UUID.randomUUID();

        id.setSchmId(schmId);

        assertEquals(schmId, id.getSchmId());
        assertNull(id.getSchmVersion());
    }

    @Test
    void testSetSchmVersion() {
        SchmDataId id = new SchmDataId();

        id.setSchmVersion(5);

        assertEquals(5, id.getSchmVersion());
        assertNull(id.getSchmId());
    }

    @Test
    void testSetBothFields() {
        SchmDataId id = new SchmDataId();
        UUID schmId = UUID.randomUUID();
        Integer version = 3;

        id.setSchmId(schmId);
        id.setSchmVersion(version);

        assertEquals(schmId, id.getSchmId());
        assertEquals(version, id.getSchmVersion());
    }

    @Test
    void testUpdateFields() {
        UUID schmId1 = UUID.randomUUID();
        UUID schmId2 = UUID.randomUUID();
        SchmDataId id = new SchmDataId(schmId1, 1);

        assertEquals(schmId1, id.getSchmId());
        assertEquals(1, id.getSchmVersion());

        id.setSchmId(schmId2);
        id.setSchmVersion(2);

        assertEquals(schmId2, id.getSchmId());
        assertEquals(2, id.getSchmVersion());
    }

    @Test
    void testNullSchmId() {
        SchmDataId id = new SchmDataId(null, 1);

        assertNull(id.getSchmId());
        assertEquals(1, id.getSchmVersion());
    }

    @Test
    void testNullVersion() {
        UUID schmId = UUID.randomUUID();
        SchmDataId id = new SchmDataId(schmId, null);

        assertEquals(schmId, id.getSchmId());
        assertNull(id.getSchmVersion());
    }

    @Test
    void testBothNullFields() {
        SchmDataId id = new SchmDataId(null, null);

        assertNull(id.getSchmId());
        assertNull(id.getSchmVersion());
    }

    @Test
    void testSetNullValues() {
        SchmDataId id = new SchmDataId(UUID.randomUUID(), 1);

        id.setSchmId(null);
        id.setSchmVersion(null);

        assertNull(id.getSchmId());
        assertNull(id.getSchmVersion());
    }
}
