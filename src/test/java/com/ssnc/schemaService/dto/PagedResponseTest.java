package com.ssnc.schemaService.dto;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PagedResponseTest {

    @Test
    void testNoArgsConstructor() {
        PagedResponse<String> response = new PagedResponse<>();
        assertNotNull(response);
    }

    @Test
    void testAllArgsConstructor() {
        List<String> content = Arrays.asList("item1", "item2");
        PagedResponse<String> response = new PagedResponse<>(
                content, 100L, 10, 0, 10, true, false);

        assertEquals(content, response.getSchemas());
        assertEquals(100L, response.getTotalElements());
        assertEquals(10, response.getTotalPages());
        assertEquals(0, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
        assertTrue(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    void testSettersAndGetters() {
        PagedResponse<String> response = new PagedResponse<>();
        List<String> content = Arrays.asList("test");

        response.setSchemas(content);
        response.setTotalElements(50L);
        response.setTotalPages(5);
        response.setCurrentPage(2);
        response.setPageSize(10);
        response.setHasNext(true);
        response.setHasPrevious(true);

        assertEquals(content, response.getSchemas());
        assertEquals(50L, response.getTotalElements());
        assertEquals(5, response.getTotalPages());
        assertEquals(2, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
        assertTrue(response.isHasNext());
        assertTrue(response.isHasPrevious());
    }
}
