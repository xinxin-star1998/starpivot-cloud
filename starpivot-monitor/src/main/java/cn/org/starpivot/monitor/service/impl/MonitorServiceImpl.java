package cn.org.starpivot.monitor.service.impl;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.stat.DruidStatManagerFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import cn.org.starpivot.common.cache.CacheConstants;
import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.monitor.domain.vo.DruidMonitorVO;

import cn.org.starpivot.monitor.domain.vo.OnlineUserVO;
import cn.org.starpivot.monitor.domain.vo.RedisCacheVO;
import cn.org.starpivot.monitor.domain.vo.ServerInfoVO;
import cn.org.starpivot.monitor.service.MonitorService;
import cn.org.starpivot.monitor.domain.entity.MonitorUser;
import cn.org.starpivot.monitor.service.MonitorDeptQueryService;
import cn.org.starpivot.monitor.service.MonitorUserQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.DataType;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static cn.org.starpivot.common.util.LogUtils.truncateString;

/**
 * 监控服务实现类
 *
 * @author xinxin
 * @since 2026-01-25
 */
@Slf4j
@Service
public class MonitorServiceImpl implements MonitorService {

    @Autowired(required = false)
    private DataSource dataSource;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private MonitorUserQueryService monitorUserQueryService;

    @Autowired(required = false)
    private MonitorDeptQueryService monitorDeptQueryService;

    @Autowired
    private CloudOnlineUserService cloudOnlineUserService;

    private static final SystemInfo SYSTEM_INFO = new SystemInfo();
    private static final HardwareAbstractionLayer HAL = SYSTEM_INFO.getHardware();
    private static final OperatingSystem OS = SYSTEM_INFO.getOperatingSystem();

    // 常量定义
    private static final int BYTES_TO_KB = 1024;
    private static final int KB_TO_MB = 1024;
    private static final int MB_TO_GB = 1024;
    private static final double PERCENTAGE_BASE = 100.0;
    private static final int SCAN_COUNT = 100; // SCAN 每次扫描的 key 数量
    private static final int CPU_CACHE_SECONDS = 2; // CPU 信息缓存时间（秒）
    private static final Set<String> VIRTUAL_FS_TYPES = Set.of(
            "tmpfs" , "devtmpfs" , "overlay" , "squashfs" , "proc" , "sysfs" , "cgroup" , "cgroup2" ,
            "nsfs" , "tracefs" , "debugfs" , "securityfs" , "pstore" , "mqueue" , "hugetlbfs" ,
            "rpc_pipefs" , "configfs" , "fusectl" , "autofs"
    );

    // CPU 信息缓存
    private static final Map<String, Object> cpuInfoCache = new ConcurrentHashMap<>();
    private static final String CACHE_KEY_CPU_INFO = "cpu_info";
    private static final String CACHE_KEY_CPU_TIMESTAMP = "cpu_timestamp";
    private final Object cpuTicksLock = new Object();
    private volatile long[] previousCpuTicks = HAL.getProcessor().getSystemCpuLoadTicks();

    /** 缓存内容预览最大长度（字符数，UTF‑8 截断前） */
    private static final int CACHE_CONTENT_MAX_LENGTH = 5000;

    /** 缓存查看专用 ObjectMapper（懒加载，避免频繁 new） */
    private volatile ObjectMapper cacheViewObjectMapper;

    /** Redis 能力缓存（避免每次请求都探测） */
    private volatile RedisCapabilities redisCapabilities;
    private static final long REDIS_CAPABILITY_CACHE_MS = 60_000L;
    private final AtomicBoolean clusterScanWarningLogged = new AtomicBoolean(false);

    @Override
    public ServerInfoVO getServerInfo() {
        ServerInfoVO serverInfo = new ServerInfoVO();

        try {
            // CPU 信息
            ServerInfoVO.CpuInfo cpuInfo = getCpuInfo();
            serverInfo.setCpu(cpuInfo);

            // 内存信息
            ServerInfoVO.MemoryInfo memoryInfo = getMemoryInfo();
            serverInfo.setMemory(memoryInfo);

            // JVM 信息
            ServerInfoVO.JvmInfo jvmInfo = getJvmInfo();
            serverInfo.setJvm(jvmInfo);

            // 系统信息
            ServerInfoVO.SystemInfo systemInfo = getSystemInfo();
            serverInfo.setSystem(systemInfo);

            // 磁盘信息
            ServerInfoVO.DiskInfo diskInfo = getDiskInfo();
            serverInfo.setDisk(diskInfo);
        } catch (Exception e) {
            log.error("获取服务器信息失败", e);
            throw new BizException(ErrorCode.SYSTEM_ERROR, "获取服务器信息失败: " + e.getMessage());
        }

        return serverInfo;
    }

