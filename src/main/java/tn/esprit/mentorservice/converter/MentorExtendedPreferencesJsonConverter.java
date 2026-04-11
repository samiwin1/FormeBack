package tn.esprit.mentorservice.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import tn.esprit.mentorservice.dto.MentorExtendedPreferences;

@Converter(autoApply = false)
public class MentorExtendedPreferencesJsonConverter implements AttributeConverter<MentorExtendedPreferences, String> {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(MentorExtendedPreferences attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize mentor extended preferences", e);
        }
    }

    @Override
    public MentorExtendedPreferences convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return MAPPER.readValue(dbData, MentorExtendedPreferences.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to deserialize mentor extended preferences", e);
        }
    }
}
