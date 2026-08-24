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
 * 若标注则根据当前用户角色的数据范围动态追加 WHERE 条件。
 * 无法解析范围或改写 SQL 时追加 {@code 1 = 0}（失败即拒绝），避免放行全表。
 * </p>
 */
@Slf4j
public class DataPermissionInnerInterceptor implements InnerInterceptor {

    static final String DENY_SQL = "1 = 0";

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
        if (InterceptorIgnoreHelper.willIgnoreDataPermission(ms.getId())) {
            return;
        }

        DataPermission annotation = getDataPermissionAnnotation(ms);
        if (annotation == null) {
            return;
        }

        Long userId = SecurityContextUtils.getUserId();
        if (userId == null) {
            applySqlCondition(boundSql, DENY_SQL, ms.getId());
            return;
        }

        DataScopeProvider dataScopeProvider = dataScopeProviderProvider.getIfAvailable();
        if (dataScopeProvider == null) {
            log.warn("DataPermission: no DataScopeProvider bean, denying rows for mapper {}", ms.getId());
            applySqlCondition(boundSql, DENY_SQL, ms.getId());
            return;
        }

        DataScope dataScope = dataScopeProvider.resolve(userId);
        if (dataScope == null) {
            applySqlCondition(boundSql, DENY_SQL, ms.getId());
            return;
        }

        String sqlCondition = buildSqlCondition(dataScope, annotation);
        if (!StringUtils.hasText(sqlCondition)) {
            return;
        }
        applySqlCondition(boundSql, sqlCondition, ms.getId());
    }

    private void applySqlCondition(BoundSql boundSql, String sqlCondition, String msId) throws SQLException {
        String originalSql = boundSql.getSql();
        String modifiedSql = appendWhereCondition(originalSql, sqlCondition);
        if (!StringUtils.hasText(modifiedSql)) {
            throw new SQLException("DataPermission: failed to apply data scope filter for " + msId);
        }
        PluginUtils.MPBoundSql mpBoundSql = PluginUtils.mpBoundSql(boundSql);
        mpBoundSql.sql(modifiedSql);
        log.debug("DataPermission: modified SQL for mapper {}, added condition: {}", msId, sqlCondition);
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
     * 根据数据范围构建 SQL WHERE 条件片段。
     *
     * @param dataScope  数据权限上下文
     * @param annotation 注解配置
     * @return SQL 条件片段；全部权限返回空字符串；无可见数据返回 {@link #DENY_SQL}
     */
    String buildSqlCondition(DataScope dataScope, DataPermission annotation) {
        if (dataScope.isAll()) {
            return "";
        }

        String sqlFilter = dataScope.getSqlFilter();
        if (StringUtils.hasText(sqlFilter)) {
            return sqlFilter;
        }

        String deptAlias = annotation.deptAlias();
        String userAlias = annotation.userAlias();
        List<Long> deptIds = dataScope.getDeptIds();
        Long userId = dataScope.getUserId();

        String deptCondition = null;
        if (deptIds != null && !deptIds.isEmpty()) {
            if (deptIds.size() == 1) {
                deptCondition = deptAlias + " = " + deptIds.get(0);
            } else {
                String inList = deptIds.stream()
                        .map(String::valueOf)
                        .collect(Collectors.joining(","));
                deptCondition = deptAlias + " IN (" + inList + ")";
            }
        }

        String selfCondition = null;
        if (dataScope.isIncludeSelf() && userId != null) {
            selfCondition = userAlias + " = " + userId;
        }

        if (deptCondition != null && selfCondition != null) {
            return "(" + deptCondition + " OR " + selfCondition + ")";
        }
        if (deptCondition != null) {
            return deptCondition;
        }
        if (selfCondition != null) {
            return selfCondition;
        }
        return DENY_SQL;
    }

    /**
     * 将 WHERE 条件追加到原始 SQL。
     *
     * @param originalSql 原始 SQL
     * @param condition   要追加的条件（不含 AND 前缀）
     * @return 修改后的 SQL，解析与回退均失败时返回 {@code null}
     */
    String appendWhereCondition(String originalSql, String condition) {
        try {
            Statement statement = CCJSqlParserUtil.parse(originalSql);
            if (statement instanceof Select select) {
                processSelect(select, condition);
                return select.toString();
            }
            return originalSql;
        } catch (Exception e) {
            log.warn("DataPermission: failed to parse SQL, using fallback. SQL: {}", originalSql, e);
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
            throw new IllegalStateException("DataPermission: failed to parse condition expression: " + condition, e);
        }
    }

    /**
     * 回退方案：通过字符串操作追加 WHERE 条件。
     *
     * @return 修改后的 SQL，失败时返回 {@code null}
     */
    private String fallbackAppendCondition(String sql, String condition) {
        try {
            String upperSql = sql.toUpperCase();
            int insertPos = findInsertPosition(upperSql);

            String before = sql.substring(0, insertPos);
            String after = sql.substring(insertPos);

            if (upperSql.contains("WHERE")) {
                return before + " AND " + condition + " " + after;
            }
            return before + " WHERE " + condition + " " + after;
        } catch (Exception e) {
            log.warn("DataPermission: fallback append failed", e);
            return null;
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
