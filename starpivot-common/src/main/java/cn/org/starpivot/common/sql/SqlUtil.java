package cn.org.starpivot.common.sql;

import cn.org.starpivot.common.exception.BizException;
import org.apache.commons.lang3.StringUtils;

/**
 * SQL 安全工具类，用于校验排序字段并拦截常见 SQL 注入关键字。
 * <p>
 * 在动态拼接 ORDER BY 或用户输入参与 SQL 的场景下调用，校验失败时抛出 {@link BizException}。
 * </p>
 */
public class SqlUtil
{
    /** SQL 注入关键字正则片段，用于 {@link #filterKeyword(String)} */
    public static String SQL_REGEX = "\u000B|and |extractvalue|updatexml|sleep|information_schema|exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |or |union |like |+|/*|user()";

    /** ORDER BY 字段合法字符模式（字母、数字、下划线、空格、逗号、点） */
    public static String SQL_PATTERN = "[a-zA-Z0-9_\\ \\,\\.]+";
    private static final int ORDER_BY_MAX_LENGTH = 500;

    /**
     * 校验并返回 ORDER BY 片段；非法或超长时抛出 {@link BizException}。
     *
     * @param value 待校验的排序 SQL 片段
     * @return 原值（校验通过）
     */
    public static String escapeOrderBySql(String value) {
        if (StringUtils.isNotEmpty(value) && !isValidOrderBySql(value)) {
            throw new BizException("参数不符合规范，不能进行查询");
        }
        if (StringUtils.length(value) > ORDER_BY_MAX_LENGTH) {
            throw new BizException("参数已超过最大限制，不能进行查询");
        }
        return value;
    }

    /**
     * 判断排序字段是否仅包含 {@link #SQL_PATTERN} 允许的字符。
     *
     * @param value 待校验字符串
     * @return 合法返回 {@code true}
     */
    public static boolean isValidOrderBySql(String value) {
        return value.matches(SQL_PATTERN);
    }

    /**
     * 扫描输入值是否包含 {@link #SQL_REGEX} 中的危险关键字，命中则抛出 {@link BizException}。
     *
     * @param value 用户输入或动态 SQL 片段
     */
    public static void filterKeyword(String value) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        String[] sqlKeywords = StringUtils.split(SQL_REGEX, "\\|");
        for (String sqlKeyword : sqlKeywords) {
            if (StringUtils.indexOfIgnoreCase(value, sqlKeyword) > -1) {
                throw new BizException("参数存在SQL注入风险");
            }
        }
    }
}