    /**
     * 获取 CPU 信息
     * 使用缓存机制避免频繁阻塞线程
     */
    private ServerInfoVO.CpuInfo getCpuInfo() {
        // 检查缓存
        Long cacheTimestamp = (Long) cpuInfoCache.get(CACHE_KEY_CPU_TIMESTAMP);
        if (cacheTimestamp != null && System.currentTimeMillis() - cacheTimestamp < CPU_CACHE_SECONDS * 1000) {
            ServerInfoVO.CpuInfo cachedInfo = (ServerInfoVO.CpuInfo) cpuInfoCache.get(CACHE_KEY_CPU_INFO);
            if (cachedInfo != null) {
                return cachedInfo;
            }
        }

        // 缓存失效或不存在，重新计算（无阻塞采样，避免线程 sleep）
        CentralProcessor processor = HAL.getProcessor();
        long[] prevTicks;
        long[] ticks;
        synchronized (cpuTicksLock) {
            prevTicks = previousCpuTicks;
            ticks = processor.getSystemCpuLoadTicks();
            previousCpuTicks = ticks;
        }

        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long cSys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long totalCpu = user + nice + cSys + idle + iowait + irq + softirq + steal;
        if (totalCpu <= 0) {
            totalCpu = 1;
        }

        ServerInfoVO.CpuInfo cpuInfo = new ServerInfoVO.CpuInfo();
        cpuInfo.setCpuNum(processor.getLogicalProcessorCount());
        cpuInfo.setTotal(PERCENTAGE_BASE);
        cpuInfo.setSys(PERCENTAGE_BASE * cSys / totalCpu);
        cpuInfo.setUsed(PERCENTAGE_BASE * user / totalCpu);
        cpuInfo.setWait(PERCENTAGE_BASE * iowait / totalCpu);
        cpuInfo.setFree(PERCENTAGE_BASE * idle / totalCpu);

        // 更新缓存
        cpuInfoCache.put(CACHE_KEY_CPU_INFO, cpuInfo);
        cpuInfoCache.put(CACHE_KEY_CPU_TIMESTAMP, System.currentTimeMillis());

        return cpuInfo;
    }

    /**
     * 获取内存信息
     */
    private ServerInfoVO.MemoryInfo getMemoryInfo() {
        GlobalMemory memory = HAL.getMemory();
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;

        ServerInfoVO.MemoryInfo memoryInfo = new ServerInfoVO.MemoryInfo();
        memoryInfo.setTotal(total / BYTES_TO_KB / KB_TO_MB); // 转换为 MB
        memoryInfo.setUsed(used / BYTES_TO_KB / KB_TO_MB);
        memoryInfo.setFree(available / BYTES_TO_KB / KB_TO_MB);
        memoryInfo.setUsage(PERCENTAGE_BASE * used / total);

        return memoryInfo;
    }

    /**
     * 获取 JVM 信息
     */
    private ServerInfoVO.JvmInfo getJvmInfo() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

        long totalMemory = memoryMXBean.getHeapMemoryUsage().getMax();
        long usedMemory = memoryMXBean.getHeapMemoryUsage().getUsed();
        if (totalMemory <= 0) {
            totalMemory = memoryMXBean.getHeapMemoryUsage().getCommitted();
        }
        if (totalMemory <= 0) {
            totalMemory = 1;
        }
        long freeMemory = totalMemory - usedMemory;

        ServerInfoVO.JvmInfo jvmInfo = new ServerInfoVO.JvmInfo();
        jvmInfo.setName(runtimeMXBean.getVmName());
        jvmInfo.setVersion(runtimeMXBean.getVmVersion());
        jvmInfo.setStartTime(runtimeMXBean.getStartTime());
        jvmInfo.setRunTime(runtimeMXBean.getUptime());
        jvmInfo.setHome(System.getProperty("java.home" , "" ));
        jvmInfo.setUserDir(System.getProperty("user.dir" , "" ));
        jvmInfo.setInputArgs(String.join(" " , runtimeMXBean.getInputArguments()));
        jvmInfo.setMax(totalMemory / BYTES_TO_KB / KB_TO_MB); // 转换为 MB
        jvmInfo.setTotal(memoryMXBean.getHeapMemoryUsage().getCommitted() / BYTES_TO_KB / KB_TO_MB);
        jvmInfo.setUsed(usedMemory / BYTES_TO_KB / KB_TO_MB);
        jvmInfo.setFree(freeMemory / BYTES_TO_KB / KB_TO_MB);
        jvmInfo.setUsage(PERCENTAGE_BASE * usedMemory / totalMemory);

