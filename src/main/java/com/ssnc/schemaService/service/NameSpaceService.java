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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class NameSpaceService {

    private static final Logger logger = LoggerFactory.getLogger(NameSpaceService.class);

    // Cache namespace IDs to avoid DB queries on every request
    // Key format: "tenantId:namespaceName" for tenant-aware caching
    private final Cache<String, UUID> namespaceIdCache = Caffeine.newBuilder()
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
    public UUID ensureNamespaceExists(UUID tenantId, String namespace) {
        String cacheKey = tenantId + AppConstants.CACHE_KEY_SEPARATOR + namespace;

        // Check cache first to avoid DB query on every request
        UUID cachedId = namespaceIdCache.getIfPresent(cacheKey);
        if (cachedId != null) {
            return cachedId;
        }

        try {
            Optional<Nmspc> existing = nameSpaceRepository.findByTenantIdAndNmspcName(tenantId, namespace);

            if (existing.isPresent()) {
                UUID nmspcId = existing.get().getNmspcId();
                // Cache the namespace ID after successful lookup
                namespaceIdCache.put(cacheKey, nmspcId);
                return nmspcId;
            }

            // Create new namespace
            UUID nmspcId = createNamespaceInternal(tenantId, namespace);
            // Cache the namespace ID after successful creation
            namespaceIdCache.put(cacheKey, nmspcId);
            return nmspcId;

        } catch (DataIntegrityViolationException e) {
            // Check if this was the expected unique constraint violation (concurrent creation)
            // vs. other constraint violations (e.g., foreign key, not null)
            String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            String rootCauseMsg = e.getRootCause() != null && e.getRootCause().getMessage() != null
                    ? e.getRootCause().getMessage().toLowerCase() : "";

            boolean isUniqueConstraintViolation =
                    errorMsg.contains(AppConstants.DB_KEYWORD_UNIQUE) ||
                    errorMsg.contains(AppConstants.DB_KEYWORD_DUPLICATE) ||
                    errorMsg.contains(AppConstants.DB_KEYWORD_NMSPC_NAME) ||
                    errorMsg.contains(AppConstants.DB_KEYWORD_NAMESPACE) ||
                    rootCauseMsg.contains(AppConstants.DB_KEYWORD_UNIQUE) ||
                    rootCauseMsg.contains(AppConstants.DB_KEYWORD_DUPLICATE) ||
                    rootCauseMsg.contains(AppConstants.DB_KEYWORD_NMSPC_NAME) ||
                    rootCauseMsg.contains(AppConstants.DB_KEYWORD_NAMESPACE);

            if (isUniqueConstraintViolation) {
                // Concurrent creation - another thread created it; re-fetch
                logger.debug(ErrorMessages.NAMESPACE_CONCURRENT_CREATION);
                UUID nmspcId = nameSpaceRepository.findByTenantIdAndNmspcName(tenantId, namespace)
                        .map(Nmspc::getNmspcId)
                        .orElseThrow(() -> new IllegalStateException(ErrorMessages.NAMESPACE_RACE_CONDITION_UNRESOLVED));
                // Cache the namespace ID after successful resolution
                namespaceIdCache.put(cacheKey, nmspcId);
                return nmspcId;
            }

            // Different constraint violation (e.g., foreign key, not null) - throw generic error
            logger.error(ErrorMessages.NAMESPACE_CONSTRAINT_ERROR_LOG, e);
            throw new IllegalStateException(ErrorMessages.NAMESPACE_CONSTRAINT_VIOLATION, e);
        }
        // Note: Other DataAccessExceptions (connection timeout, deadlock, etc.) are not caught
        // They will bubble up to allow retry logic or proper error handling at higher levels
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
