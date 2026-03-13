package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.Schm;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class SchmSpecifications {

    private SchmSpecifications() {}

    public static Specification<Schm> withFilters(SchmFilterCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getPublishedOnly() != null && criteria.getPublishedOnly()) {
                predicates.add(cb.isNotNull(root.get("publishVersion")));
                predicates.add(cb.gt(root.get("publishVersion"), 0));
            }

            if (criteria.getSchmId() != null) {
                predicates.add(cb.equal(root.get("schmId"), criteria.getSchmId()));
            }

            if (criteria.getSchemaName() != null) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("schmName")),
                                "%" + criteria.getSchemaName().toLowerCase() + "%"
                        )
                );
            }

            if (criteria.getSchemaType() != null) {
                predicates.add(
                        cb.equal(
                                cb.lower(root.get("schemaType")),
                                criteria.getSchemaType().toLowerCase()
                        )
                );
            }

            if (criteria.getContentType() != null) {
                predicates.add(
                        cb.equal(
                                cb.lower(root.get("contentType")),
                                criteria.getContentType().toLowerCase()
                        )
                );
            }

            if (criteria.getGroup() != null) {
                predicates.add(
                        cb.equal(
                                cb.lower(root.get("group")),
                                criteria.getGroup().toLowerCase()
                        )
                );
            }

            if (criteria.getLockBy() != null) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("lockBy")),
                                "%" + criteria.getLockBy().toLowerCase() + "%"
                        )
                );
            }

            if (criteria.getPublishVersion() != null) {
                predicates.add(cb.equal(root.get("publishVersion"), criteria.getPublishVersion()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
