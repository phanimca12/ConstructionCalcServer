package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.ExtRef;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExtRefRepository extends JpaRepository<ExtRef, String> {

    /**
     * SECURITY: Find external reference by tenant, name, type, and version.
     * CRITICAL: Must filter by tenantId to prevent cross-tenant data leakage.
     * Used to check unique constraint (tenant_id, ext_ref_name, ext_ref_type, ext_ref_version).
     */
    Optional<ExtRef> findByTenantIdAndExtRefNameAndExtRefTypeAndExtRefVersion(
            UUID tenantId, String extRefName, String extRefType, String extRefVersion);

    /**
     * Find external reference by ID
     */
    Optional<ExtRef> findByExtRefId(String extRefId);

    /**
     * Find external references by type
     */
    List<ExtRef> findByExtRefType(String extRefType);
}
