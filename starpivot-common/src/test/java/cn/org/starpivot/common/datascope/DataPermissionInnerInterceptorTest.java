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

import static org.junit.jupiter.api.Assertions.*;
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
        // lenient: 仅 beforeQuery 集成测试才触发 getIfAvailable()，反射测试不会
        lenient().when(dataScopeProviderProvider.getIfAvailable()).thenReturn(dataScopeProvider);
        interceptor = new DataPermissionInnerInterceptor(dataScopeProviderProvider);
    }

    /**
     * 模拟 @DataPermission 注解用于测试。
     */
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

        /**
         * 通过反射调用私有方法 buildSqlCondition。
         */
        private String invokeBuildSqlCondition(DataScope dataScope, DataPermission annotation) throws Exception {
            var method = DataPermissionInnerInterceptor.class
                    .getDeclaredMethod("buildSqlCondition", DataScope.class, DataPermission.class);
            method.setAccessible(true);
            return (String) method.invoke(interceptor, dataScope, annotation);
        }

        @Test
        @DisplayName("data_scope=1 全部权限：deptIds=null, userDeptId=100 → 返回空字符串")
        void allScope_returnsEmpty() throws Exception {
            DataScope scope = new DataScope(null, null, 1L, 100L);
            DataPermission annotation = mockAnnotation("dept_id", "create_by");

            String result = invokeBuildSqlCondition(scope, annotation);

            assertEquals("", result);
        }

        @Test
        @DisplayName("data_scope=2 自定义权限：deptIds=[100,101] → 返回 IN 条件")
        void customScope_returnsInCondition() throws Exception {
            DataScope scope = new DataScope(null, Arrays.asList(100L, 101L), 1L, 100L);
            DataPermission annotation = mockAnnotation("d.dept_id", "u.user_id");

            String result = invokeBuildSqlCondition(scope, annotation);

            assertEquals("d.dept_id IN (100,101)", result);
        }

        @Test
        @DisplayName("data_scope=3 本部门：deptIds=[100] 单部门 → 返回 = 条件")
        void deptScope_returnsEqualsCondition() throws Exception {
            DataScope scope = new DataScope(null, Collections.singletonList(100L), 1L, 100L);
            DataPermission annotation = mockAnnotation("d.dept_id", "u.user_id");

            String result = invokeBuildSqlCondition(scope, annotation);

            assertEquals("d.dept_id = 100", result);
        }

        @Test
        @DisplayName("data_scope=4 本部门及子部门：deptIds=[100,101,102] → 返回 IN 条件")
        void deptAndChildScope_returnsInCondition() throws Exception {
            DataScope scope = new DataScope(null, Arrays.asList(100L, 101L, 102L), 1L, 100L);
            DataPermission annotation = mockAnnotation("d.dept_id", "u.user_id");

            String result = invokeBuildSqlCondition(scope, annotation);

            assertEquals("d.dept_id IN (100,101,102)", result);
        }

        @Test
        @DisplayName("data_scope=5 仅本人：deptIds=null, userDeptId=null, userId=42 → 返回 userAlias = userId")
        void selfScope_returnsUserCondition() throws Exception {
            DataScope scope = new DataScope();
            scope.setUserId(42L);
            DataPermission annotation = mockAnnotation("d.dept_id", "u.user_id");

            String result = invokeBuildSqlCondition(scope, annotation);

            assertEquals("u.user_id = 42", result);
        }

        @Test
        @DisplayName("预置 sqlFilter 优先使用")
        void presetSqlFilter_takesPrecedence() throws Exception {
            DataScope scope = new DataScope("custom_filter = 1", Arrays.asList(100L), 1L, 100L);
            DataPermission annotation = mockAnnotation("d.dept_id", "u.user_id");

            String result = invokeBuildSqlCondition(scope, annotation);

            assertEquals("custom_filter = 1", result);
        }
    }

    @Nested
    @DisplayName("appendWhereCondition SQL 修改")
    class AppendWhereConditionTests {

        /**
         * 通过反射调用私有方法 appendWhereCondition。
         */
        private String invokeAppendWhereCondition(String sql, String condition) throws Exception {
            var method = DataPermissionInnerInterceptor.class
                    .getDeclaredMethod("appendWhereCondition", String.class, String.class);
            method.setAccessible(true);
            return (String) method.invoke(interceptor, sql, condition);
        }

        @Test
        @DisplayName("无 WHERE 子句的 SELECT → 追加 WHERE")
        void selectWithoutWhere_appendsWhere() throws Exception {
            String sql = "SELECT * FROM sys_user";
            String condition = "dept_id = 100";

            String result = invokeAppendWhereCondition(sql, condition);

            assertNotNull(result);
            assertTrue(result.toLowerCase().contains("where"));
            assertTrue(result.contains("dept_id = 100"));
        }

        @Test
        @DisplayName("已有 WHERE 子句 → 追加 AND 条件")
        void selectWithWhere_appendsAnd() throws Exception {
            String sql = "SELECT * FROM sys_user WHERE status = '0'";
            String condition = "dept_id = 100";

            String result = invokeAppendWhereCondition(sql, condition);

            assertNotNull(result);
            assertTrue(result.toLowerCase().contains("and"));
            assertTrue(result.contains("dept_id = 100"));
            assertTrue(result.contains("status = '0'"));
        }

        @Test
        @DisplayName("含 ORDER BY 子句 → 条件插入在 ORDER BY 之前")
        void selectWithOrderBy_conditionBeforeOrderBy() throws Exception {
            String sql = "SELECT * FROM sys_user ORDER BY create_time DESC";
            String condition = "dept_id = 100";

            String result = invokeAppendWhereCondition(sql, condition);

            assertNotNull(result);
            int conditionPos = result.indexOf("dept_id = 100");
            int orderByPos = result.toUpperCase().indexOf("ORDER BY");
            assertTrue(conditionPos < orderByPos, "条件应在 ORDER BY 之前");
        }

        @Test
        @DisplayName("含 LIMIT 子句 → 条件插入在 LIMIT 之前")
        void selectWithLimit_conditionBeforeLimit() throws Exception {
            String sql = "SELECT * FROM sys_user LIMIT 10";
            String condition = "dept_id = 100";

            String result = invokeAppendWhereCondition(sql, condition);

            assertNotNull(result);
            int conditionPos = result.indexOf("dept_id = 100");
            int limitPos = result.toUpperCase().indexOf("LIMIT");
            assertTrue(conditionPos < limitPos, "条件应在 LIMIT 之前");
        }

        @Test
        @DisplayName("带表别名的条件 → 正确保留别名")
        void conditionWithAlias_preserved() throws Exception {
            String sql = "SELECT u.* FROM sys_user u WHERE u.status = '0'";
            String condition = "u.dept_id IN (100,101)";

            String result = invokeAppendWhereCondition(sql, condition);

            assertNotNull(result);
            // JSqlParser 序列化时可能在逗号后加空格，用 contains 分段断言
            assertTrue(result.contains("u.dept_id IN"), "应包含 IN 条件: " + result);
            assertTrue(result.contains("100") && result.contains("101"), "应包含部门 ID: " + result);
        }
    }
}
