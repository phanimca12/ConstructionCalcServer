package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NameSpaceDtoTest {

    @Test
    void testSettersAndGetters() {
        NameSpaceDto dto = new NameSpaceDto();

        UUID nmspcId = UUID.randomUUID();
        dto.setNmspcId(nmspcId);
        assertEquals(nmspcId, dto.getNmspcId());

        UUID tenantId = UUID.randomUUID();
        dto.setTenantId(tenantId);
        assertEquals(tenantId, dto.getTenantId());

        dto.setName("TestNamespace");
        assertEquals("TestNamespace", dto.getName());

        dto.setDescription("Test Description");
        assertEquals("Test Description", dto.getDescription());

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
        NameSpaceDto dto = new NameSpaceDto();

        assertNull(dto.getNmspcId());
        assertNull(dto.getTenantId());
        assertNull(dto.getName());
        assertNull(dto.getDescription());
        assertNull(dto.getCreatedByUser());
        assertNull(dto.getModifiedByUser());
    }
}
