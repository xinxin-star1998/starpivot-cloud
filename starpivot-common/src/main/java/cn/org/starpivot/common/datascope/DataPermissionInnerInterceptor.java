package cn.org.starpivot.common.datascope;

import cn.org.starpivot.common.annotation.DataPermission;
import cn.org.starpivot.common.entity.DataScope;
import cn.org.starpivot.common.security.SecurityContextUtils;
import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.select.SetOperationList;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 数据权限 MyBatis-Plus 内部拦截器。
 * <p>
 * 在 {@code beforeQuery} 阶段检查 Mapper 方法是否标注 {@link DataPermission}，
 * 若标注则根据当前用户角色的 {@code data_scope} 动态追加 WHERE 条件。
 * </p>
 * <p>拦截器行为：</p>
 * <ul>
 *   <li>{@code data_scope=1}（全部权限）— 不追加任何条件</li>
 *   <li>{@code data_scope=2}（自定义权限）— 追加 {@code dept_id IN (deptIds)}</li>
 *   <li>{@code data_scope=3}（本部门）— 追加 {@code dept_id = userDeptId}</li>
 *   <li>{@code data_scope=4}（本部门及子部门）— 追加 {@code dept_id IN (deptIds)}</li>
 *   <li>{@code data_scope=5}（仅本人）— 追加 {@code userAlias = userId}</li>
 * </ul>
 */
@Slf4j
public class DataPermissionInnerInterceptor implements InnerInterceptor {

    private final ObjectProvider<DataScopeProvider> dataScopeProviderProvider;

    /**
     * 构造拦截器。
     *
     * @param dataScopeProviderProvider 数据范围提供者延迟工厂，不可为 {@code null}
     */
    public DataPermissionInnerInterceptor(ObjectProvider<DataScopeProvider> dataScopeProviderProvider) {
        this.dataScopeProviderProvider = dataScopeProviderProvider;
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter,
                            RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        // 检查是否忽略拦截器（如 @InterceptorIgnore(dataPermission = "true")）
        if (InterceptorIgnoreHelper.willIgnoreDataPermission(ms.getId())) {
            return;
        }

        // 获取 @DataPermission 注解
        DataPermission annotation = getDataPermissionAnnotation(ms);
        if (annotation == null) {
            return;
        }

        // 获取当前登录用户
        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            log.debug("DataPermission: no authenticated user, skipping data scope filter");
            return;
        }

        // 延迟获取 DataScopeProvider（避免 Bean 初始化循环依赖）
        DataScopeProvider dataScopeProvider = dataScopeProviderProvider.getIfAvailable();
        if (dataScopeProvider == null) {
            log.debug("DataPermission: no DataScopeProvider bean found, skipping filter");
            return;
        }

        // 解析数据权限范围
        DataScope dataScope = dataScopeProvider.resolve(userId);
        if (dataScope == null) {
            log.debug("DataPermission: unable to resolve data scope for user {}, skipping filter", userId);
            return;
        }

        // 构建 SQL 过滤条件
        String sqlCondition = buildSqlCondition(dataScope, annotation);
        if (!StringUtils.hasText(sqlCondition)) {
            // 全部数据权限或无需过滤
            return;
        }

