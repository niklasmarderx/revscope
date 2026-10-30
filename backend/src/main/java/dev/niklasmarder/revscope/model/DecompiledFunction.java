package dev.niklasmarder.revscope.model;

/** A single function together with its decompiled C-like source. */
public record DecompiledFunction(String address, String name, String signature, String code) {}
