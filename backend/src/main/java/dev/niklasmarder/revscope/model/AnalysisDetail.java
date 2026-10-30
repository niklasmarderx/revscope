package dev.niklasmarder.revscope.model;

import java.util.List;

/** Everything the UI needs to render one analysis (minus per-function code). */
public record AnalysisDetail(
        AnalysisSummary summary,
        List<FunctionSummary> functions,
        List<StringRef> strings,
        List<ImportRef> imports,
        List<CallEdge> callGraph) {}
