package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TenantDtoTest {

    @Test
    void testSettersAndGetters() {
        TenantDto dto = new TenantDto();

        UUID tenantId = UUID.randomUUID();
        dto.setTenantId(tenantId);
        assertEquals(tenantId, dto.getTenantId());

        dto.setName("TestTenant");
        assertEquals("TestTenant", dto.getName());

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
        TenantDto dto = new TenantDto();

        assertNull(dto.getTenantId());
        assertNull(dto.getName());
        assertNull(dto.getCreatedByUser());
        assertNull(dto.getModifiedByUser());
    }
}
