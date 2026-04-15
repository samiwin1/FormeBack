package tn.esprit.forme.certificationservice.domain.entity.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tn.esprit.forme.certificationservice.domain.enums.MeetingProvider;

@Converter(autoApply = false)

public class MeetingProviderConverter implements AttributeConverter<MeetingProvider, String> {

    @Override
    public String convertToDatabaseColumn(MeetingProvider attribute) {
        return attribute == null ? null : attribute.name();
    }

    @Override
    public MeetingProvider convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return MeetingProvider.MEET;
        }
        try {
            return MeetingProvider.valueOf(dbData.trim());
        } catch (IllegalArgumentException ex) {
            // Backward-compatible fallback for legacy/bad rows.
            return MeetingProvider.MEET;
        }
    }
}
