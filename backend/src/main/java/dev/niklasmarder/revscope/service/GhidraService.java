package dev.niklasmarder.revscope.service;

import dev.niklasmarder.revscope.model.AnalysisResult;

/** Strategy for turning an uploaded binary into an {@link AnalysisResult}. */
public interface GhidraService {

    /** Analyse the given bytes and return functions plus decompiled sources. */
    AnalysisResult analyze(String fileName, byte[] content);

    /** Short identifier of the active backend, e.g. {@code "sample"} or {@code "headless"}. */
    String backendName();
}
