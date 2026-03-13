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
     * Get schemas with optional filtering by type, group, and content type
     */
    public List<SchemaDto> getSchemas(String namespace, String type, String group, String contentType, boolean publishedOnly) {
        namespaceFilterManager.enableIfPresent(namespace);

        SchmFilterCriteria criteria = new SchmFilterCriteria();
        criteria.setSchemaType(type);
        criteria.setGroup(group);
        criteria.setContentType(contentType);
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
     * Update an existing schema (schmName is non-editable, always fetched from DB)
     */
    @Transactional
    public SchemaDto updateSchema(String namespace, UUID schmId, SchemaDto schemaDto) {
        namespaceFilterManager.enableIfPresent(namespace);

        // Fetch existing entity - schmName is non-editable and always from DB
        Schm existing = schmRepository.findBySchmId(schmId)
                .orElseThrow(() -> new IllegalArgumentException("Schema not found: " + schmId));

        // Update only editable fields (schmName is preserved from DB)
        existing.setSchmDesc(schemaDto.getDescription());
        existing.setSchemaType(schemaDto.getSchemaType());
        existing.setContentType(schemaDto.getContentType());
        existing.setGroup(schemaDto.getGroup());
        existing.setLockBy(schemaDto.getLockBy());
        existing.setUpdatedBy(schemaDto.getModifiedByUser());

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
                    throw new IllegalStateException("Schema " + schmId + " already has published version " + schema.getPublishVersion());
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
                throw new IllegalArgumentException("Version " + versionNumber + " does not exist for schema " + schmId);
            }
        }
    }

    /**
     * Unpublish a schema by setting publish version to null
     */
    @Transactional
    public void unPublishSchemaVersion(String namespace, UUID schmId) {
        namespaceFilterManager.enableIfPresent(namespace);
        Optional<Schm> schemaOpt = schmRepository.findBySchmId(schmId);
        if (schemaOpt.isPresent()) {
            Schm schema = schemaOpt.get();
            schema.setPublishVersion(null);
            schmRepository.save(schema);
        } else {
            throw new IllegalArgumentException("Schema not found: " + schmId);
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
            newVersion.setIsDraft(true);
            newVersion.setUpdatedBy("Draft Creator");
            newVersion.setCreatedBy("Draft Creator");
            newVersion.setCreatedDatetime(LocalDateTime.now());
            newVersion.setUpdatedDatetime(LocalDateTime.now());

            SchmData saved = schmDataRepository.save(newVersion);
            return mapToVersionResponse(saved);
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
        newVersion.setUpdatedBy("Test");
        newVersion.setCreatedBy("Test");

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
