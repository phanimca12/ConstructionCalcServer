package com.ssnc.schemaService.service;

import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.ExtRefResponse;
import com.ssnc.schemaService.dto.ExtRefWithSchemasRequest;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.entity.ExtRef;
import com.ssnc.schemaService.entity.ExtRefType;
import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmExtRefXref;
import com.ssnc.schemaService.repo.ExtRefRepository;
import com.ssnc.schemaService.repo.SchmExtRefXrefRepository;
import com.ssnc.schemaService.repo.SchmRepository;
import com.ssnc.schemaService.tenant.NamespaceFilterManager;
import com.ssnc.schemaService.tenant.TenantContext;
import com.ssnc.shared.security.JwtClaimsContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExternalReferenceService {

    @Autowired
    private ExtRefRepository extRefRepository;

    @Autowired
    private SchmExtRefXrefRepository schmExtRefXrefRepository;

    @Autowired
    private SchmRepository schmRepository;

    @Autowired
    private NamespaceFilterManager namespaceFilterManager;

    @Autowired
    private JwtClaimsContext jwtClaimsContext;

    /**
     * Get external references with optional type filter
     */
    @Transactional(readOnly = true)
    public List<ExtRefDto> getExternalReferences(String nameSpace, String type) {
        namespaceFilterManager.enableIfPresent(nameSpace);

        List<ExtRef> extRefs;
        if (type != null && !type.isEmpty()) {
            // Validate the type against the enum
            ExtRefType.fromString(type);
            extRefs = extRefRepository.findByExtRefType(type);
        } else {
            extRefs = extRefRepository.findAll();
        }

        return extRefs.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Get schemas by external reference
     */
    @Transactional(readOnly = true)
    public List<SchemaDto> getSchemasByExternalReference(
            String nameSpace,
            String extRefType,
            UUID extRefId,
            String extRefVersion) {

        namespaceFilterManager.enableIfPresent(nameSpace);

        // Validate the type against the enum
        ExtRefType.fromString(extRefType);

        List<SchmExtRefXref> xrefs = schmExtRefXrefRepository
                .findByExtRefExtRefTypeAndExtRefExtRefIdAndExtRefExtRefVersion(
                        extRefType, extRefId, extRefVersion);

        // Batch fetch all schemas (fix N+1 query problem)
        List<UUID> schmIds = xrefs.stream()
                .map(SchmExtRefXref::getSchmId)
                .collect(Collectors.toList());

        List<Schm> schemas = schmRepository.findAllById(schmIds);

        return schemas.stream()
                .map(this::mapSchmToDto)
                .collect(Collectors.toList());
    }

    /**
     * Create external reference with associated schemas.
     *
     * IMMUTABILITY RULE: Once an external reference is created with a specific version,
     * it is IMMUTABLE. You cannot change the name, type, or schema associations for an
     * existing version. If changes are needed, create a new version.
     *
     * This method implements idempotency - if the record already exists with identical
     * data (name, type, version, and schema associations), it returns success without
     * making any changes.
     *
     * @throws IllegalArgumentException if trying to modify an existing version
     */
    @Transactional
    public ExtRefResponse createOrUpdateExternalReference(
            String nameSpace,
            String extRefType,
            String extRefName,
            UUID extRefId,
            String extRefVersion,
            ExtRefWithSchemasRequest request) {

        // ===== INPUT VALIDATION - ALL DONE BEFORE ANY DATABASE OPERATIONS =====
        // Defensive null check (should be caught by @Valid but defense in depth)
        if (request == null) {
            throw new IllegalArgumentException(ErrorMessages.EXTERNAL_REFERENCE_REQUEST_BODY_NULL);
        }

        namespaceFilterManager.enableIfPresent(nameSpace);

        // Validate the type against the enum
        ExtRefType.fromString(extRefType);

        // Validate and prepare requested schema IDs - check for nulls BEFORE any DB operations
        final List<UUID> requestedSchmIds = (request.getSchemas() != null && !request.getSchemas().isEmpty())
                ? request.getSchemas().stream()
                    .map(ExtRefWithSchemasRequest.SchemaReference::getSchmId)
                    .peek(id -> {
                        if (id == null) {
                            throw new IllegalArgumentException(ErrorMessages.SCHEMA_ID_CANNOT_BE_NULL);
                        }
                    })
                    .collect(Collectors.toList())
                : new ArrayList<>();

        // ===== DATABASE OPERATIONS START HERE =====
        String currentUser = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;
        String tenantName = TenantContext.getTenantName();

        // Check if external reference already exists by ID
        Optional<ExtRef> existingExtRef = extRefRepository.findById(extRefId);

        // Check for unique constraint violation (tenant_name, ext_ref_name, ext_ref_type, ext_ref_version)
        // This prevents database constraint violations and provides clear error messages
        Optional<ExtRef> existingByUnique = extRefRepository.findByExtRefNameAndExtRefTypeAndExtRefVersion(
                extRefName, extRefType, extRefVersion);

        if (existingByUnique.isPresent() && !existingByUnique.get().getExtRefId().equals(extRefId)) {
            throw new IllegalArgumentException(String.format(
                    ErrorMessages.EXTERNAL_REFERENCE_DUPLICATE,
                    extRefName, extRefType, extRefVersion, existingByUnique.get().getExtRefId()));
        }

        // IMMUTABILITY CHECK: If external reference exists, it must be identical (idempotent)
        // or an error must be thrown. Updates are NOT allowed.
        if (existingExtRef.isPresent()) {
            ExtRef existing = existingExtRef.get();

            // Check if ALL metadata matches (name, type, version)
            boolean metadataMatches = Objects.equals(existing.getExtRefName(), extRefName)
                    && Objects.equals(existing.getExtRefType(), extRefType)
                    && Objects.equals(existing.getExtRefVersion(), extRefVersion);

            // Get existing schema associations
            List<SchmExtRefXref> existingXrefs = schmExtRefXrefRepository.findByExtRefId(extRefId);
            List<UUID> existingSchmIds = existingXrefs.stream()
                    .map(SchmExtRefXref::getSchmId)
                    .sorted()
                    .collect(Collectors.toList());

            List<UUID> sortedRequestedSchmIds = new ArrayList<>(requestedSchmIds);
            sortedRequestedSchmIds.sort(UUID::compareTo);

            boolean schemasMatch = existingSchmIds.equals(sortedRequestedSchmIds);

            // If EVERYTHING matches, this is idempotent - return success
            if (metadataMatches && schemasMatch) {
                return new ExtRefResponse(
                        mapToDto(existing),
                        ErrorMessages.EXTERNAL_REFERENCE_UP_TO_DATE,
                        false
                );
            }

            // If anything differs, throw error - versions are IMMUTABLE
            if (!metadataMatches) {
                throw new IllegalArgumentException(String.format(
                        ErrorMessages.EXTERNAL_REFERENCE_IMMUTABLE,
                        extRefId, extRefVersion));
            }

            // Metadata matches but schemas differ
            throw new IllegalArgumentException(String.format(
                    ErrorMessages.EXTERNAL_REFERENCE_VERSION_IMMUTABLE,
                    extRefVersion, extRefId, existingSchmIds, sortedRequestedSchmIds));
        }

        // At this point, external reference does NOT exist (or was idempotent and already returned)

        // CRITICAL: Sort UUIDs before locking to prevent deadlocks
        // Without this, concurrent requests locking the same schemas in different orders
        // can deadlock: Request A locks [UUID-111, UUID-222], Request B locks [UUID-222, UUID-111]
        // We use sortedSchmIds consistently in both validation AND insertion for clarity and maintainability
        List<UUID> sortedSchmIds = new ArrayList<>(requestedSchmIds);
        Collections.sort(sortedSchmIds);

        // ===== VALIDATE ALL SCHEMAS BEFORE ANY DATABASE WRITES =====
        // SECURITY: Fail fast validation prevents data corruption
        // Validate schemas BEFORE saving ExtRef to avoid orphaned records if validation fails
        if (!sortedSchmIds.isEmpty()) {
            // Validate all schemas exist and are published BEFORE making any changes
            for (UUID schmId : sortedSchmIds) {
                // Use pessimistic lock to prevent unpublish race condition
                // Locks are held until end of transaction (@Transactional method)
                Schm schema = schmRepository.findWithLockBySchmId(schmId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                String.format(ErrorMessages.SCHEMA_NOT_FOUND_FOR_REFERENCE, schmId)));

                // Only allow references to published schemas
                if (schema.getPublishVersion() == null) {
                    throw new IllegalArgumentException(
                            String.format(ErrorMessages.CANNOT_REFERENCE_UNPUBLISHED_SCHEMA, schmId));
                }
            }
        }

        // ===== ALL VALIDATIONS PASSED - NOW CREATE EXTERNAL REFERENCE =====
        // Create new external reference entity
        ExtRef extRef = new ExtRef();
        extRef.setExtRefId(extRefId);
        extRef.setTenantName(tenantName);
        extRef.setExtRefName(extRefName);
        extRef.setExtRefType(extRefType);
        extRef.setExtRefVersion(extRefVersion);
        extRef.setCreatedBy(currentUser);
        extRef.setUpdatedBy(currentUser);

        extRef = extRefRepository.save(extRef);

        // ===== CREATE SCHEMA ASSOCIATIONS =====
        // Create associations for all requested schemas
        if (!sortedSchmIds.isEmpty()) {
            for (UUID schmId : sortedSchmIds) {
                SchmExtRefXref xref = new SchmExtRefXref();
                xref.setTenantName(tenantName);
                xref.setSchmId(schmId);
                xref.setExtRefId(extRefId);
                xref.setCreatedBy(currentUser);
                schmExtRefXrefRepository.save(xref);
            }
        }

        return new ExtRefResponse(
                mapToDto(extRef),
                ErrorMessages.EXTERNAL_REFERENCE_CREATED_SUCCESS,
                true);
    }

    /**
     * Map ExtRef entity to DTO
     */
    private ExtRefDto mapToDto(ExtRef extRef) {
        ExtRefDto dto = new ExtRefDto();
        dto.setExtRefId(extRef.getExtRefId());
        dto.setTenantName(extRef.getTenantName());
        dto.setExtRefName(extRef.getExtRefName());
        dto.setExtRefType(extRef.getExtRefType());
        dto.setExtRefVersion(extRef.getExtRefVersion());
        dto.setCreatedDatetime(extRef.getCreatedDatetime());
        dto.setUpdatedDatetime(extRef.getUpdatedDatetime());
        dto.setCreatedBy(extRef.getCreatedBy());
        dto.setUpdatedBy(extRef.getUpdatedBy());
        return dto;
    }

    /**
     * Map Schm entity to SchemaDto
     */
    private SchemaDto mapSchmToDto(Schm schm) {
        SchemaDto dto = new SchemaDto();
        dto.setId(schm.getSchmId());
        dto.setName(schm.getSchmName());
        dto.setDescription(schm.getSchmDesc());
        dto.setSchemaType(schm.getSchemaType());
        dto.setContentType(schm.getContentType());
        dto.setLockBy(schm.getLockBy());
        dto.setGroup(schm.getGroup());
        dto.setCreatedByUser(schm.getCreatedBy());
        dto.setCreateDateTime(schm.getCreatedDatetime());
        dto.setModifiedByUser(schm.getUpdatedBy());
        dto.setModifiedDateTime(schm.getUpdatedDatetime());
        return dto;
    }
}
