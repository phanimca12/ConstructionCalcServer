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
import org.springframework.dao.DataIntegrityViolationException;
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

    @Autowired
    private com.ssnc.schemaService.repo.TenantRepository tenantRepository;

    @Autowired
    private com.ssnc.schemaService.repo.NameSpaceRepository nameSpaceRepository;

    /**
     * Get external references with optional type filter
     */
    @Transactional(readOnly = true)
    public List<ExtRefDto> getExternalReferences(String nameSpace, String type) {
        // Defense in depth: Validate input lengths
        if (nameSpace != null && nameSpace.length() > 32) {
            throw new IllegalArgumentException("Namespace must not exceed 32 characters");
        }
        if (type != null && type.length() > 64) {
            throw new IllegalArgumentException("Type must not exceed 64 characters");
        }

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
            String extRefId,
            String extRefVersion) {

        // Defense in depth: Validate input lengths
        if (nameSpace != null && nameSpace.length() > 32) {
            throw new IllegalArgumentException("Namespace must not exceed 32 characters");
        }
        if (extRefType != null && extRefType.length() > 64) {
            throw new IllegalArgumentException("External reference type must not exceed 64 characters");
        }
        if (extRefId != null && extRefId.length() > 64) {
            throw new IllegalArgumentException("External reference ID must not exceed 64 characters");
        }
        if (extRefVersion != null && extRefVersion.length() > 64) {
            throw new IllegalArgumentException("External reference version must not exceed 64 characters");
        }

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

        // Avoid unnecessary database call if no schemas to fetch
        List<Schm> schemas = schmIds.isEmpty()
                ? Collections.emptyList()
                : schmRepository.findAllById(schmIds);

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
            String extRefId,
            String extRefVersion,
            ExtRefWithSchemasRequest request) {

        // ===== INPUT VALIDATION - ALL DONE BEFORE ANY DATABASE OPERATIONS =====
        // Defense in depth: Validate input lengths to prevent database truncation errors
        if (nameSpace != null && nameSpace.length() > 32) {
            throw new IllegalArgumentException("Namespace must not exceed 32 characters");
        }
        if (extRefType != null && extRefType.length() > 64) {
            throw new IllegalArgumentException("External reference type must not exceed 64 characters");
        }
        if (extRefName != null && extRefName.length() > 256) {
            throw new IllegalArgumentException("External reference name must not exceed 256 characters");
        }
        if (extRefId != null && extRefId.length() > 64) {
            throw new IllegalArgumentException("External reference ID must not exceed 64 characters");
        }
        if (extRefVersion != null && extRefVersion.length() > 64) {
            throw new IllegalArgumentException("External reference version must not exceed 64 characters");
        }

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

        // Resolve tenant_id from tenant_name
        UUID tenantId = tenantRepository.findByTenantName(tenantName)
                .map(com.ssnc.schemaService.entity.Tenant::getTenantId)
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Tenant not found: %s", tenantName)));

        // Check if external reference already exists by ID
        Optional<ExtRef> existingExtRef = extRefRepository.findById(extRefId);

        // SECURITY: Check for unique constraint violation (tenant_id, ext_ref_name, ext_ref_type, ext_ref_version)
        // CRITICAL: Must filter by tenantId to prevent cross-tenant data leakage
        // This prevents database constraint violations and provides clear error messages
        Optional<ExtRef> existingByUnique = extRefRepository
                .findByTenantIdAndExtRefNameAndExtRefTypeAndExtRefVersion(
                        tenantId, extRefName, extRefType, extRefVersion);

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
        extRef.setTenantId(tenantId);
        extRef.setExtRefName(extRefName);
        extRef.setExtRefType(extRefType);
        extRef.setExtRefVersion(extRefVersion);
        extRef.setCreatedBy(currentUser);
        extRef.setUpdatedBy(currentUser);

        // RACE CONDITION HANDLING: Wrap save in try-catch to handle concurrent duplicate creation
        // Between our check (line 217) and save, another request may create same record
        try {
            extRef = extRefRepository.save(extRef);
        } catch (DataIntegrityViolationException e) {
            // Check if this was our unique constraint violation
            String errorMsg = e.getMessage() != null ? e.getMessage().toLowerCase() : "";
            if (errorMsg.contains("uk_ext_ref_tenant_id_name_type_version") ||
                errorMsg.contains("unique") && errorMsg.contains("ext_ref")) {
                throw new IllegalArgumentException(String.format(
                        ErrorMessages.EXTERNAL_REFERENCE_DUPLICATE,
                        extRefName, extRefType, extRefVersion, "another record"));
            }
            // Re-throw if it's a different constraint or database error
            throw e;
        }

        // ===== CREATE SCHEMA ASSOCIATIONS =====
        // Create associations for all requested schemas using batch save to avoid N+1 writes
        if (!sortedSchmIds.isEmpty()) {
            List<SchmExtRefXref> xrefs = new ArrayList<>();
            for (UUID schmId : sortedSchmIds) {
                SchmExtRefXref xref = new SchmExtRefXref();
                xref.setTenantId(tenantId);
                xref.setSchmId(schmId);
                xref.setExtRefId(extRefId);
                xref.setCreatedBy(currentUser);
                xrefs.add(xref);
            }
            // Batch save all cross-references in one operation
            schmExtRefXrefRepository.saveAll(xrefs);
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
        dto.setTenantId(extRef.getTenantId());
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
        dto.setTenantId(schm.getTenantId());
        dto.setNmspcId(schm.getNmspcId());
        dto.setName(schm.getSchmName());
        dto.setDescription(schm.getSchmDesc());
        dto.setSchemaType(schm.getSchemaType());
        dto.setContentType(schm.getContentType());
        dto.setLockBy(schm.getLockBy());
        dto.setSchmGroup(schm.getSchmGroup());
        dto.setCreatedByUser(schm.getCreatedBy());
        dto.setCreateDateTime(schm.getCreatedDatetime());
        dto.setModifiedByUser(schm.getUpdatedBy());
        dto.setModifiedDateTime(schm.getUpdatedDatetime());
        return dto;
    }
}
