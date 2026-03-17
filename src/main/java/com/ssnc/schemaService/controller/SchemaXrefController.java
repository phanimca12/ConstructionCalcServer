package com.ssnc.schemaService.controller;

import com.ssnc.schemaService.dto.SchmXrefDto;
import com.ssnc.schemaService.service.SchmXrefService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/schema-xrefs")
public class SchemaXrefController {

    @Autowired
    private SchmXrefService schmXrefService;

    /**
     * GET /schema-xrefs
     * Get all cross-references with optional filtering
     */
    @GetMapping
    public ResponseEntity<List<SchmXrefDto>> getAllXrefs(
            @RequestParam(required = false) UUID schmId,
            @RequestParam(required = false) String refType,
            @RequestParam(required = false) UUID refGuid) {

        List<SchmXrefDto> xrefs;

        if (schmId != null && refType != null) {
            xrefs = schmXrefService.getXrefsBySchmIdAndRefType(schmId, refType);
        } else if (schmId != null) {
            xrefs = schmXrefService.getXrefsBySchmId(schmId);
        } else if (refGuid != null) {
            xrefs = schmXrefService.getXrefsByRefGuid(refGuid);
        } else {
            xrefs = schmXrefService.getAllXrefs();
        }

        return ResponseEntity.ok(xrefs);
    }

    /**
     * GET /schema-xrefs/{xrefId}
     * Get cross-reference by ID
     */
    @GetMapping("/{xrefId}")
    public ResponseEntity<SchmXrefDto> getXrefById(@PathVariable UUID xrefId) {
        return schmXrefService.getXrefById(xrefId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /schema-xrefs
     * Create a new cross-reference
     */
    @PostMapping
    public ResponseEntity<SchmXrefDto> createXref(@RequestBody SchmXrefDto xrefDto) {
        try {
            SchmXrefDto created = schmXrefService.createXref(xrefDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * PUT /schema-xrefs/{xrefId}
     * Update an existing cross-reference
     */
    @PutMapping("/{xrefId}")
    public ResponseEntity<SchmXrefDto> updateXref(
            @PathVariable UUID xrefId,
            @RequestBody SchmXrefDto xrefDto) {
        try {
            SchmXrefDto updated = schmXrefService.updateXref(xrefId, xrefDto);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * DELETE /schema-xrefs/{xrefId}
     * Delete a cross-reference
     */
    @DeleteMapping("/{xrefId}")
    public ResponseEntity<Void> deleteXref(@PathVariable UUID xrefId) {
        try {
            schmXrefService.deleteXref(xrefId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}