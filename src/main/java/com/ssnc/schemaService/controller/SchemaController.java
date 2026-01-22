package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmData;
import com.ssnc.schemaService.repo.SchmFilterCriteria;
import com.ssnc.schemaService.service.SchemaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/schemas/{namespace}")
public class SchemaController {

    @Autowired
    SchemaService schemaService;

    @GetMapping()
    public List<Schm> getSchemas(@PathVariable("namespace") String namespace,
                                 @RequestParam(required = false) String id,
                                 @RequestParam(required = false) String name,
                                 @RequestParam(required = false) String lockBy,
                                 @RequestParam(required = false) Integer publishVersion){
        SchmFilterCriteria criteria = new SchmFilterCriteria();
        if(id != null)
            criteria.setSchmId(UUID.fromString(id));
        criteria.setSchemaName(name);
        criteria.setLockBy(lockBy);
        criteria.setPublishVersion(publishVersion);
        return schemaService.getSchemas(namespace, criteria);
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public Schm createSchema(@PathVariable("namespace") String namespace,
                             @RequestBody Schm request) {
        request.setNamespace(namespace);
        return schemaService.createOrUpdateSchema(request);
    }

    @GetMapping("/{id}")
    public Schm getSchemaById(@PathVariable("namespace") String namespace,
                                    @PathVariable("id") String id){
        return schemaService.getSchemaById(namespace, id);
    }

    @PostMapping("/{id}")
    @ResponseStatus(HttpStatus.CREATED)
    public Schm updateSchema(@PathVariable("namespace") String namespace,
                             @PathVariable("id") String id,
                             @RequestBody Schm request) {
        request.setNamespace(namespace);
        request.setSchmId(UUID.fromString(id));
        return schemaService.createOrUpdateSchema(request);
    }

    @PostMapping("/{id}/version")
    @ResponseStatus(HttpStatus.CREATED)
    public SchmData createOrUpdateSchemaData(@PathVariable("namespace") String namespace,
                                     @PathVariable("id") String id,
                                     @RequestBody SchmData request) {
        request.getId().setSchmId(UUID.fromString(id));
        return schemaService.createOrUpdateSchemaData(request);
    }

    @GetMapping("/{id}/version/publish")
    public List<SchmData> getPublishedSchemaData(@PathVariable("namespace") String namespace,
                                             @PathVariable("id") String id){
        return schemaService.getPublishedSchema(namespace, id);
    }
}
