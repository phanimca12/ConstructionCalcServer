package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SchemaDtoTest {

    @Test
    void testSettersAndGetters() {
        SchemaDto dto = new SchemaDto();

        UUID id = UUID.randomUUID();
        dto.setId(id);
        assertEquals(id, dto.getId());

        dto.setName("TestSchema");
        assertEquals("TestSchema", dto.getName());

        dto.setDescription("Test Description");
        assertEquals("Test Description", dto.getDescription());

        dto.setSchemaType("JSON");
        assertEquals("JSON", dto.getSchemaType());

        dto.setContentType("application/json");
        assertEquals("application/json", dto.getContentType());

        dto.setSchmGroup("TestGroup");
        assertEquals("TestGroup", dto.getSchmGroup());

        dto.setPublished(1);
        assertEquals(1, dto.getPublished());

        dto.setDraft(2);
        assertEquals(2, dto.getDraft());

        dto.setLockBy("testUser");
        assertEquals("testUser", dto.getLockBy());

        dto.setCreatedByUser("creator");
        assertEquals("creator", dto.getCreatedByUser());

        dto.setModifiedByUser("modifier");
        assertEquals("modifier", dto.getModifiedByUser());

        LocalDateTime now = LocalDateTime.now();
        dto.setCreateDateTime(now);
        assertEquals(now, dto.getCreateDateTime());

        dto.setModifiedDateTime(now);
        assertEquals(now, dto.getModifiedDateTime());

        SchemaVersionDto version = new SchemaVersionDto();
        dto.setVersion(version);
        assertEquals(version, dto.getVersion());
    }

    @Test
    void testNullValues() {
        SchemaDto dto = new SchemaDto();

        assertNull(dto.getId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getSchemaType());
        assertNull(dto.getContentType());
        assertNull(dto.getPublished());
        assertNull(dto.getDraft());
        assertNull(dto.getVersion());
    }

    @Test
    void testVersionManagement() {
        SchemaDto dto = new SchemaDto();

        assertNull(dto.getPublished());
        assertNull(dto.getDraft());

        dto.setPublished(1);
        dto.setDraft(2);

        assertEquals(1, dto.getPublished());
        assertEquals(2, dto.getDraft());

        dto.setPublished(null);
        assertNull(dto.getPublished());
        assertEquals(2, dto.getDraft());
    }
}
