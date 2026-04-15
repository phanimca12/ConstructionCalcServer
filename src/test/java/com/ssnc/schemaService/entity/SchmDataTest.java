package com.ssnc.schemaService.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SchmDataTest {

    @Test
    void testSettersAndGetters() {
        SchmData schmData = new SchmData();

        SchmDataId id = new SchmDataId(UUID.randomUUID(), 1);
        schmData.setId(id);
        assertEquals(id, schmData.getId());

        schmData.setSchmData("Schema content");
        assertEquals("Schema content", schmData.getSchmData());

        schmData.setSchmVersionName("v1.0");
        assertEquals("v1.0", schmData.getSchmVersionName());

        schmData.setIsDraft(true);
        assertTrue(schmData.getIsDraft());

        LocalDateTime now = LocalDateTime.now();
        schmData.setCreatedDatetime(now);
        assertEquals(now, schmData.getCreatedDatetime());

        schmData.setCreatedBy("creator");
        assertEquals("creator", schmData.getCreatedBy());

        schmData.setUpdatedDatetime(now);
        assertEquals(now, schmData.getUpdatedDatetime());

        schmData.setUpdatedBy("updater");
        assertEquals("updater", schmData.getUpdatedBy());
    }

    @Test
    void testEqualsAndHashCode() {
        SchmDataId id = new SchmDataId(UUID.randomUUID(), 1);

        SchmData schmData1 = new SchmData();
        schmData1.setId(id);
        schmData1.setSchmData("Content");

        SchmData schmData2 = new SchmData();
        schmData2.setId(id);
        schmData2.setSchmData("Content");

        assertEquals(schmData1, schmData2);
        assertEquals(schmData1.hashCode(), schmData2.hashCode());
    }

    @Test
    void testToString() {
        SchmData schmData = new SchmData();
        schmData.setSchmData("Test Content");

        String toString = schmData.toString();
        assertNotNull(toString);
    }

    @Test
    void testIsDraftToggle() {
        SchmData schmData = new SchmData();

        assertNull(schmData.getIsDraft());

        schmData.setIsDraft(true);
        assertTrue(schmData.getIsDraft());

        schmData.setIsDraft(false);
        assertFalse(schmData.getIsDraft());

        schmData.setIsDraft(null);
        assertNull(schmData.getIsDraft());
    }

    @Test
    void testNullValues() {
        SchmData schmData = new SchmData();

        assertNull(schmData.getId());
        assertNull(schmData.getSchmData());
        assertNull(schmData.getSchmVersionName());
        assertNull(schmData.getIsDraft());
        assertNull(schmData.getCreatedBy());
        assertNull(schmData.getUpdatedBy());
    }

    @Test
    void testLongContent() {
        SchmData schmData = new SchmData();

        String longContent = "x".repeat(10000);
        schmData.setSchmData(longContent);

        assertEquals(longContent, schmData.getSchmData());
        assertEquals(10000, schmData.getSchmData().length());
    }
}
