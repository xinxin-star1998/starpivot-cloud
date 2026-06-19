package cn.org.starpivot.monitor.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 服务器信息 VO
 *
 * @author xinxin
 * @since 2026-01-25
 */
@Data
public class ServerInfoVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * CPU 信息
     */
    private CpuInfo cpu;

    /**
     * 内存信息
     */
    private MemoryInfo memory;

    /**
     * JVM 信息
     */
    private JvmInfo jvm;

    /**
     * 系统信息
     */
    private SystemInfo system;

    /**
     * 磁盘信息
     */
    private DiskInfo disk;

    /**
     * CPU 信息
     */
    @Data
    public static class CpuInfo implements Serializable {
        /**
         * CPU 核心数
         */
        private Integer cpuNum;

        /**
         * CPU 总使用率
         */
        private Double total;

        /**
         * CPU 系统使用率
         */
        private Double sys;

        /**
         * CPU 用户使用率
         */
        private Double used;

        /**
         * CPU 当前等待率
         */
        private Double wait;

        /**
         * CPU 当前空闲率
         */
        private Double free;
    }

    /**
     * 内存信息
     */
    @Data
    public static class MemoryInfo implements Serializable {
        /**
         * 内存总量（MB）
         */
        private Long total;

        /**
         * 已用内存（MB）
         */
        private Long used;

        /**
         * 剩余内存（MB）
         */
        private Long free;

        /**
         * 使用率
         */
        private Double usage;
    }

    /**
     * JVM 信息
     */
    @Data
    public static class JvmInfo implements Serializable {
        /**
         * JVM 名称
         */
        private String name;

        /**
         * JVM 版本
         */
        private String version;

        /**
         * JVM 启动时间（毫秒）
         */
        private Long startTime;

        /**
         * JVM 运行时长（毫秒）
         */
        private Long runTime;

        /**
         * Java 安装路径
         */
        private String home;

        /**
         * 项目路径
         */
        private String userDir;

        /**
         * JVM 运行参数
         */
        private String inputArgs;

        /**
         * JVM 最大可用内存（MB）
         */
        private Long max;

        /**
         * JVM 已分配内存（MB）
         */
        private Long total;

        /**
         * JVM 已使用内存（MB）
         */
        private Long used;

        /**
         * JVM 剩余内存（MB）
         */
        private Long free;

        /**
         * JVM 内存使用率
         */
        private Double usage;
    }

    /**
     * 系统信息
     */
    @Data
    public static class SystemInfo implements Serializable {
        /**
         * 服务器名称
         */
        private String computerName;

        /**
         * 操作系统
         */
        private String osName;

        /**
         * 系统架构
         */
        private String osArch;

        /**
         * 服务器IP
         */
        private String computerIp;
    }

    /**
     * 磁盘信息
     */
    @Data
    public static class DiskInfo implements Serializable {
        /**
         * 磁盘总容量（GB）
         */
        private Long total;

        /**
         * 磁盘已用容量（GB）
         */
        private Long used;

        /**
         * 磁盘剩余容量（GB）
         */
        private Long free;

        /**
         * 磁盘使用率
         */
        private Double usage;

        /**
         * 磁盘分区明细
         */
        private List<DiskStoreInfo> stores;
    }

    /**
     * 磁盘分区明细
     */
    @Data
    public static class DiskStoreInfo implements Serializable {
        /**
         * 挂载目录（如 /、/boot）
         */
        private String mount;

        /**
         * 文件系统（如 ext4）
         */
        private String fileSystem;

        /**
         * 磁盘类型/设备（如 /dev/nvme0n1p2）
         */
        private String typeName;

        /**
         * 总大小（GB）
         */
        private Double totalGb;

        /**
         * 可用大小（GB）
         */
        private Double usableGb;

        /**
         * 已用大小（GB）
         */
        private Double usedGb;

        /**
         * 已用百分比
         */
        private Double usage;
    }
}