        return jvmInfo;
    }

    /**
     * 获取系统信息
     */
    private ServerInfoVO.SystemInfo getSystemInfo() {
        try {
            ServerInfoVO.SystemInfo systemInfo = new ServerInfoVO.SystemInfo();
            systemInfo.setComputerName(InetAddress.getLocalHost().getHostName());
            systemInfo.setOsName(System.getProperty("os.name"));
            systemInfo.setOsArch(System.getProperty("os.arch"));
            systemInfo.setComputerIp(InetAddress.getLocalHost().getHostAddress());
            return systemInfo;
        } catch (Exception e) {
            log.error("获取系统信息失败", e);
            ServerInfoVO.SystemInfo systemInfo = new ServerInfoVO.SystemInfo();
            systemInfo.setComputerName("未知");
            systemInfo.setOsName(System.getProperty("os.name", "未知"));
            systemInfo.setOsArch(System.getProperty("os.arch", "未知"));
            systemInfo.setComputerIp("未知");
            return systemInfo;
        }
    }

    /**
     * 获取磁盘信息
     */
    private ServerInfoVO.DiskInfo getDiskInfo() {
        FileSystem fileSystem = OS.getFileSystem();
        // Linux 下 getFileStores() 可能只返回较少挂载点，优先获取完整列表
        List<OSFileStore> fileStores = fileSystem.getFileStores(false);
        if (fileStores == null || fileStores.isEmpty()) {
            fileStores = fileSystem.getFileStores();
        }

        long total = 0;
        long free = 0;
        List<ServerInfoVO.DiskStoreInfo> stores = new ArrayList<>();
        Set<String> seenMounts = new HashSet<>();

        for (OSFileStore store : fileStores) {
            String mount = store.getMount();
            if (mount == null || mount.isBlank()) {
                continue;
            }
            if (!isKeyPartition(store, mount)) {
                continue;
            }
            // 去重：同一挂载点只保留一条，避免重复显示
            if (!seenMounts.add(mount)) {
                continue;
            }

            long storeTotal = store.getTotalSpace();
            // 跳过无容量或异常挂载点
            if (storeTotal <= 0) {
                continue;
            }
            long storeUsable = store.getUsableSpace();
            long storeUsed = Math.max(storeTotal - storeUsable, 0L);

            total += storeTotal;
            free += storeUsable;

            ServerInfoVO.DiskStoreInfo storeInfo = new ServerInfoVO.DiskStoreInfo();
            storeInfo.setMount(mount);
            storeInfo.setFileSystem(store.getType());
            storeInfo.setTypeName(store.getName());
            storeInfo.setTotalGb(toGb(storeTotal));
            storeInfo.setUsableGb(toGb(storeUsable));
            storeInfo.setUsedGb(toGb(storeUsed));
            storeInfo.setUsage(storeTotal > 0 ? round2(PERCENTAGE_BASE * storeUsed / storeTotal) : 0.0);
            stores.add(storeInfo);
        }

        stores.sort(Comparator.comparing(ServerInfoVO.DiskStoreInfo::getMount));

        long used = total - free;

        ServerInfoVO.DiskInfo diskInfo = new ServerInfoVO.DiskInfo();
        diskInfo.setTotal(total / BYTES_TO_KB / KB_TO_MB / MB_TO_GB); // 转换为 GB
        diskInfo.setUsed(used / BYTES_TO_KB / KB_TO_MB / MB_TO_GB);
        diskInfo.setFree(free / BYTES_TO_KB / KB_TO_MB / MB_TO_GB);
        diskInfo.setUsage(total > 0 ? PERCENTAGE_BASE * used / total : 0.0);
        diskInfo.setStores(stores);

        return diskInfo;
    }

    private boolean isKeyPartition(OSFileStore store, String mount) {
        String fsType = Optional.ofNullable(store.getType()).orElse("" ).toLowerCase(Locale.ROOT);
        String name = Optional.ofNullable(store.getName()).orElse("" ).toLowerCase(Locale.ROOT);
        String lowerMount = mount.toLowerCase(Locale.ROOT);

        // Windows 盘符（如 C:\、D:\）直接保留，避免本地环境无磁盘显示
        if (mount.matches("^[a-zA-Z]:\\\\.*" )) {
            return true;
        }
        // 其他非 Unix 风格挂载（如网络卷）默认保留
        if (!mount.startsWith("/" )) {
            return true;
        }

        // 过滤虚拟文件系统和临时挂载
        if (VIRTUAL_FS_TYPES.contains(fsType)) {
            return false;
        }
        if (lowerMount.startsWith("/proc" )
                || lowerMount.startsWith("/sys" )
                || lowerMount.startsWith("/run" )
                || lowerMount.startsWith("/dev" )
                || lowerMount.startsWith("/snap" )) {
            return false;
        }

        // 保留核心业务分区和常见 Linux 关键目录
        return "/".equals(mount)
                || mount.startsWith("/boot" )
                || mount.startsWith("/home" )
                || mount.startsWith("/var" )
                || mount.startsWith("/opt" )
                || mount.startsWith("/srv" )
                || mount.startsWith("/usr" )
                || mount.startsWith("/data" )
                || mount.startsWith("/mnt" )
                || mount.startsWith("/disk" )
                || mount.startsWith("/volume" )
                || name.startsWith("/dev/" );
    }

    private Double toGb(long bytes) {
        double gb = bytes / 1024.0 / 1024.0 / 1024.0;
        return round2(gb);
    }

    private Double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    @Override
    public DruidMonitorVO getDruidMonitorInfo() {
        return getDruidMonitorInfo(false, null);
    }

    @Override
    public DruidMonitorVO getDruidMonitorInfoWithSlowSql(Long slowSqlThreshold) {
        return getDruidMonitorInfo(true, slowSqlThreshold);
    }

    /**
     * 获取 Druid 监控信息（支持包含慢SQL列表）
     *
     * @param includeSlowSqlList 是否包含慢SQL列表
     * @param slowSqlThreshold 慢SQL阈值（毫秒），当 includeSlowSqlList 为 true 时有效，null 则使用默认值 5000
     * @return Druid 监控信息
     */
    public DruidMonitorVO getDruidMonitorInfo(boolean includeSlowSqlList, Long slowSqlThreshold) {
        if (dataSource == null || !(dataSource instanceof DruidDataSource)) {
            DruidMonitorVO vo = new DruidMonitorVO();
            vo.setAvailable(false);
            vo.setMessage("数据源不是 Druid 数据源或数据源未配置");
            return vo;
        }

        try {
            DruidDataSource druidDataSource = (DruidDataSource) dataSource;
            DruidMonitorVO monitorVO = new DruidMonitorVO();
            monitorVO.setName(druidDataSource.getName());
            monitorVO.setDbType(druidDataSource.getDbType());
            monitorVO.setDriverClassName(druidDataSource.getDriverClassName());

            // 连接池信息
            DruidMonitorVO.ConnectionPoolInfo poolInfo = new DruidMonitorVO.ConnectionPoolInfo();
            poolInfo.setInitialSize(druidDataSource.getInitialSize());
            poolInfo.setMinIdle(druidDataSource.getMinIdle());
            poolInfo.setMaxActive(druidDataSource.getMaxActive());
            poolInfo.setActiveCount(druidDataSource.getActiveCount());
            poolInfo.setActivePeak(druidDataSource.getActivePeak());
            if (druidDataSource.getMaxActive() > 0) {
                poolInfo.setUsage(PERCENTAGE_BASE * druidDataSource.getActiveCount() / druidDataSource.getMaxActive());
            } else {
                poolInfo.setUsage(0.0);
            }
            monitorVO.setConnectionPool(poolInfo);

            // SQL 统计信息
            DruidStatManagerFacade statManagerFacade = DruidStatManagerFacade.getInstance();
            // 获取 SQL 统计列表，传入 null 表示获取所有 SQL 统计
            List<Map<String, Object>> sqlList = statManagerFacade.getSqlStatDataList(null);

            DruidMonitorVO.SqlStatInfo sqlStatInfo = new DruidMonitorVO.SqlStatInfo();
            long executeCount = 0;
            long executeMillisTotal = 0;
            long slowSqlCount = 0;
            long errorSqlCount = 0;

            // 慢SQL列表（如果需要）
            List<DruidMonitorVO.SlowSqlInfo> slowSqlList = null;
            if (includeSlowSqlList) {
                slowSqlList = new ArrayList<>();
                // 默认慢SQL阈值为 5000 毫秒
                long threshold = (slowSqlThreshold != null && slowSqlThreshold > 0) ? slowSqlThreshold : 5000L;

                if (sqlList != null) {
                    for (Map<String, Object> sql : sqlList) {
                        try {
                            String sqlId = getStringValue(sql, "ID");
                            String sqlText = getStringValue(sql, "SQL");
                            Long count = getLongValue(sql, "ExecuteCount");
                            Long millis = getLongValue(sql, "ExecuteMillisTotal");
                            Long maxMillis = getLongValue(sql, "ExecuteMillisMax");
                            Long slowCount = getLongValue(sql, "SlowCount");
                            Long errorCount = getLongValue(sql, "ErrorCount");
                            Long lastExecuteTime = getLongValue(sql, "LastExecuteTime");

                            // 累计统计信息
                            if (count != null) executeCount += count;
                            if (millis != null) executeMillisTotal += millis;
                            if (slowCount != null) slowSqlCount += slowCount;
                            if (errorCount != null) errorSqlCount += errorCount;

                            // 判断是否为慢SQL并添加到列表
                            if (count != null && count > 0 && millis != null) {
                                double avgTime = (double) millis / count;
                                if (avgTime >= threshold || (slowCount != null && slowCount > 0)) {
                                    DruidMonitorVO.SlowSqlInfo slowSqlInfo = new DruidMonitorVO.SlowSqlInfo();
                                    slowSqlInfo.setSqlId(sqlId);
                                    // SQL文本截断到5000字符，避免过长
                                    slowSqlInfo.setSqlText(sqlText != null ? truncateString(sqlText, 5000) : "");
                                    slowSqlInfo.setExecuteCount(count);
                                    slowSqlInfo.setExecuteTimeTotal(millis);
                                    slowSqlInfo.setExecuteTimeMax(maxMillis);
                                    slowSqlInfo.setExecuteTimeAvg(avgTime);
                                    slowSqlInfo.setSlowCount(slowCount != null ? slowCount : 0L);
                                    slowSqlInfo.setErrorCount(errorCount != null ? errorCount : 0L);
                                    slowSqlInfo.setLastExecuteTime(lastExecuteTime);
                                    slowSqlList.add(slowSqlInfo);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("处理慢SQL信息失败", e);
                        }
                    }
                }
            } else {
                // 不包含慢SQL列表时，只统计总数
                if (sqlList != null) {
                    for (Map<String, Object> sql : sqlList) {
                        Long count = getLongValue(sql, "ExecuteCount");
                        Long millis = getLongValue(sql, "ExecuteMillisTotal");
                        Long slowCount = getLongValue(sql, "SlowCount");
                        Long errorCount = getLongValue(sql, "ErrorCount");

                        if (count != null) executeCount += count;
                        if (millis != null) executeMillisTotal += millis;
                        if (slowCount != null) slowSqlCount += slowCount;
                        if (errorCount != null) errorSqlCount += errorCount;
                    }
                }
            }

            sqlStatInfo.setExecuteCount(executeCount);
            sqlStatInfo.setExecuteMillisTotal(executeMillisTotal);
            sqlStatInfo.setExecuteMillisAvg(executeCount > 0 ? (double) executeMillisTotal / executeCount : 0.0);
            sqlStatInfo.setSlowSqlCount(slowSqlCount);
            sqlStatInfo.setErrorSqlCount(errorSqlCount);
            monitorVO.setSqlStat(sqlStatInfo);

            // 设置慢SQL列表
            monitorVO.setSlowSqlList(slowSqlList);

            monitorVO.setAvailable(true);
            return monitorVO;
        } catch (Exception e) {
            log.error("获取 Druid 监控信息失败", e);
            DruidMonitorVO vo = new DruidMonitorVO();
            vo.setAvailable(false);
            vo.setMessage("获取 Druid 监控信息失败: " + e.getMessage());
            return vo;
        }
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 获取字符串值
     */
    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 使用 SCAN 命令扫描 Redis key，避免阻塞 Redis
     *
     * @param pattern key 匹配模式
     * @return 匹配的 key 集合
     */
    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        if (redisTemplate == null) {
            return keys;
        }

        RedisCapabilities capabilities = getRedisCapabilities();
        if (capabilities.clusterMode && clusterScanWarningLogged.compareAndSet(false, true)) {
            log.warn("Redis 当前为集群模式，SCAN 结果可能为局部视图，pattern: {}", pattern);
        }

        try {
            ScanOptions options = ScanOptions.scanOptions()
                    .match(pattern)
                    .count(SCAN_COUNT)
                    .build();

            try (Cursor<String> cursor = redisTemplate.scan(options)) {
                while (cursor.hasNext()) {
                    keys.add(cursor.next());
                }
            }
        } catch (Exception e) {
            log.error("使用 SCAN 命令扫描 key 失败，pattern: {}", pattern, e);
        }

        return keys;
    }

    @Override
    public List<OnlineUserVO> getOnlineUserList(String userName, String ipaddr) {
        return cloudOnlineUserService.getOnlineUserList(userName, ipaddr);
    }

    @Override
    public boolean forceLogout(String sessionId) {
        return cloudOnlineUserService.forceLogout(sessionId);
    }





    @Override
    public List<RedisCacheVO> getCacheList() {
        if (redisTemplate == null) {
            throw new BizException(ErrorCode.REDIS_ERROR, "Redis 未配置或未启用");
        }

        try {
            Map<String, RedisCacheVO> resultMap = new LinkedHashMap<>();
            for (String groupName : CacheConstants.predefinedGroups()) {
                RedisCacheVO vo = new RedisCacheVO();
                vo.setCacheName(groupName);
                vo.setRemark(CacheConstants.getRemark(groupName));
                resultMap.put(groupName, vo);
            }

            Set<String> allKeys = scanKeys("*");
            for (String key : allKeys) {
                String groupName = extractCacheGroupName(key);
                if (groupName == null || groupName.isEmpty()) {
                    continue;
                }
                resultMap.computeIfAbsent(groupName, name -> {
                    RedisCacheVO vo = new RedisCacheVO();
                    vo.setCacheName(name);
                    vo.setRemark(CacheConstants.getRemark(name));
                    return vo;
                });
            }

            return new ArrayList<>(resultMap.values());
        } catch (Exception e) {
            log.error("获取缓存列表失败", e);
            throw new BizException(ErrorCode.REDIS_ERROR, "获取缓存列表失败: " + e.getMessage());
        }
    }

    /**
     * 将 Redis 键归为「缓存列表」中的一行（与 getCacheKeys、deleteCache 使用同一分组名）。
     * <p>Spring Data Redis 使用双冒号分隔缓存名与业务键，实际键形如 {@code sys_dict::status}。
     * 若仅按首个单冒号分组，会把 {@code login_tokens:refresh:1} 正确归为 {@code login_tokens}。</p>
     */
    private String extractCacheGroupName(String key) {
        if (key == null || key.isEmpty()) {
            return null;
        }
        int doubleColon = key.indexOf("::");
        if (doubleColon > 0) {
            return key.substring(0, doubleColon);
        }
        int colonIndex = key.indexOf(':');
        if (colonIndex > 0) {
            return key.substring(0, colonIndex);
        }
        return key;
    }

    @Override
    public List<RedisCacheVO.CacheKeyInfo> getCacheKeys(String cacheName) {
        if (redisTemplate == null) {
            throw new BizException(ErrorCode.REDIS_ERROR, "Redis 未配置或未启用");
        }

        if (cacheName == null || cacheName.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "缓存名称不能为空");
        }

        try {
            // 使用缓存名称作为前缀，扫描所有匹配的键
            // 例如：cacheName = "jwt"，则扫描 "jwt:*"
            String keyPattern = cacheName + ":*";
            Set<String> keys = scanKeys(keyPattern);
            // 无前缀冒号时，整键即为一组，需包含与分组名完全相同的键
            if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheName))) {
                keys.add(cacheName);
            }
            List<RedisCacheVO.CacheKeyInfo> keyInfoList = new ArrayList<>();

            for (String key : keys) {
                RedisCacheVO.CacheKeyInfo keyInfo = new RedisCacheVO.CacheKeyInfo();
                keyInfo.setKey(key);

                // 获取键类型
                String type = redisTemplate.execute((RedisCallback<String>) connection -> {
                    DataType dataType = connection.keyCommands().type(key.getBytes());
                    return dataType != null ? dataType.code() : "unknown";
                });
                keyInfo.setType(type != null ? type : "unknown");

                // 获取过期时间
                Long ttl = redisTemplate.getExpire(key);
                keyInfo.setTtl(ttl != null ? ttl : -2L);

                // 获取值大小（估算）
                try {
                    Object value = redisTemplate.opsForValue().get(key);
                    if (value != null) {
                        // 简单估算：将对象序列化为字符串的长度
                        long size = value.toString().getBytes().length;
                        keyInfo.setSize(size);
                    } else {
                        keyInfo.setSize(0L);
                    }
                } catch (Exception e) {
                    log.debug("获取键大小失败，key: {}", key, e);
                    keyInfo.setSize(0L);
                }

                keyInfoList.add(keyInfo);
            }

            return keyInfoList;
        } catch (Exception e) {
            log.error("获取缓存键列表失败，cacheName: {}", cacheName, e);
            throw new BizException(ErrorCode.REDIS_ERROR, "获取缓存键列表失败: " + e.getMessage());
        }
    }

    @Override
    public RedisCacheVO.CacheContentInfo getCacheContent(String cacheName, String key) {
        if (redisTemplate == null) {
            throw new BizException(ErrorCode.REDIS_ERROR, "Redis 未配置或未启用");
        }

        if (key == null || key.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "缓存键名不能为空");
        }

        try {
            RedisCacheVO.CacheContentInfo contentInfo = new RedisCacheVO.CacheContentInfo();
            contentInfo.setCacheName(cacheName);
            contentInfo.setKey(key);

            // 检查键是否存在
            Boolean exists = redisTemplate.hasKey(key);
            if (!Boolean.TRUE.equals(exists)) {
                contentInfo.setType("none");
                contentInfo.setTtl(-2L);
                contentInfo.setContent("(键不存在)");
                return contentInfo;
            }

            // 获取键类型
            String type = redisTemplate.execute((RedisCallback<String>) connection -> {
                try {
                    DataType dataType = connection.keyCommands().type(key.getBytes());
                    return dataType != null ? dataType.code() : "unknown";
                } catch (Exception e) {
                    log.warn("获取键类型失败，key: {}", key, e);
                    return "unknown";
                }
            });
            contentInfo.setType(type != null ? type : "unknown");

            // 获取过期时间
            Long ttl = redisTemplate.getExpire(key);
            contentInfo.setTtl(ttl != null ? ttl : -2L);

            // 根据类型获取缓存值
            String content = getCacheValueByType(key, type);
            contentInfo.setContent(content);

            return contentInfo;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取缓存内容失败，cacheName: {}, key: {}", cacheName, key, e);
            throw new BizException(ErrorCode.REDIS_ERROR, "获取缓存内容失败: " + e.getMessage());
        }
    }

    /**
     * 根据 Redis 数据类型获取缓存值
     *
     * @param key  键名
     * @param type 数据类型（string, hash, list, set, zset）
     * @return 格式化后的内容字符串
     */
    private String getCacheValueByType(String key, String type) {
        if (type == null || "none".equals(type) || "unknown".equals(type)) {
            return "(无法获取内容)";
        }

        try {
            ObjectMapper objectMapper = getOrCreateCacheViewObjectMapper();

            switch (type.toLowerCase()) {
                case "string":
                    // String 类型 - 先尝试反序列化，失败则使用原始字节
                    try {
                        Object stringValue = redisTemplate.opsForValue().get(key);
                        if (stringValue != null) {
                            return truncateString(formatObjectValue(stringValue, objectMapper), CACHE_CONTENT_MAX_LENGTH);
                        }
                    } catch (Exception e) {
                        // 反序列化失败，尝试获取原始字节
                        log.debug("反序列化失败，尝试获取原始字节，key: {}, error: {}", key, e.getMessage());
                    }
                    // 如果反序列化失败，尝试获取原始字节
                    try {
                        byte[] rawBytes = redisTemplate.execute((RedisCallback<byte[]>) connection ->
                            connection.stringCommands().get(key.getBytes()));
                        if (rawBytes != null) {
                            String rawString = new String(rawBytes, "UTF-8");
                            // 尝试格式化 JSON
                            try {
                                Object jsonValue = objectMapper.readValue(rawString, Object.class);
                                String pretty = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonValue);
                                return truncateString(pretty, CACHE_CONTENT_MAX_LENGTH);
                            } catch (Exception e) {
                                // 不是有效的 JSON，直接返回原始字符串
                                return truncateString(rawString, CACHE_CONTENT_MAX_LENGTH);
                            }
                        }
                    } catch (Exception e) {
                        log.debug("获取原始字节失败，key: {}", key, e);
                    }
                    return "(空值)";

                case "hash":
                    // Hash 类型
                    Map<Object, Object> hashValue = redisTemplate.opsForHash().entries(key);
                    if (!hashValue.isEmpty()) {
                        return truncateString(formatObjectValue(hashValue, objectMapper), CACHE_CONTENT_MAX_LENGTH);
                    }
                    return "(空哈希)";

                case "list":
                    // List 类型
                    List<Object> listValue = redisTemplate.opsForList().range(key, 0, -1);
                    if (listValue != null && !listValue.isEmpty()) {
                        return truncateString(formatObjectValue(listValue, objectMapper), CACHE_CONTENT_MAX_LENGTH);
                    }
                    return "(空列表)";

                case "set":
                    // Set 类型
                    Set<Object> setValue = redisTemplate.opsForSet().members(key);
                    if (setValue != null && !setValue.isEmpty()) {
                        return truncateString(formatObjectValue(setValue, objectMapper), CACHE_CONTENT_MAX_LENGTH);
                    }
                    return "(空集合)";

                case "zset":
                    // Sorted Set 类型：一次性取出成员及分数，避免二次网络往返
                    Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object>> zsetWithScores =
                        redisTemplate.opsForZSet().rangeWithScores(key, 0, -1);
                    if (zsetWithScores != null && !zsetWithScores.isEmpty()) {
                        Map<String, Double> zsetMap = new LinkedHashMap<>();
                        for (org.springframework.data.redis.core.ZSetOperations.TypedTuple<Object> tuple : zsetWithScores) {
                            Object member = tuple.getValue();
                            Double score = tuple.getScore();
                            if (member != null) {
                                zsetMap.put(member.toString(), score != null ? score : 0.0);
                            }
                        }
                        return truncateString(formatObjectValue(zsetMap, objectMapper), CACHE_CONTENT_MAX_LENGTH);
                    }
                    return "(空有序集合)";

                default:
                    // 未知类型，尝试作为 String 获取
                    try {
                        Object value = redisTemplate.opsForValue().get(key);
                        if (value != null) {
                            return truncateString(formatObjectValue(value, objectMapper), CACHE_CONTENT_MAX_LENGTH);
                        }
                    } catch (Exception e) {
                        log.debug("尝试获取未知类型键值失败，key: {}, type: {}", key, type, e);
                    }
                    return "(不支持的类型: " + type + ")";
            }
        } catch (Exception e) {
            log.error("获取缓存值失败，key: {}, type: {}", key, type, e);
            return "(获取内容失败: " + e.getMessage() + ")";
        }
    }

    /**
     * 格式化对象值为字符串
     * 对于无法序列化的对象，提供友好的降级处理
     *
     * @param value 对象值
     * @param objectMapper Jackson ObjectMapper
     * @return 格式化后的字符串
     */
    private String formatObjectValue(Object value, ObjectMapper objectMapper) {
        if (value == null) {
            return "(null)";
        }

        // 如果是基本类型或字符串，直接返回
        if (value instanceof String || value instanceof Number || value instanceof Boolean) {
            return value.toString();
        }

        // 尝试使用 Jackson 序列化
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (com.fasterxml.jackson.databind.JsonMappingException e) {
            // Jackson 无法序列化（通常是缺少默认构造函数或复杂的嵌套对象）
            log.debug("Jackson 序列化失败，尝试使用反射获取对象信息: {}", e.getMessage());
            return formatObjectWithReflection(value);
        } catch (Exception e) {
            // 其他序列化错误，使用反射方式
            log.debug("序列化失败，使用反射方式: {}", e.getMessage());
            return formatObjectWithReflection(value);
        }
    }

    /**
     * 使用反射方式格式化对象
     * 对于无法序列化的对象，尝试提取关键信息
     *
     * @param value 对象值
     * @return 格式化后的字符串
     */
    private String formatObjectWithReflection(Object value) {
        if (value == null) {
            return "(null)";
        }

        try {
            Class<?> clazz = value.getClass();
            StringBuilder sb = new StringBuilder();
            sb.append("{\n");
            sb.append("  \"@class\": \"").append(clazz.getName()).append("\",\n");

            // 尝试使用反射获取字段值
            java.lang.reflect.Field[] fields = clazz.getDeclaredFields();
            boolean hasFields = false;
            for (java.lang.reflect.Field field : fields) {
                // 跳过静态字段和序列化相关字段
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) ||
                    "serialVersionUID".equals(field.getName())) {
                    continue;
                }

                try {
                    field.setAccessible(true);
                    Object fieldValue = field.get(value);
                    if (hasFields) {
                        sb.append(",\n");
                    }
                    sb.append("  \"").append(field.getName()).append("\": ");

                    if (fieldValue == null) {
                        sb.append("null");
                    } else if (fieldValue instanceof String || fieldValue instanceof Number || fieldValue instanceof Boolean) {
                        // 基本类型，直接输出
                        if (fieldValue instanceof String) {
                            sb.append("\"").append(escapeJsonString(fieldValue.toString())).append("\"");
                        } else {
                            sb.append(fieldValue);
                        }
                    } else {
                        // 复杂对象，显示类型和 toString()
                        sb.append("\"").append(escapeJsonString(fieldValue.getClass().getSimpleName() + ": " + fieldValue)).append("\"");
                    }
                    hasFields = true;
                } catch (Exception e) {
                    // 忽略无法访问的字段
                    log.debug("无法访问字段 {}: {}", field.getName(), e.getMessage());
                }
            }

            if (!hasFields) {
                // 如果没有可访问的字段，使用 toString()
                sb.append("  \"toString\": \"").append(escapeJsonString(value.toString())).append("\"");
            }

            sb.append("\n}");
            return sb.toString();
        } catch (Exception e) {
            log.debug("反射格式化失败，使用 toString(): {}", e.getMessage());
            // 最后的降级方案：使用 toString()
            return "{\n  \"@class\": \"" + value.getClass().getName() + "\",\n" +
                   "  \"toString\": \"" + escapeJsonString(value.toString()) + "\"\n}";
        }
    }

    /**
     * 转义 JSON 字符串中的特殊字符
     *
     * @param str 原始字符串
     * @return 转义后的字符串
     */
    private String escapeJsonString(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    /**
     * 懒加载并缓存用于缓存内容查看的 ObjectMapper，避免频繁创建实例
     */
    private ObjectMapper getOrCreateCacheViewObjectMapper() {
        ObjectMapper local = cacheViewObjectMapper;
        if (local != null) {
            return local;
        }
        synchronized (this) {
            if (cacheViewObjectMapper == null) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);
                cacheViewObjectMapper = mapper;
            }
            return cacheViewObjectMapper;
        }
    }

    @Override
    public long deleteCache(String cacheName) {
        if (redisTemplate == null) {
            throw new BizException(ErrorCode.REDIS_ERROR, "Redis 未配置或未启用");
        }

        if (cacheName == null || cacheName.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "缓存名称不能为空");
        }

        try {
            long total = 0L;
            // 分组规则里「无前缀冒号」时整键即分组名，仅 scan「prefix:*」会漏删
            Boolean deletedExact = redisTemplate.delete(cacheName);
            if (Boolean.TRUE.equals(deletedExact)) {
                total++;
            }

            String keyPattern = cacheName + ":*";
            Set<String> keys = scanKeys(keyPattern);
            if (!keys.isEmpty()) {
                Long deletedBatch = redisTemplate.delete(keys);
                total += deletedBatch != null ? deletedBatch : 0L;
            }

            return total;
        } catch (Exception e) {
            log.error("删除缓存失败，cacheName: {}", cacheName, e);
            throw new BizException(ErrorCode.REDIS_ERROR, "删除缓存失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteCacheKey(String cacheName, String key) {
        if (redisTemplate == null) {
            throw new BizException(ErrorCode.REDIS_ERROR, "Redis 未配置或未启用");
        }

        if (key == null || key.trim().isEmpty()) {
            throw new BizException(ErrorCode.PARAM_ERROR, "缓存键名不能为空");
        }

        try {
            Boolean deleted = redisTemplate.delete(key);
            return Boolean.TRUE.equals(deleted);
        } catch (Exception e) {
            log.error("删除缓存键失败，cacheName: {}, key: {}", cacheName, key, e);
            throw new BizException(ErrorCode.REDIS_ERROR, "删除缓存键失败: " + e.getMessage());
        }
    }

    @Override
    public boolean clearAllCache() {
        if (redisTemplate == null) {
            throw new BizException(ErrorCode.REDIS_ERROR, "Redis 未配置或未启用");
        }

        try {
            RedisCapabilities capabilities = getRedisCapabilities();
            if (capabilities.supportAsyncFlush) {
                try {
                    redisTemplate.execute((RedisCallback<Object>) connection -> {
                        RedisServerCommands serverCommands = connection.serverCommands();
                        serverCommands.flushDb(RedisServerCommands.FlushOption.ASYNC);
                        return null;
                    });
                    log.info("Redis 当前数据库已成功清空（FLUSHDB ASYNC）");
                    return true;
                } catch (Exception asyncEx) {
                    log.warn("FLUSHDB ASYNC 执行失败，自动降级为同步 FLUSHDB，原因: {}", asyncEx.getMessage());
                    refreshRedisCapabilities(false);
                }
            }

            redisTemplate.execute((RedisCallback<Object>) connection -> {
                RedisServerCommands serverCommands = connection.serverCommands();
                serverCommands.flushDb();
                return null;
            });
            log.info("Redis 当前数据库已成功清空（FLUSHDB）");
            return true;
        } catch (Exception e) {
            log.error("清空所有缓存失败", e);
            throw new BizException(ErrorCode.REDIS_ERROR, "清空所有缓存失败: " + e.getMessage());
        }
    }

    private RedisCapabilities getRedisCapabilities() {
        RedisCapabilities local = redisCapabilities;
        long now = System.currentTimeMillis();
        if (local != null && now - local.detectedAt <= REDIS_CAPABILITY_CACHE_MS) {
            return local;
        }
        synchronized (this) {
            local = redisCapabilities;
            now = System.currentTimeMillis();
            if (local != null && now - local.detectedAt <= REDIS_CAPABILITY_CACHE_MS) {
                return local;
            }
            RedisCapabilities detected = detectRedisCapabilities();
            redisCapabilities = detected;
            return detected;
        }
    }

    private void refreshRedisCapabilities(boolean supportAsyncFlush) {
        RedisCapabilities current = getRedisCapabilities();
        redisCapabilities = new RedisCapabilities(
            current.version,
            current.clusterMode,
            supportAsyncFlush,
            System.currentTimeMillis()
        );
    }

    private RedisCapabilities detectRedisCapabilities() {
        String version = "unknown";
        boolean clusterMode = false;

        try {
            Properties serverInfo = redisTemplate.execute((RedisCallback<Properties>) connection ->
                connection.serverCommands().info("server"));
            if (serverInfo != null) {
                Object versionVal = serverInfo.get("redis_version");
                if (versionVal != null) {
                    version = String.valueOf(versionVal);
                }
            }
        } catch (Exception e) {
            log.debug("获取 Redis 版本信息失败，使用默认兼容策略", e);
        }

        try {
            Properties clusterInfo = redisTemplate.execute((RedisCallback<Properties>) connection ->
                connection.serverCommands().info("cluster"));
            if (clusterInfo != null) {
                Object enabled = clusterInfo.get("cluster_enabled");
                clusterMode = "1".equals(String.valueOf(enabled));
            }
        } catch (Exception e) {
            log.debug("获取 Redis 集群信息失败，按非集群处理", e);
        }

        // Redis 4.0+ 支持 FLUSHDB ASYNC；未知版本按支持处理，运行时失败会自动降级
        boolean supportAsyncFlush = isVersionAtLeast(version, 4);
        if ("unknown".equals(version)) {
            supportAsyncFlush = true;
        }

        log.info("Redis 能力探测完成，version={}, clusterMode={}, supportAsyncFlush={}",
            version, clusterMode, supportAsyncFlush);
        return new RedisCapabilities(version, clusterMode, supportAsyncFlush, System.currentTimeMillis());
    }

    private boolean isVersionAtLeast(String version, int major) {
        if (version == null || version.isBlank() || "unknown".equals(version)) {
            return false;
        }
        try {
            String[] parts = version.split("\\.");
            int majorVersion = Integer.parseInt(parts[0]);
            return majorVersion >= major;
        } catch (Exception e) {
            return false;
        }
    }

    private static class RedisCapabilities {
        private final String version;
        private final boolean clusterMode;
        private final boolean supportAsyncFlush;
        private final long detectedAt;

        private RedisCapabilities(String version, boolean clusterMode, boolean supportAsyncFlush, long detectedAt) {
            this.version = version;
            this.clusterMode = clusterMode;
            this.supportAsyncFlush = supportAsyncFlush;
            this.detectedAt = detectedAt;
        }
    }

}
