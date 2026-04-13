package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.ExtRef;
import com.ssnc.schemaService.entity.ExtRefId;
import com.ssnc.schemaService.entity.ExtRefType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ExtRefRepository extends JpaRepository<ExtRef, ExtRefId> {

    /**
     * SECURITY: Find external reference by tenant, name, type, and version.
     * CRITICAL: Must filter by tenantId to prevent cross-tenant data leakage.
     * Used to check unique constraint (tenant_id, ext_ref_name, ext_ref_type, ext_ref_version).
     * Note: Uses id_extRefVersion to access composite key field.
     */
    Optional<ExtRef> findByTenantIdAndExtRefNameAndExtRefTypeAndId_ExtRefVersion(
            UUID tenantId, String extRefName, ExtRefType extRefType, String extRefVersion);

    /**
     * Find all external references with the same ID (across all versions)
     */
    List<ExtRef> findByIdExtRefId(String extRefId);

    /**
     * Find external references by type
     */
    List<ExtRef> findByExtRefType(ExtRefType extRefType);
}
