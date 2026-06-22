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

    private List<ConsoleDashboardVo.TodoItem> buildTodoList() {
        List<ConsoleDashboardVo.TodoItem> todoList = new ArrayList<>();
        todoList.add(ConsoleDashboardVo.TodoItem.builder()
                .username("系统初始化完成").date(LocalDateTime.now().format(DATE_TIME_FORMATTER)).complete(true).build());
        todoList.add(ConsoleDashboardVo.TodoItem.builder()
                .username("欢迎使用 StarPivot 系统").date(LocalDateTime.now().format(DATE_TIME_FORMATTER)).complete(false).build());
        return todoList;
    }

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

    private long countLoginBetween(LocalDateTime start, LocalDateTime end) {
        return sysLogininforService.lambdaQuery()
                .ge(SysLogininfor::getLoginTime, start)
                .lt(SysLogininfor::getLoginTime, end)
                .count();
    }

    private long countOperBetween(LocalDateTime start, LocalDateTime end) {
        return sysOperLogService.lambdaQuery()
                .ge(SysOperLog::getOperTime, start)
                .lt(SysOperLog::getOperTime, end)
                .count();
    }

    private long countUserBetween(LocalDateTime start, LocalDateTime end) {
        return sysUserService.lambdaQuery()
                .eq(SysUser::getDelFlag, AppConstants.DelFlag.NORMAL)
                .ge(SysUser::getCreateTime, start)
                .lt(SysUser::getCreateTime, end)
                .count();
    }

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

    private ConsoleDashboardVo.CardItem buildCard(String des, String icon, Long num, String change) {
        ConsoleDashboardVo.CardItem cardItem = new ConsoleDashboardVo.CardItem();
        cardItem.setDes(des);
        cardItem.setIcon(icon);
        cardItem.setNum(num);
        cardItem.setChange(change);
        return cardItem;
    }

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

    private Integer parseSex(String sex) {
        if ("0".equals(sex)) {
            return 1;
        }
        return 0;
    }

    private String safe(String value) {
        return StringUtils.hasText(value) ? value : "-";
    }
}