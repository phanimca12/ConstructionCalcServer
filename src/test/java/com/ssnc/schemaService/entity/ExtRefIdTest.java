package com.ssnc.schemaService.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtRefIdTest {

    @Test
    void testDefaultConstructor() {
        ExtRefId id = new ExtRefId();

        assertNull(id.getExtRefId());
        assertNull(id.getExtRefVersion());
    }

    @Test
    void testParameterizedConstructor() {
        ExtRefId id = new ExtRefId("ref123", "v1.0");

        assertEquals("ref123", id.getExtRefId());
        assertEquals("v1.0", id.getExtRefVersion());
    }

    @Test
    void testSettersAndGetters() {
        ExtRefId id = new ExtRefId();

        id.setExtRefId("ref456");
        assertEquals("ref456", id.getExtRefId());

        id.setExtRefVersion("v2.0");
        assertEquals("v2.0", id.getExtRefVersion());
    }

    @Test
    void testEquals_SameObject() {
        ExtRefId id = new ExtRefId("ref123", "v1.0");
        assertEquals(id, id);
    }

    @Test
    void testEquals_EqualObjects() {
        ExtRefId id1 = new ExtRefId("ref123", "v1.0");
        ExtRefId id2 = new ExtRefId("ref123", "v1.0");

        assertEquals(id1, id2);
    }

    @Test
    void testEquals_DifferentExtRefId() {
        ExtRefId id1 = new ExtRefId("ref123", "v1.0");
        ExtRefId id2 = new ExtRefId("ref456", "v1.0");

        assertNotEquals(id1, id2);
    }

    @Test
    void testEquals_DifferentVersion() {
        ExtRefId id1 = new ExtRefId("ref123", "v1.0");
        ExtRefId id2 = new ExtRefId("ref123", "v2.0");

        assertNotEquals(id1, id2);
    }

    @Test
    void testEquals_NullObject() {
        ExtRefId id = new ExtRefId("ref123", "v1.0");
        assertNotEquals(id, null);
    }

    @Test
    void testEquals_DifferentClass() {
        ExtRefId id = new ExtRefId("ref123", "v1.0");
        String other = "different";
        assertNotEquals(id, other);
    }

    @Test
    void testHashCode_EqualObjects() {
        ExtRefId id1 = new ExtRefId("ref123", "v1.0");
        ExtRefId id2 = new ExtRefId("ref123", "v1.0");

        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void testHashCode_Consistency() {
        ExtRefId id = new ExtRefId("ref123", "v1.0");
        int hashCode1 = id.hashCode();
        int hashCode2 = id.hashCode();

        assertEquals(hashCode1, hashCode2);
    }

    @Test
    void testNullFields() {
        ExtRefId id1 = new ExtRefId(null, null);
        ExtRefId id2 = new ExtRefId(null, null);

        assertEquals(id1, id2);
        assertEquals(id1.hashCode(), id2.hashCode());
    }

    @Test
    void testMixedNullFields() {
        ExtRefId id1 = new ExtRefId("ref123", null);
        ExtRefId id2 = new ExtRefId(null, "v1.0");
        ExtRefId id3 = new ExtRefId("ref123", null);

        assertNotEquals(id1, id2);
        assertEquals(id1, id3);
    }

    @Test
    void testToString() {
        ExtRefId id = new ExtRefId("ref123", "v1.0");
        String toString = id.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("ref123"));
        assertTrue(toString.contains("v1.0"));
    }

    @Test
    void testSerializable() {
        // Verify that ExtRefId implements Serializable
        ExtRefId id = new ExtRefId("ref123", "v1.0");
        assertInstanceOf(java.io.Serializable.class, id);
    }
}
