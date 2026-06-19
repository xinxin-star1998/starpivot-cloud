package cn.org.starpivot.common.sql;

import cn.org.starpivot.common.exception.BizException;
import org.apache.commons.lang3.StringUtils;

public class SqlUtil
{
    public static String SQL_REGEX = "\u000B|and |extractvalue|updatexml|sleep|information_schema|exec |insert |select |delete |update |drop |count |chr |mid |master |truncate |char |declare |or |union |like |+|/*|user()";
    public static String SQL_PATTERN = "[a-zA-Z0-9_\\ \\,\\.]+";
    private static final int ORDER_BY_MAX_LENGTH = 500;

    public static String escapeOrderBySql(String value) {
        if (StringUtils.isNotEmpty(value) && !isValidOrderBySql(value)) {
            throw new BizException("鍙傛暟涓嶇鍚堣鑼冿紝涓嶈兘杩涜鏌ヨ");
        }
        if (StringUtils.length(value) > ORDER_BY_MAX_LENGTH) {
            throw new BizException("鍙傛暟宸茶秴杩囨渶澶ч檺鍒讹紝涓嶈兘杩涜鏌ヨ");
        }
        return value;
    }

    public static boolean isValidOrderBySql(String value) {
        return value.matches(SQL_PATTERN);
    }

    public static void filterKeyword(String value) {
        if (StringUtils.isEmpty(value)) {
            return;
        }
        String[] sqlKeywords = StringUtils.split(SQL_REGEX, "\\|");
        for (String sqlKeyword : sqlKeywords) {
            if (StringUtils.indexOfIgnoreCase(value, sqlKeyword) > -1) {
                throw new BizException("鍙傛暟瀛樺湪SQL娉ㄥ叆椋庨櫓");
            }
        }
    }
}

