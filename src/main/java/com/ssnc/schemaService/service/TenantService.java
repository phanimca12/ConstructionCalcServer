package com.ssnc.schemaService.service;

import com.ssnc.schemaService.dto.TenantDto;
import com.ssnc.schemaService.entity.Tenant;
import com.ssnc.schemaService.repo.TenantRepository;
import com.ssnc.shared.security.JwtClaimsContext;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    @Autowired
    TenantRepository tenantRepository;
    JwtClaimsContext jwtClaimsContext;

    @Transactional
    public List<Tenant> createTenantsFromContext() throws Exception {
        if (jwtClaimsContext != null && !jwtClaimsContext.isPopulated()) {
            String error = "JWT claims context is not populated. Cannot create tenants.";
            logger.error(error);
            throw new IllegalStateException(error);
        }

        String tenantName = jwtClaimsContext.getTenant();

        if (tenantName == null || tenantName.isEmpty()) {
            logger.debug("No tenant found in JWT context");
            return new ArrayList<>();
        }

        // Check if tenant already exists
        Optional<Tenant> existingTenant = tenantRepository.findByTenantName(tenantName);
        if (existingTenant.isPresent()) {
            logger.debug("Tenant already onboarded: {}", tenantName);
            return new ArrayList<>();
        }

        // Create new tenant
        String userName = jwtClaimsContext.getUserId() != null ? jwtClaimsContext.getUserId() : "system";

        logger.debug("Preparing to create new tenant: {}", tenantName);

        Tenant newTenant = new Tenant();
        newTenant.setTenantName(tenantName);
        newTenant.setCreatedBy(userName);
        newTenant.setUpdatedBy(userName);

        Tenant savedTenant = tenantRepository.save(newTenant);
        logger.info("Successfully created tenant: {}", tenantName);

        return List.of(savedTenant);
    }

    public TenantDto createTenant(TenantDto tenantDto) {
        Tenant tenant = new Tenant();
        tenant.setTenantName(tenantDto.getName());
        tenant.setCreatedBy(tenantDto.getCreatedByUser());
        tenant.setUpdatedBy(tenantDto.getModifiedByUser());

        Tenant savedTenant = tenantRepository.save(tenant);

        return mapToTenantDto(savedTenant);
    }

    public TenantDto getTenantByName(String name) {
        Tenant tenant = tenantRepository.findByTenantName(name)
                .orElseThrow(() -> new EntityNotFoundException("Tenant not found"));
        return mapToTenantDto(tenant);
    }

    public List<TenantDto> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(this::mapToTenantDto)
                .toList();
    }

    private TenantDto mapToTenantDto(Tenant tenant) {
        TenantDto dto = new TenantDto();
        dto.setName(tenant.getTenantName());
        dto.setCreatedByUser(tenant.getCreatedBy());
        dto.setCreateDateTime(tenant.getCreatedDatetime());
        dto.setModifiedByUser(tenant.getUpdatedBy());
        dto.setModifiedDateTime(tenant.getUpdatedDatetime());
        return dto;
    }
}
