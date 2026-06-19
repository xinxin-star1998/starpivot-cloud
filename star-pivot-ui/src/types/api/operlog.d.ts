/**
 * 操作日志相关类型定义
 *
 * @module types/api/operlog
 * @author xinxin
 * @since 2026-01-23
 */

/**
 * 操作日志列表项
 */
export interface OperLogListItem {
  /** 日志主键 */
  operId: number
  /** 模块标题 */
  title?: string
  /** 业务类型（字典 sys_oper_type：0其他 1新增 2修改 3删除 4授权 5导出 6导入 7强退 8生成代码 9清空数据） */
  businessType?: number
  /** 方法名称 */
  method?: string
  /** 请求方式 */
  requestMethod?: string
  /** 操作类别（0其它 1后台用户 2手机端用户） */
  operatorType?: number
  /** 操作人员 */
  operName?: string
  /** 部门名称 */
  deptName?: string
  /** 请求URL */
  operUrl?: string
  /** 主机地址 */
  operIp?: string
  /** 操作地点 */
  operLocation?: string
  /** 请求参数 */
  operParam?: string
  /** 返回参数 */
  jsonResult?: string
  /** 操作状态（0正常 1异常） */
  status?: number
  /** 错误消息 */
  errorMsg?: string
  /** 操作时间 */
  operTime?: string
  /** 消耗时间（毫秒） */
  costTime?: number
}

/**
 * 操作日志搜索参数
 */
export interface OperLogSearchParams extends Api.Common.CommonSearchParams {
  /** 模块标题 */
  title?: string
  /** 业务类型（字典 sys_oper_type：0其他 1新增 2修改 3删除 4授权 5导出 6导入 7强退 8生成代码 9清空数据） */
  businessType?: number
  /** 操作人员 */
  operName?: string
  /** 操作状态（0正常 1异常） */
  status?: number
  /** 开始时间 */
  startTime?: string
  /** 结束时间 */
  endTime?: string
}

/**
 * 操作日志列表响应
 */
export type OperLogList = Api.Common.PaginatedResponse<OperLogListItem>
