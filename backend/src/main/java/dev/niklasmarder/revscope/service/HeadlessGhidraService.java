package dev.niklasmarder.revscope.service;

import dev.niklasmarder.revscope.model.AnalysisResult;
import dev.niklasmarder.revscope.model.DecompiledFunction;
import dev.niklasmarder.revscope.model.FunctionSummary;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Drives a real Ghidra installation in headless mode: writes the uploaded bytes
 * to a temp file, runs {@code support/analyzeHeadless} with the bundled
 * {@code ExportAnalysis} post-script, and parses its JSON output.
 *
 * <p>Active only when {@code ghidra.home} points at a built Ghidra install.
 */
public class HeadlessGhidraService implements GhidraService {

    private final Path ghidraHome;
    private final Path scriptPath;

    public HeadlessGhidraService(Path ghidraHome, Path scriptPath) {
        this.ghidraHome = ghidraHome;
        this.scriptPath = scriptPath;
    }

    @Override
    public String backendName() {
        return "headless";
    }

    @Override
    public AnalysisResult analyze(String fileName, byte[] content) {
        try {
            Path work = Files.createTempDirectory("revscope-");
            Path binary = work.resolve(sanitize(fileName));
            Files.write(binary, content);
            Path output = work.resolve("analysis.json");

            String launcher = isWindows() ? "analyzeHeadless.bat" : "analyzeHeadless";
            Path headless = ghidraHome.resolve("support").resolve(launcher);

            List<String> cmd = List.of(
                headless.toString(),
                work.toString(), "revscope-" + System.nanoTime(),
                "-import", binary.toString(),
                "-scriptPath", scriptPath.toAbsolutePath().getParent().toString(),
                "-postScript", scriptPath.getFileName().toString(), output.toString(),
                "-deleteProject");

            Process process = new ProcessBuilder(cmd).redirectErrorStream(true).start();
            process.getInputStream().readAllBytes();
            int exit = process.waitFor();
            if (exit != 0 || !Files.exists(output)) {
                throw new IllegalStateException("analyzeHeadless failed (exit " + exit + ")");
            }
            return parse(Files.readString(output));
        } catch (IOException e) {
            throw new IllegalStateException("Ghidra headless analysis failed: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Ghidra headless analysis interrupted", e);
        }
    }

    private AnalysisResult parse(String json) {
        JsonParser parser = JsonParserFactory.getJsonParser();
        Map<String, Object> root = parser.parseMap(json);
        String arch = str(root.getOrDefault("arch", "unknown"));

        List<FunctionSummary> functions = new ArrayList<>();
        Map<String, DecompiledFunction> decompiled = new LinkedHashMap<>();
        if (root.get("functions") instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> f) {
                    String address = str(f.get("address"));
                    String name = str(f.get("name"));
                    functions.add(new FunctionSummary(address, name, intOf(f.get("size")), intOf(f.get("paramCount"))));
                    decompiled.put(address,
                        new DecompiledFunction(address, name, str(f.get("signature")), str(f.get("code"))));
                }
            }
        }
        return new AnalysisResult(arch, functions, decompiled);
    }

    private static String sanitize(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "binary";
        }
        return fileName.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private static boolean isWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static int intOf(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
