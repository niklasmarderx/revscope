package dev.niklasmarder.revscope;

import dev.niklasmarder.revscope.model.AnalysisDetail;
import dev.niklasmarder.revscope.model.AnalysisSummary;
import dev.niklasmarder.revscope.service.AnalysisStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AnalysisStoreTest {

    @Autowired
    private AnalysisStore store;

    @Test
    void persistsAndReadsBackAnalysis() {
        AnalysisSummary summary = store.create("persist.bin", new byte[]{9, 8, 7, 6, 5});
        assertTrue(summary.functionCount() > 0);

        assertTrue(store.all().stream().anyMatch(a -> a.id().equals(summary.id())));

        AnalysisDetail detail = store.detail(summary.id()).orElseThrow();
        assertEquals(summary.functionCount(), detail.functions().size());
        assertFalse(detail.strings().isEmpty());
        assertFalse(detail.imports().isEmpty());

        String address = detail.functions().get(0).address();
        assertTrue(store.function(summary.id(), address).isPresent());
        assertTrue(store.function(summary.id(), "0xdeadbeef").isEmpty());
    }
}
