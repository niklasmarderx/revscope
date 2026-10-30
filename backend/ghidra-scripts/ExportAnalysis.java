// revscope post-script for Ghidra's headless analyzer.
// Usage (wired automatically by HeadlessGhidraService):
//   analyzeHeadless <proj> <name> -import <binary> \
//     -scriptPath ghidra-scripts -postScript ExportAnalysis.java <output.json> -deleteProject
//
// Exports every function plus its decompiled C to a JSON file consumed by the backend.
import ghidra.app.decompiler.DecompInterface;
import ghidra.app.decompiler.DecompileResults;
import ghidra.app.script.GhidraScript;
import ghidra.program.model.listing.Function;
import ghidra.program.model.listing.FunctionManager;

import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;

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
            .append("\",\n  \"functions\": [\n");

        FunctionManager functions = currentProgram.getFunctionManager();
        boolean first = true;
        for (Function fn : functions.getFunctions(true)) {
            if (!first) {
                json.append(",\n");
            }
            first = false;

            String code = "";
            DecompileResults results = decompiler.decompileFunction(fn, 30, monitor);
            if (results != null && results.decompileCompleted()) {
                code = results.getDecompiledFunction().getC();
            }

            json.append("    {\n")
                .append("      \"address\": \"0x").append(fn.getEntryPoint().toString()).append("\",\n")
                .append("      \"name\": \"").append(escape(fn.getName())).append("\",\n")
                .append("      \"size\": ").append(fn.getBody().getNumAddresses()).append(",\n")
                .append("      \"paramCount\": ").append(fn.getParameterCount()).append(",\n")
                .append("      \"signature\": \"").append(escape(fn.getPrototypeString(false, false))).append("\",\n")
                .append("      \"code\": \"").append(escape(code)).append("\"\n")
                .append("    }");
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
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                    .replace("\n", "\\n").replace("\r", "").replace("\t", "\\t");
    }
}
