package dev.niklasmarder.revscope.service;

import dev.niklasmarder.revscope.model.CallEdge;
import dev.niklasmarder.revscope.model.DecompiledFunction;
import dev.niklasmarder.revscope.model.FunctionSummary;
import dev.niklasmarder.revscope.model.ImportRef;
import dev.niklasmarder.revscope.model.StringRef;

import java.util.List;
import java.util.Map;

/** The heavy per-analysis detail serialized into the entity's JSON payload column. */
public record StoredDetail(
        String arch,
        List<FunctionSummary> functions,
        Map<String, DecompiledFunction> decompiled,
        List<StringRef> strings,
        List<ImportRef> imports,
        List<CallEdge> callGraph) {}
