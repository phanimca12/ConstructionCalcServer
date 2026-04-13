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
     * Find cross-references by external reference ID
     */
    List<SchmExtRefXref> findByExtRefId(String extRefId);

    /**
     * Find schemas by external reference details
     * Uses JPA method naming to navigate through the extRef relationship
     */
    List<SchmExtRefXref> findByExtRefExtRefTypeAndExtRefExtRefIdAndExtRefExtRefVersion(
            ExtRefType extRefType, String extRefId, String extRefVersion);

    /**
     * Check if cross-reference exists
     */
    boolean existsBySchmIdAndExtRefId(UUID schmId, String extRefId);

    /**
     * Delete cross-reference by schema ID and external reference ID
     */
    @Modifying
    @Transactional
    void deleteBySchmIdAndExtRefId(UUID schmId, String extRefId);
}
