package com.ssnc.schemaService.repo;

import com.ssnc.schemaService.entity.Nmspc;
import com.ssnc.schemaService.entity.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TenantRepository extends JpaRepository<Tenant, String> {
    Optional<Tenant> findByTenantName(String tenantName);
}
