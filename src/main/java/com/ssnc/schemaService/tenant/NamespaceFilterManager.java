package com.ssnc.schemaService.tenant;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Component
public class NamespaceFilterManager {

    @PersistenceContext
    private EntityManager entityManager;

    public void enableIfPresent(String ns) {
        if (ns != null) {
            entityManager.unwrap(Session.class)
                         .enableFilter("namespaceFilter")
                         .setParameter("namespace", ns);
        }
    }
}
