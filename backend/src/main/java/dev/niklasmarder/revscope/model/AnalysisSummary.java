package dev.niklasmarder.revscope.model;

import java.time.Instant;

/** Metadata about one analysed binary (no per-function detail). */
public record AnalysisSummary(
        String id,
        String fileName,
        long fileSize,
        String arch,
        String status,
        int functionCount,
        Instant createdAt) {}
