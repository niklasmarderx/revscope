// revscope post-script for Ghidra's headless analyzer.
// Exports functions (with decompiled C), strings, imports and the call graph as JSON.
// Wired automatically by HeadlessGhidraService:
//   analyzeHeadless <proj> <name> -import <binary> \
//     -scriptPath ghidra-scripts -postScript ExportAnalysis.java <output.json> -deleteProject
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.DataIterator;
import ghidra.program.model.listing.Data;
import ghidra.program.model.listing.Function;
import ghidra.program.model.symbol.Symbol;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;

public class ExportAnalysis extends GhidraScript {

    @Override
    protected void run() throws Exception {
        String[] args = getScriptArgs();
        String outputPath = args.length > 0 ? args[0] : "analysis.json";

        DecompInterface decompiler = new DecompInterface();
        decompiler.openProgram(currentProgram);

        StringBuilder json = new StringBuilder();
        json.append("{\n  \"arch\": \"")
            .append(escape(currentProgram.getLanguageID().getIdAsString()))
            .append("\",\n");

        json.append("  \"functions\": [\n");
        boolean first = true;
        for (Function fn : currentProgram.getFunctionManager().getFunctions(true)) {
            if (!first) {
                json.append(",\n");
            }
            first = false;
            String code = "";
            DecompileResults results = decompiler.decompileFunction(fn, 30, monitor);
            if (results != null && results.decompileCompleted()) {
                code = results.getDecompiledFunction().getC();
            }
            json.append("    {\"address\": \"0x").append(fn.getEntryPoint().toString())
                .append("\", \"name\": \"").append(escape(fn.getName()))
                .append("\", \"size\": ").append(fn.getBody().getNumAddresses())
                .append(", \"paramCount\": ").append(fn.getParameterCount())
                .append(", \"signature\": \"").append(escape(fn.getPrototypeString(false, false)))
                .append("\", \"code\": \"").append(escape(code)).append("\"}");
        }
        json.append("\n  ],\n");

        json.append("  \"strings\": [\n");
        first = true;
        try {
            DataIterator dataIterator = currentProgram.getListing().getDefinedData(true);
            while (dataIterator.hasNext()) {
                Data data = dataIterator.next();
                if (!data.hasStringValue()) {
                    continue;
                }
                Object value = data.getValue();
                if (value == null) {
                    continue;
                }
                if (!first) {
                    json.append(",\n");
                }
                first = false;
                json.append("    {\"address\": \"0x").append(data.getAddress().toString())
                    .append("\", \"value\": \"").append(escape(value.toString())).append("\"}");
            }
        } catch (Exception e) {
            println("revscope: strings export skipped: " + e.getMessage());
        }
        json.append("\n  ],\n");

        json.append("  \"imports\": [\n");
        first = true;
        try {
            for (Symbol symbol : currentProgram.getSymbolTable().getExternalSymbols()) {
                if (!first) {
                    json.append(",\n");
                }
                first = false;
                String library = symbol.getParentNamespace() != null
                    ? symbol.getParentNamespace().getName() : "";
                json.append("    {\"name\": \"").append(escape(symbol.getName()))
                    .append("\", \"library\": \"").append(escape(library)).append("\"}");
            }
        } catch (Exception e) {
            println("revscope: imports export skipped: " + e.getMessage());
        }
        json.append("\n  ],\n");

        json.append("  \"callGraph\": [\n");
        first = true;
        try {
            for (Function fn : currentProgram.getFunctionManager().getFunctions(true)) {
                Set<Function> called = fn.getCalledFunctions(monitor);
                for (Function callee : called) {
                    if (!first) {
                        json.append(",\n");
                    }
                    first = false;
                    json.append("    {\"from\": \"0x").append(fn.getEntryPoint().toString())
                        .append("\", \"to\": \"0x").append(callee.getEntryPoint().toString()).append("\"}");
                }
            }
        } catch (Exception e) {
            println("revscope: call graph export skipped: " + e.getMessage());
        }
        json.append("\n  ]\n}\n");

        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(outputPath)))) {
            writer.print(json);
        }
        println("revscope: wrote analysis to " + outputPath);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '\\' -> sb.append("\\\\");
                case '"' -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> { }
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
