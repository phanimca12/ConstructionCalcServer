package com.ssnc.schemaService.controller;

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
    public Nmspc createNamespace(@RequestBody Nmspc request) {
        return nameSpaceService.createNameSpace(request);
    }

    @GetMapping("/{namespace}")
    public Nmspc getNameSpaceByName(@PathVariable String nameSpace) {
        return nameSpaceService.getNameSpaceByName(nameSpace);
    }

    @GetMapping
    public List<Nmspc> getAllNameSpaces() {
        return nameSpaceService.getAllNameSpaces();
    }
}
