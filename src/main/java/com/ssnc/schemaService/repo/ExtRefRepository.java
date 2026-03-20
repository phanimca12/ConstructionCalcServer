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
     * Find external reference by name and type
     */
    Optional<ExtRef> findByExtRefNameAndExtRefType(String extRefName, String extRefType);

    /**
     * Find all external references by type
     */
    List<ExtRef> findByExtRefType(String extRefType);

    /**
     * Find external reference by name
     */
    List<ExtRef> findByExtRefName(String extRefName);
}