        // 修改 SQL
        String originalSql = boundSql.getSql();
        String modifiedSql = appendWhereCondition(originalSql, sqlCondition);
        if (modifiedSql != null && !modifiedSql.equals(originalSql)) {
            PluginUtils.MPBoundSql mpBoundSql = PluginUtils.mpBoundSql(boundSql);
            mpBoundSql.sql(modifiedSql);
            log.debug("DataPermission: modified SQL for mapper {}, added condition: {}", ms.getId(), sqlCondition);
        }
    }

    /**
     * 从 MappedStatement 中获取 Mapper 方法上的 {@link DataPermission} 注解。
     */
    private DataPermission getDataPermissionAnnotation(MappedStatement ms) {
        String msId = ms.getId();
        try {
            int lastDot = msId.lastIndexOf('.');
            if (lastDot < 0) {
                return null;
            }
            String className = msId.substring(0, lastDot);
            String methodName = msId.substring(lastDot + 1);
            Class<?> mapperClass = Class.forName(className);
            for (Method method : mapperClass.getMethods()) {
                if (method.getName().equals(methodName)) {
                    DataPermission annotation = method.getAnnotation(DataPermission.class);
                    if (annotation != null) {
                        return annotation;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            log.warn("DataPermission: mapper class not found: {}", msId, e);
        } catch (Exception e) {
            log.warn("DataPermission: failed to get annotation from mapper: {}", msId, e);
        }
        return null;
    }

    /**
     * 根据 data_scope 构建 SQL WHERE 条件片段。
     * <p>DataScope 来源约定：</p>
     * <ul>
     *   <li>全部权限：{@code deptIds=null}, {@code userDeptId!=null} — 不追加条件</li>
     *   <li>自定义/本部门/本部门及子部门：{@code deptIds} 非空 — 追加 deptAlias IN/={...}</li>
     *   <li>仅本人：{@code deptIds=null}, {@code userDeptId=null}, {@code userId!=null} — 追加 userAlias = userId</li>
     * </ul>
     *
     * @param dataScope  数据权限上下文
     * @param annotation 注解配置
     * @return SQL 条件片段，无过滤需求时返回空字符串
     */
    private String buildSqlCondition(DataScope dataScope, DataPermission annotation) {
        String deptAlias = annotation.deptAlias();
        String userAlias = annotation.userAlias();
        List<Long> deptIds = dataScope.getDeptIds();
        Long userId = dataScope.getUserId();
        Long userDeptId = dataScope.getUserDeptId();
        String sqlFilter = dataScope.getSqlFilter();

        // 如果 DataScopeProvider 已经直接提供了 SQL 片段，优先使用
        if (StringUtils.hasText(sqlFilter)) {
            return sqlFilter;
        }

        // 有部门 ID 列表：自定义数据权限 / 本部门 / 本部门及子部门
        if (deptIds != null && !deptIds.isEmpty()) {
            if (deptIds.size() == 1) {
                return deptAlias + " = " + deptIds.get(0);
            }
            String inList = deptIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            return deptAlias + " IN (" + inList + ")";
        }

        // 仅本人：deptIds 为空且 userDeptId 也为空（由 buildSelfOnlyScope 构建）
        if (deptIds == null && userDeptId == null && userId != null) {
            return userAlias + " = " + userId;
        }

        // 全部数据权限（deptIds=null, userDeptId!=null）或其他未知情况：不追加条件
        return "";
    }

    /**
     * 将 WHERE 条件追加到原始 SQL。
     * <p>
     * 使用 JSqlParser 解析并修改 SQL，支持 SELECT 语句的 WHERE 和子查询。
     * </p>
     *
     * @param originalSql 原始 SQL
     * @param condition   要追加的条件（不含 AND 前缀）
     * @return 修改后的 SQL，解析失败时返回 {@code null}
     */
    private String appendWhereCondition(String originalSql, String condition) {
        try {
            Statement statement = CCJSqlParserUtil.parse(originalSql);
            if (statement instanceof Select select) {
                processSelect(select, condition);
                return select.toString();
            }
            // 非 SELECT 语句暂不处理
            return originalSql;
        } catch (Exception e) {
            log.warn("DataPermission: failed to parse SQL, skipping modification. SQL: {}", originalSql, e);
            // 解析失败时回退到字符串拼接方式
            return fallbackAppendCondition(originalSql, condition);
        }
    }

    /**
     * 处理 SELECT 语句，向主查询和子查询追加 WHERE 条件。
     */
    private void processSelect(Select select, String condition) {
        if (select instanceof PlainSelect plainSelect) {
            processPlainSelect(plainSelect, condition);
        } else if (select instanceof SetOperationList setOp) {
            // UNION 等集合操作：处理每个 SELECT
            for (Select subSelect : setOp.getSelects()) {
                processSelect(subSelect, condition);
            }
        }
    }

    /**
     * 处理 PlainSelect，追加 WHERE 条件。
     */
    private void processPlainSelect(PlainSelect plainSelect, String condition) {
        try {
            Expression conditionExpr = CCJSqlParserUtil.parseCondExpression(condition);
            Expression existingWhere = plainSelect.getWhere();
            if (existingWhere == null) {
                plainSelect.setWhere(conditionExpr);
            } else {
                plainSelect.setWhere(new AndExpression(existingWhere, conditionExpr));
            }
        } catch (Exception e) {
            log.warn("DataPermission: failed to parse condition expression: {}", condition, e);
        }
    }

    /**
     * 回退方案：通过字符串操作追加 WHERE 条件。
     * <p>仅在 JSqlParser 解析失败时使用。</p>
     */
    private String fallbackAppendCondition(String sql, String condition) {
        try {
            // 简单处理：在 ORDER BY / GROUP BY / LIMIT 之前插入 WHERE 条件
            String upperSql = sql.toUpperCase();
            int insertPos = findInsertPosition(upperSql);

            String before = sql.substring(0, insertPos);
            String after = sql.substring(insertPos);

            if (upperSql.contains("WHERE")) {
                return before + " AND " + condition + " " + after;
            } else {
                return before + " WHERE " + condition + " " + after;
            }
        } catch (Exception e) {
            log.warn("DataPermission: fallback append failed, returning original SQL", e);
            return sql;
        }
    }

    /**
     * 查找条件插入位置（ORDER BY / GROUP BY / HAVING / LIMIT 之前）。
     */
    private int findInsertPosition(String upperSql) {
        int pos = upperSql.indexOf(" ORDER BY");
        if (pos > 0) return pos;
        pos = upperSql.indexOf(" GROUP BY");
        if (pos > 0) return pos;
        pos = upperSql.indexOf(" HAVING");
        if (pos > 0) return pos;
        pos = upperSql.indexOf(" LIMIT");
        if (pos > 0) return pos;
        pos = upperSql.indexOf(" FOR UPDATE");
        if (pos > 0) return pos;
        return upperSql.length();
    }
}
