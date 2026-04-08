package com.ssnc.schemaService.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.TenantDto;
import com.ssnc.schemaService.entity.Tenant;
import com.ssnc.schemaService.repo.TenantRepository;
import com.ssnc.shared.security.JwtClaimsContext;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    // Cache tenant existence to avoid DB queries on every request
    // TTL of 10 minutes balances freshness with performance
    private final Cache<String, Boolean> tenantExistsCache = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    @Autowired
    TenantRepository tenantRepository;
    @Autowired
    JwtClaimsContext jwtClaimsContext;

    /**
     * Ensures a tenant exists, creating it if necessary.
     * Used by filters to auto-create tenants from JWT context.
     * Includes caching and race condition handling.
     *
     * @param tenantName - The tenant name to ensure exists
     */
    public void ensureTenantExists(String tenantName) {
        // Check cache first to avoid DB query on every request
        Boolean cached = tenantExistsCache.getIfPresent(tenantName);
        if (Boolean.TRUE.equals(cached)) {
            return;
        }

        // Cache miss - check DB and create if needed
        try {
            Optional<Tenant> existingTenant = tenantRepository.findByTenantName(tenantName);

            if (existingTenant.isEmpty()) {
                createTenantInternal(tenantName);
            }

            // Cache the tenant existence (only after successful DB operation)
            tenantExistsCache.put(tenantName, true);

        } catch (DataIntegrityViolationException e) {
            // Another thread created it concurrently; cache and continue
            logger.debug("Tenant already exists (concurrent creation): {}", tenantName);
            tenantExistsCache.put(tenantName, true);
        } catch (DataAccessException e) {
            // DB connectivity or other data access issues
            logger.error("Failed to ensure tenant '{}' exists: {}", tenantName, e.getMessage(), e);
            throw new IllegalStateException("Tenant operation failed: " + e.getMessage(), e);
        }
    }

    private Tenant createTenantInternal(String tenantName) {
        String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

        logger.debug(ErrorMessages.TENANT_ONBOARDING_PREPARING, tenantName);

        Tenant newTenant = new Tenant();
        newTenant.setTenantName(tenantName);
        newTenant.setCreatedBy(userName);
        newTenant.setUpdatedBy(userName);

        try {
            Tenant savedTenant = tenantRepository.save(newTenant);
            logger.info(ErrorMessages.TENANT_ONBOARDING_SUCCESS, tenantName);
            return savedTenant;
        } catch (DataAccessException e) {
            logger.error("Failed to create tenant '{}': {}", tenantName, e.getMessage(), e);
            throw new IllegalStateException("Tenant creation failed: " + e.getMessage(), e);
        }
    }

    public TenantDto getTenantByName(String name) {
        Tenant tenant = tenantRepository.findByTenantName(name)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.TENANT_NOT_FOUND));
        return mapToTenantDto(tenant);
    }

    public List<TenantDto> getAllTenants() {
        return tenantRepository.findAll().stream()
                .map(this::mapToTenantDto)
                .toList();
    }

    private TenantDto mapToTenantDto(Tenant tenant) {
        TenantDto dto = new TenantDto();
        dto.setTenantId(tenant.getTenantId());
        dto.setName(tenant.getTenantName());
        dto.setCreatedByUser(tenant.getCreatedBy());
        dto.setCreateDateTime(tenant.getCreatedDatetime());
        dto.setModifiedByUser(tenant.getUpdatedBy());
        dto.setModifiedDateTime(tenant.getUpdatedDatetime());
        return dto;
    }
}
