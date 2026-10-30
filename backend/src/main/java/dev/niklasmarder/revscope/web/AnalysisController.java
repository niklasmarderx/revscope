package dev.niklasmarder.revscope.web;

import dev.niklasmarder.revscope.model.AnalysisDetail;
import dev.niklasmarder.revscope.model.AnalysisSummary;
import dev.niklasmarder.revscope.model.DecompiledFunction;
import dev.niklasmarder.revscope.model.FunctionExplanation;
import dev.niklasmarder.revscope.service.AiService;
import dev.niklasmarder.revscope.service.AnalysisStore;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

/** REST API for uploading binaries, browsing analysis, and AI explanations. */
@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisStore store;
    private final AiService ai;

    public AnalysisController(AnalysisStore store, AiService ai) {
        this.store = store;
        this.ai = ai;
    }

    @GetMapping("/health")
    public String health() {
        return "ok";
    }

    @PostMapping("/analyses")
    @ResponseStatus(HttpStatus.CREATED)
    public AnalysisSummary upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is empty");
        }
        try {
            return store.create(file.getOriginalFilename(), file.getBytes());
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "could not read upload", e);
        }
    }

    @GetMapping("/analyses")
    public List<AnalysisSummary> list() {
        return store.all();
    }

    @GetMapping("/analyses/{id}")
    public AnalysisDetail detail(@PathVariable String id) {
        return store.detail(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no analysis " + id));
    }

    @GetMapping("/analyses/{id}/functions/{address}")
    public DecompiledFunction function(@PathVariable String id, @PathVariable String address) {
        return requireFunction(id, address);
    }

    @PostMapping("/analyses/{id}/functions/{address}/explain")
    public FunctionExplanation explain(@PathVariable String id, @PathVariable String address) {
        return ai.explain(requireFunction(id, address));
    }

    private DecompiledFunction requireFunction(String id, String address) {
        return store.function(id, address)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no function " + address));
    }
}
