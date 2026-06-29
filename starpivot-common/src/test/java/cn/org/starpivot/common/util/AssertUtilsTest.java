package cn.org.starpivot.common.util;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AssertUtilsTest {

    @Test
    void notNull_throwsBizExceptionWhenNull() {
        BizException ex = assertThrows(BizException.class,
                () -> AssertUtils.notNull(null, ErrorCode.PARAM_INVALID, "文件夹ID不能为空"));
        assertEquals(ErrorCode.PARAM_INVALID, ex.getCode());
        assertEquals("文件夹ID不能为空", ex.getMessage());
    }

    @Test
    void notEmptyCollection_throwsWhenEmpty() {
        BizException ex = assertThrows(BizException.class,
                () -> AssertUtils.notEmpty(Collections.emptyList(), ErrorCode.PARAM_INVALID, "删除ID不能为空"));
        assertEquals("删除ID不能为空", ex.getMessage());
    }

    @Test
    void isTrue_throwsWhenFalse() {
        BizException ex = assertThrows(BizException.class,
                () -> AssertUtils.isTrue(false, "条件不满足"));
        assertEquals(ErrorCode.PARAM_INVALID, ex.getCode());
    }

    @Test
    void notEmptyCollection_acceptsNonEmpty() {
        AssertUtils.notEmpty(List.of(1L), ErrorCode.PARAM_INVALID, "不应抛出");
    }
}
