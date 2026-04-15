package tn.esprit.events_service.service;

import tn.esprit.events_service.dto.AiMessageResponse;
import tn.esprit.events_service.dto.ViewerRole;

public interface EventAiService {

    AiMessageResponse participantAssist(Long eventId, String userMessage, ViewerRole role, Long userId);

    AiMessageResponse askAboutReadme(Long depositId, String userMessage, ViewerRole role, Long sponsorUserId);

    AiMessageResponse analyzeEvent(Long eventId, ViewerRole role);
}
