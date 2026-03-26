package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.SchmExtRefXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchmExtRefXrefRepository extends JpaRepository<SchmExtRefXref, UUID> {

    /**
     * Find cross-references by schema ID
     */
    List<SchmExtRefXref> findBySchmId(UUID schmId);

    /**
     * Find cross-references by external reference ID
     */
    List<SchmExtRefXref> findByExtRefId(UUID extRefId);

    /**
     * Find schemas by external reference details
     * Uses JPA method naming to navigate through the extRef relationship
     */
    List<SchmExtRefXref> findByExtRefExtRefTypeAndExtRefExtRefIdAndExtRefExtRefVersion(
            String extRefType, UUID extRefId, String extRefVersion);

    /**
     * Check if cross-reference exists
     */
    boolean existsBySchmIdAndExtRefId(UUID schmId, UUID extRefId);

    /**
     * Delete cross-reference by schema ID and external reference ID
     */
    void deleteBySchmIdAndExtRefId(UUID schmId, UUID extRefId);
}
