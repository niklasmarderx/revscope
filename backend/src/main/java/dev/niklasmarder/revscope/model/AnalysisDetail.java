package dev.niklasmarder.revscope.model;

import java.util.List;

/** Summary plus the list of functions for a single analysis. */
public record AnalysisDetail(AnalysisSummary summary, List<FunctionSummary> functions) {}
