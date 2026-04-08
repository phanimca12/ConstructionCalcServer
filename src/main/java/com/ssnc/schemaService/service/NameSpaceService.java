package com.ssnc.schemaService.service;

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

@Service
public class NameSpaceService {

    private static final Logger logger = LoggerFactory.getLogger(NameSpaceService.class);

    @Autowired
    NameSpaceRepository nameSpaceRepository;

    @Autowired
    private JwtClaimsContext jwtClaimsContext;

    /**
     * Ensures a namespace exists for the given tenant, creating it if necessary.
     * Used by filters and services to auto-create namespaces.
     * Includes race condition handling.
     *
     * @param tenantId - The tenant ID
     * @param namespace - The namespace name
     * @return The namespace ID
     */
    public UUID ensureNamespaceExists(UUID tenantId, String namespace) {
        try {
            Optional<Nmspc> existing = nameSpaceRepository.findByTenantIdAndNmspcName(tenantId, namespace);

            if (existing.isPresent()) {
                return existing.get().getNmspcId();
            }

            // Create new namespace
            return createNamespaceInternal(tenantId, namespace);

        } catch (DataIntegrityViolationException e) {
            // Another thread created it concurrently; re-fetch
            logger.debug("Namespace already exists (concurrent creation): {} for tenant: {}", namespace, tenantId);
            return nameSpaceRepository.findByTenantIdAndNmspcName(tenantId, namespace)
                    .map(Nmspc::getNmspcId)
                    .orElseThrow(() -> new IllegalStateException(
                            String.format("Namespace creation race condition unresolved: %s", namespace)));
        } catch (DataAccessException e) {
            // DB connectivity or other data access issues
            logger.error("Failed to ensure namespace '{}' exists for tenant '{}': {}",
                    namespace, tenantId, e.getMessage(), e);
            throw new IllegalStateException("Namespace operation failed: " + e.getMessage(), e);
        }
    }

    private UUID createNamespaceInternal(UUID tenantId, String namespace) {
        try {
            String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                    ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

            Nmspc newNmspc = new Nmspc();
            newNmspc.setTenantId(tenantId);
            newNmspc.setNmspcName(namespace);
            newNmspc.setCreatedBy(userName);
            newNmspc.setUpdatedBy(userName);

            Nmspc saved = nameSpaceRepository.save(newNmspc);
            logger.info("Created new namespace: {} for tenant: {}", namespace, tenantId);
            return saved.getNmspcId();

        } catch (DataAccessException e) {
            logger.error("Failed to create namespace '{}' for tenant '{}': {}",
                    namespace, tenantId, e.getMessage(), e);
            throw new IllegalStateException("Namespace creation failed: " + e.getMessage(), e);
        }
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
