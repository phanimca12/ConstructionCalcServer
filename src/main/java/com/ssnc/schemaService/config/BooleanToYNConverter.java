package com.ssnc.schemaService.config;

import com.ssnc.schemaService.constants.AppConstants;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class BooleanToYNConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean attribute) {
        if (attribute == null) {
            return null; // or AppConstants.BOOLEAN_NO if you want default
        }
        return attribute ? AppConstants.BOOLEAN_YES : AppConstants.BOOLEAN_NO;
    }

    @Override
    public Boolean convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null; // or false if you want default
        }
        return dbData.equalsIgnoreCase(AppConstants.BOOLEAN_YES);
    }
}
