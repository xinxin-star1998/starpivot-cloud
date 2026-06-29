package cn.org.starpivot.file.support;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FileHashUtilsTest {

    @Test
    void sha256Hex_sameContentProducesSameHash() {
        MockMultipartFile first = new MockMultipartFile(
                "file", "a.txt", "text/plain", "hello".getBytes());
        MockMultipartFile second = new MockMultipartFile(
                "file", "b.txt", "text/plain", "hello".getBytes());

        assertEquals(FileHashUtils.sha256Hex(first), FileHashUtils.sha256Hex(second));
    }

    @Test
    void sha256Hex_differentContentProducesDifferentHash() {
        MockMultipartFile first = new MockMultipartFile(
                "file", "a.txt", "text/plain", "hello".getBytes());
        MockMultipartFile second = new MockMultipartFile(
                "file", "b.txt", "text/plain", "world".getBytes());

        assertNotEquals(FileHashUtils.sha256Hex(first), FileHashUtils.sha256Hex(second));
    }
}
