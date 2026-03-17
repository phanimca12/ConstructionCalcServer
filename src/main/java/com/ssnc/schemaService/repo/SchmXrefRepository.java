package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.SchmXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchmXrefRepository extends JpaRepository<SchmXref, UUID> {

    /**
     * Find all cross-references by schema ID
     */
    List<SchmXref> findBySchmId(UUID schmId);

    /**
     * Find cross-references by schema ID and reference type
     */
    List<SchmXref> findBySchmIdAndRefType(UUID schmId, String refType);

    /**
     * Find cross-references by reference GUID
     */
    List<SchmXref> findByRefGuid(UUID refGuid);
}