package cn.org.starpivot.ai.rag;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public final class VectorUtils {

    private VectorUtils() {
    }

    public static float[] deserialize(String json, ObjectMapper objectMapper) {
        if (json == null || json.isBlank()) {
            return new float[0];
        }
        try {
            List<Double> values = objectMapper.readValue(json, new TypeReference<>() {});
            float[] vector = new float[values.size()];
            for (int i = 0; i < values.size(); i++) {
                Double value = values.get(i);
                if (value == null) {
                    return new float[0];
                }
                vector[i] = value.floatValue();
            }
            return vector;
        } catch (Exception ex) {
            return new float[0];
        }
    }

    public static String serialize(float[] vector, ObjectMapper objectMapper) {
        if (vector == null || vector.length == 0) {
            return null;
        }
        try {
            Double[] values = new Double[vector.length];
            for (int i = 0; i < vector.length; i++) {
                values[i] = (double) vector[i];
            }
            return objectMapper.writeValueAsString(values);
        } catch (Exception ex) {
            return null;
        }
    }

    public static double cosineSimilarity(float[] left, float[] right) {
        if (left == null || right == null || left.length == 0 || right.length == 0 || left.length != right.length) {
            return 0D;
        }
        double dot = 0D;
        double normLeft = 0D;
        double normRight = 0D;
        for (int i = 0; i < left.length; i++) {
            dot += left[i] * right[i];
            normLeft += left[i] * left[i];
            normRight += right[i] * right[i];
        }
        if (normLeft == 0D || normRight == 0D) {
            return 0D;
        }
        return dot / (Math.sqrt(normLeft) * Math.sqrt(normRight));
    }
}
