package com.ssnc.schemaService.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssnc.schemaService.constants.ErrorMessages;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class SchemaDto {
    private UUID id;

    @NotBlank(message = "Schema name is required")
    private String name;

    private String description;
    private String schemaType;
    private String contentType;
    private String lockBy;

    @JsonProperty("group")
    private String schmGroup;

    private Integer published;
    private Integer draft;

    private SchemaVersionDto version;

    private String createdByUser;
    private LocalDateTime createDateTime;
    private String modifiedByUser;
    private LocalDateTime modifiedDateTime;
}
