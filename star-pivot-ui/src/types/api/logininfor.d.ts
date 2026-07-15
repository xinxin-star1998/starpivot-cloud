/**
 * 登录日志相关类型定义
 *
 * @module types/api/logininfor
 * @author xinxin
 * @since 2026-01-23
 */

/**
 * 登录日志列表项
 */
export interface LogininforListItem {
  /** 访问ID */
  infoId: number
  /** 用户账号 */
  userName?: string
  /** 登录IP地址 */
  ipaddr?: string
  /** 登录地点 */
  loginLocation?: string
  /** 浏览器类型 */
  browser?: string
  /** 操作系统 */
  os?: string
  /** 登录状态（0成功 1失败） */
  status?: string
  /** 提示消息 */
  msg?: string
  /** 访问时间 */
  loginTime?: string
}

/**
 * 登录日志搜索参数
 */
export interface LogininforSearchParams extends Api.Common.CommonSearchParams {
  /** 用户账号 */
  userName?: string
  /** 登录IP地址 */
  ipaddr?: string
  /** 登录状态（0成功 1失败） */
  status?: string
  /** 开始时间 */
  startTime?: string
  /** 结束时间 */
  endTime?: string
}

/**
 * 登录日志列表响应
 */
export type LogininforList = Api.Common.PageResponse<LogininforListItem>
