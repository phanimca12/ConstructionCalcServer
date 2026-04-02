package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExtRefDtoTest {

    private ExtRefDto extRefDto;
    private UUID testExtRefId;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        extRefDto = new ExtRefDto();
        testExtRefId = UUID.randomUUID();
        testDateTime = LocalDateTime.now();
    }

    @Test
    void testExtRefId() {
        extRefDto.setExtRefId(testExtRefId);
        assertEquals(testExtRefId, extRefDto.getExtRefId());
    }

    @Test
    void testTenantName() {
        extRefDto.setTenantName("client1Id");
        assertEquals("client1Id", extRefDto.getTenantName());
    }

    @Test
    void testExtRefName() {
        extRefDto.setExtRefName("Test Process");
        assertEquals("Test Process", extRefDto.getExtRefName());
    }

    @Test
    void testExtRefType() {
        extRefDto.setExtRefType("Process");
        assertEquals("Process", extRefDto.getExtRefType());
    }

    @Test
    void testExtRefVersion() {
        extRefDto.setExtRefVersion("1.0.0");
        assertEquals("1.0.0", extRefDto.getExtRefVersion());
    }

    @Test
    void testCreatedDatetime() {
        extRefDto.setCreatedDatetime(testDateTime);
        assertEquals(testDateTime, extRefDto.getCreatedDatetime());
    }

    @Test
    void testUpdatedDatetime() {
        extRefDto.setUpdatedDatetime(testDateTime);
        assertEquals(testDateTime, extRefDto.getUpdatedDatetime());
    }

    @Test
    void testCreatedBy() {
        extRefDto.setCreatedBy("testUser");
        assertEquals("testUser", extRefDto.getCreatedBy());
    }

    @Test
    void testUpdatedBy() {
        extRefDto.setUpdatedBy("testUser");
        assertEquals("testUser", extRefDto.getUpdatedBy());
    }

    @Test
    void testAllFields() {
        UUID extRefId = UUID.randomUUID();
        LocalDateTime createdDateTime = LocalDateTime.now();
        LocalDateTime updatedDateTime = LocalDateTime.now().plusMinutes(5);

        extRefDto.setExtRefId(extRefId);
        extRefDto.setTenantName("client1Id");
        extRefDto.setExtRefName("Complete Process");
        extRefDto.setExtRefType("Automation");
        extRefDto.setExtRefVersion("2.0.0");
        extRefDto.setCreatedDatetime(createdDateTime);
        extRefDto.setUpdatedDatetime(updatedDateTime);
        extRefDto.setCreatedBy("user1");
        extRefDto.setUpdatedBy("user2");

        assertEquals(extRefId, extRefDto.getExtRefId());
        assertEquals("client1Id", extRefDto.getTenantName());
        assertEquals("Complete Process", extRefDto.getExtRefName());
        assertEquals("Automation", extRefDto.getExtRefType());
        assertEquals("2.0.0", extRefDto.getExtRefVersion());
        assertEquals(createdDateTime, extRefDto.getCreatedDatetime());
        assertEquals(updatedDateTime, extRefDto.getUpdatedDatetime());
        assertEquals("user1", extRefDto.getCreatedBy());
        assertEquals("user2", extRefDto.getUpdatedBy());
    }

    @Test
    void testNullValues() {
        assertNull(extRefDto.getExtRefId());
        assertNull(extRefDto.getTenantName());
        assertNull(extRefDto.getExtRefName());
        assertNull(extRefDto.getExtRefType());
        assertNull(extRefDto.getExtRefVersion());
        assertNull(extRefDto.getCreatedDatetime());
        assertNull(extRefDto.getUpdatedDatetime());
        assertNull(extRefDto.getCreatedBy());
        assertNull(extRefDto.getUpdatedBy());
    }

    @Test
    void testEqualsAndHashCode() {
        ExtRefDto dto1 = new ExtRefDto();
        dto1.setExtRefId(testExtRefId);
        dto1.setExtRefName("Test");

        ExtRefDto dto2 = new ExtRefDto();
        dto2.setExtRefId(testExtRefId);
        dto2.setExtRefName("Test");

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        extRefDto.setExtRefId(testExtRefId);
        extRefDto.setExtRefName("Test Process");
        extRefDto.setExtRefType("Process");

        String result = extRefDto.toString();
        assertNotNull(result);
        assertTrue(result.contains("ExtRefDto"));
    }
}
