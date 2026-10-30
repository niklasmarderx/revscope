package dev.niklasmarder.revscope.service;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.Message;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.OutputConfig;
import dev.niklasmarder.revscope.model.DecompiledFunction;
import dev.niklasmarder.revscope.model.FunctionExplanation;
import tools.jackson.databind.ObjectMapper;

import java.util.stream.Collectors;

/**
 * Calls Claude (via the official Anthropic Java SDK) to rename and summarize a
 * decompiled function. Active only when {@code anthropic.api-key} is configured.
 */
public class AnthropicAiService implements AiService {

    private static final String MODEL = "claude-opus-5-5";

    private final AnthropicClient client;
    private final ObjectMapper mapper;

    public AnthropicAiService(String apiKey, ObjectMapper mapper) {
        this.client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();
        this.mapper = mapper;
    }

    @Override
    public FunctionExplanation explain(DecompiledFunction function) {
        String prompt = """
            You are a reverse-engineering assistant. Given a decompiled C function, \
            propose a concise, descriptive snake_case function name and a one-sentence \
            summary of what it does. Respond with ONLY a JSON object of the form \
            {"suggestedName": "...", "summary": "..."}.

            Current name: %s
            Address: %s

            %s
            """.formatted(function.name(), function.address(), function.code());

        MessageCreateParams params = MessageCreateParams.builder()
            .model(MODEL)
            .maxTokens(1024L)
            .outputConfig(OutputConfig.builder().effort(OutputConfig.Effort.LOW).build())
            .addUserMessage(prompt)
            .build();

        Message response = client.messages().create(params);
        String text = response.content().stream()
            .flatMap(block -> block.text().stream())
            .map(block -> block.text())
            .collect(Collectors.joining("\n"))
            .trim();

        return parse(function, text);
    }

    private FunctionExplanation parse(DecompiledFunction function, String text) {
        try {
            int start = text.indexOf('{');
            int end = text.lastIndexOf('}');
            if (start >= 0 && end > start) {
                Suggestion s = mapper.readValue(text.substring(start, end + 1), Suggestion.class);
                return new FunctionExplanation(function.address(),
                    s.suggestedName() != null ? s.suggestedName() : function.name(),
                    s.summary() != null ? s.summary() : text, "claude");
            }
        } catch (Exception ignored) {
            // fall through to returning the raw text as the summary
        }
        return new FunctionExplanation(function.address(), function.name(), text, "claude");
    }

    private record Suggestion(String suggestedName, String summary) {}
}
