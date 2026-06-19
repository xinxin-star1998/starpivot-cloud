package cn.org.starpivot.common.utils.date;

import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public final class DateUtils {

    public static final String YYYY = "yyyy";
    public static final String YYYY_MM = "yyyy-MM";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
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

    public static Date getNowDate() {
        return new Date();
    }

    public static String getDate() {
        return dateTimeNow(YYYY_MM_DD);
    }

    public static String getTime() {
        return dateTimeNow(YYYY_MM_DD_HH_MM_SS);
    }

    public static String dateTimeNow() {
        return dateTimeNow(YYYYMMDDHHMMSS);
    }

    public static String dateTimeNow(String format) {
        return parseDateToStr(format, new Date());
    }

    public static String dateTime(Date date) {
        return parseDateToStr(YYYY_MM_DD, date);
    }

    public static String parseDateToStr(String format, Date date) {
        DateTimeFormatter formatter = getFormatter(format);
        return formatter.format(date.toInstant().atZone(ZoneId.systemDefault()));
    }

    public static Date dateTime(String format, String ts) {
        DateTimeFormatter formatter = getFormatter(format);
        LocalDateTime localDateTime = LocalDateTime.parse(ts, formatter);
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static String datePath() {
        DateTimeFormatter formatter = getFormatter("yyyy/MM/dd");
        return formatter.format(LocalDate.now());
    }

    public static String dateTimeNumeric() {
        DateTimeFormatter formatter = getFormatter("yyyyMMdd");
        return formatter.format(LocalDate.now());
    }

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

    public static Date getServerStartDate() {
        return new Date(ManagementFactory.getRuntimeMXBean().getStartTime());
    }

    public static int differentDaysByMillisecond(Date date1, Date date2) {
        return Math.abs((int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24)));
    }

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

    public static Date toDate(LocalDateTime temporalAccessor) {
        return Date.from(temporalAccessor.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date toDate(LocalDate temporalAccessor) {
        return Date.from(temporalAccessor.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public static Instant toInstant(Date date) {
        return date.toInstant();
    }
}
