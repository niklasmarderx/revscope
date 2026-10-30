package dev.niklasmarder.revscope.model;

import java.util.List;
import java.util.Map;

/** Raw result returned by a {@code GhidraService} backend. */
public record AnalysisResult(
        String arch,
        List<FunctionSummary> functions,
        Map<String, DecompiledFunction> decompiled) {}
