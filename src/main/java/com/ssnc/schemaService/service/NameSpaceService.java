package com.ssnc.schemaService.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.NameSpaceDto;
import com.ssnc.schemaService.entity.Nmspc;
import com.ssnc.schemaService.repo.NameSpaceRepository;
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

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class NameSpaceService {

    private static final Logger logger = LoggerFactory.getLogger(NameSpaceService.class);

    /**
     * Composite cache key for namespace lookups.
     * Using a record ensures type safety and eliminates cache key collision
     * issues that could occur with string concatenation if namespace contains separators.
     */
    private record NamespaceCacheKey(UUID tenantId, String namespace) {}

    // Cache namespace IDs to avoid DB queries on every request
    // Uses composite key for tenant-aware caching without collision risk
    private final Cache<NamespaceCacheKey, UUID> namespaceIdCache = Caffeine.newBuilder()
            .expireAfterWrite(AppConstants.CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES)
            .maximumSize(AppConstants.CACHE_MAX_SIZE)
            .build();

    @Autowired
    NameSpaceRepository nameSpaceRepository;

    @Autowired
    private JwtClaimsContext jwtClaimsContext;

    /**
     * Ensures a namespace exists for the given tenant, creating it if necessary.
     * Used by filters and services to auto-create namespaces.
     * Includes caching and race condition handling.
     *
     * @param tenantId - The tenant ID
     * @param namespace - The namespace name
     * @return The namespace ID
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public UUID ensureNamespaceExists(UUID tenantId, String namespace) {
        NamespaceCacheKey cacheKey = new NamespaceCacheKey(tenantId, namespace);

        // Check cache first to avoid DB query on every request
        UUID cachedId = namespaceIdCache.getIfPresent(cacheKey);
        if (cachedId != null) {
            return cachedId;
        }

        try {
            Optional<Nmspc> existing = nameSpaceRepository.findByTenantIdAndNmspcName(tenantId, namespace);

            UUID nmspcId;
            if (existing.isPresent()) {
                nmspcId = existing.get().getNmspcId();
            } else {
                // Create new namespace
                nmspcId = createNamespaceInternal(tenantId, namespace);
            }

            // Cache ONLY after successful transaction commit
            registerCacheUpdate(cacheKey, nmspcId);
            return nmspcId;

        } catch (DataIntegrityViolationException e) {
            // Check if this was the expected unique constraint violation using JDBC SQLState codes
            if (isUniqueConstraintViolation(e)) {
                // Concurrent creation - another thread created it; re-fetch
                logger.debug(ErrorMessages.NAMESPACE_CONCURRENT_CREATION);
                UUID nmspcId = nameSpaceRepository.findByTenantIdAndNmspcName(tenantId, namespace)
                        .map(Nmspc::getNmspcId)
                        .orElseThrow(() -> new IllegalStateException(ErrorMessages.NAMESPACE_RACE_CONDITION_UNRESOLVED));
                // Cache after successful resolution
                registerCacheUpdate(cacheKey, nmspcId);
                return nmspcId;
            }

            // Different constraint violation (e.g., foreign key, not null) - throw generic error
            logger.error(ErrorMessages.NAMESPACE_CONSTRAINT_ERROR_LOG, e);
            throw new IllegalStateException(ErrorMessages.NAMESPACE_CONSTRAINT_VIOLATION, e);
        }
        // Note: Other DataAccessExceptions (connection timeout, deadlock, etc.) are not caught
        // They will bubble up to allow retry logic or proper error handling at higher levels
    }

    /**
     * Checks if a DataIntegrityViolationException is a unique constraint violation
     * using JDBC SQLState codes (database-agnostic).
     *
     * @param e - The exception to check
     * @return true if it's a unique constraint violation on namespace
     */
    private boolean isUniqueConstraintViolation(DataIntegrityViolationException e) {
        Throwable rootCause = e.getRootCause();

        if (rootCause instanceof SQLException) {
            SQLException sqlEx = (SQLException) rootCause;
            String sqlState = sqlEx.getSQLState();

            // Standard SQLState codes for unique constraint violations:
            // 23505 - PostgreSQL unique_violation
            // 23000 - MySQL/MariaDB integrity_constraint_violation
            // 23505 - H2 unique_violation
            if ("23505".equals(sqlState) || "23000".equals(sqlState)) {
                // Verify it's specifically the namespace constraint
                String message = sqlEx.getMessage();
                return message != null &&
                       (message.toLowerCase().contains("nmspc_name") ||
                        message.toLowerCase().contains("namespace"));
            }
        }

        return false;
    }

    /**
     * Registers a cache update to happen only after the current transaction commits.
     * This prevents cache poisoning if the transaction rolls back.
     *
     * @param cacheKey - The composite cache key
     * @param nmspcId - The namespace ID to cache
     */
    private void registerCacheUpdate(NamespaceCacheKey cacheKey, UUID nmspcId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    namespaceIdCache.put(cacheKey, nmspcId);
                    logger.debug("Cached namespace ID after commit");
                }
            });
        } else {
            // No active transaction - cache immediately (e.g., in tests)
            namespaceIdCache.put(cacheKey, nmspcId);
        }
    }

    private UUID createNamespaceInternal(UUID tenantId, String namespace) {
        String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

        Nmspc newNmspc = new Nmspc();
        newNmspc.setTenantId(tenantId);
        newNmspc.setNmspcName(namespace);
        newNmspc.setCreatedBy(userName);
        newNmspc.setUpdatedBy(userName);

        Nmspc saved = nameSpaceRepository.save(newNmspc);
        logger.info(ErrorMessages.NAMESPACE_CREATED);
        return saved.getNmspcId();
        // Note: DataAccessExceptions from save() are allowed to bubble up naturally
        // This preserves exception types (connection timeout, deadlock, etc.) for proper handling
    }

    private NameSpaceDto mapToNameSpaceDto(Nmspc nmspc) {
        NameSpaceDto dto = new NameSpaceDto();
        dto.setNmspcId(nmspc.getNmspcId());
        dto.setTenantId(nmspc.getTenantId());
        dto.setName(nmspc.getNmspcName());
        dto.setDescription(nmspc.getDescription());
        dto.setCreatedByUser(nmspc.getCreatedBy());
        dto.setCreateDateTime(nmspc.getCreatedDatetime());
        dto.setModifiedByUser(nmspc.getUpdatedBy());
        dto.setModifiedDateTime(nmspc.getUpdatedDatetime());
        return dto;
    }

    public NameSpaceDto getNameSpaceByName(String name) {
        Nmspc nmspc = nameSpaceRepository.findByNmspcName(name)
                .orElseThrow(() -> new EntityNotFoundException(ErrorMessages.NAMESPACE_NOT_FOUND));
        return mapToNameSpaceDto(nmspc);
    }

    public List<NameSpaceDto> getAllNameSpaces() {
        return nameSpaceRepository.findAll().stream()
                .map(this::mapToNameSpaceDto)
                .toList();
    }
}
