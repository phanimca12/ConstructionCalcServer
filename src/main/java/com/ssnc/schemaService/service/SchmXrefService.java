package com.ssnc.schemaService.service;

import com.ssnc.schemaService.dto.ExtRefDto;
import com.ssnc.schemaService.dto.SchemaDto;
import com.ssnc.schemaService.dto.SchmXrefDto;
import com.ssnc.schemaService.entity.Schm;
import com.ssnc.schemaService.entity.SchmXref;
import com.ssnc.schemaService.entity.XRefType;
import com.ssnc.schemaService.repo.SchmRepository;
import com.ssnc.schemaService.repo.SchmXrefRepository;
import com.ssnc.schemaService.tenant.NamespaceFilterManager;
import com.ssnc.schemaService.util.XRefTypeMapper;
import com.ssnc.shared.security.JwtClaimsContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SchmXrefService {

    @Autowired
    private SchmXrefRepository schmXrefRepository;

    @Autowired
    private SchmRepository schmRepository;

    @Autowired
    private NamespaceFilterManager namespaceFilterManager;

    @Autowired(required = false)
    private JwtClaimsContext jwtClaimsContext;

    /**
     * Get all cross-references
     */
    public List<SchmXrefDto> getAllXrefs() {
        List<SchmXref> xrefs = schmXrefRepository.findAll();
        return xrefs.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Get cross-reference by ID
     */
    public Optional<SchmXrefDto> getXrefById(UUID xrefId) {
        return schmXrefRepository.findById(xrefId)
                .map(this::mapToDto);
    }

    /**
     * Get cross-references by schema ID
     */
    public List<SchmXrefDto> getXrefsBySchmId(UUID schmId) {
        List<SchmXref> xrefs = schmXrefRepository.findBySchmId(schmId);
        return xrefs.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Get cross-references by schema ID and reference type
     */
    public List<SchmXrefDto> getXrefsBySchmIdAndRefType(UUID schmId, String refType) {
        XRefType enumType = XRefTypeMapper.toEnum(refType);
        List<SchmXref> xrefs = schmXrefRepository.findBySchmIdAndRefType(schmId, enumType);
        return xrefs.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Get cross-references by reference GUID
     */
    public List<SchmXrefDto> getXrefsByRefGuid(UUID refGuid) {
        List<SchmXref> xrefs = schmXrefRepository.findByRefGuid(refGuid);
        return xrefs.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    /**
     * Create a new cross-reference
     */
    @Transactional
    public SchmXrefDto createXref(SchmXrefDto xrefDto) {
        SchmXref xref = mapToEntity(xrefDto);
        SchmXref saved = schmXrefRepository.save(xref);
        return mapToDto(saved);
    }

    /**
     * Update an existing cross-reference
     */
    @Transactional
    public SchmXrefDto updateXref(UUID xrefId, SchmXrefDto xrefDto) {
        SchmXref existing = schmXrefRepository.findById(xrefId)
                .orElseThrow(() -> new IllegalArgumentException("Cross-reference not found: " + xrefId));

        // Update fields
        existing.setSchmId(xrefDto.getSchmId());
        existing.setSchmName(xrefDto.getSchmName());
        existing.setSchmType(xrefDto.getSchmType());
        existing.setNmspName(xrefDto.getNmspName());
        existing.setRefType(XRefTypeMapper.toEnum(xrefDto.getRefType()));
        existing.setRefVersion(xrefDto.getRefVersion());
        existing.setRefName(xrefDto.getRefName());
        existing.setRefGuid(xrefDto.getRefGuid());
        existing.setRefGuidChar(xrefDto.getRefGuidChar());
        existing.setUpdatedBy(xrefDto.getUpdatedBy());

        SchmXref updated = schmXrefRepository.save(existing);
        return mapToDto(updated);
    }

    /**
     * Delete a cross-reference
     */
    @Transactional
    public void deleteXref(UUID xrefId) {
        if (!schmXrefRepository.existsById(xrefId)) {
            throw new IllegalArgumentException("Cross-reference not found: " + xrefId);
        }
        schmXrefRepository.deleteById(xrefId);
    }

    /**
     * Get external references for a namespace with optional type filter
     */
    public List<ExtRefDto> getExtRefs(String nameSpace, String type) {
        namespaceFilterManager.enableIfPresent(nameSpace);

        List<SchmXref> xrefs;
        if (type != null && !type.isEmpty()) {
            XRefType enumType = XRefTypeMapper.toEnum(type);
            xrefs = schmXrefRepository.findByNmspNameAndRefType(nameSpace, enumType);
        } else {
            xrefs = schmXrefRepository.findByNmspName(nameSpace);
        }

        return xrefs.stream()
                .map(this::mapToExtRefDto)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * Get schemas associated with a specific external reference
     */
    public List<SchemaDto> getSchemasForExtRef(String nameSpace, String extRefType, String extRefName, String extRefVersion) {
        namespaceFilterManager.enableIfPresent(nameSpace);

        XRefType enumType = XRefTypeMapper.toEnum(extRefType);
        List<SchmXref> xrefs;

        if (extRefVersion != null && !extRefVersion.isEmpty()) {
            xrefs = schmXrefRepository.findByNmspNameAndRefTypeAndRefNameAndRefVersion(nameSpace, enumType, extRefName, extRefVersion);
        } else {
            xrefs = schmXrefRepository.findByNmspNameAndRefTypeAndRefName(nameSpace, enumType, extRefName);
        }

        return xrefs.stream()
                .map(xref -> schmRepository.findById(xref.getSchmId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(this::mapToSchemaDto)
                .collect(Collectors.toList());
    }

    /**
     * Associate schemas with an external reference
     */
    @Transactional
    public List<SchemaDto> associateSchemasWithExtRef(String nameSpace, String extRefType, String extRefName, String extRefVersion, List<SchemaDto> schemas) {
        namespaceFilterManager.enableIfPresent(nameSpace);

        String userName = jwtClaimsContext != null && jwtClaimsContext.getUserId() != null
                ? jwtClaimsContext.getUserId() : "system";

        XRefType enumType = XRefTypeMapper.toEnum(extRefType);

        // Delete existing associations for this version
        List<SchmXref> existingXrefs;
        if (extRefVersion != null && !extRefVersion.isEmpty()) {
            existingXrefs = schmXrefRepository.findByNmspNameAndRefTypeAndRefNameAndRefVersion(nameSpace, enumType, extRefName, extRefVersion);
        } else {
            existingXrefs = schmXrefRepository.findByNmspNameAndRefTypeAndRefName(nameSpace, enumType, extRefName);
        }
        existingXrefs.forEach(xref -> schmXrefRepository.delete(xref));

        // Create new associations
        for (SchemaDto schemaDto : schemas) {
            SchmXref xref = new SchmXref();
            xref.setSchmId(schemaDto.getId());
            xref.setSchmName(schemaDto.getName());
            xref.setSchmType(schemaDto.getSchemaType());
            xref.setNmspName(nameSpace);
            xref.setRefType(enumType);
            xref.setRefName(extRefName);
            xref.setRefVersion(extRefVersion);
            xref.setCreatedBy(userName);
            xref.setUpdatedBy(userName);
            schmXrefRepository.save(xref);
        }

        return schemas;
    }

    /**
     * Get external references for a schema
     */
    public List<ExtRefDto> getExtRefsForSchema(String nameSpace, UUID schmId) {
        namespaceFilterManager.enableIfPresent(nameSpace);

        List<SchmXref> xrefs = schmXrefRepository.findBySchmId(schmId);
        return xrefs.stream()
                .map(this::mapToExtRefDto)
                .collect(Collectors.toList());
    }

    /**
     * Map entity to DTO
     */
    private SchmXrefDto mapToDto(SchmXref xref) {
        SchmXrefDto dto = new SchmXrefDto();
        dto.setXrefId(xref.getXrefId());
        dto.setSchmId(xref.getSchmId());
        dto.setSchmName(xref.getSchmName());
        dto.setSchmType(xref.getSchmType());
        dto.setNmspName(xref.getNmspName());
        dto.setRefType(XRefTypeMapper.toString(xref.getRefType()));
        dto.setRefVersion(xref.getRefVersion());
        dto.setRefName(xref.getRefName());
        dto.setRefGuid(xref.getRefGuid());
        dto.setRefGuidChar(xref.getRefGuidChar());
        dto.setCreatedBy(xref.getCreatedBy());
        dto.setCreatedDatetime(xref.getCreatedDatetime());
        dto.setUpdatedBy(xref.getUpdatedBy());
        dto.setUpdatedDatetime(xref.getUpdatedDatetime());
        return dto;
    }

    /**
     * Map DTO to entity
     */
    private SchmXref mapToEntity(SchmXrefDto dto) {
        SchmXref xref = new SchmXref();
        xref.setXrefId(dto.getXrefId());
        xref.setSchmId(dto.getSchmId());
        xref.setSchmName(dto.getSchmName());
        xref.setSchmType(dto.getSchmType());
        xref.setNmspName(dto.getNmspName());
        xref.setRefType(XRefTypeMapper.toEnum(dto.getRefType()));
        xref.setRefVersion(dto.getRefVersion());
        xref.setRefName(dto.getRefName());
        xref.setRefGuid(dto.getRefGuid());
        xref.setRefGuidChar(dto.getRefGuidChar());
        xref.setCreatedBy(dto.getCreatedBy());
        xref.setUpdatedBy(dto.getUpdatedBy());
        return xref;
    }

    /**
     * Map SchmXref entity to ExtRefDto
     */
    private ExtRefDto mapToExtRefDto(SchmXref xref) {
        ExtRefDto dto = new ExtRefDto();
        dto.setExtRefId(xref.getXrefId());
        dto.setExtRefName(xref.getRefName());
        dto.setExtRefType(XRefTypeMapper.toString(xref.getRefType()));
        dto.setExtRefVersion(xref.getRefVersion());
        dto.setCreatedByUser(xref.getCreatedBy());
        dto.setCreateDateTime(xref.getCreatedDatetime());
        dto.setModifiedByUser(xref.getUpdatedBy());
        dto.setModifiedDateTime(xref.getUpdatedDatetime());
        return dto;
    }

    /**
     * Map Schm entity to SchemaDto
     */
    private SchemaDto mapToSchemaDto(Schm schm) {
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

        if (schm.getPublishVersion() != null) {
            dto.setPublished(String.valueOf(schm.getPublishVersion()));
        }

        return dto;
    }
}