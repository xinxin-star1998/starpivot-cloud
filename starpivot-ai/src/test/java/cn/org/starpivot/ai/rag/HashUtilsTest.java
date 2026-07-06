package cn.org.starpivot.ai.rag;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class HashUtilsTest {

    @Test
    void md5_isDeterministic() {
        assertEquals(HashUtils.md5("hello"), HashUtils.md5("hello"));
    }

    @Test
    void md5_differsForDifferentInputs() {
        assertNotEquals(HashUtils.md5("hello"), HashUtils.md5("world"));
    }

    @Test
    void md5_returns32HexChars() {
        assertEquals(32, HashUtils.md5("test question").length());
    }
}
