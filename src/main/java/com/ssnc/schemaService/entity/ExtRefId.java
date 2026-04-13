package com.ssnc.schemaService.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite primary key for ExtRef entity.
 * Allows multiple versions of the same external reference ID.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtRefId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "EXT_REF_ID", nullable = false, length = 64)
    private String extRefId;

    @Column(name = "EXT_REF_VERSION", nullable = false, length = 64)
    private String extRefVersion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExtRefId extRefId1 = (ExtRefId) o;
        return Objects.equals(extRefId, extRefId1.extRefId) &&
                Objects.equals(extRefVersion, extRefId1.extRefVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(extRefId, extRefVersion);
    }
}
