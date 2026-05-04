package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmData;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SchmRepository extends JpaRepository<Schm, UUID>, JpaSpecificationExecutor<Schm> {

    /**
     * Get published schema version
     */
    @Query("""
        SELECT d
        FROM Schm s
        JOIN SchmData d ON d.id.schmId = s.schmId
         AND d.id.schmVersion = s.publishVersion
        WHERE s.schmId = :schmId
          AND s.publishVersion IS NOT NULL
    """)
    Optional<SchmData> getPublishedVersion(@Param("schmId") UUID schmId);

    /**
     * Get specific schema version
     */
    @Query("""
        SELECT d
        FROM SchmData d
        WHERE d.id.schmId = :schmId
          AND d.id.schmVersion = :versionNumber
    """)
    Optional<SchmData> getSchemaVersion(@Param("schmId") UUID schmId, @Param("versionNumber") Integer versionNumber);

    /**
     * Get latest version
     */
    @Query("""
        SELECT d
        FROM SchmData d
        WHERE d.id.schmId = :schmId
          AND d.id.schmVersion = (
              SELECT MAX(d2.id.schmVersion)
              FROM SchmData d2
              WHERE d2.id.schmId = :schmId
          )
    """)
    Optional<SchmData> getLatestVersion(@Param("schmId") UUID schmId);

    /**
     * Get all versions for a schema
     */
    @Query("""
        SELECT d
        FROM SchmData d
        WHERE d.id.schmId = :schmId
        ORDER BY d.id.schmVersion DESC
    """)
    List<SchmData> getAllVersions(@Param("schmId") UUID schmId);

    /**
     * Find schemas by type and group
     */
    List<Schm> findBySchemaTypeAndSchmGroup(String schemaType, String schmGroup);

    /**
     * Find schemas by type
     */
    List<Schm> findBySchemaType(String schemaType);

    /**
     * Find schemas by group
     */
    List<Schm> findBySchmGroup(String schmGroup);

    /**
     * Get schema by ID
     */
    Optional<Schm> findBySchmId(UUID schmId);

    /**
     * Get schema by ID with pessimistic write lock to prevent race conditions.
     * Use this when you need to check-then-modify a schema atomically.
     * Spring Data JPA will derive the query and apply the lock.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Schm> findWithLockBySchmId(UUID schmId);

    /**
     * Find schema by exact name match (case-sensitive).
     * Spring Data JPA method - performs exact match based on database collation.
     */
    Optional<Schm> findBySchmName(String name);

    /**
     * Find schema by tenant, namespace, and name.
     * SECURITY: Ensures proper multi-tenant isolation by scoping lookup to tenant and namespace.
     */
    Optional<Schm> findByTenantIdAndNmspcIdAndSchmName(UUID tenantId, UUID nmspcId, String schmName);
}
