package com.ssnc.schemaService.service;

import com.ssnc.schemaService.constants.AppConstants;
import com.ssnc.schemaService.constants.ErrorMessages;
import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.dto.SchemaVersionDto;
import com.ssnc.schemaService.dto.SchemaWithVersionDto;
import com.ssnc.schemaService.entity.ExtRef;
import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmData;
import com.ssnc.schemaService.entity.SchmDataId;
import com.ssnc.schemaService.entity.SchmExtRefXref;
import com.ssnc.schemaService.repo.ExtRefRepository;
import com.ssnc.schemaService.repo.SchmDataRepository;
import com.ssnc.schemaService.repo.SchmExtRefXrefRepository;
import com.ssnc.schemaService.repo.SchmFilterCriteria;
import com.ssnc.schemaService.repo.SchmRepository;
import com.ssnc.schemaService.repo.SchmSpecifications;
import com.ssnc.schemaService.tenant.NamespaceFilterManager;
import com.ssnc.schemaService.tenant.TenantContext;
import com.ssnc.shared.security.JwtClaimsContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

    /**
     * Get schemas with optional filtering and sorting
     */
    public List<SchemaDto> getSchemas(String namespace, String name, String type, String group,
                                       String modifiedByUser, String versionModifiedByUser,
                                       String sort, String withVersion) {
        namespaceFilterManager.enableIfPresent(namespace);

        SchmFilterCriteria criteria = new SchmFilterCriteria();
        criteria.setName(name);
        criteria.setSchemaType(type);
        criteria.setGroup(group);
        criteria.setModifiedByUser(modifiedByUser);
        criteria.setVersionModifiedByUser(versionModifiedByUser);
        criteria.setSort(sort);
        criteria.setWithVersion(withVersion);

        List<Schm> schemas = schmRepository.findAll(SchmSpecifications.withFilters(criteria));


        // Map to DTOs and apply version filtering
        return schemas.stream()
                .map(this::mapToSchemaResponse)
                .filter(schemaDto -> filterByVersion(schemaDto, withVersion))
                .sorted(getSortComparator(sort))
                .collect(Collectors.toList());
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
                    Comparator.nullsFirst(Comparator.naturalOrder()));
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
                        Comparator.nullsFirst(Comparator.naturalOrder()));
            case AppConstants.SORT_NAME_DESC:
                return Comparator.comparing(SchemaDto::getName,
                        Comparator.nullsFirst(Comparator.naturalOrder())).reversed();
            default:
                return Comparator.comparing(SchemaDto::getName,
                        Comparator.nullsFirst(Comparator.naturalOrder()));
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

        Schm schema = mapToSchmEntity(schemaDto);
        schema.setNamespace(namespace);
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
     * Get schema by ID with optional version filtering
     */
    public List<SchemaWithVersionDto> getSchemasById(String namespace, UUID schmId, String versionNumber, String versionName) {
        namespaceFilterManager.enableIfPresent(namespace);

        Optional<Schm> schemaOpt = schmRepository.findBySchmId(schmId);
        if (schemaOpt.isEmpty()) {
            return new ArrayList<>();
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

        SchemaWithVersionDto response = new SchemaWithVersionDto();
        response.setSchema(mapToSchemaResponse(schema));
        response.setVersions(versions.stream().map(this::mapToVersionResponse).collect(Collectors.toList()));

        return List.of(response);
    }

    /**
     * Update an existing schema (schmName is non-editable, always fetched from DB)
     */
    @Transactional
    public SchemaDto updateSchema(String namespace, UUID schmId, SchemaDto schemaDto) {
        namespaceFilterManager.enableIfPresent(namespace);

        // Fetch existing entity - schmName is non-editable and always from DB
        Schm existing = schmRepository.findBySchmId(schmId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(ErrorMessages.SCHEMA_NOT_FOUND, schmId)));

        String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : AppConstants.SYSTEM_USER;

        // Update only editable fields (schmName is preserved from DB)
        existing.setSchmDesc(schemaDto.getDescription());
        existing.setSchemaType(schemaDto.getSchemaType());
        existing.setContentType(schemaDto.getContentType());
        existing.setGroup(schemaDto.getGroup());
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
        //Verify schema exists before trying to publish version
        Optional<Schm> schemaOpt = schmRepository.findBySchmId(schmId);
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

                schema.setPublishVersion(versionNumber);
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
        return schmDataRepository.findByIdSchmIdAndIsDraft(schmId, true)
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
        return schmDataRepository.findByIdSchmIdAndIsDraft(schmId, true)
                .map(SchmData::getSchmData);
    }

    /**
     * Update draft content or create new version if draft doesn't exist
     */
    @Transactional
    public SchemaVersionDto updateDraftContent(String namespace, UUID schmId, String content) {
        namespaceFilterManager.enableIfPresent(namespace);

        Optional<SchmData> draftOpt = schmDataRepository.findByIdSchmIdAndIsDraft(schmId, true);

        if (draftOpt.isPresent()) {
            // Update existing draft
            SchmData draft = draftOpt.get();
            draft.setSchmData(content);
            SchmData updated = schmDataRepository.save(draft);
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

        schema.setLockBy(null);
        schmRepository.save(schema);
    }

    /**
     * Get external references for a schema
     */
    @Transactional(readOnly = true)
    public List<ExtRefDto> getExternalReferencesBySchemaId(String namespace, UUID schmId) {
        namespaceFilterManager.enableIfPresent(namespace);

        // Verify schema exists
        schmRepository.findBySchmId(schmId)
                .orElseThrow(() -> new IllegalArgumentException(String.format(ErrorMessages.SCHEMA_NOT_FOUND, schmId)));

        // Get all cross-references for this schema
        List<SchmExtRefXref> xrefs = schmExtRefXrefRepository.findBySchmId(schmId);

        // Batch fetch all external references (fix N+1 query problem)
        List<UUID> extRefIds = xrefs.stream()
                .map(SchmExtRefXref::getExtRefId)
                .collect(Collectors.toList());

        List<ExtRef> extRefs = extRefRepository.findAllById(extRefIds);

        return extRefs.stream()
                .map(this::mapExtRefToDto)
                .collect(Collectors.toList());
    }

    /**
     * Map ExtRef entity to ExtRefDto
     */
    private ExtRefDto mapExtRefToDto(ExtRef extRef) {
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
    }

    /**
     * Map Schm entity to SchemaResponse DTO
     */
    private SchemaDto mapToSchemaResponse(Schm schm) {
        SchemaDto response = new SchemaDto();
        response.setId(schm.getSchmId());
        response.setName(schm.getSchmName());
        response.setDescription(schm.getSchmDesc());
        response.setSchemaType(schm.getSchemaType());
        response.setContentType(schm.getContentType());
        response.setLockBy(schm.getLockBy());
        response.setGroup(schm.getGroup());
        response.setCreatedByUser(schm.getCreatedBy());
        response.setCreateDateTime(schm.getCreatedDatetime());
        response.setModifiedByUser(schm.getUpdatedBy());
        response.setModifiedDateTime(schm.getUpdatedDatetime());

        // Set published version number from SCHM table
        if (schm.getPublishVersion() != null) {
            response.setPublished(String.valueOf(schm.getPublishVersion()));
        }

        // Set draft version number from SCHM_DATA table where isDraft = Y
        schmDataRepository.findByIdSchmIdAndIsDraft(schm.getSchmId(), true)
                .ifPresent(draft -> response.setDraft(String.valueOf(draft.getId().getSchmVersion())));

        return response;
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
        schm.setGroup(response.getGroup());
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
