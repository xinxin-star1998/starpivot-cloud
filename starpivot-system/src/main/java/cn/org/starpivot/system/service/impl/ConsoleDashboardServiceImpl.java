package cn.org.starpivot.system.service.impl;

import cn.org.starpivot.common.cache.CacheConstants;
import cn.org.starpivot.common.entity.AppConstants;
import cn.org.starpivot.system.domain.entity.SysLogininfor;
import cn.org.starpivot.system.domain.entity.SysOperLog;
import cn.org.starpivot.system.domain.entity.SysUser;
import cn.org.starpivot.system.domain.vo.ConsoleDashboardVo;
import cn.org.starpivot.system.service.ConsoleDashboardService;
import cn.org.starpivot.system.service.SysLogininforService;
import cn.org.starpivot.system.service.SysOperLogService;
import cn.org.starpivot.system.service.SysUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 工作台仪表盘服务实现类。
 * <p>
 * 聚合用户数、登录趋势、操作日志等统计数据，结果经 Redis 短时缓存。
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConsoleDashboardServiceImpl implements ConsoleDashboardService {

    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("M月");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final String CACHE_KEY = CacheConstants.dashboardKey();
    private static final long CACHE_EXPIRE_MINUTES = CacheConstants.TTL_DASHBOARD.toMinutes();

    private final SysUserService sysUserService;
    private final SysLogininforService sysLogininforService;
    private final SysOperLogService sysOperLogService;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * {@inheritDoc}
     * <p>优先从 Redis 缓存读取，未命中时聚合各服务数据并缓存。</p>
     */
    @Override
    public ConsoleDashboardVo getConsoleData() {
        ConsoleDashboardVo cached = (ConsoleDashboardVo) redisTemplate.opsForValue().get(CACHE_KEY);
        if (cached != null) {
            return cached;
        }

        ConsoleDashboardVo vo = new ConsoleDashboardVo();
        vo.setCardList(buildCards());
        vo.setVisitTrend(buildVisitTrend());
        vo.setUserTrend(buildUserTrend());
        vo.setTodoList(buildTodoList());
        vo.setDynamicList(buildDynamicList());
        vo.setNewUserList(buildNewUserList());

        redisTemplate.opsForValue().set(CACHE_KEY, vo, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return vo;
    }

    /**
     * 构建工作台顶部四张统计卡片（访问、在线、操作、新用户）及环比变化。
     *
     * @return 卡片列表
     */
    private List<ConsoleDashboardVo.CardItem> buildCards() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();
        LocalDateTime weekStart = today.minusDays(6).atStartOfDay();
        LocalDateTime prevWeekStart = today.minusDays(13).atStartOfDay();
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime prevMonthStart = monthStart.minusMonths(1);

        long totalVisit = sysLogininforService.count();
        long todayVisit = countLoginBetween(todayStart, LocalDateTime.now());
        long yesterdayVisit = countLoginBetween(yesterdayStart, todayStart);

        long onlineLikeCount = countLoginBetween(LocalDateTime.now().minusMinutes(30), LocalDateTime.now());
        long todayOnlineLike = countLoginBetween(todayStart, LocalDateTime.now());
        long yesterdayOnlineLike = countLoginBetween(yesterdayStart, todayStart);

        long totalOperate = sysOperLogService.count();
        long weekOperate = countOperBetween(weekStart, LocalDateTime.now());
        long prevWeekOperate = countOperBetween(prevWeekStart, weekStart);

        long monthNewUser = countUserBetween(monthStart, LocalDateTime.now());
        long prevMonthNewUser = countUserBetween(prevMonthStart, monthStart);

        List<ConsoleDashboardVo.CardItem> cards = new ArrayList<>(4);
        cards.add(buildCard("总访问次数", "ri:pie-chart-line", totalVisit, calcChange(todayVisit, yesterdayVisit)));
        cards.add(buildCard("在线访客数", "ri:group-line", onlineLikeCount, calcChange(todayOnlineLike, yesterdayOnlineLike)));
        cards.add(buildCard("点击量", "ri:fire-line", totalOperate, calcChange(weekOperate, prevWeekOperate)));
        cards.add(buildCard("新用户", "ri:progress-2-line", monthNewUser, calcChange(monthNewUser, prevMonthNewUser)));
        return cards;
    }

    /**
     * 构建近 12 个月登录访问趋势图数据。
     *
     * @return 含 x 轴月份与对应访问量的趋势数据
     */
    private ConsoleDashboardVo.TrendData buildVisitTrend() {
        List<String> xAxis = new ArrayList<>(12);
        List<Long> data = new ArrayList<>(12);
        LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);

        LocalDateTime start = firstDayOfCurrentMonth.minusMonths(11).atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        List<Map<String, Object>> results = sysLogininforService.countByMonthRange(start, end);

        for (int i = 0; i < 12; i++) {
            LocalDate month = firstDayOfCurrentMonth.minusMonths(11 - i);
            xAxis.add(month.format(MONTH_FORMATTER));

            long count = 0L;
            if (results != null && !results.isEmpty()) {
                String monthKey = month.getYear() + "-" + String.format("%02d", month.getMonthValue());
                for (Map<String, Object> row : results) {
                    if (monthKey.equals(row.get("yearMonth"))) {
                        count = ((Number) row.get("count")).longValue();
                        break;
                    }
                }
            }
            data.add(count);
        }

        ConsoleDashboardVo.TrendData trendData = new ConsoleDashboardVo.TrendData();
        trendData.setXAxisData(xAxis);
        trendData.setData(data);
        return trendData;
    }

    /**
     * 构建近 12 个月新增用户趋势图数据。
     *
     * @return 含 x 轴月份与对应用户数的趋势数据
     */
    private ConsoleDashboardVo.TrendData buildUserTrend() {
        List<String> xAxis = new ArrayList<>(12);
        List<Long> data = new ArrayList<>(12);
        LocalDate firstDayOfCurrentMonth = LocalDate.now().withDayOfMonth(1);

        LocalDateTime start = firstDayOfCurrentMonth.minusMonths(11).atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        List<Map<String, Object>> results = sysUserService.countByMonthRange(start, end);

        for (int i = 0; i < 12; i++) {
            LocalDate month = firstDayOfCurrentMonth.minusMonths(11 - i);
            xAxis.add(month.format(MONTH_FORMATTER));

            long count = 0L;
            if (results != null && !results.isEmpty()) {
                String monthKey = month.getYear() + "-" + String.format("%02d", month.getMonthValue());
                for (Map<String, Object> row : results) {
                    if (monthKey.equals(row.get("yearMonth"))) {
                        count = ((Number) row.get("count")).longValue();
                        break;
                    }
                }
            }
            data.add(count);
        }

        ConsoleDashboardVo.TrendData trendData = new ConsoleDashboardVo.TrendData();
        trendData.setXAxisData(xAxis);
        trendData.setData(data);
        return trendData;
    }

    /**
     * 构建最新注册用户列表（最多 6 条），含本月登录活跃度百分比。
     *
     * @return 新用户展示项列表
     */
    private List<ConsoleDashboardVo.NewUserItem> buildNewUserList() {
        List<SysUser> users = sysUserService.lambdaQuery()
                .eq(SysUser::getDelFlag, AppConstants.DelFlag.NORMAL)
                .orderByDesc(SysUser::getCreateTime)
                .last("limit 6")
                .list();

        List<String> userNames = users.stream()
                .map(SysUser::getUserName)
                .filter(StringUtils::hasText)
                .toList();

        Map<String, Long> loginCountMap = new java.util.HashMap<>();
        if (!userNames.isEmpty()) {
            LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
            List<Map<String, Object>> loginCounts = sysLogininforService.countByUserNames(userNames, monthStart);
            for (Map<String, Object> row : loginCounts) {
                loginCountMap.put((String) row.get("userName"), ((Number) row.get("count")).longValue());
            }
        }

        List<ConsoleDashboardVo.NewUserItem> newUserList = new ArrayList<>(users.size());
        for (SysUser user : users) {
            ConsoleDashboardVo.NewUserItem item = new ConsoleDashboardVo.NewUserItem();
            item.setUsername(StringUtils.hasText(user.getNickName()) ? user.getNickName() : user.getUserName());
            item.setProvince("--");
            item.setSex(parseSex(user.getSex()));

            long loginCount = loginCountMap.getOrDefault(user.getUserName(), 0L);
            item.setPercentage((int) Math.min(loginCount * 10, 100));
            newUserList.add(item);
        }
        return newUserList;
    }

    /**
     * 构建工作台待办事项列表（当前为系统内置示例数据）。
     *
     * @return 待办项列表
     */
    private List<ConsoleDashboardVo.TodoItem> buildTodoList() {
        List<ConsoleDashboardVo.TodoItem> todoList = new ArrayList<>();
        todoList.add(ConsoleDashboardVo.TodoItem.builder()
                .username("系统初始化完成").date(LocalDateTime.now().format(DATE_TIME_FORMATTER)).complete(true).build());
        todoList.add(ConsoleDashboardVo.TodoItem.builder()
                .username("欢迎使用 StarPivot 系统").date(LocalDateTime.now().format(DATE_TIME_FORMATTER)).complete(false).build());
        return todoList;
    }

    /**
     * 构建最近操作动态列表（最多 8 条操作日志）。
     *
     * @return 动态展示项列表
     */
    private List<ConsoleDashboardVo.DynamicItem> buildDynamicList() {
        List<SysOperLog> operLogs = sysOperLogService.lambdaQuery()
                .orderByDesc(SysOperLog::getOperTime)
                .last("limit 8")
                .list();

        List<ConsoleDashboardVo.DynamicItem> dynamicList = new ArrayList<>(operLogs.size());
        for (SysOperLog log : operLogs) {
            ConsoleDashboardVo.DynamicItem item = new ConsoleDashboardVo.DynamicItem();
            item.setUsername(StringUtils.hasText(log.getOperName()) ? log.getOperName() : "系统");
            item.setType(resolveBusinessType(log.getBusinessType()));
            item.setTarget(StringUtils.hasText(log.getTitle()) ? log.getTitle() : safe(log.getOperUrl()));
            dynamicList.add(item);
        }
        return dynamicList;
    }

    /**
     * 统计指定时间区间内的登录次数。
     *
     * @param start 起始时间（含）
     * @param end   结束时间（不含）
     * @return 登录记录数
     */
    private long countLoginBetween(LocalDateTime start, LocalDateTime end) {
        return sysLogininforService.lambdaQuery()
                .ge(SysLogininfor::getLoginTime, start)
                .lt(SysLogininfor::getLoginTime, end)
                .count();
    }

    /**
     * 统计指定时间区间内的操作日志条数。
     *
     * @param start 起始时间（含）
     * @param end   结束时间（不含）
     * @return 操作日志数
     */
    private long countOperBetween(LocalDateTime start, LocalDateTime end) {
        return sysOperLogService.lambdaQuery()
                .ge(SysOperLog::getOperTime, start)
                .lt(SysOperLog::getOperTime, end)
                .count();
    }

    /**
     * 统计指定时间区间内的新增用户数（未删除）。
     *
     * @param start 起始时间（含）
     * @param end   结束时间（不含）
     * @return 新增用户数
     */
    private long countUserBetween(LocalDateTime start, LocalDateTime end) {
        return sysUserService.lambdaQuery()
                .eq(SysUser::getDelFlag, AppConstants.DelFlag.NORMAL)
                .ge(SysUser::getCreateTime, start)
                .lt(SysUser::getCreateTime, end)
                .count();
    }

    /**
     * 计算环比变化百分比字符串。
     *
     * @param current  当前周期数值
     * @param previous 对比周期数值
     * @return 带正负号的百分比字符串（如 {@code +10%}）
     */
    private String calcChange(long current, long previous) {
        if (previous <= 0) {
            return current > 0 ? "+100%" : "+0%";
        }
        BigDecimal ratio = BigDecimal.valueOf(current - previous)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(previous), 0, RoundingMode.HALF_UP);
        String sign = ratio.signum() >= 0 ? "+" : "";
        return sign + ratio.toPlainString() + "%";
    }

    /**
     * 组装单张统计卡片。
     *
     * @param des    卡片描述
     * @param icon   图标标识
     * @param num    统计数值
     * @param change 环比变化文本
     * @return 卡片项
     */
    private ConsoleDashboardVo.CardItem buildCard(String des, String icon, Long num, String change) {
        ConsoleDashboardVo.CardItem cardItem = new ConsoleDashboardVo.CardItem();
        cardItem.setDes(des);
        cardItem.setIcon(icon);
        cardItem.setNum(num);
        cardItem.setChange(change);
        return cardItem;
    }

    /**
     * 将操作日志业务类型编码转换为展示用动词。
     *
     * @param businessType 业务类型编码
     * @return 中文动词（如「新增了」），未知时返回「执行了」
     */
    private String resolveBusinessType(Integer businessType) {
        if (businessType == null) {
            return "执行了";
        }
        return switch (businessType) {
            case 1 -> "新增了";
            case 2 -> "修改了";
            case 3 -> "删除了";
            case 4 -> "授权了";
            case 5 -> "导出了";
            case 6 -> "导入了";
            case 7 -> "强退了";
            case 8 -> "生成了代码";
            case 9 -> "清空了数据";
            default -> "执行了";
        };
    }

    /**
     * 将系统性别编码转换为前端展示用数值（{@code 0} 男，{@code 1} 女）。
     *
     * @param sex 性别编码（{@code 0} 男，其他视为女）
     * @return 前端展示用性别值
     */
    private Integer parseSex(String sex) {
        if ("0".equals(sex)) {
            return 1;
        }
        return 0;
    }

    /**
     * 空值安全处理，空白字符串返回占位符。
     *
     * @param value 原始字符串
     * @return 非空原值，否则返回 {@code "-"}
     */
    private String safe(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }
}