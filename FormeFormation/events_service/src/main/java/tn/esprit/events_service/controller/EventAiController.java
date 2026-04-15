package tn.esprit.events_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.events_service.dto.AiMessageRequest;
import tn.esprit.events_service.dto.AiMessageResponse;
import tn.esprit.events_service.dto.ViewerRole;
import tn.esprit.events_service.service.EventAiService;
import tn.esprit.events_service.util.RequestHeaderUtil;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventAiController {

    private final EventAiService eventAiService;

    @PostMapping("/{eventId}/ai/participant-chat")
    public AiMessageResponse participantChat(
            @PathVariable Long eventId,
            @Valid @RequestBody AiMessageRequest body,
            @RequestHeader(value = "X-Viewer-Role", required = false) String roleHeader,
            @RequestHeader("X-User-Id") Long userId
    ) {
        ViewerRole role = RequestHeaderUtil.parseViewerRole(roleHeader);
        return eventAiService.participantAssist(eventId, body.getMessage(), role, userId);
    }

    @PostMapping("/deposits/{depositId}/ai/readme-chat")
    public AiMessageResponse readmeChat(
            @PathVariable Long depositId,
            @Valid @RequestBody AiMessageRequest body,
            @RequestHeader(value = "X-Viewer-Role", required = false) String roleHeader,
            @RequestHeader(value = "X-User-Id", required = false) Long userId
    ) {
        ViewerRole role = RequestHeaderUtil.parseViewerRole(roleHeader);
        return eventAiService.askAboutReadme(depositId, body.getMessage(), role, userId);
    }

    @PostMapping("/{eventId}/ai/analyze")
    public AiMessageResponse analyze(
            @PathVariable Long eventId,
            @RequestHeader(value = "X-Viewer-Role", required = false) String roleHeader
    ) {
        ViewerRole role = RequestHeaderUtil.parseViewerRole(roleHeader);
        return eventAiService.analyzeEvent(eventId, role);
    }
}
