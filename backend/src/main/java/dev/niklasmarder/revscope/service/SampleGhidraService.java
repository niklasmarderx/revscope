package dev.niklasmarder.revscope.service;

import dev.niklasmarder.revscope.model.AnalysisResult;
import dev.niklasmarder.revscope.model.DecompiledFunction;
import dev.niklasmarder.revscope.model.FunctionSummary;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Produces deterministic, realistic-looking sample analysis so the dashboard
 * works end-to-end without a Ghidra installation. Configure {@code ghidra.home}
 * to switch to {@link HeadlessGhidraService}.
 */
public class SampleGhidraService implements GhidraService {

    private static final String[] ARCHES = {
        "x86:LE:64:default", "x86:LE:32:default", "AARCH64:LE:64:v8A", "ARM:LE:32:v7"
    };

    private static final String[] NAMES = {
        "entry", "main", "FUN_00401160", "process_record", "validate_input",
        "FUN_004013a0", "decrypt_block", "parse_header", "FUN_00401620", "cleanup"
    };

    @Override
    public String backendName() {
        return "sample";
    }

    @Override
    public AnalysisResult analyze(String fileName, byte[] content) {
        long seed = (fileName == null ? 1L : fileName.hashCode()) ^ (content.length * 2654435761L);
        Random rnd = new Random(seed);
        String arch = ARCHES[(int) Math.floorMod(seed, ARCHES.length)];

        int count = 6 + rnd.nextInt(5);
        List<FunctionSummary> functions = new ArrayList<>();
        Map<String, DecompiledFunction> decompiled = new LinkedHashMap<>();

        long addr = 0x00401000L;
        for (int i = 0; i < count; i++) {
            String name = NAMES[i % NAMES.length];
            int size = 24 + rnd.nextInt(400);
            int params = rnd.nextInt(4);
            String address = String.format("0x%08x", addr);

            functions.add(new FunctionSummary(address, name, size, params));
            String signature = buildSignature(name, params);
            decompiled.put(address,
                new DecompiledFunction(address, name, signature, buildBody(signature, name, params, rnd)));

            addr += size + rnd.nextInt(16);
        }
        return new AnalysisResult(arch, functions, decompiled);
    }

    private String buildSignature(String name, int params) {
        StringBuilder sb = new StringBuilder();
        sb.append("main".equals(name) ? "int " : "undefined8 ").append(name).append('(');
        if (params == 0) {
            sb.append("void");
        } else {
            for (int p = 0; p < params; p++) {
                if (p > 0) {
                    sb.append(", ");
                }
                sb.append("long param_").append(p + 1);
            }
        }
        return sb.append(')').toString();
    }

    private String buildBody(String signature, String name, int params, Random rnd) {
        StringBuilder sb = new StringBuilder();
        sb.append(signature).append("\n{\n");
        sb.append("    int iVar1;\n");
        sb.append("    undefined8 uVar2;\n\n");
        if (params > 0) {
            sb.append("    iVar1 = (int)param_1;\n");
            sb.append("    if (iVar1 == 0) {\n");
            sb.append("        return 0;\n");
            sb.append("    }\n");
        } else {
            sb.append("    iVar1 = ").append(rnd.nextInt(1000)).append(";\n");
        }
        sb.append("    uVar2 = ")
          .append("main".equals(name) ? "process_record(iVar1)" : "0x" + Integer.toHexString(rnd.nextInt(0xffff)))
          .append(";\n");
        sb.append("    return ").append("main".equals(name) ? "(int)uVar2" : "uVar2").append(";\n");
        return sb.append("}\n").toString();
    }
}
