package cn.org.starpivot.common.datascope;

import cn.org.starpivot.common.annotation.DataPermission;
import cn.org.starpivot.common.entity.DataScope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

/**
 * {@link DataPermissionInnerInterceptor} 单元测试。
 * <p>验证各 data_scope 场景下的 SQL 条件构建逻辑。</p>
 */
@ExtendWith(MockitoExtension.class)
class DataPermissionInnerInterceptorTest {

    @Mock
    private DataScopeProvider dataScopeProvider;

    @Mock
    private ObjectProvider<DataScopeProvider> dataScopeProviderProvider;

    private DataPermissionInnerInterceptor interceptor;

    @BeforeEach
    void setUp() {
        lenient().when(dataScopeProviderProvider.getIfAvailable()).thenReturn(dataScopeProvider);
        interceptor = new DataPermissionInnerInterceptor(dataScopeProviderProvider);
    }

    private static DataPermission mockAnnotation(String deptAlias, String userAlias) {
        return new DataPermission() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return DataPermission.class;
            }

            @Override
            public String deptAlias() {
                return deptAlias;
            }

            @Override
            public String userAlias() {
                return userAlias;
            }
        };
    }

    @Nested
    @DisplayName("buildSqlCondition 场景")
    class BuildSqlConditionTests {

        @Test
        @DisplayName("全部权限：isAll=true → 返回空字符串")
        void allScope_returnsEmpty() {
            DataScope scope = DataScope.all(1L, 100L);
            DataPermission annotation = mockAnnotation("dept_id", "create_by");

            String result = interceptor.buildSqlCondition(scope, annotation);

            assertEquals("", result);
        }

        @Test
        @DisplayName("自定义权限：deptIds=[100,101] → 返回 IN 条件")
        void customScope_returnsInCondition() {
            DataScope scope = DataScope.restricted(1L, 100L, Arrays.asList(100L, 101L), false);
            DataPermission annotation = mockAnnotation("u.dept_id", "u.user_id");

            String result = interceptor.buildSqlCondition(scope, annotation);

            assertEquals("u.dept_id IN (100,101)", result);
        }

        @Test
        @DisplayName("本部门：deptIds=[100] → 返回 = 条件")
        void deptScope_returnsEqualsCondition() {
            DataScope scope = DataScope.restricted(1L, 100L, Collections.singletonList(100L), false);
            DataPermission annotation = mockAnnotation("u.dept_id", "u.user_id");

            String result = interceptor.buildSqlCondition(scope, annotation);

            assertEquals("u.dept_id = 100", result);
        }

        @Test
        @DisplayName("本部门及子部门：deptIds 多个 → 返回 IN 条件")
        void deptAndChildScope_returnsInCondition() {
            DataScope scope = DataScope.restricted(1L, 100L, Arrays.asList(100L, 101L, 102L), false);
            DataPermission annotation = mockAnnotation("u.dept_id", "u.user_id");

            String result = interceptor.buildSqlCondition(scope, annotation);

            assertEquals("u.dept_id IN (100,101,102)", result);
        }

        @Test
        @DisplayName("仅本人：includeSelf=true → 返回 userAlias = userId")
        void selfScope_returnsUserCondition() {
            DataScope scope = DataScope.restricted(42L, null, List.of(), true);
            DataPermission annotation = mockAnnotation("u.dept_id", "u.user_id");

            String result = interceptor.buildSqlCondition(scope, annotation);

            assertEquals("u.user_id = 42", result);
        }

        @Test
        @DisplayName("自定义部门并上仅本人 → 部门条件 OR 本人")
        void customAndSelf_returnsOrCondition() {
            DataScope scope = DataScope.restricted(5L, 100L, Arrays.asList(100L, 101L), true);
            DataPermission annotation = mockAnnotation("u.dept_id", "u.user_id");

            String result = interceptor.buildSqlCondition(scope, annotation);

            assertEquals("(u.dept_id IN (100,101) OR u.user_id = 5)", result);
        }

        @Test
        @DisplayName("无可见数据 → 返回 1 = 0")
        void noneScope_returnsDeny() {
            DataScope scope = DataScope.none(9L);
            DataPermission annotation = mockAnnotation("u.dept_id", "u.user_id");

            String result = interceptor.buildSqlCondition(scope, annotation);

            assertEquals(DataPermissionInnerInterceptor.DENY_SQL, result);
        }

        @Test
        @DisplayName("预置 sqlFilter 优先使用")
        void presetSqlFilter_takesPrecedence() {
            DataScope scope = new DataScope("custom_filter = 1", Arrays.asList(100L), 1L, 100L);
            DataPermission annotation = mockAnnotation("u.dept_id", "u.user_id");

            String result = interceptor.buildSqlCondition(scope, annotation);

            assertEquals("custom_filter = 1", result);
        }
    }

    @Nested
    @DisplayName("appendWhereCondition SQL 修改")
    class AppendWhereConditionTests {

        @Test
        @DisplayName("无 WHERE 子句的 SELECT → 追加 WHERE")
        void selectWithoutWhere_appendsWhere() {
            String sql = "SELECT * FROM sys_user";
            String condition = "dept_id = 100";

            String result = interceptor.appendWhereCondition(sql, condition);

            assertNotNull(result);
            assertTrue(result.toLowerCase().contains("where"));
            assertTrue(result.contains("dept_id = 100"));
        }

        @Test
        @DisplayName("已有 WHERE 子句 → 追加 AND 条件")
        void selectWithWhere_appendsAnd() {
            String sql = "SELECT * FROM sys_user WHERE status = '0'";
            String condition = "dept_id = 100";

            String result = interceptor.appendWhereCondition(sql, condition);

            assertNotNull(result);
            assertTrue(result.toLowerCase().contains("and"));
            assertTrue(result.contains("dept_id = 100"));
            assertTrue(result.contains("status = '0'"));
        }

        @Test
        @DisplayName("含 ORDER BY 子句 → 条件插入在 ORDER BY 之前")
        void selectWithOrderBy_conditionBeforeOrderBy() {
            String sql = "SELECT * FROM sys_user ORDER BY create_time DESC";
            String condition = "dept_id = 100";

            String result = interceptor.appendWhereCondition(sql, condition);

            assertNotNull(result);
            int conditionPos = result.indexOf("dept_id = 100");
            int orderByPos = result.toUpperCase().indexOf("ORDER BY");
            assertTrue(conditionPos < orderByPos, "条件应在 ORDER BY 之前");
        }

        @Test
        @DisplayName("含 LIMIT 子句 → 条件插入在 LIMIT 之前")
        void selectWithLimit_conditionBeforeLimit() {
            String sql = "SELECT * FROM sys_user LIMIT 10";
            String condition = "dept_id = 100";

            String result = interceptor.appendWhereCondition(sql, condition);

            assertNotNull(result);
            int conditionPos = result.indexOf("dept_id = 100");
            int limitPos = result.toUpperCase().indexOf("LIMIT");
            assertTrue(conditionPos < limitPos, "条件应在 LIMIT 之前");
        }

        @Test
        @DisplayName("带表别名的条件 → 正确保留别名")
        void conditionWithAlias_preserved() {
            String sql = "SELECT u.* FROM sys_user u WHERE u.status = '0'";
            String condition = "u.dept_id IN (100,101)";

            String result = interceptor.appendWhereCondition(sql, condition);

            assertNotNull(result);
            assertTrue(result.contains("u.dept_id IN"), "应包含 IN 条件: " + result);
            assertTrue(result.contains("100") && result.contains("101"), "应包含部门 ID: " + result);
        }

        @Test
        @DisplayName("部门 OR 本人条件可被解析追加")
        void orCondition_appended() {
            String sql = "SELECT u.* FROM sys_user u WHERE u.del_flag = '0'";
            String condition = "(u.dept_id IN (100,101) OR u.user_id = 5)";

            String result = interceptor.appendWhereCondition(sql, condition);

            assertNotNull(result);
            assertTrue(result.contains("u.user_id"), result);
            assertTrue(result.contains("u.dept_id"), result);
        }
    }
}
