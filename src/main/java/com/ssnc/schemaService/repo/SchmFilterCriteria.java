package com.ssnc.schemaService.repo;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class SchmFilterCriteria {
    private UUID schmId;
    private String name;
    private String schemaType;
    private String group;
    private String lockBy;
    private Integer publishVersion;
    private String modifiedByUser;
    private String versionModifiedByUser;
    private String sort;
    private String withVersion;

}
