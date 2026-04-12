package tn.esprit.formation_service.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Serves metadata for agent-thinking animation frames.
 * Frames are stored in FormeFormation/agent-thinking as frame_001.png, frame_002.png, etc.
 */
@RestController
@RequestMapping("/api/agent-thinking")
public class AgentThinkingController {

    private static final Pattern FRAME_PATTERN = Pattern.compile("frame_\\d{3}\\.png");

    @Value("${agent.thinking.path:../agent-thinking}")
    private String agentThinkingPath;

    @GetMapping("/frames")
    public ResponseEntity<?> getFramesMetadata() {
        try {
            Path dir = Paths.get(agentThinkingPath).toAbsolutePath().normalize();
            if (!Files.isDirectory(dir)) {
                return ResponseEntity.ok(Map.of("count", 0, "baseUrl", "/api/agent-thinking"));
            }

            List<String> frames = Files.list(dir)
                    .filter(p -> Files.isRegularFile(p) && FRAME_PATTERN.matcher(p.getFileName().toString()).matches())
                    .map(p -> p.getFileName().toString())
                    .sorted()
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of(
                    "count", frames.size(),
                    "baseUrl", "/api/agent-thinking"
            ));
        } catch (IOException e) {
            return ResponseEntity.ok(Map.of("count", 0, "baseUrl", "/api/agent-thinking"));
        }
    }
}
