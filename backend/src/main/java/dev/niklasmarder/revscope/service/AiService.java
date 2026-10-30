package dev.niklasmarder.revscope.service;

import dev.niklasmarder.revscope.model.DecompiledFunction;
import dev.niklasmarder.revscope.model.FunctionExplanation;

/** Suggests a descriptive name and summary for a decompiled function. */
public interface AiService {
    FunctionExplanation explain(DecompiledFunction function);
}
