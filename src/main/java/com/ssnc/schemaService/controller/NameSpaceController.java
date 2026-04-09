package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.constants.ApiConstants;
import com.ssnc.schemaService.dto.NameSpaceDto;
import com.ssnc.schemaService.service.NameSpaceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApiConstants.PATH_NAMESPACES_BASE)
public class NameSpaceController {
    @Autowired
    NameSpaceService nameSpaceService;

    @GetMapping(ApiConstants.PATH_NAMESPACE_BY_NAME)
    public NameSpaceDto getNameSpaceByName(@PathVariable(ApiConstants.PARAM_NAMESPACE) String namespace) {
        return nameSpaceService.getNameSpaceByName(namespace);
    }

    @GetMapping
    public List<NameSpaceDto> getAllNameSpaces() {
        return nameSpaceService.getAllNameSpaces();
    }
}
