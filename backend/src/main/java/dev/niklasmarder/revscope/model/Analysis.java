package dev.niklasmarder.revscope.model;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** A stored analysis: its metadata, functions and decompiled sources. */
public class Analysis {

    private final String id;
    private final String fileName;
    private final long fileSize;
    private final String arch;
    private final String status;
    private final Instant createdAt;
    private final List<FunctionSummary> functions;
    private final Map<String, DecompiledFunction> decompiled;

    public Analysis(String id, String fileName, long fileSize, String backend,
                    AnalysisResult result, Instant createdAt) {
        this.id = id;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.arch = result.arch();
        this.status = "analyzed (" + backend + ")";
        this.createdAt = createdAt;
        this.functions = result.functions();
        this.decompiled = result.decompiled();
    }

    public String id() {
        return id;
    }

    public AnalysisSummary summary() {
        return new AnalysisSummary(id, fileName, fileSize, arch, status, functions.size(), createdAt);
    }

    public AnalysisDetail detail() {
        return new AnalysisDetail(summary(), functions);
    }

    public DecompiledFunction function(String address) {
        return decompiled.get(address);
    }
}
