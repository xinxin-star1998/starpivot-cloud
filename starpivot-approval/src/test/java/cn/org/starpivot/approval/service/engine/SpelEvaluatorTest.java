package cn.org.starpivot.approval.service.engine;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SpelEvaluatorTest {

    private final SpelEvaluator evaluator = new SpelEvaluator();

    @Test
    void emptyOrDefaultExpressionIsTrue() {
        assertTrue(evaluator.evaluateBoolean(null, Map.of()));
        assertTrue(evaluator.evaluateBoolean("", Map.of()));
        assertTrue(evaluator.evaluateBoolean("default", Map.of()));
    }

    @Test
    void evaluatesContextMapComparison() {
        Map<String, Object> context = Map.of("amount", 12800);
        assertTrue(evaluator.evaluateBoolean("#context['amount'] > 10000", context));
        assertFalse(evaluator.evaluateBoolean("#context['amount'] > 20000", context));
    }

    @Test
    void rejectsTypeReferenceInjection() {
        assertThrows(Exception.class,
                () -> evaluator.evaluateBoolean("T(java.lang.Runtime).getRuntime().exec('id')", Map.of()));
    }
}
