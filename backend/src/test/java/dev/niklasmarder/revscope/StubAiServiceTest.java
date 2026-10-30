package dev.niklasmarder.revscope;

import dev.niklasmarder.revscope.model.DecompiledFunction;
import dev.niklasmarder.revscope.model.FunctionExplanation;
import dev.niklasmarder.revscope.service.StubAiService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StubAiServiceTest {

    private final StubAiService service = new StubAiService();

    @Test
    void suggestsNameAndSummary() {
        DecompiledFunction fn = new DecompiledFunction("0x00401000", "FUN_00401000",
            "undefined8 FUN_00401000(long param_1)", "undefined8 FUN_00401000(long param_1)\n{\n  return 0;\n}");
        FunctionExplanation explanation = service.explain(fn);

        assertEquals("0x00401000", explanation.address());
        assertNotNull(explanation.suggestedName());
        assertFalse(explanation.suggestedName().startsWith("FUN_"));
        assertFalse(explanation.summary().isBlank());
        assertEquals("heuristic", explanation.source());
    }
}
