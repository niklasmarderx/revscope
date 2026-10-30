package dev.niklasmarder.revscope.service;

import dev.niklasmarder.revscope.model.DecompiledFunction;
import dev.niklasmarder.revscope.model.FunctionExplanation;

/**
 * Deterministic heuristic backend so the "explain" feature works without an API key.
 * Set {@code anthropic.api-key} to switch to {@link AnthropicAiService}.
 */
public class StubAiService implements AiService {

    @Override
    public FunctionExplanation explain(DecompiledFunction function) {
        String code = function.code() == null ? "" : function.code();
        String suggested = suggestName(function, code);
        String summary = summarize(code);
        return new FunctionExplanation(function.address(), suggested,
            summary + " (heuristic — set anthropic.api-key for real AI analysis)", "heuristic");
    }

    private String suggestName(DecompiledFunction function, String code) {
        if (function.name() != null && !function.name().startsWith("FUN_")) {
            return function.name();
        }
        String lower = code.toLowerCase();
        if (lower.contains("decrypt")) {
            return "decrypt_payload";
        }
        if (lower.contains("recv") || lower.contains("socket")) {
            return "handle_connection";
        }
        if (lower.contains("param_1")) {
            return "process_argument";
        }
        return "sub_" + function.address().replace("0x", "");
    }

    private String summarize(String code) {
        if (code.contains("return 0;")) {
            return "Validates its input and returns early when a zero condition holds.";
        }
        if (code.contains("process_record")) {
            return "Drives the main record-processing path and returns its result.";
        }
        return "Computes a value from its inputs and returns it.";
    }
}
