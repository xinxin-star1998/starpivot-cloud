package cn.org.starpivot.job;

import cn.org.starpivot.common.job.JobInvokeTarget;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JobInvokeTargetTest {

    private static final String[] WHITELIST = {"cn.org.starpivot.job.task"};

    @Test
    void parse_shortClassName() {
        JobInvokeTarget.Parsed parsed = JobInvokeTarget.parse("CleanOperLogTask.cleanOperLog()");
        assertEquals("CleanOperLogTask", parsed.className());
        assertEquals("cleanOperLog", parsed.methodName());
        assertTrue(parsed.args().isEmpty());
        assertDoesNotThrow(() -> JobInvokeTarget.assertSafe(
                "CleanOperLogTask.cleanOperLog()", WHITELIST, new String[]{}));
    }

    @Test
    void parse_fullClassNameStillWorks() {
        JobInvokeTarget.Parsed noArg = JobInvokeTarget.parse(
                "cn.org.starpivot.job.task.CleanOperLogTask.cleanOperLog()");
        assertEquals("cn.org.starpivot.job.task.CleanOperLogTask", noArg.className());
        assertEquals("cleanOperLog", noArg.methodName());
    }

    @Test
    void assertSafe_rejectsMonolithPackage() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                JobInvokeTarget.assertSafe(
                        "com.star.pivot.quartz.task.CleanOperLogTask.cleanOperLog()",
                        WHITELIST,
                        new String[]{}));
        assertTrue(ex.getMessage().contains("白名单"));
    }
}
