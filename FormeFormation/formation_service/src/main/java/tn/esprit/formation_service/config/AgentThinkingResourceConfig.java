package tn.esprit.formation_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Serves agent-thinking animation frames from FormeFormation/agent-thinking.
 * Frames are kept in the backend project, not in the frontend.
 */
@Configuration
public class AgentThinkingResourceConfig implements WebMvcConfigurer {

    @Value("${agent.thinking.path:../agent-thinking}")
    private String agentThinkingPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = agentThinkingPath.endsWith("/") ? agentThinkingPath : agentThinkingPath + "/";
        if (!location.startsWith("file:") && !location.startsWith("classpath:")) {
            location = "file:" + location;
        }
        registry.addResourceHandler("/api/agent-thinking/**")
                .addResourceLocations(location);
    }
}
