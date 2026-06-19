package cn.org.starpivot.monitor.utils;

import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.OperatingSystem;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * 服务器信息工具类
 *
 * @author xinxin
 * @since 2026-03-04
 */
public class ServerUtil {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");

    private ServerUtil() {
        // 私有构造函数，防止实例化
    }

    /**
     * 获取系统信息对象
     *
     * @return 系统信息对象
     */
    public static SystemInfo getSystemInfo() {
        return new SystemInfo();
    }

    /**
     * 获取CPU使用率
     *
     * @param hal 硬件抽象层
     * @return CPU使用率（百分比）
     */
    public static double getCpuUsage(HardwareAbstractionLayer hal) {
        CentralProcessor processor = hal.getProcessor();
        long[] prevTicks = processor.getSystemCpuLoadTicks();

        // 等待1秒
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return 0.0;
        }

        long[] ticks = processor.getSystemCpuLoadTicks();
        long total = 0;
        long used = 0;

        for (int i = 0; i < ticks.length; i++) {
            total += ticks[i];
            if (i != CentralProcessor.TickType.IDLE.getIndex()) {
                used += ticks[i];
            }
        }

        return total > 0 ? (double) used / total * 100 : 0.0;
    }

    /**
     * 获取内存使用率
     *
     * @param hal 硬件抽象层
     * @return 内存使用率（百分比）
     */
    public static double getMemoryUsage(HardwareAbstractionLayer hal) {
        GlobalMemory memory = hal.getMemory();
        long total = memory.getTotal();
        long available = memory.getAvailable();
        return total > 0 ? (double) (total - available) / total * 100 : 0.0;
    }

    /**
     * 格式化字节数
     *
     * @param bytes 字节数
     * @return 格式化后的字符串
     */
    public static String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return DECIMAL_FORMAT.format(bytes / 1024.0) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return DECIMAL_FORMAT.format(bytes / (1024.0 * 1024.0)) + " MB";
        } else {
            return DECIMAL_FORMAT.format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
        }
    }

    /**
     * 格式化百分比
     *
     * @param value 数值
     * @return 百分比字符串
     */
    public static String formatPercent(double value) {
        return DECIMAL_FORMAT.format(value) + "%";
    }

    /**
     * 获取操作系统信息
     *
     * @return 操作系统信息
     */
    public static OperatingSystem getOperatingSystem() {
        return getSystemInfo().getOperatingSystem();
    }
}
