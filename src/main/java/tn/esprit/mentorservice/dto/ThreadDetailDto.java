package tn.esprit.mentorservice.dto;

import java.util.List;

public record ThreadDetailDto(
        Long id,
        String title,
        Long formationId,
        List<ThreadMessageDto> messages
) {
}
