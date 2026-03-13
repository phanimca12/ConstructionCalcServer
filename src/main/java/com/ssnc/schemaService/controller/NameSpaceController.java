package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.dto.NameSpaceDto;
import com.ssnc.schemaService.entity.Nmspc;
import com.ssnc.schemaService.service.NameSpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/namespaces")
public class NameSpaceController {
    @Autowired
    NameSpaceService nameSpaceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NameSpaceDto createNamespace(@RequestBody NameSpaceDto request) {
        return nameSpaceService.createNameSpace(request);
    }

    @GetMapping("/{namespace}")
    public NameSpaceDto getNameSpaceByName(@PathVariable String namespace) {
        return nameSpaceService.getNameSpaceByName(namespace);
    }

    @GetMapping
    public List<NameSpaceDto> getAllNameSpaces() {
        return nameSpaceService.getAllNameSpaces();
    }
}
