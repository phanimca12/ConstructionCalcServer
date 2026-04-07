package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.Nmspc;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface NameSpaceRepository extends JpaRepository<Nmspc, UUID> {
    Optional<Nmspc> findByNmspcName(String nameSpace);
    Optional<Nmspc> findByNmspcId(UUID nmspcId);
    Optional<Nmspc> findByTenantIdAndNmspcName(UUID tenantId, String nameSpace);
}
