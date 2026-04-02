package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.ExtRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExtRefRepository extends JpaRepository<ExtRef, UUID> {

    /**
     * Find external reference by name, type, and version
     */
    Optional<ExtRef> findByExtRefNameAndExtRefTypeAndExtRefVersion(
            String extRefName, String extRefType, String extRefVersion);

    /**
     * Find external reference by ID
     */
    Optional<ExtRef> findByExtRefId(UUID extRefId);

    /**
     * Find external references by type
     */
    List<ExtRef> findByExtRefType(String extRefType);
}
