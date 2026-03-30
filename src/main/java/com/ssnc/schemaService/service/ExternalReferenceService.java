package com.ssnc.schemaService.service;

import com.ssnc.schemaService.constants.AppConstants;
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
import java.util.List;
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
     * Create or update external reference with associated schemas.
     * This method implements idempotency - if the record already exists with the same
     * name, type, version, and schema associations, it returns success without updating.
     * If the schemas array is empty, all associations will be removed.
     */
    @Transactional
    public ExtRefResponse createOrUpdateExternalReference(
            String nameSpace,
            String extRefType,
            String extRefName,
            UUID extRefId,
            String extRefVersion,
            ExtRefWithSchemasRequest request) {

        namespaceFilterManager.enableIfPresent(nameSpace);

        // Validate the type against the enum
        ExtRefType.fromString(extRefType);

        String currentUser = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;
        String tenantName = TenantContext.getTenantName();

        // Check if external reference already exists
        Optional<ExtRef> existingExtRef = extRefRepository.findById(extRefId);

        // Prepare requested schema IDs and validate they are not null
        final List<UUID> requestedSchmIds = (request.getSchemas() != null && !request.getSchemas().isEmpty())
                ? request.getSchemas().stream()
                    .map(ExtRefWithSchemasRequest.SchemaReference::getSchmId)
                    .peek(id -> {
                        if (id == null) {
                            throw new IllegalArgumentException("Schema ID cannot be null in request");
                        }
                    })
                    .collect(Collectors.toList())
                : new ArrayList<>();

        // Check for idempotency - if record already exists with same values, return without updating
        if (existingExtRef.isPresent()) {
            ExtRef existing = existingExtRef.get();

            // Check if name, type, and version are the same
            boolean metadataUnchanged = existing.getExtRefName().equals(extRefName)
                    && existing.getExtRefType().equals(extRefType)
                    && existing.getExtRefVersion().equals(extRefVersion);

            if (metadataUnchanged) {
                // Check if schema associations are the same
                List<SchmExtRefXref> existingXrefs = schmExtRefXrefRepository.findByExtRefId(extRefId);
                List<UUID> existingSchmIds = existingXrefs.stream()
                        .map(SchmExtRefXref::getSchmId)
                        .sorted()
                        .collect(Collectors.toList());

                List<UUID> sortedRequestedSchmIds = new ArrayList<>(requestedSchmIds);
                sortedRequestedSchmIds.sort(UUID::compareTo);

                // If schema associations are also the same, return without updating
                if (existingSchmIds.equals(sortedRequestedSchmIds)) {
                    return new ExtRefResponse(
                            mapToDto(existing),
                            "External reference is already up to date. No changes were made.",
                            false
                    );
                }
            }
        }

        ExtRef extRef;
        boolean isUpdate = existingExtRef.isPresent();

        if (isUpdate) {
            // Update existing
            extRef = existingExtRef.get();
            extRef.setExtRefName(extRefName);
            extRef.setExtRefType(extRefType);
            extRef.setExtRefVersion(extRefVersion);
            extRef.setUpdatedBy(currentUser);
        } else {
            // Create new
            extRef = new ExtRef();
            extRef.setExtRefId(extRefId);
            extRef.setTenantName(tenantName);
            extRef.setExtRefName(extRefName);
            extRef.setExtRefType(extRefType);
            extRef.setExtRefVersion(extRefVersion);
            extRef.setCreatedBy(currentUser);
            extRef.setUpdatedBy(currentUser);
        }

        extRef = extRefRepository.save(extRef);

        // Handle schema associations with differential update (more efficient than delete all + recreate)
        List<SchmExtRefXref> existingXrefs = schmExtRefXrefRepository.findByExtRefId(extRefId);

        // Get the set of existing and requested schema IDs
        List<UUID> existingSchmIds = existingXrefs.stream()
                .map(SchmExtRefXref::getSchmId)
                .collect(Collectors.toList());

        // Determine what to delete (in existing but not in requested)
        List<SchmExtRefXref> toDelete = existingXrefs.stream()
                .filter(xref -> !requestedSchmIds.contains(xref.getSchmId()))
                .collect(Collectors.toList());

        // Determine what to add (in requested but not in existing)
        List<UUID> toAdd = requestedSchmIds.stream()
                .filter(schmId -> !existingSchmIds.contains(schmId))
                .collect(Collectors.toList());

        // Delete removed associations
        if (!toDelete.isEmpty()) {
            schmExtRefXrefRepository.deleteAll(toDelete);
        }

        // Create new associations
        // First validate all schemas, then save (fail-fast approach)
        if (!toAdd.isEmpty()) {
            // Step 1: Validate all schemas exist and are published
            for (UUID schmId : toAdd) {
                // Use pessimistic lock to prevent unpublish race condition
                Schm schema = schmRepository.findWithLockBySchmId(schmId)
                        .orElseThrow(() -> new IllegalArgumentException(
                                String.format("Schema %s not found", schmId)));

                // Only allow references to published schemas
                if (schema.getPublishVersion() == null) {
                    throw new IllegalArgumentException(
                            String.format("Cannot create reference to unpublished schema %s. " +
                                    "Schema must be published before creating external references.", schmId));
                }
            }

            // Step 2: All validations passed, now save the associations
            for (UUID schmId : toAdd) {
                SchmExtRefXref xref = new SchmExtRefXref();
                xref.setTenantName(tenantName);
                xref.setSchmId(schmId);
                xref.setExtRefId(extRefId);
                xref.setCreatedBy(currentUser);
                schmExtRefXrefRepository.save(xref);
            }
        }

        String message = isUpdate
                ? "External reference updated successfully."
                : "External reference created successfully.";

        return new ExtRefResponse(mapToDto(extRef), message, true);
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
