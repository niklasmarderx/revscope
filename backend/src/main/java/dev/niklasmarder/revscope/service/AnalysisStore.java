package dev.niklasmarder.revscope.service;

import dev.niklasmarder.revscope.model.Analysis;
import dev.niklasmarder.revscope.model.AnalysisResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** In-memory registry of analyses (swap for a database as the project grows). */
@Service
public class AnalysisStore {

    private final ConcurrentMap<String, Analysis> store = new ConcurrentHashMap<>();
    private final GhidraService ghidra;

    public AnalysisStore(GhidraService ghidra) {
        this.ghidra = ghidra;
    }

    public Analysis create(String fileName, byte[] content) {
        AnalysisResult result = ghidra.analyze(fileName, content);
        String id = UUID.randomUUID().toString().substring(0, 8);
        Analysis analysis = new Analysis(id, fileName, content.length, ghidra.backendName(), result, Instant.now());
        store.put(id, analysis);
        return analysis;
    }

    public Collection<Analysis> all() {
        return store.values();
    }

    public Analysis get(String id) {
        return store.get(id);
    }
}
