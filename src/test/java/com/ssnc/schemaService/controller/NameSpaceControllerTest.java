package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.dto.NameSpaceDto;
import com.ssnc.schemaService.service.NameSpaceService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NameSpaceControllerTest {

    @Mock
    private NameSpaceService nameSpaceService;

    @InjectMocks
    private NameSpaceController nameSpaceController;

    private NameSpaceDto testNameSpaceDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testNameSpaceDto = new NameSpaceDto();
        testNameSpaceDto.setId("testNamespace");
        testNameSpaceDto.setName("testNamespace");
        testNameSpaceDto.setDescription("Test Namespace Description");
        testNameSpaceDto.setCreatedByUser("testUser");
        testNameSpaceDto.setCreateDateTime(LocalDateTime.now());
        testNameSpaceDto.setModifiedByUser("testUser");
        testNameSpaceDto.setModifiedDateTime(LocalDateTime.now());
    }

    @Test
    void testCreateNamespace_Success() {
        when(nameSpaceService.createNameSpace(any(NameSpaceDto.class)))
                .thenReturn(testNameSpaceDto);

        NameSpaceDto result = nameSpaceController.createNamespace(testNameSpaceDto);

        assertNotNull(result);
        assertEquals("testNamespace", result.getName());
        assertEquals("Test Namespace Description", result.getDescription());
        verify(nameSpaceService, times(1)).createNameSpace(testNameSpaceDto);
    }

    @Test
    void testCreateNamespace_WithNullDescription() {
        NameSpaceDto dtoWithoutDesc = new NameSpaceDto();
        dtoWithoutDesc.setName("namespace2");
        dtoWithoutDesc.setDescription(null);

        when(nameSpaceService.createNameSpace(any(NameSpaceDto.class)))
                .thenReturn(dtoWithoutDesc);

        NameSpaceDto result = nameSpaceController.createNamespace(dtoWithoutDesc);

        assertNotNull(result);
        assertEquals("namespace2", result.getName());
        assertNull(result.getDescription());
        verify(nameSpaceService).createNameSpace(dtoWithoutDesc);
    }

    @Test
    void testGetNameSpaceByName_Success() {
        when(nameSpaceService.getNameSpaceByName("testNamespace"))
                .thenReturn(testNameSpaceDto);

        NameSpaceDto result = nameSpaceController.getNameSpaceByName("testNamespace");

        assertNotNull(result);
        assertEquals("testNamespace", result.getName());
        assertEquals("Test Namespace Description", result.getDescription());
        verify(nameSpaceService).getNameSpaceByName("testNamespace");
    }

    @Test
    void testGetNameSpaceByName_NotFound() {
        when(nameSpaceService.getNameSpaceByName("nonExistent"))
                .thenThrow(new EntityNotFoundException("Namespace not found"));

        assertThrows(EntityNotFoundException.class, () -> {
            nameSpaceController.getNameSpaceByName("nonExistent");
        });

        verify(nameSpaceService).getNameSpaceByName("nonExistent");
    }

    @Test
    void testGetAllNameSpaces_Success() {
        NameSpaceDto namespace2 = new NameSpaceDto();
        namespace2.setId("namespace2");
        namespace2.setName("namespace2");
        namespace2.setDescription("Second namespace");

        List<NameSpaceDto> namespaceList = Arrays.asList(testNameSpaceDto, namespace2);

        when(nameSpaceService.getAllNameSpaces()).thenReturn(namespaceList);

        List<NameSpaceDto> result = nameSpaceController.getAllNameSpaces();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("testNamespace", result.get(0).getName());
        assertEquals("namespace2", result.get(1).getName());
        verify(nameSpaceService).getAllNameSpaces();
    }

    @Test
    void testGetAllNameSpaces_EmptyList() {
        when(nameSpaceService.getAllNameSpaces()).thenReturn(Collections.emptyList());

        List<NameSpaceDto> result = nameSpaceController.getAllNameSpaces();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(nameSpaceService).getAllNameSpaces();
    }

    @Test
    void testGetAllNameSpaces_SingleItem() {
        when(nameSpaceService.getAllNameSpaces())
                .thenReturn(Collections.singletonList(testNameSpaceDto));

        List<NameSpaceDto> result = nameSpaceController.getAllNameSpaces();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("testNamespace", result.get(0).getName());
        verify(nameSpaceService).getAllNameSpaces();
    }
}
