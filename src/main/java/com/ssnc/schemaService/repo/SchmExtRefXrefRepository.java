package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.ExtRefType;
import com.ssnc.schemaService.entity.SchmExtRefXref;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Repository
public interface SchmExtRefXrefRepository extends JpaRepository<SchmExtRefXref, UUID> {

    /**
     * Find cross-references by schema ID
     */
    List<SchmExtRefXref> findBySchmId(UUID schmId);

    /**
     * Find cross-references by external reference ID (all versions)
     */
    List<SchmExtRefXref> findByExtRefId(String extRefId);

    /**
     * Find cross-references by external reference ID and version (specific version)
     * Uses direct fields instead of navigating through the extRef relationship to avoid composite key issues.
     * Note: extRefType is not needed since (extRefId, extRefVersion) uniquely identifies an ExtRef.
     */
    List<SchmExtRefXref> findByExtRefIdAndExtRefVersion(String extRefId, String extRefVersion);

    /**
     * Check if cross-reference exists for specific version
     */
    boolean existsBySchmIdAndExtRefIdAndExtRefVersion(UUID schmId, String extRefId, String extRefVersion);

    /**
     * Delete cross-reference by schema ID and external reference ID and version
     */
    @Modifying
    @Transactional
    void deleteBySchmIdAndExtRefIdAndExtRefVersion(UUID schmId, String extRefId, String extRefVersion);
}
