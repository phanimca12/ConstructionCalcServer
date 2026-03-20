package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.SchmExtRefXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchmExtRefXrefRepository extends JpaRepository<SchmExtRefXref, UUID> {

    /**
     * Find all cross-references by schema ID
     */
    List<SchmExtRefXref> findBySchmId(UUID schmId);

    /**
     * Find all cross-references by external reference ID
     */
    List<SchmExtRefXref> findByExtRefId(UUID extRefId);

    /**
     * Find all cross-references by external reference type
     */
    List<SchmExtRefXref> findByExtRefType(String extRefType);

    /**
     * Find all cross-references by external reference name and type
     */
    List<SchmExtRefXref> findByExtRefNameAndExtRefType(String extRefName, String extRefType);

    /**
     * Delete all cross-references by external reference ID and type
     */
    void deleteByExtRefNameAndExtRefType(String extRefName, String extRefType);
}
