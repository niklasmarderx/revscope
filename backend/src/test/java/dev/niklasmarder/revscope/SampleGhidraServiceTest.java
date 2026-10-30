package dev.niklasmarder.revscope;

import dev.niklasmarder.revscope.model.AnalysisResult;
import dev.niklasmarder.revscope.service.SampleGhidraService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SampleGhidraServiceTest {

    private final SampleGhidraService service = new SampleGhidraService();

    @Test
    void producesFunctionsWithDecompiledCode() {
        AnalysisResult result = service.analyze("demo.bin", new byte[]{1, 2, 3, 4});

        assertFalse(result.functions().isEmpty(), "expected some functions");
        assertEquals(result.functions().size(), result.decompiled().size());
        result.functions().forEach(fn -> {
            assertTrue(result.decompiled().containsKey(fn.address()));
            assertTrue(result.decompiled().get(fn.address()).code().contains("return"));
        });
    }

    @Test
    void isDeterministicForSameInput() {
        byte[] bytes = "hello world".getBytes();
        AnalysisResult a = service.analyze("same.bin", bytes);
        AnalysisResult b = service.analyze("same.bin", bytes);
        assertEquals(a.arch(), b.arch());
        assertEquals(a.functions().size(), b.functions().size());
    }
}
