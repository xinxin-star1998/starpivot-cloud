package cn.org.starpivot.monitor.service.support;

import cn.org.starpivot.common.exception.BizException;
import cn.org.starpivot.common.exception.ErrorCode;
import cn.org.starpivot.monitor.domain.vo.ServerInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于 OSHI / JMX 的服务器运行时指标采集。
 */
@Slf4j
@Component
public class ServerMetricsCollector {

    private static final SystemInfo SYSTEM_INFO = new SystemInfo();
    private static final HardwareAbstractionLayer HAL = SYSTEM_INFO.getHardware();
    private static final OperatingSystem OS = SYSTEM_INFO.getOperatingSystem();

    private static final int BYTES_TO_KB = 1024;
    private static final int KB_TO_MB = 1024;
    private static final int MB_TO_GB = 1024;
    private static final double PERCENTAGE_BASE = 100.0;
    private static final int CPU_CACHE_SECONDS = 2;
    private static final Set<String> VIRTUAL_FS_TYPES = Set.of(
            "tmpfs", "devtmpfs", "overlay", "squashfs", "proc", "sysfs", "cgroup", "cgroup2",
            "nsfs", "tracefs", "debugfs", "securityfs", "pstore", "mqueue", "hugetlbfs",
            "rpc_pipefs", "configfs", "fusectl", "autofs");

    private static final Map<String, Object> CPU_INFO_CACHE = new ConcurrentHashMap<>();
    private static final String CACHE_KEY_CPU_INFO = "cpu_info";
    private static final String CACHE_KEY_CPU_TIMESTAMP = "cpu_timestamp";

    private final Object cpuTicksLock = new Object();
    private volatile long[] previousCpuTicks = HAL.getProcessor().getSystemCpuLoadTicks();

    public ServerInfoVO collectServerInfo() {
        ServerInfoVO serverInfo = new ServerInfoVO();
        try {
            serverInfo.setCpu(getCpuInfo());
            serverInfo.setMemory(getMemoryInfo());
            serverInfo.setJvm(getJvmInfo());
            serverInfo.setSystem(getSystemInfo());
            serverInfo.setDisk(getDiskInfo());
        } catch (Exception e) {
            log.error("获取服务器信息失败", e);
            throw new BizException(ErrorCode.SYSTEM_ERROR, "获取服务器信息失败: " + e.getMessage());
        }
        return serverInfo;
    }

    private ServerInfoVO.CpuInfo getCpuInfo() {
        Long cacheTimestamp = (Long) CPU_INFO_CACHE.get(CACHE_KEY_CPU_TIMESTAMP);
        if (cacheTimestamp != null && System.currentTimeMillis() - cacheTimestamp < CPU_CACHE_SECONDS * 1000L) {
            ServerInfoVO.CpuInfo cachedInfo = (ServerInfoVO.CpuInfo) CPU_INFO_CACHE.get(CACHE_KEY_CPU_INFO);
            if (cachedInfo != null) {
                return cachedInfo;
            }
        }

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

        CPU_INFO_CACHE.put(CACHE_KEY_CPU_INFO, cpuInfo);
        CPU_INFO_CACHE.put(CACHE_KEY_CPU_TIMESTAMP, System.currentTimeMillis());
        return cpuInfo;
    }

    private ServerInfoVO.MemoryInfo getMemoryInfo() {
        GlobalMemory memory = HAL.getMemory();
        long total = memory.getTotal();
        long available = memory.getAvailable();
        long used = total - available;

        ServerInfoVO.MemoryInfo memoryInfo = new ServerInfoVO.MemoryInfo();
        memoryInfo.setTotal(total / BYTES_TO_KB / KB_TO_MB);
        memoryInfo.setUsed(used / BYTES_TO_KB / KB_TO_MB);
        memoryInfo.setFree(available / BYTES_TO_KB / KB_TO_MB);
        memoryInfo.setUsage(PERCENTAGE_BASE * used / total);
        return memoryInfo;
    }

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
        jvmInfo.setHome(System.getProperty("java.home", ""));
        jvmInfo.setUserDir(System.getProperty("user.dir", ""));
        jvmInfo.setInputArgs(String.join(" ", runtimeMXBean.getInputArguments()));
        jvmInfo.setMax(totalMemory / BYTES_TO_KB / KB_TO_MB);
        jvmInfo.setTotal(memoryMXBean.getHeapMemoryUsage().getCommitted() / BYTES_TO_KB / KB_TO_MB);
        jvmInfo.setUsed(usedMemory / BYTES_TO_KB / KB_TO_MB);
        jvmInfo.setFree(freeMemory / BYTES_TO_KB / KB_TO_MB);
        jvmInfo.setUsage(PERCENTAGE_BASE * usedMemory / totalMemory);
        return jvmInfo;
    }

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

    private ServerInfoVO.DiskInfo getDiskInfo() {
        FileSystem fileSystem = OS.getFileSystem();
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
            if (!isKeyPartition(store, mount) || !seenMounts.add(mount)) {
                continue;
            }

            long storeTotal = store.getTotalSpace();
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
        diskInfo.setTotal(total / BYTES_TO_KB / KB_TO_MB / MB_TO_GB);
        diskInfo.setUsed(used / BYTES_TO_KB / KB_TO_MB / MB_TO_GB);
        diskInfo.setFree(free / BYTES_TO_KB / KB_TO_MB / MB_TO_GB);
        diskInfo.setUsage(total > 0 ? PERCENTAGE_BASE * used / total : 0.0);
        diskInfo.setStores(stores);
        return diskInfo;
    }

    private boolean isKeyPartition(OSFileStore store, String mount) {
        String fsType = Optional.ofNullable(store.getType()).orElse("").toLowerCase(Locale.ROOT);
        String name = Optional.ofNullable(store.getName()).orElse("").toLowerCase(Locale.ROOT);
        String lowerMount = mount.toLowerCase(Locale.ROOT);

        if (mount.matches("^[a-zA-Z]:\\\\.*")) {
            return true;
        }
        if (!mount.startsWith("/")) {
            return true;
        }
        if (VIRTUAL_FS_TYPES.contains(fsType)) {
            return false;
        }
        if (lowerMount.startsWith("/proc")
                || lowerMount.startsWith("/sys")
                || lowerMount.startsWith("/run")
                || lowerMount.startsWith("/dev")
                || lowerMount.startsWith("/snap")) {
            return false;
        }

        return "/".equals(mount)
                || mount.startsWith("/boot")
                || mount.startsWith("/home")
                || mount.startsWith("/var")
                || mount.startsWith("/opt")
                || mount.startsWith("/srv")
                || mount.startsWith("/usr")
                || mount.startsWith("/data")
                || mount.startsWith("/mnt")
                || mount.startsWith("/disk")
                || mount.startsWith("/volume")
                || name.startsWith("/dev/");
    }

    private Double toGb(long bytes) {
        return round2(bytes / 1024.0 / 1024.0 / 1024.0);
    }

    private Double round2(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
