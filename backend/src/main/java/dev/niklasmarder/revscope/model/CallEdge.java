package dev.niklasmarder.revscope.model;

/** A call relationship between two functions, by entry-point address. */
public record CallEdge(String fromAddress, String toAddress) {}
