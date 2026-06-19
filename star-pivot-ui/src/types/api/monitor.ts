/**
 * 监控相关类型定义
 */

/**
 * 服务器信息
 */
export interface ServerInfo {
  cpu: CpuInfo
  memory: MemoryInfo
  jvm: JvmInfo
  system: SystemInfo
  disk: DiskInfo
}

/**
 * CPU 信息
 */
export interface CpuInfo {
  /** CPU 核心数 */
  cpuNum: number
  /** CPU 总使用率 */
  total: number
  /** CPU 系统使用率 */
  sys: number
  /** CPU 用户使用率 */
  used: number
  /** CPU 当前等待率 */
  wait: number
  /** CPU 当前空闲率 */
  free: number
}

/**
 * 内存信息
 */
export interface MemoryInfo {
  /** 内存总量（MB） */
  total: number
  /** 已用内存（MB） */
  used: number
  /** 剩余内存（MB） */
  free: number
  /** 使用率 */
  usage: number
}

/**
 * JVM 信息
 */
export interface JvmInfo {
  /** JVM 名称 */
  name: string
  /** JVM 版本 */
  version: string
  /** JVM 启动时间（毫秒） */
  startTime: number
  /** JVM 运行时长（毫秒） */
  runTime: number
  /** Java 安装路径 */
  home: string
  /** 项目路径 */
  userDir: string
  /** JVM 运行参数 */
  inputArgs: string
  /** JVM 最大可用内存（MB） */
  max: number
  /** JVM 已分配内存（MB） */
  total: number
  /** JVM 已使用内存（MB） */
  used: number
  /** JVM 剩余内存（MB） */
  free: number
  /** JVM 内存使用率 */
  usage: number
}

/**
 * 系统信息
 */
export interface SystemInfo {
  /** 服务器名称 */
  computerName: string
  /** 操作系统 */
  osName: string
  /** 系统架构 */
  osArch: string
  /** 服务器IP */
  computerIp: string
}

/**
 * 磁盘信息
 */
export interface DiskInfo {
  /** 磁盘总容量（GB） */
  total: number
  /** 磁盘已用容量（GB） */
  used: number
  /** 磁盘剩余容量（GB） */
  free: number
  /** 磁盘使用率 */
  usage: number
  /** 磁盘分区明细 */
  stores?: DiskStoreInfo[]
}

/**
 * 磁盘分区明细
 */
export interface DiskStoreInfo {
  /** 挂载目录 */
  mount: string
  /** 文件系统 */
  fileSystem: string
  /** 磁盘类型/设备 */
  typeName: string
  /** 总大小（GB） */
  totalGb: number
  /** 可用大小（GB） */
  usableGb: number
  /** 已用大小（GB） */
  usedGb: number
  /** 已用百分比 */
  usage: number
}

/**
 * Druid 监控信息
 */
export interface DruidMonitorInfo {
  /** 是否可用：true 表示数据源为 Druid 可展示监控；false 表示非 Druid 或未配置，仅展示提示 */
  available?: boolean
  /** 不可用时的提示文案 */
  message?: string
  /** 数据源名称 */
  name?: string
  /** 数据库类型 */
  dbType?: string
  /** 驱动类名 */
  driverClassName?: string
  /** 连接池信息 */
  connectionPool?: ConnectionPoolInfo
  /** SQL 统计信息 */
  sqlStat?: SqlStatInfo
  /** 慢SQL列表（可选，当需要详细慢SQL信息时返回） */
  slowSqlList?: SlowSqlInfo[]
}

/**
 * 慢SQL详细信息
 */
export interface SlowSqlInfo {
  /** SQL ID（Druid生成的SQL标识） */
  sqlId?: string
  /** SQL语句 */
  sqlText?: string
  /** 执行次数 */
  executeCount?: number
  /** 总执行时间（毫秒） */
  executeTimeTotal?: number
  /** 最大执行时间（毫秒） */
  executeTimeMax?: number
  /** 平均执行时间（毫秒） */
  executeTimeAvg?: number
  /** 慢SQL次数 */
  slowCount?: number
  /** 错误次数 */
  errorCount?: number
  /** 最后执行时间（时间戳，毫秒） */
  lastExecuteTime?: number
}

/**
 * 连接池信息
 */
export interface ConnectionPoolInfo {
  /** 初始连接数 */
  initialSize: number
  /** 最小空闲连接数 */
  minIdle: number
  /** 最大活跃连接数 */
  maxActive: number
  /** 当前连接数 */
  activeCount: number
  /** 活跃连接数 */
  activePeak: number
  /** 连接池使用率 */
  usage: number
}

/**
 * SQL 统计信息
 */
export interface SqlStatInfo {
  /** SQL 执行总数 */
  executeCount: number
  /** SQL 执行总耗时（毫秒） */
  executeMillisTotal: number
  /** 平均执行时间（毫秒） */
  executeMillisAvg: number
  /** 慢 SQL 数量 */
  slowSqlCount: number
  /** 错误 SQL 数量 */
  errorSqlCount: number
}

/**
 * Redis 缓存信息
 */
export interface RedisCacheInfo {
  /** 缓存名称 */
  cacheName: string
  /** 备注 */
  remark: string
  /** 键名列表 */
  keys?: CacheKeyInfo[]
}

/**
 * 缓存键信息
 */
export interface CacheKeyInfo {
  /** 键名 */
  key: string
  /** 键类型（string, hash, list, set, zset） */
  type: string
  /** 过期时间（秒），-1表示永不过期，-2表示键不存在 */
  ttl: number
  /** 值大小（字节） */
  size: number
}

/**
 * 缓存内容信息
 */
export interface CacheContentInfo {
  /** 缓存名称 */
  cacheName: string
  /** 缓存键名 */
  key: string
  /** 缓存内容（JSON 格式） */
  content: string
  /** 键类型 */
  type: string
  /** 过期时间（秒） */
  ttl: number
}

/**
 * 在线用户
 */
export interface OnlineUser {
  /** 会话ID */
  sessionId: string
  /** 用户ID */
  userId: number
  /** 用户名 */
  userName: string
  /** 用户昵称 */
  nickName?: string
  /** 部门名称 */
  deptName?: string
  /** IP地址 */
  ipaddr?: string
  /** 登录地点 */
  loginLocation?: string
  /** 浏览器 */
  browser?: string
  /** 操作系统 */
  os?: string
  /** 登录时间 */
  loginTime: string
  /** 最后访问时间 */
  lastAccessTime: string
}

/**
 * 在线用户查询参数
 */
export interface OnlineUserQueryParams {
  /** 用户名（可选） */
  userName?: string
  /** IP地址（可选） */
  ipaddr?: string
}
