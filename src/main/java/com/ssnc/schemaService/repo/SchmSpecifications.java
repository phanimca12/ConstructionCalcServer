package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmData;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class SchmSpecifications {

    private SchmSpecifications() {}

    public static Specification<Schm> withFilters(SchmFilterCriteria criteria) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (criteria.getSchmId() != null) {
                predicates.add(cb.equal(root.get("schmId"), criteria.getSchmId()));
            }

            if (criteria.getName() != null) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("schmName")),
                                "%" + criteria.getName().toLowerCase() + "%"
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

            if (criteria.getGroup() != null) {
                predicates.add(
                        cb.equal(
                                cb.lower(root.get("group")),
                                criteria.getGroup().toLowerCase()
                        )
                );
            }

            if (criteria.getModifiedByUser() != null) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("updatedBy")),
                                "%" + criteria.getModifiedByUser().toLowerCase() + "%"
                        )
                );
            }

            if (criteria.getVersionModifiedByUser() != null) {
                // Join with SchmData to filter by version modified user
                Join<Schm, SchmData> schmDataJoin = root.join("versions");
                predicates.add(
                        cb.like(
                                cb.lower(schmDataJoin.get("updatedBy")),
                                "%" + criteria.getVersionModifiedByUser().toLowerCase() + "%"
                        )
                );
                // Make query distinct to avoid duplicates from join
                query.distinct(true);
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
