package tn.esprit.forme.certificationservice.application.service;

import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service

public class CertificateEventsService {

    private final Map<Long, Set<SseEmitter>> emittersByLearner = new ConcurrentHashMap<>();

    public SseEmitter subscribe(Long learnerId) {
        SseEmitter emitter = new SseEmitter(0L);
        emittersByLearner.computeIfAbsent(learnerId, id -> ConcurrentHashMap.newKeySet()).add(emitter);

        emitter.onCompletion(() -> unregister(learnerId, emitter));
        emitter.onTimeout(() -> unregister(learnerId, emitter));
        emitter.onError((ex) -> unregister(learnerId, emitter));

        try {
            emitter.send(SseEmitter.event().name("connected").data("ok"));
        } catch (IOException ex) {
            unregister(learnerId, emitter);
        }
        return emitter;
    }

    public void publishCertificateReady(Long learnerId, Long issuedCertificationId, String certificateNumber) {
        Set<SseEmitter> emitters = emittersByLearner.get(learnerId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }

        String payload = "{\"issuedCertificationId\":" + issuedCertificationId
                + ",\"certificateNumber\":\"" + escapeJson(certificateNumber) + "\"}";

        for (SseEmitter emitter : emitters.toArray(SseEmitter[]::new)) {
            try {
                emitter.send(SseEmitter.event().name("certificate-ready").data(payload));
            } catch (IOException ex) {
                unregister(learnerId, emitter);
            }
        }
    }

    private void unregister(Long learnerId, SseEmitter emitter) {
        Set<SseEmitter> emitters = emittersByLearner.get(learnerId);
        if (emitters == null) {
            return;
        }
        emitters.remove(emitter);
        if (emitters.isEmpty()) {
            emittersByLearner.remove(learnerId);
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
