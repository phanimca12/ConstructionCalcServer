package com.ssnc.schemaService.service;

import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.dto.SchemaExportDto;
import com.ssnc.schemaService.dto.SchemaExportRequest;
import com.ssnc.schemaService.dto.SchemaExportResponse;
import com.ssnc.schemaService.dto.SchemaImportRequest;
import com.ssnc.schemaService.dto.SchemaImportResponse;
import com.ssnc.schemaService.dto.SchemaVersionDto;
import com.ssnc.schemaService.dto.SchemaWithVersionDto;
import com.ssnc.schemaService.entity.ExtRef;
import com.ssnc.schemaService.entity.ExtRefId;
import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmData;
import com.ssnc.schemaService.entity.SchmDataId;
import com.ssnc.schemaService.entity.SchmExtRefXref;
import com.ssnc.schemaService.entity.Tenant;
import com.ssnc.schemaService.repo.ExtRefRepository;
import com.ssnc.schemaService.repo.SchmDataRepository;
import com.ssnc.schemaService.repo.SchmExtRefXrefRepository;
import com.ssnc.schemaService.repo.SchmFilterCriteria;
import com.ssnc.schemaService.repo.SchmRepository;
import com.ssnc.schemaService.repo.SchmSpecifications;
import com.ssnc.schemaService.repo.TenantRepository;
import com.ssnc.schemaService.tenant.NamespaceFilterManager;
import com.ssnc.schemaService.tenant.TenantContext;
import com.ssnc.schemaService.util.DatabaseExceptionUtils;
import com.ssnc.shared.security.JwtClaimsContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SchemaService {

    @Autowired
    private SchmRepository schmRepository;

    @Autowired
    private SchmDataRepository schmDataRepository;

    @Autowired
    private NamespaceFilterManager namespaceFilterManager;

    @Autowired
    private JwtClaimsContext jwtClaimsContext;

    @Autowired
    private SchmExtRefXrefRepository schmExtRefXrefRepository;

    @Autowired
    private ExtRefRepository extRefRepository;

    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private NameSpaceService nameSpaceService;

    /**
     * Get schemas with optional filtering, sorting, and pagination
     *
     * @param namespace - Namespace filter
     * @param name - Optional name filter
     * @param type - Optional schema type filter
     * @param group - Optional group filter
     * @param modifiedByUser - Optional user filter
     * @param versionModifiedByUser - Optional version modified by user filter
     * @param sort - Optional sort parameter
     * @param withVersion - Optional version filter (none, draft, published, latest)
     * @param pageable - Pagination parameters
     * @return Paginated list of schemas
     */
    public Page<SchemaDto> getSchemas(String namespace, String name, String type, String group,
                                       String modifiedByUser, String versionModifiedByUser,
                                       String sort, String withVersion, Pageable pageable) {
        namespaceFilterManager.enableIfPresent(namespace);

        SchmFilterCriteria criteria = new SchmFilterCriteria();
        criteria.setName(name);
        criteria.setSchemaType(type);
        criteria.setGroup(group);
        criteria.setModifiedByUser(modifiedByUser);
        criteria.setVersionModifiedByUser(versionModifiedByUser);
        criteria.setSort(sort);
        criteria.setWithVersion(withVersion);

        // Get all schemas matching criteria (filtering done at DB level)
        List<Schm> schemas = schmRepository.findAll(SchmSpecifications.withFilters(criteria));

        // Map to DTOs and apply version filtering and sorting
        List<SchemaDto> filteredSchemas = schemas.stream()
                .map(schm -> mapToSchemaResponse(schm, withVersion))
                .filter(schemaDto -> filterByVersion(schemaDto, withVersion))
                .sorted(getSortComparator(sort))
                .collect(Collectors.toList());

        // Apply pagination manually (since filtering/sorting happens in Java)
        if (pageable.isUnpaged()) {
            return new PageImpl<>(filteredSchemas, pageable, filteredSchemas.size());
        }

        // SECURITY: Validate offset to prevent integer overflow DoS attack
        long offset = pageable.getOffset();
        if (offset < 0 || offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    String.format(ErrorMessages.INVALID_PAGINATION_OFFSET,
                            offset, Integer.MAX_VALUE));
        }
        int start = (int) offset;
        int end = Math.min(start + pageable.getPageSize(), filteredSchemas.size());

        // Use >= to handle edge case where start equals size
        if (start >= filteredSchemas.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, filteredSchemas.size());
        }

        List<SchemaDto> paginatedList = filteredSchemas.subList(start, end);
        return new PageImpl<>(paginatedList, pageable, filteredSchemas.size());
    }

    /**
     * Filter schemas based on withVersion parameter
     */
    private boolean filterByVersion(SchemaDto schemaDto, String withVersion) {
        if (withVersion == null || AppConstants.VERSION_NAME_NONE.equalsIgnoreCase(withVersion)) {
            return true;
        }

        switch (withVersion.toLowerCase()) {
            case AppConstants.VERSION_NAME_DRAFT:
                // Only include schemas that have a draft version
                return schemaDto.getDraft() != null;
            case AppConstants.VERSION_NAME_PUBLISHED:
                // Only include schemas that have a published version
                return schemaDto.getPublished() != null;
            case AppConstants.VERSION_NAME_LATEST:
                // Include schemas that have at least one version (draft or published)
                return schemaDto.getDraft() != null || schemaDto.getPublished() != null;
            default:
                return true;
        }
    }

    /**
     * Get comparator based on sort parameter
     */
    private Comparator<SchemaDto> getSortComparator(String sort) {
        if (sort == null) {
            return Comparator.comparing(SchemaDto::getName,
                    Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
        }

        switch (sort) {
            case AppConstants.SORT_VERSION_UPDATE_ASC:
                return Comparator.comparing(SchemaDto::getModifiedDateTime,
                        Comparator.nullsLast(Comparator.naturalOrder()));
            case AppConstants.SORT_VERSION_UPDATE_DESC:
                return Comparator.comparing(SchemaDto::getModifiedDateTime,
                        Comparator.nullsLast(Comparator.reverseOrder()));
            case AppConstants.SORT_NAME_ASC:
                return Comparator.comparing(SchemaDto::getName,
                        Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
            case AppConstants.SORT_NAME_DESC:
                return Comparator.comparing(SchemaDto::getName,
                        Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)).reversed();
            default:
                return Comparator.comparing(SchemaDto::getName,
                        Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER));
        }
   }

    /**
     * Create a new schema
     */
    @Transactional
    public SchemaDto createSchema(String namespace, SchemaDto schemaDto, String content) throws IOException {
        namespaceFilterManager.enableIfPresent(namespace);
        Optional<Schm> exists = schmRepository.findBySchmName(schemaDto.getName());
        if(exists.isPresent()) {
            throw new IllegalArgumentException(String.format(ErrorMessages.SCHEMA_ALREADY_EXISTS, schemaDto.getName()));
        }

        String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;
        String tenantName = TenantContext.getTenantName();

        // Resolve tenant_id and nmspc_id
        UUID tenantId = tenantRepository.findByTenantName(tenantName)
                .map(Tenant::getTenantId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.TENANT_CONFIG_INVALID));

        // SECURITY: Use tenant-aware namespace lookup to ensure proper isolation
        // Auto-create namespace if it doesn't exist
        UUID nmspcId = nameSpaceService.ensureNamespaceExists(tenantId, namespace);

        Schm schema = mapToSchmEntity(schemaDto);
        schema.setTenantId(tenantId);
        schema.setNmspcId(nmspcId);
        schema.setCreatedBy(userName);
        schema.setUpdatedBy(userName);
        Schm saved = schmRepository.save(schema);

        // If content is provided, create initial version
        if (content != null && !content.isEmpty()) {
            createSchemaDataFromFile(saved.getSchmId(), content);
        }

        return mapToSchemaResponse(saved);
    }

    /**
     * Import a schema with content. If schema name already exists, throw an error.
     * On successful save, publish the saved version atomically.
     */
    @Transactional
    public SchemaDto importSchema(String namespace, SchemaDto schemaDto, String content) throws IOException {
        namespaceFilterManager.enableIfPresent(namespace);

        // Validate that content is provided
        if (content == null || content.isEmpty()) {
            throw new IllegalArgumentException(ErrorMessages.SCHEMA_CONTENT_REQUIRED);
        }

        String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;
        String tenantName = TenantContext.getTenantName();

        // Resolve tenant_id and nmspc_id
        UUID tenantId = tenantRepository.findByTenantName(tenantName)
                .map(Tenant::getTenantId)
                .orElseThrow(() -> new IllegalArgumentException(ErrorMessages.TENANT_CONFIG_INVALID));

        // SECURITY: Use tenant-aware namespace lookup to ensure proper isolation
        // Auto-create namespace if it doesn't exist
        UUID nmspcId = nameSpaceService.ensureNamespaceExists(tenantId, namespace);

        // SECURITY: Use tenant+namespace-aware lookup to ensure proper multi-tenant isolation
        // Check if schema name already exists within this tenant and namespace
        Optional<Schm> exists = schmRepository.findByTenantIdAndNmspcIdAndSchmName(
                tenantId, nmspcId, schemaDto.getName());
        if(exists.isPresent()) {
            throw new IllegalArgumentException(ErrorMessages.SCHEMA_IMPORT_EXISTS);
        }

        Schm schema = mapToSchmEntity(schemaDto);
        schema.setTenantId(tenantId);
        schema.setNmspcId(nmspcId);
        schema.setCreatedBy(userName);
        schema.setUpdatedBy(userName);

        Schm saved;
        try {
            // Save schema - DB unique constraint handles race condition
            saved = schmRepository.save(schema);
        } catch (DataIntegrityViolationException e) {
            // Use database-agnostic JDBC SQLState codes instead of string matching
            if (DatabaseExceptionUtils.isUniqueConstraintViolation(e, "schm_name")) {
                throw new IllegalArgumentException(ErrorMessages.SCHEMA_IMPORT_EXISTS);
            }
            throw e;
        }

        // Create initial version with content
        createSchemaDataFromFile(saved.getSchmId(), content);

        // Call existing publish method to maintain consistency and reuse validation logic
        // Spring will join the existing transaction (PROPAGATION_REQUIRED default)
        try {
            publishSchemaVersion(namespace, saved.getSchmId(), 1);
        } catch (Exception e) {
            throw new IllegalStateException(ErrorMessages.SCHEMA_PUBLISH_FAILED_ON_IMPORT, e);
        }

        // Refresh to get latest state with published version
        // Don't fall back to stale data - throw exception if refresh fails
        return mapToSchemaResponse(schmRepository.findBySchmId(saved.getSchmId())
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.SCHEMA_NOT_FOUND_AFTER_IMPORT)));
    }

    /**
     * Import multiple schemas with content. Processes each schema independently.
     * On successful save, publishes the saved version.
     * Each import runs in its own transaction (inherited from importSchema method).
     *
     * @param namespace - Namespace for the schemas
     * @param importRequests - List of schema import requests (each containing schema and content)
     * @return List of import responses (success/failure per schema)
     */
    public List<SchemaImportResponse> importSchemas(String namespace, List<SchemaImportRequest> importRequests) {
        List<SchemaImportResponse> responses = new ArrayList<>();

        for (SchemaImportRequest request : importRequests) {
            try {
                // Validate request structure
                if (request.getSchema() == null) {
                    throw new IllegalArgumentException(ErrorMessages.SCHEMA_REQUIRED);
                }
                if (request.getSchema().getName() == null || request.getSchema().getName().trim().isEmpty()) {
                    throw new IllegalArgumentException(ErrorMessages.SCHEMA_NAME_REQUIRED);
                }

                SchemaDto imported = importSchema(namespace, request.getSchema(), request.getContent());
                responses.add(new SchemaImportResponse(imported));
            } catch (IllegalArgumentException e) {
                String schemaName = (request.getSchema() != null && request.getSchema().getName() != null)
                        ? request.getSchema().getName()
                        : null;
                responses.add(new SchemaImportResponse(
                        schemaName,
                        ErrorMessages.ERROR_PREFIX_BAD_REQUEST + e.getMessage()));
            } catch (IllegalStateException e) {
                responses.add(new SchemaImportResponse(
                        request.getSchema().getName(),
                        ErrorMessages.ERROR_PREFIX_CONFLICT + e.getMessage()));
            } catch (IOException e) {
                responses.add(new SchemaImportResponse(
                        request.getSchema().getName(),
                        ErrorMessages.ERROR_PREFIX_INTERNAL_SERVER + ErrorMessages.SCHEMA_CREATION_FAILED));
            } catch (Exception e) {
                String schemaName = (request.getSchema() != null && request.getSchema().getName() != null)
                        ? request.getSchema().getName()
                        : null;
                responses.add(new SchemaImportResponse(
                        schemaName,
                        ErrorMessages.ERROR_PREFIX_ERROR + e.getMessage()));
            }
        }

        return responses;
    }

    /**
     * Export a single schema with its published version content.
     * Returns schema information (name, description, type, contentType, group) and published content.
     *
     * @param namespace - Namespace for the schema
     * @param schmId - Schema ID
     * @return Schema export DTO with published content
     */
    public SchemaExportDto exportSchema(String namespace, UUID schmId) {
        namespaceFilterManager.enableIfPresent(namespace);

        // Find schema
        Schm schema = schmRepository.findBySchmId(schmId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(ErrorMessages.SCHEMA_NOT_FOUND_BY_ID, schmId)));

        // Check if schema has published version
        if (schema.getPublishVersion() == null) {
            throw new IllegalStateException(String.format(ErrorMessages.SCHEMA_NO_PUBLISHED_VERSION, schema.getSchmName()));
        }

        // Get published version content
        String content = getPublishedContent(namespace, schmId)
                .orElseThrow(() -> new IllegalStateException(String.format(ErrorMessages.SCHEMA_PUBLISHED_CONTENT_NOT_FOUND, schema.getSchmName())));

        // Map to export DTO
        SchemaExportDto exportDto = new SchemaExportDto();
        exportDto.setName(schema.getSchmName());
        exportDto.setDescription(schema.getSchmDesc());
        exportDto.setSchemaType(schema.getSchemaType());
        exportDto.setContentType(schema.getContentType());
        exportDto.setSchmGroup(schema.getSchmGroup());
        exportDto.setContent(content);

        return exportDto;
    }

    /**
     * Export multiple schemas with their published version content.
     * Processes each schema independently based on schmId or name.
     *
     * @param namespace - Namespace for the schemas
     * @param exportRequests - List of schema export requests (containing schmId and/or name)
     * @return List of export responses (success/failure per schema)
     */
    public List<SchemaExportResponse> exportSchemas(String namespace, List<SchemaExportRequest> exportRequests) {
        namespaceFilterManager.enableIfPresent(namespace);

        List<SchemaExportResponse> responses = new ArrayList<>();

        for (SchemaExportRequest request : exportRequests) {
            try {
                UUID schmId;

                // Determine schema ID from request
                if (request.getSchmId() != null) {
                    schmId = request.getSchmId();
                } else if (request.getName() != null && !request.getName().isEmpty()) {
                    // Find schema by name
                    Schm schema = schmRepository.findBySchmName(request.getName())
                            .orElseThrow(() -> new IllegalArgumentException(String.format(ErrorMessages.SCHEMA_NOT_FOUND_BY_NAME, request.getName())));
                    schmId = schema.getSchmId();
                } else {
                    throw new IllegalArgumentException(ErrorMessages.SCHEMA_ID_OR_NAME_REQUIRED);
                }

                // Export schema
                SchemaExportDto exportDto = exportSchema(namespace, schmId);
                responses.add(new SchemaExportResponse(exportDto));

            } catch (IllegalArgumentException e) {
                String schemaName = request.getName() != null ? request.getName() :
                                   (request.getSchmId() != null ? request.getSchmId().toString() : "Unknown");
                responses.add(new SchemaExportResponse(
                        schemaName,
                        ErrorMessages.ERROR_PREFIX_NOT_FOUND + e.getMessage()));
            } catch (IllegalStateException e) {
                String schemaName = request.getName() != null ? request.getName() :
                                   (request.getSchmId() != null ? request.getSchmId().toString() : "Unknown");
                responses.add(new SchemaExportResponse(
                        schemaName,
                        ErrorMessages.ERROR_PREFIX_ERROR + e.getMessage()));
            } catch (Exception e) {
                String schemaName = request.getName() != null ? request.getName() :
                                   (request.getSchmId() != null ? request.getSchmId().toString() : "Unknown");
                responses.add(new SchemaExportResponse(
                        schemaName,
                        ErrorMessages.ERROR_PREFIX_ERROR + e.getMessage()));
            }
        }

        return responses;
    }

    /**
     * Get schema by ID with optional version filtering and pagination
     *
     * @param namespace - Namespace filter
     * @param schmId - Schema ID
     * @param versionNumber - Optional version number filter
     * @param versionName - Optional version name filter (published, latest)
     * @param pageable - Pagination parameters (applies to versions list)
     * @return Paginated schema with versions
     */
    public Page<SchemaWithVersionDto> getSchemasById(String namespace, UUID schmId, String versionNumber, String versionName, Pageable pageable) {
        namespaceFilterManager.enableIfPresent(namespace);

        Optional<Schm> schemaOpt = schmRepository.findBySchmId(schmId);
        if (schemaOpt.isEmpty()) {
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }

        Schm schema = schemaOpt.get();
        List<SchmData> versions = new ArrayList<>();

        // Handle special version names
        if (versionName != null) {
            if (AppConstants.VERSION_NAME_PUBLISHED.equalsIgnoreCase(versionName)) {
                schmRepository.getPublishedVersion(schmId).ifPresent(versions::add);
            } else if (AppConstants.VERSION_NAME_LATEST.equalsIgnoreCase(versionName)) {
                schmRepository.getLatestVersion(schmId).ifPresent(versions::add);
            }
        } else if (versionNumber != null) {
            // Get specific version
            schmRepository.getSchemaVersion(schmId, Integer.parseInt(versionNumber)).ifPresent(versions::add);
        } else {
            // Get all versions
            versions = schmRepository.getAllVersions(schmId);
        }

        // Apply pagination to versions list
        // SECURITY: Validate offset to prevent integer overflow DoS attack
        long offset = pageable.getOffset();
        if (offset < 0 || offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    String.format(ErrorMessages.INVALID_PAGINATION_OFFSET,
                            offset, Integer.MAX_VALUE));
        }
        int start = (int) offset;
        int end = Math.min(start + pageable.getPageSize(), versions.size());

        List<SchemaVersionDto> paginatedVersions;
        // Use >= to handle edge case where start equals size
        if (start >= versions.size()) {
            paginatedVersions = new ArrayList<>();
        } else {
            paginatedVersions = versions.subList(start, end).stream()
                    .map(this::mapToVersionResponse)
                    .collect(Collectors.toList());
        }

        SchemaWithVersionDto response = new SchemaWithVersionDto();
        response.setSchema(mapToSchemaResponse(schema));
        response.setVersions(paginatedVersions);

        // Return page with single element (the schema), but versions inside are paginated
        return new PageImpl<>(List.of(response), pageable, 1);
    }

    /**
     * Update an existing schema (schmName is non-editable, always fetched from DB)
     */
    @Transactional
    public SchemaDto updateSchema(String namespace, UUID schmId, SchemaDto schemaDto) {
        namespaceFilterManager.enableIfPresent(namespace);

        // Use pessimistic locking to prevent lost updates during concurrent modifications
        // Fetch existing entity - schmName is non-editable and always from DB
        Schm existing = schmRepository.findWithLockBySchmId(schmId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(ErrorMessages.SCHEMA_NOT_FOUND, schmId)));

        String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

        // Update only editable fields (schmName is preserved from DB)
        existing.setSchmDesc(schemaDto.getDescription());
        existing.setSchemaType(schemaDto.getSchemaType());
        existing.setContentType(schemaDto.getContentType());
        existing.setSchmGroup(schemaDto.getSchmGroup());
        existing.setLockBy(schemaDto.getLockBy());
        existing.setUpdatedBy(userName);

        Schm updated = schmRepository.save(existing);
        return mapToSchemaResponse(updated);
    }

    /**
     * Get specific schema version
     */
    public Optional<SchemaVersionDto> getSchemaVersion(String namespace, UUID schmId, Integer versionNumber) {
        namespaceFilterManager.enableIfPresent(namespace);
        return schmRepository.getSchemaVersion(schmId, versionNumber)
                .map(this::mapToVersionResponse);
    }

    /**
     * Publish a specific schema version
     */
    @Transactional
    public void publishSchemaVersion(String namespace, UUID schmId, Integer versionNumber) {
        namespaceFilterManager.enableIfPresent(namespace);

        // Use pessimistic locking to prevent race conditions during publish
        // This ensures the read-modify-write sequence is atomic
        Optional<Schm> schemaOpt = schmRepository.findWithLockBySchmId(schmId);
        if(schemaOpt.isPresent()){
            Schm schema = schemaOpt.get();
            if (schema.getPublishVersion() != null && schema.getPublishVersion().equals(versionNumber)) {
                    throw new IllegalStateException(String.format(ErrorMessages.SCHEMA_ALREADY_PUBLISHED, schmId, schema.getPublishVersion()));
                }

            // Verify the version exists
            Optional<SchmData> versionOpt = schmRepository.getSchemaVersion(schmId, versionNumber);
            if (versionOpt.isPresent()) {
                SchmData schemaData = versionOpt.get();
                // Set isDraft to false when publishing
                schemaData.setIsDraft(false);
                schmDataRepository.save(schemaData);

                // Get current user for audit trail
                String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                        ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

                // Update schema: set publish version and optionally clear draft
                schema.setPublishVersion(versionNumber);
                schema.setUpdatedBy(userName);
                // updatedDatetime is automatically set by @UpdateTimestamp

                // Only clear draft version if we're publishing the current draft
                // This prevents orphaning draft work when publishing an older version
                if (schema.getDraftVersion() != null && schema.getDraftVersion().equals(versionNumber)) {
                    schema.setDraftVersion(null);
                }

                schmRepository.save(schema);
            } else {
                throw new IllegalArgumentException(String.format(ErrorMessages.SCHEMA_VERSION_NOT_FOUND, versionNumber, schmId));
            }
        }
    }

    /**
     * Unpublish a schema by setting publish version to null.
     * Uses pessimistic locking to prevent race conditions where a reference
     * could be created between checking for references and unpublishing.
     */
    @Transactional
    public void unPublishSchemaVersion(String namespace, UUID schmId) {
        namespaceFilterManager.enableIfPresent(namespace);

        // Use pessimistic write lock to prevent concurrent modifications
        // This ensures no other transaction can create references while we're unpublishing
        Optional<Schm> schemaOpt = schmRepository.findWithLockBySchmId(schmId);

        if (schemaOpt.isPresent()) {
            // Check if schema is referenced by any external references
            // The lock held above prevents new references from being created during this check
            List<SchmExtRefXref> xrefs = schmExtRefXrefRepository.findBySchmId(schmId);
            if (!xrefs.isEmpty()) {
                throw new IllegalStateException(String.format(ErrorMessages.SCHEMA_IN_USE, schmId));
            }

            Schm schema = schemaOpt.get();
            schema.setPublishVersion(null);
            schmRepository.save(schema);
        } else {
            throw new IllegalArgumentException(String.format(ErrorMessages.SCHEMA_NOT_FOUND, schmId));
        }
    }

    /**
     * Get published version
     */
    public Optional<SchemaVersionDto> getPublishedVersion(String namespace, UUID schmId) {
        namespaceFilterManager.enableIfPresent(namespace);
        return schmRepository.getPublishedVersion(schmId)
                .map(this::mapToVersionResponse);
    }

    /**
     * Get draft version
     */
    public Optional<SchemaVersionDto> getDraftVersion(String namespace, UUID schmId) {
        namespaceFilterManager.enableIfPresent(namespace);
        return schmRepository.findBySchmId(schmId)
                .flatMap(schema -> {
                    if (schema.getDraftVersion() != null) {
                        return schmDataRepository.findById(
                                new SchmDataId(schmId, schema.getDraftVersion()));
                    }
                    return Optional.empty();
                })
                .map(this::mapToVersionResponse);
    }

    /**
     * Get published content
     */
    public Optional<String> getPublishedContent(String namespace, UUID schmId) {
        namespaceFilterManager.enableIfPresent(namespace);
        return schmRepository.getPublishedVersion(schmId)
                .map(SchmData::getSchmData);
    }

    /**
     * Get version content
     */
    public Optional<String> getVersionContent(String namespace, UUID schmId, Integer versionNumber) {
        namespaceFilterManager.enableIfPresent(namespace);
        return schmRepository.getSchemaVersion(schmId, versionNumber)
                .map(SchmData::getSchmData);
    }

    /**
     * Get draft content
     */
    public Optional<String> getDraftContent(String namespace, UUID schmId) {
        namespaceFilterManager.enableIfPresent(namespace);
        return schmRepository.findBySchmId(schmId)
                .flatMap(schema -> {
                    if (schema.getDraftVersion() != null) {
                        return schmDataRepository.findById(
                                new SchmDataId(schmId, schema.getDraftVersion()));
                    }
                    return Optional.empty();
                })
                .map(SchmData::getSchmData);
    }

    /**
     * Update draft content or create new version if draft doesn't exist
     */
    @Transactional
    public SchemaVersionDto updateDraftContent(String namespace, UUID schmId, String content) {
        namespaceFilterManager.enableIfPresent(namespace);

        // Use pessimistic locking to prevent race conditions during draft updates
        // This ensures the read-modify-write sequence is atomic
        Schm schema = schmRepository.findWithLockBySchmId(schmId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(ErrorMessages.SCHEMA_NOT_FOUND, schmId)));

        Optional<SchmData> draftOpt = Optional.empty();
        if (schema.getDraftVersion() != null) {
            draftOpt = schmDataRepository.findById(
                    new SchmDataId(schmId, schema.getDraftVersion()));
        }

        if (draftOpt.isPresent()) {
            // Update existing draft content only (schema draft_version pointer stays the same)
            String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                    ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

            SchmData draft = draftOpt.get();
            draft.setSchmData(content);
            draft.setUpdatedBy(userName);
            // updatedDatetime is automatically set by @UpdateTimestamp
            SchmData updated = schmDataRepository.save(draft);

            // NOTE: We do NOT save the schema entity here because draft_version didn't change
            // The schema entity was fetched with pessimistic lock, but we're not modifying it
            // Hibernate won't auto-flush it because we haven't changed any fields

            return mapToVersionResponse(updated);
        } else {
            // Create new version
            Integer latestVersion = schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc(schmId)
                    .map(sd -> sd.getId().getSchmVersion())
                    .orElse(0);

            String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                    ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

            SchmDataId newId = new SchmDataId();
            newId.setSchmId(schmId);
            newId.setSchmVersion(latestVersion + 1);

            SchmData newVersion = new SchmData();
            newVersion.setId(newId);
            newVersion.setSchmData(content);
            newVersion.setIsDraft(true);
            newVersion.setUpdatedBy(userName);
            newVersion.setCreatedBy(userName);
            newVersion.setCreatedDatetime(LocalDateTime.now());
            newVersion.setUpdatedDatetime(LocalDateTime.now());

            SchmData saved = schmDataRepository.save(newVersion);

            // Update draft version in SCHM table WITH audit fields
            // Creating a new draft IS a schema-level change
            schema.setDraftVersion(newId.getSchmVersion());
            schema.setUpdatedBy(userName);
            // updatedDatetime is automatically set by @UpdateTimestamp
            schmRepository.save(schema);

            return mapToVersionResponse(saved);
        }
    }

    /**
     * Lock a schema for editing.
     * This operation is idempotent - if the same user calls lock multiple times,
     * it will succeed without error. This allows clients to safely retry lock operations
     * and ensures lock state remains consistent even with concurrent requests from the same user.
     *
     * Uses pessimistic locking to prevent race conditions where different users
     * might try to acquire the lock simultaneously.
     *
     * @param namespace the namespace
     * @param schmId the schema ID
     * @throws IllegalArgumentException if schema not found
     * @throws IllegalStateException if schema is locked by a different user
     */
    @Transactional
    public void lockSchema(String namespace, UUID schmId) {
        namespaceFilterManager.enableIfPresent(namespace);

        String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

        // Use pessimistic locking to prevent race condition between different users
        Schm schema = schmRepository.findWithLockBySchmId(schmId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(ErrorMessages.SCHEMA_NOT_FOUND, schmId)));

        // Check if already locked by a DIFFERENT user
        // Note: If locked by the SAME user, this is idempotent and succeeds
        if (schema.getLockBy() != null && !schema.getLockBy().equals(userName)) {
            throw new IllegalStateException(
                    String.format(ErrorMessages.SCHEMA_ALREADY_LOCKED, schmId, schema.getLockBy()));
        }

        // Set lock (idempotent if already locked by same user)
        schema.setLockBy(userName);
        schema.setUpdatedBy(userName);
        // updatedDatetime is automatically set by @UpdateTimestamp
        schmRepository.save(schema);
    }

    /**
     * Unlock a schema.
     * Uses pessimistic locking to prevent race conditions during unlock operations.
     *
     * Only the user who locked the schema (or SYSTEM_USER) can unlock it.
     *
     * @param namespace the namespace
     * @param schmId the schema ID
     * @throws IllegalArgumentException if schema not found
     * @throws IllegalStateException if schema is locked by a different user
     */
    @Transactional
    public void unlockSchema(String namespace, UUID schmId) {
        namespaceFilterManager.enableIfPresent(namespace);

        String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

        // Use pessimistic locking to prevent race condition
        Schm schema = schmRepository.findWithLockBySchmId(schmId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(ErrorMessages.SCHEMA_NOT_FOUND, schmId)));

        // Verify the current user owns the lock (or SYSTEM_USER can unlock any)
        if (schema.getLockBy() != null && !schema.getLockBy().equals(userName)
                && !AppConstants.SYSTEM_USER.equals(userName)) {
            throw new IllegalStateException(
                    String.format(ErrorMessages.SCHEMA_UNLOCK_NOT_PERMITTED, schmId, schema.getLockBy()));
        }

        // Clear lock with audit field updates
        schema.setLockBy(null);
        schema.setUpdatedBy(userName);
        // updatedDatetime is automatically set by @UpdateTimestamp
        schmRepository.save(schema);
    }

    /**
     * Get external references for a schema with pagination
     *
     * @param namespace - Namespace filter
     * @param schmId - Schema ID
     * @param pageable - Pagination parameters
     * @return Paginated list of external references
     */
    @Transactional(readOnly = true)
    public Page<ExtRefDto> getExternalReferencesBySchemaId(String namespace, UUID schmId, Pageable pageable) {
        namespaceFilterManager.enableIfPresent(namespace);

        // Verify schema exists
        schmRepository.findBySchmId(schmId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(ErrorMessages.SCHEMA_NOT_FOUND, schmId)));

        // Get all cross-references for this schema
        List<SchmExtRefXref> xrefs = schmExtRefXrefRepository.findBySchmId(schmId);

        // Batch fetch all external references (fix N+1 query problem)
        // With composite key, we need both ext_ref_id and ext_ref_version
        List<ExtRefId> extRefIds = xrefs.stream()
                .map(xref -> new ExtRefId(xref.getExtRefId(), xref.getExtRefVersion()))
                .collect(Collectors.toList());

        List<ExtRef> extRefs = extRefRepository.findAllById(extRefIds);

        // Map to DTOs
        List<ExtRefDto> extRefDtos = extRefs.stream()
                .map(this::mapExtRefToDto)
                .sorted(Comparator.comparing(
                        ExtRefDto::getExtRefName,
                        Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)))
                .collect(Collectors.toList());

        // Apply pagination manually
        // SECURITY: Validate offset to prevent integer overflow DoS attack
        long offset = pageable.getOffset();
        if (offset < 0 || offset > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(
                    String.format(ErrorMessages.INVALID_PAGINATION_OFFSET,
                            offset, Integer.MAX_VALUE));
        }
        int start = (int) offset;
        int end = Math.min(start + pageable.getPageSize(), extRefDtos.size());

        // Use >= to handle edge case where start equals size
        if (start >= extRefDtos.size()) {
            return new PageImpl<>(new ArrayList<>(), pageable, extRefDtos.size());
        }

        List<ExtRefDto> paginatedList = extRefDtos.subList(start, end);
        return new PageImpl<>(paginatedList, pageable, extRefDtos.size());
    }

    /**
     * Map ExtRef entity to ExtRefDto
     */
    private ExtRefDto mapExtRefToDto(ExtRef extRef) {
        ExtRefDto dto = new ExtRefDto();
        dto.setExtRefId(extRef.getExtRefId());
        dto.setExtRefName(extRef.getExtRefName());
        dto.setExtRefType(extRef.getExtRefType().name());
        dto.setExtRefVersion(extRef.getExtRefVersion());
        dto.setCreatedDatetime(extRef.getCreatedDatetime());
        dto.setUpdatedDatetime(extRef.getUpdatedDatetime());
        dto.setCreatedBy(extRef.getCreatedBy());
        dto.setUpdatedBy(extRef.getUpdatedBy());
        return dto;
    }

    /**
     * Create schema data from multipart file
     */
    private void createSchemaDataFromFile(UUID schmId, String content) throws IOException {
        Integer latestVersion = schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc(schmId)
                .map(sd -> sd.getId().getSchmVersion())
                .orElse(0);

        String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

        SchmDataId newId = new SchmDataId();
        newId.setSchmId(schmId);
        newId.setSchmVersion(latestVersion + 1);

        SchmData newVersion = new SchmData();
        newVersion.setId(newId);
        newVersion.setSchmData(content);
        newVersion.setIsDraft(true);
        newVersion.setUpdatedBy(userName);
        newVersion.setCreatedBy(userName);

        schmDataRepository.save(newVersion);

        // Update draft version in SCHM table WITH audit fields
        // Creating a new draft IS a schema-level change
        Schm schema = schmRepository.findBySchmId(schmId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(ErrorMessages.SCHEMA_NOT_FOUND, schmId)));
        schema.setDraftVersion(newId.getSchmVersion());
        schema.setUpdatedBy(userName);
        // updatedDatetime is automatically set by @UpdateTimestamp
        schmRepository.save(schema);
    }

    /**
     * Map Schm entity to SchemaResponse DTO
     * @param withVersion - Controls what version information to include (none, draft, published, latest)
     */
    private SchemaDto mapToSchemaResponse(Schm schm, String withVersion) {
        SchemaDto response = new SchemaDto();
        response.setId(schm.getSchmId());
        response.setName(schm.getSchmName());
        response.setDescription(schm.getSchmDesc());
        response.setSchemaType(schm.getSchemaType());
        response.setContentType(schm.getContentType());
        response.setLockBy(schm.getLockBy());
        response.setSchmGroup(schm.getSchmGroup());
        response.setCreatedByUser(schm.getCreatedBy());
        response.setCreateDateTime(schm.getCreatedDatetime());
        response.setModifiedByUser(schm.getUpdatedBy());
        response.setModifiedDateTime(schm.getUpdatedDatetime());

        // Set published version number from SCHM table
        if (schm.getPublishVersion() != null) {
            response.setPublished(schm.getPublishVersion());
        }

        // Set draft version number from SCHM table
        if (schm.getDraftVersion() != null) {
            response.setDraft(schm.getDraftVersion());
        }

        // Populate version object based on withVersion parameter
        if (withVersion != null && !AppConstants.VERSION_NAME_NONE.equalsIgnoreCase(withVersion)) {
            SchemaVersionDto versionDto = null;

            switch (withVersion.toLowerCase()) {
                case AppConstants.VERSION_NAME_DRAFT:
                    // Get draft version
                    if (schm.getDraftVersion() != null) {
                        versionDto = schmDataRepository.findById(
                                new SchmDataId(schm.getSchmId(), schm.getDraftVersion()))
                                .map(this::mapToVersionResponse)
                                .orElse(null);
                    }
                    break;

                case AppConstants.VERSION_NAME_PUBLISHED:
                    // Get published version
                    if (schm.getPublishVersion() != null) {
                        versionDto = schmDataRepository.findById(
                                new SchmDataId(schm.getSchmId(), schm.getPublishVersion()))
                                .map(this::mapToVersionResponse)
                                .orElse(null);
                    }
                    break;

                case AppConstants.VERSION_NAME_LATEST:
                    // Get latest version (draft if exists, otherwise published)
                    if (schm.getDraftVersion() != null) {
                        versionDto = schmDataRepository.findById(
                                new SchmDataId(schm.getSchmId(), schm.getDraftVersion()))
                                .map(this::mapToVersionResponse)
                                .orElse(null);
                    } else if (schm.getPublishVersion() != null) {
                        versionDto = schmDataRepository.findById(
                                new SchmDataId(schm.getSchmId(), schm.getPublishVersion()))
                                .map(this::mapToVersionResponse)
                                .orElse(null);
                    }
                    break;

                default:
                    // For any other value, don't populate version
                    break;
            }

            response.setVersion(versionDto);
        }

        return response;
    }

    /**
     * Map Schm entity to SchemaResponse DTO (without version filtering)
     */
    private SchemaDto mapToSchemaResponse(Schm schm) {
        return mapToSchemaResponse(schm, null);
    }

    /**
     * Map SchmData entity to SchemaVersionResponse DTO
     */
    private SchemaVersionDto mapToVersionResponse(SchmData data) {
        SchemaVersionDto response = new SchemaVersionDto();
        response.setVersionNumber(data.getId().getSchmVersion());
        response.setContent("/schemas/" + data.getId().getSchmId() + "/version/" + data.getId().getSchmVersion() + "/content");
        response.setIsDraft(data.getIsDraft());
        response.setCreatedByUser(data.getCreatedBy());
        response.setCreateDateTime(data.getCreatedDatetime());
        response.setModifiedByUser(data.getUpdatedBy());
        response.setModifiedDateTime(data.getUpdatedDatetime());
        return response;
    }

    /**
     * Map SchemaResponse DTO to Schm entity
     */
    private Schm mapToSchmEntity(SchemaDto response) {
        Schm schm = new Schm();
        schm.setSchmId(response.getId());
        schm.setSchmName(response.getName());
        schm.setSchmDesc(response.getDescription());
        schm.setSchemaType(response.getSchemaType());
        schm.setContentType(response.getContentType());
        schm.setLockBy(response.getLockBy());
        schm.setSchmGroup(response.getSchmGroup());
        schm.setCreatedDatetime(response.getCreateDateTime());
        schm.setUpdatedDatetime(response.getModifiedDateTime());
        return schm;
    }

    /**
     * Map SchemaVersionResponse DTO to SchmData entity
     */
    private SchmData mapToSchmDataEntity(SchemaVersionDto response, UUID schmId) {
        SchmDataId id = new SchmDataId();
        id.setSchmId(schmId);
        id.setSchmVersion(response.getVersionNumber());

        SchmData schmData = new SchmData();
        schmData.setId(id);
       // schmData.setSchmVersionName(response.getIsDraft() ? "draft" : null);
        schmData.setIsDraft(response.getIsDraft());
        schmData.setCreatedDatetime(response.getCreateDateTime());
        schmData.setUpdatedDatetime(response.getModifiedDateTime());

        return schmData;
    }

}
