package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.SchmXref;
import com.ssnc.schemaService.entity.XRefType;
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
    List<SchmXref> findBySchmIdAndRefType(UUID schmId, XRefType refType);

    /**
     * Find cross-references by reference GUID
     */
    List<SchmXref> findByRefGuid(UUID refGuid);

    /**
     * Find cross-references by namespace and reference type
     */
    List<SchmXref> findByNmspNameAndRefType(String nmspName, XRefType refType);

    /**
     * Find cross-references by namespace
     */
    List<SchmXref> findByNmspName(String nmspName);

    /**
     * Find cross-references by namespace, reference type, and reference name
     */
    List<SchmXref> findByNmspNameAndRefTypeAndRefName(String nmspName, XRefType refType, String refName);

    /**
     * Find cross-references by namespace, reference type, reference name, and version
     */
    List<SchmXref> findByNmspNameAndRefTypeAndRefNameAndRefVersion(String nmspName, XRefType refType, String refName, String refVersion);
}