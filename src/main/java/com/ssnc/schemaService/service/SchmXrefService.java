package com.ssnc.schemaService.service;

import com.ssnc.schemaService.dto.SchmXrefDto;
import com.ssnc.schemaService.entity.SchmXref;
import com.ssnc.schemaService.repo.SchmXrefRepository;
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
        List<SchmXref> xrefs = schmXrefRepository.findBySchmIdAndRefType(schmId, refType);
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
        existing.setRefType(xrefDto.getRefType());
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
     * Map entity to DTO
     */
    private SchmXrefDto mapToDto(SchmXref xref) {
        SchmXrefDto dto = new SchmXrefDto();
        dto.setXrefId(xref.getXrefId());
        dto.setSchmId(xref.getSchmId());
        dto.setSchmName(xref.getSchmName());
        dto.setSchmType(xref.getSchmType());
        dto.setNmspName(xref.getNmspName());
        dto.setRefType(xref.getRefType());
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
        xref.setRefType(dto.getRefType());
        xref.setRefVersion(dto.getRefVersion());
        xref.setRefName(dto.getRefName());
        xref.setRefGuid(dto.getRefGuid());
        xref.setRefGuidChar(dto.getRefGuidChar());
        xref.setCreatedBy(dto.getCreatedBy());
        xref.setUpdatedBy(dto.getUpdatedBy());
        return xref;
    }
}