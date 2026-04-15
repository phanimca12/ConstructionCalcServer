package com.ssnc.schemaService.repo;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SchmFilterCriteriaTest {

    @Test
    void testSettersAndGetters() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();

        UUID schmId = UUID.randomUUID();
        criteria.setSchmId(schmId);
        assertEquals(schmId, criteria.getSchmId());

        criteria.setName("testSchema");
        assertEquals("testSchema", criteria.getName());

        criteria.setSchemaType("JSON");
        assertEquals("JSON", criteria.getSchemaType());

        criteria.setGroup("testGroup");
        assertEquals("testGroup", criteria.getGroup());

        criteria.setLockBy("testUser");
        assertEquals("testUser", criteria.getLockBy());

        criteria.setPublishVersion(1);
        assertEquals(1, criteria.getPublishVersion());

        criteria.setModifiedByUser("modifier");
        assertEquals("modifier", criteria.getModifiedByUser());

        criteria.setVersionModifiedByUser("versionModifier");
        assertEquals("versionModifier", criteria.getVersionModifiedByUser());

        criteria.setSort("name");
        assertEquals("name", criteria.getSort());

        criteria.setWithVersion("draft");
        assertEquals("draft", criteria.getWithVersion());
    }

    @Test
    void testDefaultValues() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();

        assertNull(criteria.getSchmId());
        assertNull(criteria.getName());
        assertNull(criteria.getSchemaType());
        assertNull(criteria.getGroup());
        assertNull(criteria.getLockBy());
        assertNull(criteria.getPublishVersion());
        assertNull(criteria.getModifiedByUser());
        assertNull(criteria.getVersionModifiedByUser());
        assertNull(criteria.getSort());
        assertNull(criteria.getWithVersion());
    }

    @Test
    void testSetNullValues() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();

        criteria.setSchmId(UUID.randomUUID());
        criteria.setSchmId(null);
        assertNull(criteria.getSchmId());

        criteria.setName("test");
        criteria.setName(null);
        assertNull(criteria.getName());

        criteria.setSchemaType("JSON");
        criteria.setSchemaType(null);
        assertNull(criteria.getSchemaType());

        criteria.setGroup("group");
        criteria.setGroup(null);
        assertNull(criteria.getGroup());

        criteria.setPublishVersion(1);
        criteria.setPublishVersion(null);
        assertNull(criteria.getPublishVersion());
    }

    @Test
    void testEmptyStringValues() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();

        criteria.setName("");
        assertEquals("", criteria.getName());

        criteria.setSchemaType("");
        assertEquals("", criteria.getSchemaType());

        criteria.setGroup("");
        assertEquals("", criteria.getGroup());
    }

    @Test
    void testMultipleUpdates() {
        SchmFilterCriteria criteria = new SchmFilterCriteria();

        criteria.setName("schema1");
        assertEquals("schema1", criteria.getName());

        criteria.setName("schema2");
        assertEquals("schema2", criteria.getName());

        criteria.setPublishVersion(1);
        assertEquals(1, criteria.getPublishVersion());

        criteria.setPublishVersion(2);
        assertEquals(2, criteria.getPublishVersion());
    }
}
