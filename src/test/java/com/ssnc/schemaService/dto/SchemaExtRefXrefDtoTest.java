package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SchemaExtRefXrefDtoTest {

    private SchemaExtRefXrefDto xrefDto;
    private UUID testXrefId;
    private UUID testSchmId;
    private UUID testExtRefId;
    private LocalDateTime testDateTime;

    @BeforeEach
    void setUp() {
        xrefDto = new SchemaExtRefXrefDto();
        testXrefId = UUID.randomUUID();
        testSchmId = UUID.randomUUID();
        testExtRefId = UUID.randomUUID();
        testDateTime = LocalDateTime.now();
    }

    @Test
    void testXrefId() {
        xrefDto.setXrefId(testXrefId);
        assertEquals(testXrefId, xrefDto.getXrefId());
    }

    @Test
    void testTenantName() {
        xrefDto.setTenantName("client1Id");
        assertEquals("client1Id", xrefDto.getTenantName());
    }

    @Test
    void testSchmId() {
        xrefDto.setSchmId(testSchmId);
        assertEquals(testSchmId, xrefDto.getSchmId());
    }

    @Test
    void testExtRefId() {
        xrefDto.setExtRefId(testExtRefId);
        assertEquals(testExtRefId, xrefDto.getExtRefId());
    }

    @Test
    void testCreatedDatetime() {
        xrefDto.setCreatedDatetime(testDateTime);
        assertEquals(testDateTime, xrefDto.getCreatedDatetime());
    }

    @Test
    void testCreatedBy() {
        xrefDto.setCreatedBy("testUser");
        assertEquals("testUser", xrefDto.getCreatedBy());
    }

    @Test
    void testSchmName() {
        xrefDto.setSchmName("Test Schema");
        assertEquals("Test Schema", xrefDto.getSchmName());
    }

    @Test
    void testExtRefName() {
        xrefDto.setExtRefName("Test Process");
        assertEquals("Test Process", xrefDto.getExtRefName());
    }

    @Test
    void testExtRefType() {
        xrefDto.setExtRefType("Process");
        assertEquals("Process", xrefDto.getExtRefType());
    }

    @Test
    void testExtRefVersion() {
        xrefDto.setExtRefVersion("1.0.0");
        assertEquals("1.0.0", xrefDto.getExtRefVersion());
    }

    @Test
    void testAllFields() {
        UUID xrefId = UUID.randomUUID();
        UUID schmId = UUID.randomUUID();
        UUID extRefId = UUID.randomUUID();
        LocalDateTime createdDateTime = LocalDateTime.now();

        xrefDto.setXrefId(xrefId);
        xrefDto.setTenantName("client2Id");
        xrefDto.setSchmId(schmId);
        xrefDto.setExtRefId(extRefId);
        xrefDto.setCreatedDatetime(createdDateTime);
        xrefDto.setCreatedBy("user1");
        xrefDto.setSchmName("Customer Schema");
        xrefDto.setExtRefName("Workflow Process");
        xrefDto.setExtRefType("Automation");
        xrefDto.setExtRefVersion("2.5.0");

        assertEquals(xrefId, xrefDto.getXrefId());
        assertEquals("client2Id", xrefDto.getTenantName());
        assertEquals(schmId, xrefDto.getSchmId());
        assertEquals(extRefId, xrefDto.getExtRefId());
        assertEquals(createdDateTime, xrefDto.getCreatedDatetime());
        assertEquals("user1", xrefDto.getCreatedBy());
        assertEquals("Customer Schema", xrefDto.getSchmName());
        assertEquals("Workflow Process", xrefDto.getExtRefName());
        assertEquals("Automation", xrefDto.getExtRefType());
        assertEquals("2.5.0", xrefDto.getExtRefVersion());
    }

    @Test
    void testNullValues() {
        assertNull(xrefDto.getXrefId());
        assertNull(xrefDto.getTenantName());
        assertNull(xrefDto.getSchmId());
        assertNull(xrefDto.getExtRefId());
        assertNull(xrefDto.getCreatedDatetime());
        assertNull(xrefDto.getCreatedBy());
        assertNull(xrefDto.getSchmName());
        assertNull(xrefDto.getExtRefName());
        assertNull(xrefDto.getExtRefType());
        assertNull(xrefDto.getExtRefVersion());
    }

    @Test
    void testCoreFieldsOnly() {
        xrefDto.setXrefId(testXrefId);
        xrefDto.setSchmId(testSchmId);
        xrefDto.setExtRefId(testExtRefId);
        xrefDto.setCreatedBy("testUser");

        assertEquals(testXrefId, xrefDto.getXrefId());
        assertEquals(testSchmId, xrefDto.getSchmId());
        assertEquals(testExtRefId, xrefDto.getExtRefId());
        assertEquals("testUser", xrefDto.getCreatedBy());

        // Optional fields should be null
        assertNull(xrefDto.getSchmName());
        assertNull(xrefDto.getExtRefName());
        assertNull(xrefDto.getExtRefType());
        assertNull(xrefDto.getExtRefVersion());
    }

    @Test
    void testNestedFieldsOnly() {
        xrefDto.setSchmName("Schema 1");
        xrefDto.setExtRefName("External Ref 1");
        xrefDto.setExtRefType("PresentationFlow");
        xrefDto.setExtRefVersion("3.0.0");

        assertEquals("Schema 1", xrefDto.getSchmName());
        assertEquals("External Ref 1", xrefDto.getExtRefName());
        assertEquals("PresentationFlow", xrefDto.getExtRefType());
        assertEquals("3.0.0", xrefDto.getExtRefVersion());
    }

    @Test
    void testEqualsAndHashCode() {
        SchemaExtRefXrefDto dto1 = new SchemaExtRefXrefDto();
        dto1.setXrefId(testXrefId);
        dto1.setSchmId(testSchmId);
        dto1.setExtRefId(testExtRefId);

        SchemaExtRefXrefDto dto2 = new SchemaExtRefXrefDto();
        dto2.setXrefId(testXrefId);
        dto2.setSchmId(testSchmId);
        dto2.setExtRefId(testExtRefId);

        assertEquals(dto1, dto2);
        assertEquals(dto1.hashCode(), dto2.hashCode());
    }

    @Test
    void testToString() {
        xrefDto.setXrefId(testXrefId);
        xrefDto.setSchmId(testSchmId);
        xrefDto.setExtRefId(testExtRefId);
        xrefDto.setSchmName("Test Schema");
        xrefDto.setExtRefName("Test Ref");

        String result = xrefDto.toString();
        assertNotNull(result);
        assertTrue(result.contains("SchemaExtRefXrefDto"));
    }
}
