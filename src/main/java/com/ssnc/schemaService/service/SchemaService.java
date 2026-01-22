package com.ssnc.schemaService.service;

import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmData;
import com.ssnc.schemaService.repo.SchmDataRepository;
import com.ssnc.schemaService.repo.SchmFilterCriteria;
import com.ssnc.schemaService.repo.SchmRepository;
import com.ssnc.schemaService.repo.SchmSpecifications;
import com.ssnc.schemaService.tenant.NamespaceFilterManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class SchemaService {

    @Autowired
    SchmRepository schmRepository;

    @Autowired
    SchmDataRepository schmDataRepository;

    @Autowired
    NamespaceFilterManager namespaceFilterManager;

    public Schm createOrUpdateSchema(Schm request) {
       return schmRepository.save(request);
    }

    public SchmData createOrUpdateSchemaData(SchmData request) {
        if(request.getId().getSchmVersion() == null) {
            Integer latestVersion = schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc
                    (request.getId().getSchmId()).map(sd -> sd.getId().getSchmVersion())
                    .orElse(0);
            request.getId().setSchmVersion(latestVersion + 1);
        }
        return schmDataRepository.save(request);
    }

    public List<Schm> getSchemas(String namespace, SchmFilterCriteria criteria) {
        namespaceFilterManager.enableIfPresent(namespace);
        return schmRepository.findAll(SchmSpecifications.withFilters(criteria));
    }

    public List<SchmData> getPublishedSchema(String namespace, String id) {
        namespaceFilterManager.enableIfPresent(namespace);
        return schmRepository.getPublishedSchema(UUID.fromString(id));
    }

    public Schm getSchemaById(String namespace, String id) {
        return schmRepository.getByschmId(UUID.fromString(id));
    }
}
