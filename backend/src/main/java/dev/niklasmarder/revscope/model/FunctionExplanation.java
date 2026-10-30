package dev.niklasmarder.revscope.model;

/** An AI (or heuristic) suggestion: a better name and a summary for a function. */
public record FunctionExplanation(String address, String suggestedName, String summary, String source) {}
