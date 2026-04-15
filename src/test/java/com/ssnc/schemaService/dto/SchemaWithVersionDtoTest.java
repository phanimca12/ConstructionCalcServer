package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SchemaWithVersionDtoTest {

    @Test
    void testSettersAndGetters() {
        SchemaWithVersionDto dto = new SchemaWithVersionDto();

        SchemaDto schema = new SchemaDto();
        schema.setName("TestSchema");
        dto.setSchema(schema);
        assertEquals(schema, dto.getSchema());

        List<SchemaVersionDto> versions = new ArrayList<>();
        SchemaVersionDto version = new SchemaVersionDto();
        version.setVersionNumber(1);
        versions.add(version);

        dto.setVersions(versions);
        assertEquals(versions, dto.getVersions());
        assertEquals(1, dto.getVersions().size());
        assertEquals(1, dto.getVersions().get(0).getVersionNumber());
    }

    @Test
    void testNullValues() {
        SchemaWithVersionDto dto = new SchemaWithVersionDto();

        assertNull(dto.getSchema());
        assertNull(dto.getVersions());
    }

    @Test
    void testEmptyVersionsList() {
        SchemaWithVersionDto dto = new SchemaWithVersionDto();

        dto.setVersions(new ArrayList<>());

        assertNotNull(dto.getVersions());
        assertTrue(dto.getVersions().isEmpty());
    }

    @Test
    void testMultipleVersions() {
        SchemaWithVersionDto dto = new SchemaWithVersionDto();

        List<SchemaVersionDto> versions = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            SchemaVersionDto version = new SchemaVersionDto();
            version.setVersionNumber(i);
            versions.add(version);
        }

        dto.setVersions(versions);

        assertEquals(3, dto.getVersions().size());
        assertEquals(1, dto.getVersions().get(0).getVersionNumber());
        assertEquals(3, dto.getVersions().get(2).getVersionNumber());
    }
}
