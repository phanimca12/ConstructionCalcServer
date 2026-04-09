package com.ssnc.schemaService.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.TenantDto;
import com.ssnc.schemaService.entity.Tenant;
import com.ssnc.schemaService.repo.TenantRepository;
import com.ssnc.schemaService.util.DatabaseExceptionUtils;
import com.ssnc.shared.security.JwtClaimsContext;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class TenantService {

    private static final Logger logger = LoggerFactory.getLogger(TenantService.class);

    // Cache tenant existence to avoid DB queries on every request
    // TTL of 10 minutes balances freshness with performance
    private final Cache<String, Boolean> tenantExistsCache = Caffeine.newBuilder()
            .expireAfterWrite(AppConstants.CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES)
            .maximumSize(AppConstants.CACHE_MAX_SIZE)
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
    @Transactional(isolation = Isolation.READ_COMMITTED)
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

            // Cache ONLY after successful transaction commit
            registerCacheUpdate(tenantName);

        } catch (DataIntegrityViolationException e) {
            // Check if this was the expected unique constraint violation (concurrent creation)
            // vs. other constraint violations using JDBC SQLState codes
            if (DatabaseExceptionUtils.isUniqueConstraintViolation(e, "tenant")) {
                // Concurrent creation - another thread created it; cache and continue
                logger.debug(ErrorMessages.TENANT_CONCURRENT_CREATION);
                registerCacheUpdate(tenantName);
            } else {
                // Different constraint violation (e.g., foreign key, not null) - re-throw with context
                logger.error(ErrorMessages.TENANT_CONSTRAINT_ERROR_LOG, e);
                throw e;
            }
        }
        // Note: Other DataAccessExceptions (connection timeout, deadlock, etc.) are not caught
        // They will bubble up to allow retry logic or proper error handling at higher levels
    }


    /**
     * Registers a cache update to happen only after the current transaction commits.
     * This prevents cache poisoning if the transaction rolls back.
     *
     * @param tenantName - The tenant name to cache
     */
    private void registerCacheUpdate(String tenantName) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    tenantExistsCache.put(tenantName, true);
                    logger.debug("Cached tenant existence after commit");
                }
            });
        } else {
            // No active transaction - cache immediately (e.g., in tests)
            tenantExistsCache.put(tenantName, true);
        }
    }

    private Tenant createTenantInternal(String tenantName) {
        String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

        logger.debug(ErrorMessages.TENANT_ONBOARDING_PREPARING);

        Tenant newTenant = new Tenant();
        newTenant.setTenantName(tenantName);
        newTenant.setCreatedBy(userName);
        newTenant.setUpdatedBy(userName);

        Tenant savedTenant = tenantRepository.save(newTenant);
        logger.info(ErrorMessages.TENANT_ONBOARDING_SUCCESS);
        return savedTenant;
        // Note: DataAccessExceptions from save() are allowed to bubble up naturally
        // This preserves exception types (connection timeout, deadlock, etc.) for proper handling
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
