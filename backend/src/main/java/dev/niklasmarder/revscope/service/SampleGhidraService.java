package dev.niklasmarder.revscope.service;

import dev.niklasmarder.revscope.model.AnalysisResult;
import dev.niklasmarder.revscope.model.CallEdge;
import dev.niklasmarder.revscope.model.DecompiledFunction;
import dev.niklasmarder.revscope.model.FunctionSummary;
import dev.niklasmarder.revscope.model.ImportRef;
import dev.niklasmarder.revscope.model.StringRef;

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

    private static final String[] STRINGS = {
        "/usr/lib/libc.so.6", "error: %s\n", "GET %s HTTP/1.1", "invalid header",
        "password", "%d.%d.%d.%d", "connection closed", "cannot allocate %zu bytes",
        "usage: %s [options] <file>", "OpenSSL 3.0"
    };

    private static final String[][] IMPORTS = {
        {"printf", "libc"}, {"malloc", "libc"}, {"free", "libc"}, {"strcmp", "libc"},
        {"memcpy", "libc"}, {"socket", "libc"}, {"recv", "libc"}, {"EVP_DecryptInit_ex", "libcrypto"}
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
        List<String> addresses = new ArrayList<>();

        long addr = 0x00401000L;
        for (int i = 0; i < count; i++) {
            String name = NAMES[i % NAMES.length];
            int size = 24 + rnd.nextInt(400);
            int params = rnd.nextInt(4);
            String address = String.format("0x%08x", addr);
            addresses.add(address);

            functions.add(new FunctionSummary(address, name, size, params));
            String signature = buildSignature(name, params);
            decompiled.put(address,
                new DecompiledFunction(address, name, signature, buildBody(signature, name, params, rnd)));

            addr += size + rnd.nextInt(16);
        }

        return new AnalysisResult(arch, functions, decompiled,
            buildStrings(rnd), buildImports(rnd), buildCallGraph(addresses, rnd));
    }

    private List<StringRef> buildStrings(Random rnd) {
        List<StringRef> strings = new ArrayList<>();
        long base = 0x00402000L;
        int n = 5 + rnd.nextInt(STRINGS.length - 5 + 1);
        for (int i = 0; i < n; i++) {
            strings.add(new StringRef(String.format("0x%08x", base), STRINGS[i % STRINGS.length]));
            base += 8 + rnd.nextInt(24);
        }
        return strings;
    }

    private List<ImportRef> buildImports(Random rnd) {
        List<ImportRef> imports = new ArrayList<>();
        int n = 4 + rnd.nextInt(IMPORTS.length - 4 + 1);
        for (int i = 0; i < n; i++) {
            imports.add(new ImportRef(IMPORTS[i][0], IMPORTS[i][1]));
        }
        return imports;
    }

    private List<CallEdge> buildCallGraph(List<String> addresses, Random rnd) {
        List<CallEdge> edges = new ArrayList<>();
        // "main" (index 1 when present) calls several others; others form a light chain.
        for (int i = 0; i < addresses.size(); i++) {
            int targets = rnd.nextInt(3);
            for (int t = 0; t < targets; t++) {
                int j = i + 1 + rnd.nextInt(Math.max(1, addresses.size() - i - 1));
                if (j < addresses.size() && j != i) {
                    edges.add(new CallEdge(addresses.get(i), addresses.get(j)));
                }
            }
        }
        return edges;
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
