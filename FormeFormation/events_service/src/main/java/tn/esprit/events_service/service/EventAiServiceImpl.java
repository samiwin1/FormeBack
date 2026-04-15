package tn.esprit.events_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import tn.esprit.events_service.dto.AiMessageResponse;
import tn.esprit.events_service.dto.ViewerRole;
import tn.esprit.events_service.entity.Event;
import tn.esprit.events_service.entity.EventDeposit;
import tn.esprit.events_service.entity.EventParticipant;
import tn.esprit.events_service.entity.EventTier;
import tn.esprit.events_service.exception.ForbiddenException;
import tn.esprit.events_service.exception.GeminiApiException;
import tn.esprit.events_service.exception.ResourceNotFoundException;
import tn.esprit.events_service.repository.EventDepositRepository;
import tn.esprit.events_service.repository.EventParticipantRepository;
import tn.esprit.events_service.repository.EventPartnerRepository;
import tn.esprit.events_service.repository.EventRepository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;

@Service
public class EventAiServiceImpl implements EventAiService {

    private static final Logger log = LoggerFactory.getLogger(EventAiServiceImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final EventRepository eventRepository;
    private final EventDepositRepository eventDepositRepository;
    private final EventPartnerRepository eventPartnerRepository;
    private final EventParticipantRepository eventParticipantRepository;
    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String model;

    public EventAiServiceImpl(
            EventRepository eventRepository,
            EventDepositRepository eventDepositRepository,
            EventPartnerRepository eventPartnerRepository,
            EventParticipantRepository eventParticipantRepository,
            @Qualifier("geminiRestTemplate") RestTemplate restTemplate,
            @Value("${gemini.api.key:}") String apiKey,
            @Value("${gemini.api.model:gemini-1.5-flash}") String model
    ) {
        this.eventRepository = eventRepository;
        this.eventDepositRepository = eventDepositRepository;
        this.eventPartnerRepository = eventPartnerRepository;
        this.eventParticipantRepository = eventParticipantRepository;
        this.restTemplate = restTemplate;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.model = (model == null || model.isBlank()) ? "gemini-1.5-flash" : model.trim();
    }

    @Override
    @Transactional(readOnly = true)
    public AiMessageResponse participantAssist(Long eventId, String userMessage, ViewerRole role, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        if (role != ViewerRole.ADMIN) {
            if (userId == null) {
                throw new ForbiddenException("Sign in is required to use the project assistant.");
            }
            boolean isParticipant = eventParticipantRepository.findByEventIdAndUserId(eventId, userId).isPresent();
            boolean isSponsor = eventPartnerRepository.existsByEventIdAndPartnerId(eventId, userId);
            if (isSponsor && !isParticipant) {
                throw new ForbiddenException("Sponsors should use readme Q&A on participant submissions, not the competitor assistant.");
            }
            if (!isParticipant) {
                throw new ForbiddenException("Join the event as a participant to use the project assistant.");
            }
        }

        String prompt = "You are a concise project assistant for an online competition event.\n" +
                "Use ONLY the event information below. If something is not covered, say you don't have that detail.\n" +
                "Keep answers practical and under ~200 words unless the user asks for more detail.\n" +
                "Do not invent rules or deadlines.\n\n" +
                "Event title: " + event.getTitle() + "\n" +
                "Description: " + event.getDescription() + "\n" +
                "Requirements: " + event.getRequirements() + "\n" +
                "Success metrics: " + event.getSuccessMetrics() + "\n\n" +
                "Participant question:\n" + userMessage;
        return AiMessageResponse.builder().reply(callGeminiSafe(prompt, 8192)).build();
    }

    @Override
    @Transactional(readOnly = true)
    public AiMessageResponse askAboutReadme(Long depositId, String userMessage, ViewerRole role, Long sponsorUserId) {
        EventDeposit deposit = eventDepositRepository.findByIdWithParticipantAndEvent(depositId)
                .orElseThrow(() -> new ResourceNotFoundException("Deposit not found: " + depositId));

        EventParticipant participant = deposit.getParticipant();
        Long eventId = participant.getEvent().getId();

        if (role == ViewerRole.ADMIN) {
            // ok
        } else if (sponsorUserId != null
                && eventPartnerRepository.existsByEventIdAndPartnerId(eventId, sponsorUserId)) {
            // sponsor (tier purchaser) or legacy row keyed by user id
        } else {
            throw new ForbiddenException("Only admins or event sponsors can use readme Q&A.");
        }

        String prompt = "You help evaluators understand a participant submission. Base your answer ONLY on the README text;\n" +
                "if the zip likely contains more, mention that you only see the README.\n\n" +
                "README file name: " + deposit.getReadmeOriginalFilename() + "\n" +
                "README content:\n" + deposit.getReadmeContent() + "\n\n" +
                "Question:\n" + userMessage;

        return AiMessageResponse.builder().reply(callGeminiSafe(prompt, 8192)).build();
    }

    @Override
    @Transactional(readOnly = true)
    public AiMessageResponse analyzeEvent(Long eventId, ViewerRole role) {
        if (role != ViewerRole.ADMIN) {
            throw new ForbiddenException("Only admins can run event analytics AI.");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found: " + eventId));

        List<EventParticipant> participants = eventParticipantRepository.findByEventId(eventId);
        long totalParticipants = participants.size();
        long withDeposit = participants.stream()
                .mapToLong(p -> eventDepositRepository.findByParticipantId(p.getId()).isPresent() ? 1 : 0)
                .sum();

        List<EventParticipant> scored = participants.stream().filter(p -> p.getScore() != null).toList();
        OptionalDouble avg = scored.stream().mapToInt(EventParticipant::getScore).average();

        long vip = eventPartnerRepository.countByEventIdAndTier(eventId, EventTier.VIP);
        long gold = eventPartnerRepository.countByEventIdAndTier(eventId, EventTier.GOLD);
        long silv = eventPartnerRepository.countByEventIdAndTier(eventId, EventTier.SILVER);

        Map<String, Object> sponsorTiers = new LinkedHashMap<>();
        sponsorTiers.put("VIP", vip);
        sponsorTiers.put("GOLD", gold);
        sponsorTiers.put("SILVER", silv);

        Map<String, Object> statsMap = new LinkedHashMap<>();
        statsMap.put("eventTitle", event.getTitle());
        statsMap.put("totalParticipants", totalParticipants);
        statsMap.put("submissionsWithFiles", withDeposit);
        statsMap.put("participantsScored", scored.size());
        statsMap.put("averageScore", avg.isPresent() ? avg.getAsDouble() : null);
        statsMap.put("sponsorTiers", sponsorTiers);

        String stats;
        try {
            stats = OBJECT_MAPPER.writeValueAsString(statsMap);
        } catch (Exception e) {
            throw new GeminiApiException("Could not serialize event stats: " + e.getMessage(), e);
        }

        String prompt = """
                You are an operations analyst for online skill events. Using ONLY the JSON stats below, write:
                1) Two sentences on engagement / submission health.
                2) Two sentences on scoring distribution if scored; otherwise note grading not started.
                3) Two sentences of recommendations for admins.

                Keep the full answer under 120 words. Professional tone.

                Stats JSON:
                """ + stats;

        return AiMessageResponse.builder().reply(callGeminiSafe(prompt, 8192)).build();
    }

    private String callGeminiSafe(String prompt, int maxTokens) {
        if (!isApiKeyConfigured()) {
            return "AI is not configured. Set environment variable GEMINI_API_KEY (or gemini.api.key) to enable this feature.";
        }
        try {
            String reply = callGeminiWithModel(prompt, maxTokens, model);
            return reply != null && !reply.isBlank() ? reply.trim() : "The AI returned an empty response.";
        } catch (GeminiApiException e) {
            log.warn("Gemini call failed: {}", e.getMessage());
            throw e;
        }
    }

    private boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank() && !"YOUR_GEMINI_API_KEY_HERE".equals(apiKey);
    }

    private String callGeminiWithModel(String prompt, int maxTokens, String modelToUse) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + modelToUse + ":generateContent";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))));
        requestBody.put("generationConfig", Map.of(
                "maxOutputTokens", maxTokens,
                "temperature", 0.35
        ));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-goog-api-key", apiKey);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new GeminiApiException("AI did not return a valid response.");
            }
            JsonNode root = OBJECT_MAPPER.readTree(response.getBody());

            JsonNode block = root.get("promptFeedback");
            if (block != null && block.get("blockReason") != null) {
                String reason = block.get("blockReason").asText();
                throw new GeminiApiException("Request blocked by content policy (" + reason + "). Try rephrasing your question.");
            }

            JsonNode candidates = root.get("candidates");
            if (candidates == null || !candidates.isArray() || candidates.isEmpty()) {
                throw new GeminiApiException("AI returned no candidates. Check model name and API key (GEMINI_API_KEY / gemini.api.model).");
            }
            JsonNode first = candidates.get(0);
            JsonNode finish = first.get("finishReason");
            if (finish != null) {
                String fr = finish.asText();
                if ("SAFETY".equals(fr) || "RECITATION".equals(fr) || "BLOCKLIST".equals(fr)) {
                    throw new GeminiApiException("Response blocked (" + fr + "). Try a shorter or more neutral question.");
                } else if (!"STOP".equals(fr)) {
                    log.warn("Gemini finishReason was {}. The response might be incomplete or truncated.", fr);
                }
            }
            JsonNode content = first.get("content");
            if (content != null) {
                JsonNode parts = content.get("parts");
                if (parts != null && parts.isArray() && !parts.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    for (JsonNode part : parts) {
                        if (part.has("text")) {
                            sb.append(part.get("text").asText());
                        }
                    }
                    if (sb.length() > 0) {
                        return sb.toString().trim();
                    }
                }
            }
            throw new GeminiApiException("AI did not return text. finishReason=" + (finish != null ? finish.asText() : "unknown"));
        } catch (HttpStatusCodeException e) {
            String message = extractErrorMessage(e.getResponseBodyAsString());
            if (message == null) {
                message = e.getStatusCode() + ": " + e.getStatusText();
            }
            log.debug("Gemini HTTP error body: {}", e.getResponseBodyAsString());
            throw new GeminiApiException(message, e);
        } catch (GeminiApiException e) {
            throw e;
        } catch (Exception e) {
            throw new GeminiApiException("Gemini API error: " + e.getMessage(), e);
        }
    }

    private String extractErrorMessage(String responseBody) {
        if (responseBody == null || responseBody.isBlank()) {
            return null;
        }
        try {
            JsonNode root = OBJECT_MAPPER.readTree(responseBody);
            JsonNode error = root.get("error");
            if (error == null) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            if (error.get("status") != null) {
                sb.append(error.get("status").asText());
            }
            if (error.get("code") != null) {
                if (sb.length() > 0) {
                    sb.append(" ");
                }
                sb.append("(").append(error.get("code").asInt()).append(")");
            }
            if (error.get("message") != null) {
                if (sb.length() > 0) {
                    sb.append(": ");
                }
                sb.append(error.get("message").asText());
            }
            return sb.length() > 0 ? sb.toString() : null;
        } catch (Exception ignored) {
        }
        return null;
    }
}
