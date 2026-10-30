package dev.niklasmarder.revscope.model;

/** Lightweight description of a function discovered during analysis. */
public record FunctionSummary(String address, String name, int size, int paramCount) {}
