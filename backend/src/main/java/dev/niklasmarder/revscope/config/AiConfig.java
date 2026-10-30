package dev.niklasmarder.revscope.config;

import dev.niklasmarder.revscope.service.AiService;
import dev.niklasmarder.revscope.service.AnthropicAiService;
import dev.niklasmarder.revscope.service.StubAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;

/** Picks the AI backend: real Claude when an API key is set, else the heuristic stub. */
@Configuration
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    @Bean
    public AiService aiService(
            @Value("${anthropic.api-key:${ANTHROPIC_API_KEY:}}") String apiKey,
            ObjectMapper mapper) {
        if (apiKey != null && !apiKey.isBlank()) {
            log.info("Using Claude ({}) for AI explanations", "claude-opus-5-5");
            return new AnthropicAiService(apiKey, mapper);
        }
        log.info("anthropic.api-key not set - using heuristic AI stub");
        return new StubAiService();
    }
}
