package tn.esprit.forme.certificationservice.application.dto.dashboard;

public record UserDirectoryEntryDto(
        Long userId,
        String firstName,
        String lastName,
        String profession,
        String displayName
) {
}
