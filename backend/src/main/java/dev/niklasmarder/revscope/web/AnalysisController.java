package dev.niklasmarder.revscope.web;

import dev.niklasmarder.revscope.model.Analysis;
import dev.niklasmarder.revscope.model.AnalysisDetail;
import dev.niklasmarder.revscope.model.AnalysisSummary;
import dev.niklasmarder.revscope.model.DecompiledFunction;
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
import java.util.Comparator;
import java.util.List;

/** REST API for uploading binaries and browsing their analysis. */
@RestController
@RequestMapping("/api")
public class AnalysisController {

    private final AnalysisStore store;

    public AnalysisController(AnalysisStore store) {
        this.store = store;
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
            return store.create(file.getOriginalFilename(), file.getBytes()).summary();
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "could not read upload", e);
        }
    }

    @GetMapping("/analyses")
    public List<AnalysisSummary> list() {
        return store.all().stream()
                .map(Analysis::summary)
                .sorted(Comparator.comparing(AnalysisSummary::createdAt).reversed())
                .toList();
    }

    @GetMapping("/analyses/{id}")
    public AnalysisDetail detail(@PathVariable String id) {
        return require(id).detail();
    }

    @GetMapping("/analyses/{id}/functions/{address}")
    public DecompiledFunction function(@PathVariable String id, @PathVariable String address) {
        DecompiledFunction fn = require(id).function(address);
        if (fn == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no function " + address);
        }
        return fn;
    }

    private Analysis require(String id) {
        Analysis analysis = store.get(id);
        if (analysis == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "no analysis " + id);
        }
        return analysis;
    }
}
