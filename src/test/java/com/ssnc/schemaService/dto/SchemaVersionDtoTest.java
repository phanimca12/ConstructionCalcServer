package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SchemaVersionDtoTest {

    @Test
    void testSettersAndGetters() {
        SchemaVersionDto dto = new SchemaVersionDto();

        dto.setVersionNumber(1);
        assertEquals(1, dto.getVersionNumber());

        dto.setContent("content/path");
        assertEquals("content/path", dto.getContent());

        dto.setIsDraft(true);
        assertTrue(dto.getIsDraft());

        dto.setCreatedByUser("creator");
        assertEquals("creator", dto.getCreatedByUser());

        dto.setModifiedByUser("modifier");
        assertEquals("modifier", dto.getModifiedByUser());

        LocalDateTime now = LocalDateTime.now();
        dto.setCreateDateTime(now);
        assertEquals(now, dto.getCreateDateTime());

        dto.setModifiedDateTime(now);
        assertEquals(now, dto.getModifiedDateTime());
    }

    @Test
    void testNullValues() {
        SchemaVersionDto dto = new SchemaVersionDto();

        assertNull(dto.getVersionNumber());
        assertNull(dto.getContent());
        assertNull(dto.getIsDraft());
        assertNull(dto.getCreatedByUser());
        assertNull(dto.getModifiedByUser());
    }

    @Test
    void testDraftToggle() {
        SchemaVersionDto dto = new SchemaVersionDto();

        dto.setIsDraft(true);
        assertTrue(dto.getIsDraft());

        dto.setIsDraft(false);
        assertFalse(dto.getIsDraft());
    }
}
