package dev.niklasmarder.revscope.config;

import dev.niklasmarder.revscope.service.GhidraService;
import dev.niklasmarder.revscope.service.HeadlessGhidraService;
import dev.niklasmarder.revscope.service.SampleGhidraService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

/** Picks the Ghidra backend: headless when {@code ghidra.home} is valid, else sample. */
@Configuration
public class GhidraConfig {

    private static final Logger log = LoggerFactory.getLogger(GhidraConfig.class);

    @Bean
    public GhidraService ghidraService(
            @Value("${ghidra.home:}") String ghidraHome,
            @Value("${ghidra.script-path:ghidra-scripts/ExportAnalysis.java}") String scriptPath) {
        if (ghidraHome != null && !ghidraHome.isBlank() && Files.isDirectory(Path.of(ghidraHome))) {
            log.info("Using headless Ghidra backend at {}", ghidraHome);
            return new HeadlessGhidraService(Path.of(ghidraHome), Path.of(scriptPath));
        }
        log.info("ghidra.home not set - using built-in sample analysis backend");
        return new SampleGhidraService();
    }
}
