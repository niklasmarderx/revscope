package dev.niklasmarder.revscope.service;

import dev.niklasmarder.revscope.model.AnalysisDetail;
import dev.niklasmarder.revscope.model.AnalysisResult;
import dev.niklasmarder.revscope.model.AnalysisSummary;
import dev.niklasmarder.revscope.model.DecompiledFunction;
import dev.niklasmarder.revscope.persistence.AnalysisEntity;
import dev.niklasmarder.revscope.persistence.AnalysisRepository;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Creates, stores and reads analyses, backed by a JPA repository. */
@Service
public class AnalysisStore {

    private final GhidraService ghidra;
    private final AnalysisRepository repository;
    private final ObjectMapper mapper;

    public AnalysisStore(GhidraService ghidra, AnalysisRepository repository, ObjectMapper mapper) {
        this.ghidra = ghidra;
        this.repository = repository;
        this.mapper = mapper;
    }

    public AnalysisSummary create(String fileName, byte[] content) {
        AnalysisResult result = ghidra.analyze(fileName, content);
        String id = UUID.randomUUID().toString().substring(0, 8);
        StoredDetail detail = new StoredDetail(result.arch(), result.functions(), result.decompiled(),
            result.strings(), result.imports(), result.callGraph());
        AnalysisEntity entity = new AnalysisEntity(id, fileName, content.length, result.arch(),
            "analyzed (" + ghidra.backendName() + ")", result.functions().size(), Instant.now(), write(detail));
        return toSummary(repository.save(entity));
    }

    public List<AnalysisSummary> all() {
        return repository.findAll().stream()
            .map(this::toSummary)
            .sorted(Comparator.comparing(AnalysisSummary::createdAt).reversed())
            .toList();
    }

    public Optional<AnalysisDetail> detail(String id) {
        return repository.findById(id).map(entity -> {
            StoredDetail detail = read(entity.getPayload());
            return new AnalysisDetail(toSummary(entity), detail.functions(),
                detail.strings(), detail.imports(), detail.callGraph());
        });
    }

    public Optional<DecompiledFunction> function(String id, String address) {
        return repository.findById(id).map(entity -> read(entity.getPayload()).decompiled().get(address));
    }

    private AnalysisSummary toSummary(AnalysisEntity e) {
        return new AnalysisSummary(e.getId(), e.getFileName(), e.getFileSize(), e.getArch(),
            e.getStatus(), e.getFunctionCount(), e.getCreatedAt());
    }

    private String write(StoredDetail detail) {
        try {
            return mapper.writeValueAsString(detail);
        } catch (Exception e) {
            throw new IllegalStateException("could not serialize analysis detail", e);
        }
    }

    private StoredDetail read(String json) {
        try {
            return mapper.readValue(json, StoredDetail.class);
        } catch (Exception e) {
            throw new IllegalStateException("could not read analysis detail", e);
        }
    }
}
