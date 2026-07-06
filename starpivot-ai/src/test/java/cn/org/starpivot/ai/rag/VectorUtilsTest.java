package cn.org.starpivot.ai.rag;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class VectorUtilsTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void cosineSimilarity_identicalVectorsReturnOne() {
        float[] vector = {1f, 0f, 0f};
        assertEquals(1D, VectorUtils.cosineSimilarity(vector, vector), 1e-6);
    }

    @Test
    void cosineSimilarity_orthogonalVectorsReturnZero() {
        float[] left = {1f, 0f};
        float[] right = {0f, 1f};
        assertEquals(0D, VectorUtils.cosineSimilarity(left, right), 1e-6);
    }

    @Test
    void serializeDeserialize_roundTrip() {
        float[] original = {0.1f, 0.2f, 0.3f};
        String json = VectorUtils.serialize(original, objectMapper);
        float[] restored = VectorUtils.deserialize(json, objectMapper);

        assertEquals(original.length, restored.length);
        for (int i = 0; i < original.length; i++) {
            assertEquals(original[i], restored[i], 1e-6f);
        }
    }

    @Test
    void deserialize_invalidJsonReturnsEmptyArray() {
        assertEquals(0, VectorUtils.deserialize("{bad", objectMapper).length);
    }

    @Test
    void deserialize_nullElementReturnsEmptyArray() {
        assertEquals(0, VectorUtils.deserialize("[1.0,null,3.0]", objectMapper).length);
    }

    @Test
    void cosineSimilarity_mismatchedLengthReturnsZero() {
        assertEquals(0D, VectorUtils.cosineSimilarity(new float[]{1f}, new float[]{1f, 2f}));
    }

    @Test
    void cosineSimilarity_zeroVectorReturnsZero() {
        assertEquals(0D, VectorUtils.cosineSimilarity(new float[]{0f, 0f}, new float[]{1f, 2f}));
    }
}
