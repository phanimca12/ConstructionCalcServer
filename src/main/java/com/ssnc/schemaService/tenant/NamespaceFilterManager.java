package com.ssnc.schemaService.tenant;

import com.ssnc.schemaService.entity.Nmspc;
import com.ssnc.schemaService.repo.NameSpaceRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class NamespaceFilterManager {

    @PersistenceContext
    private EntityManager entityManager;

    private final NameSpaceRepository nameSpaceRepository;

    @Autowired
    public NamespaceFilterManager(@Lazy NameSpaceRepository nameSpaceRepository) {
        this.nameSpaceRepository = nameSpaceRepository;
    }

    public void enableIfPresent(String ns) {
        if (ns != null) {
            // Resolve namespace name to UUID
            UUID nmspcId = nameSpaceRepository.findByNmspcName(ns)
                    .map(Nmspc::getNmspcId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("Namespace not found: %s", ns)));

            // Enable filter with UUID directly
            entityManager.unwrap(Session.class)
                         .enableFilter("namespaceFilter")
                         .setParameter("namespaceId", nmspcId);
        }
    }
}
