package com.ssnc.schemaService.service;

import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.dto.SchemaVersionDto;
import com.ssnc.schemaService.dto.SchemaWithVersionDto;
import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmData;
import com.ssnc.schemaService.entity.SchmDataId;
import com.ssnc.schemaService.repo.SchmDataRepository;
import com.ssnc.schemaService.repo.SchmFilterCriteria;
import com.ssnc.schemaService.repo.SchmRepository;
import com.ssnc.schemaService.repo.SchmSpecifications;
import com.ssnc.schemaService.tenant.NamespaceFilterManager;
import com.ssnc.schemaService.tenant.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    /**
     * Get schemas with optional filtering by type and group
     */
    public List<SchemaDto> getSchemas(String namespace, String type, String group, boolean publishedOnly) {
        namespaceFilterManager.enableIfPresent(namespace);

        SchmFilterCriteria criteria = new SchmFilterCriteria();
        criteria.setSchemaType(type);
        criteria.setGroup(group);
        criteria.setPublishedOnly(publishedOnly);

        List<Schm> schemas = schmRepository.findAll(SchmSpecifications.withFilters(criteria));
        return schemas.stream().map(this::mapToSchemaResponse).collect(Collectors.toList());
    }

    /**
     * Create a new schema
     */
    @Transactional
    public SchemaDto createSchema(String namespace, SchemaDto schemaDto, String content) throws IOException {
        namespaceFilterManager.enableIfPresent(namespace);
        Optional<Schm> exists = schmRepository.findBySchmName(schemaDto.getName());
        if(exists.isPresent()) {
            throw new IllegalArgumentException("Schema with name " + schemaDto.getName() + " already exists");
        }

        Schm schema = mapToSchmEntity(schemaDto);
        schema.setNamespace(namespace);
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
            if ("published".equalsIgnoreCase(versionName)) {
                schmRepository.getPublishedVersion(schmId).ifPresent(versions::add);
            } else if ("latest".equalsIgnoreCase(versionName)) {
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
     * Update an existing schema
     */
    @Transactional
    public SchemaDto updateSchema(String namespace, UUID schmId, SchemaDto schemaDto) {
        Schm schema = mapToSchmEntity(schemaDto);
        schema.setNamespace(namespace);
        schema.setSchmId(schmId);
        Schm updated = schmRepository.save(schema);
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
            if (schema.getPublishVersion() != null ) {
                if (!schema.getPublishVersion().equals(versionNumber)) {
                    throw new IllegalStateException("Schema " + schmId + " already has published version " + schema.getPublishVersion());
                }else {
                    throw new IllegalStateException("Schema " + schmId + " with version " + versionNumber  + " already published ");
                }
            }

            // Verify the version exists
            Optional<SchmData> versionOpt = schmRepository.getSchemaVersion(schmId, versionNumber);
            if (versionOpt.isPresent()) {
                schema.setPublishVersion(versionNumber);
                schmRepository.save(schema);
            } else {
                throw new IllegalArgumentException("Version " + versionNumber + " does not exist for schema " + schmId);
            }
        }
    }

    /**
     * Delete/unpublish a schema version
     */
    @Transactional
    public void unPublishSchemaVersion(String namespace, UUID schmId, Integer versionNumber) {
        namespaceFilterManager.enableIfPresent(namespace);
        // If this is the published version, unpublish first
        Optional<Schm> schemaOpt = schmRepository.findBySchmId(schmId);
        if (schemaOpt.isPresent() && schemaOpt.get().getPublishVersion() != null
                && schemaOpt.get().getPublishVersion().equals(versionNumber)) {
            Schm schema = schemaOpt.get();
            schema.setPublishVersion(null);
            schmRepository.save(schema);
        }else{
            throw new IllegalArgumentException("Invalid schema " + schmId );
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

            SchmDataId newId = new SchmDataId();
            newId.setSchmId(schmId);
            newId.setSchmVersion(latestVersion + 1);

            SchmData newVersion = new SchmData();
            newVersion.setId(newId);
            newVersion.setSchmData(content);
            //newVersion.setSchmVersionName("draft");

            SchmData saved = schmDataRepository.save(newVersion);
            return mapToVersionResponse(saved);
        }
    }

    /**
     * Update schema version with draft logic
     * If the provided version is not a draft, update the existing draft or create a new one
     */
    @Transactional
    public SchemaVersionDto updateSchemaVersion(String namespace, UUID schmId, Integer version, SchemaVersionDto schemaVersionDto) {
        namespaceFilterManager.enableIfPresent(namespace);

        // Check if the provided version exists
        Optional<SchmData> providedVersionOpt = schmRepository.getSchemaVersion(schmId, version);

        if (providedVersionOpt.isEmpty()) {
            throw new IllegalArgumentException("Version " + version + " does not exist for schema " + schmId);
        }

        SchmData providedVersion = providedVersionOpt.get();

        // If the provided version is NOT a draft, we need to update or create a draft version
        if (providedVersion.getIsDraft() == null || !providedVersion.getIsDraft()) {
            // Look for an existing draft version
            Optional<SchmData> draftOpt = schmDataRepository.findByIdSchmIdAndIsDraft(schmId, true);

            if (draftOpt.isPresent()) {
                // Update the existing draft
                SchmData draft = draftOpt.get();
                draft.setSchmData(schemaVersionDto.getContent());
                draft.setUpdatedBy(schemaVersionDto.getModifiedByUser());
                draft.setUpdatedDatetime(LocalDateTime.now());
                SchmData updated = schmDataRepository.save(draft);
                return mapToVersionResponse(updated);
            } else {
                // No draft exists, create a new version as draft
                Integer highestVersion = schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc(schmId)
                        .map(sd -> sd.getId().getSchmVersion())
                        .orElse(0);

                SchmDataId newId = new SchmDataId();
                newId.setSchmId(schmId);
                newId.setSchmVersion(highestVersion + 1);

                SchmData newDraft = new SchmData();
                newDraft.setId(newId);
                newDraft.setSchmData(schemaVersionDto.getContent());
                newDraft.setIsDraft(true);
                newDraft.setCreatedBy(schemaVersionDto.getModifiedByUser());
                newDraft.setUpdatedBy(schemaVersionDto.getModifiedByUser());

                SchmData saved = schmDataRepository.save(newDraft);
                return mapToVersionResponse(saved);
            }
        } else {
            // The provided version IS a draft, update it directly
            providedVersion.setSchmData(schemaVersionDto.getContent());
            providedVersion.setUpdatedBy(schemaVersionDto.getModifiedByUser());
            providedVersion.setUpdatedDatetime(LocalDateTime.now());
            SchmData updated = schmDataRepository.save(providedVersion);
            return mapToVersionResponse(updated);
        }
    }

    /**
     * Create schema data from multipart file
     */
    private void createSchemaDataFromFile(UUID schmId, String content) throws IOException {
        Integer latestVersion = schmDataRepository.findTopByIdSchmIdOrderByIdSchmVersionDesc(schmId)
                .map(sd -> sd.getId().getSchmVersion())
                .orElse(0);

        SchmDataId newId = new SchmDataId();
        newId.setSchmId(schmId);
        newId.setSchmVersion(latestVersion + 1);

        SchmData newVersion = new SchmData();
        newVersion.setId(newId);
        newVersion.setSchmData(content);
        newVersion.setIsDraft(true);

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

        // Set published and draft URLs
        if (schm.getPublishVersion() != null) {
            response.setPublished("/schemas/" + schm.getNamespace() + "/" + schm.getSchmId() + "/version/published/content");
        }
        response.setDraft("/schemas/" + schm.getNamespace() + "/" + schm.getSchmId() + "/version/draft/content");

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
        schm.setCreatedBy(response.getCreatedByUser());
        schm.setCreatedDatetime(response.getCreateDateTime());
        schm.setUpdatedBy(response.getModifiedByUser());
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
        schmData.setCreatedBy(response.getCreatedByUser());
        schmData.setCreatedDatetime(response.getCreateDateTime());
        schmData.setUpdatedBy(response.getModifiedByUser());
        schmData.setUpdatedDatetime(response.getModifiedDateTime());

        return schmData;
    }
}
