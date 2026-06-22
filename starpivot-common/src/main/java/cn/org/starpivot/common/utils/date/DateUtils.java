package cn.org.starpivot.common.utils.date;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期时间工具类，基于 {@link java.time} API 封装常用格式化、解析与换算。
 * <p>
 * 默认使用系统默认时区；与 {@link cn.org.starpivot.common.entity.BaseEntity} 中
 * {@code yyyy-MM-dd HH:mm:ss} 格式保持一致。
 * </p>
 */
public final class DateUtils {

    /** 年份格式：{@code yyyy} */
    public static final String YYYY = "yyyy";
    /** 年月格式：{@code yyyy-MM} */
    public static final String YYYY_MM = "yyyy-MM";
    /** 日期格式：{@code yyyy-MM-dd} */
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    /** 紧凑日期时间：{@code yyyyMMddHHmmss} */
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    /** 标准日期时间：{@code yyyy-MM-dd HH:mm:ss} */
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    private static final String[] PARSE_PATTERNS = {
            "yyyy-MM-dd", "yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM",
            "yyyy/MM/dd", "yyyy/MM/dd HH:mm:ss", "yyyy/MM/dd HH:mm", "yyyy/MM",
            "yyyy.MM.dd", "yyyy.MM.dd HH:mm:ss", "yyyy.MM.dd HH:mm", "yyyy.MM"};

    private DateUtils() {
    }

    private static DateTimeFormatter getFormatter(String pattern) {
        return DateTimeFormatter.ofPattern(pattern);
    }

    /** @return 当前时间的 {@link Date} 实例 */
    public static Date getNowDate() {
        return new Date();
    }

    /** @return 当前日期字符串（{@link #YYYY_MM_DD}） */
    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    /** @return 当前日期时间字符串（{@link #YYYY_MM_DD_HH_MM_SS}） */
    public static String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    /** @return 当前紧凑日期时间（{@link #YYYYMMDDHHMMSS}） */
    public static String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    /**
     * 按指定格式输出当前时间。
     *
     * @param format 日期格式模式
     * @return 格式化后的字符串
     */
    public static String dateTimeNow(String format) {
        return parseDateToStr(format, new Date());
    }

    /**
     * 将 {@link Date} 格式化为 {@link #YYYY_MM_DD} 字符串。
     *
     * @param date 待格式化日期
     * @return 日期字符串
     */
    public static String dateTime(Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    /**
     * 将 {@link Date} 按指定模式格式化为字符串。
     *
     * @param format 日期格式模式
     * @param date   待格式化日期
     * @return 格式化后的字符串
     */
    public static String parseDateToStr(String format, Date date) {
        DateTimeFormatter formatter = getFormatter(format);
        return formatter.format(date.toInstant().atZone(ZoneId.systemDefault()));
    }

    /**
     * 将字符串按指定模式解析为 {@link Date}。
     *
     * @param format 日期格式模式
     * @param ts     日期时间字符串
     * @return 解析结果
     */
    public static Date dateTime(String format, String ts) {
        DateTimeFormatter formatter = getFormatter(format);
        LocalDateTime localDateTime = LocalDateTime.parse(ts, formatter);
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /** @return 当前日期路径（{@code yyyy/MM/dd}），常用于文件目录划分 */
    public static String datePath() {
        DateTimeFormatter formatter = getFormatter("yyyy/MM/dd");
        return formatter.format(LocalDate.now());
    }

    /** @return 当前数字日期（{@code yyyyMMdd}） */
    public static String dateTimeNumeric() {
        DateTimeFormatter formatter = getFormatter("yyyyMMdd");
        return formatter.format(LocalDate.now());
    }

    /**
     * 按内置 {@link #PARSE_PATTERNS} 依次尝试解析日期字符串。
     *
     * @param str 日期对象或字符串，{@code null} 时返回 {@code null}
     * @return 解析成功返回 {@link Date}，均不匹配时返回 {@code null}
     */
    public static Date parseDate(Object str) {
        if (str == null) {
            return null;
        }
        String dateStr = str.toString();
        for (String pattern : PARSE_PATTERNS) {
            try {
                DateTimeFormatter formatter = getFormatter(pattern);
                if (pattern.contains("HH") || pattern.contains("mm") || pattern.contains("ss")) {
                    LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
                    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
                } else {
                    LocalDate localDate = LocalDate.parse(dateStr, formatter);
                    return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /** @return JVM 启动时刻对应的 {@link Date} */
    public static Date getServerStartDate() {
        return new Date(ManagementFactory.getRuntimeMXBean().getStartTime());
    }

    /**
     * 计算两个日期间相差的天数（绝对值）。
     *
     * @param date1 日期一
     * @param date2 日期二
     * @return 相差天数
     */
    public static int differentDaysByMillisecond(Date date1, Date date2) {
        return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
    }

    /**
     * 计算两时刻之间的时间差，格式化为「X天X小时X分钟」。
     *
     * @param endDate   结束时间
     * @param startTime 开始时间
     * @return 可读时间差描述
     */
    public static String timeDistance(Date endDate, Date startTime) {
        long nd = 1000 * 24 * 60 * 60L;
        long nh = 1000 * 60 * 60L;
        long nm = 1000 * 60L;
        long diff = endDate.getTime() - startTime.getTime();
        long day = diff / nd;
        long hour = diff % nd / nh;
        long min = diff % nd % nh / nm;
        return day + "天" + hour + "小时" + min + "分钟";
    }

    /** @param temporalAccessor {@link LocalDateTime} 实例 */
    public static Date toDate(LocalDateTime temporalAccessor) {
        return Date.from(temporalAccessor.atZone(ZoneId.systemDefault()).toInstant());
    }

    /** @param temporalAccessor {@link LocalDate} 实例（当天 00:00:00） */
    public static Date toDate(LocalDate temporalAccessor) {
        return Date.from(temporalAccessor.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /** @param date {@link Date} 实例 */
    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /** @param date {@link Date} 实例 */
    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** @param date {@link Date} 实例 */
    public static Instant toInstant(Date date) {
        return date.toInstant();
    }
}
