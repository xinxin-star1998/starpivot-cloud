import request from '@/utils/http'

/**
 * 定时任务
 */
export interface SysJob {
  jobId?: number
  jobName?: string
  jobGroup?: string
  invokeTarget?: string
  cronExpression?: string
  misfirePolicy?: string
  concurrent?: string
  status?: string
  createBy?: string
  createTime?: string
  updateBy?: string
  updateTime?: string
  remark?: string
}

/**
 * 定时任务查询参数
 */
export interface SysJobQueryParams {
  pageNum?: number
  pageSize?: number
  jobName?: string
  jobGroup?: string
  status?: string
}

/**
 * 定时任务日志
 */
export interface SysJobLog {
  jobLogId?: number
  jobName?: string
  jobGroup?: string
  invokeTarget?: string
  jobMessage?: string
  status?: string
  exceptionInfo?: string
  createTime?: string
}

/**
 * 任务日志查询参数
 */
export interface SysJobLogQueryParams {
  pageNum?: number
  pageSize?: number
  jobName?: string
  jobGroup?: string
  status?: string
}

/** 分页列表（与后端 PageResponse 一致） */
export interface JobPageResponse<T> {
  rows: T[]
  total: number
  pageNum?: number
  pageSize?: number
  pageCount?: number
}

/** 获取任务列表（分页） */
export function fetchJobList(params: SysJobQueryParams) {
  return request.post<JobPageResponse<SysJob>>({
    url: '/api/monitor/job/list',
    data: params
  })
}

/** 根据ID获取任务详情 */
export function fetchJobById(jobId: number) {
  return request.get<SysJob>({
    url: `/api/monitor/job/${jobId}`
  })
}

/** 新增定时任务 */
export function fetchAddJob(data: SysJob) {
  return request.post({
    url: '/api/monitor/job',
    data
  })
}

/** 修改定时任务 */
export function fetchUpdateJob(data: SysJob) {
  return request.put({
    url: '/api/monitor/job',
    data
  })
}

/** 删除定时任务 */
export function fetchDeleteJob(ids: number[]) {
  return request.del({
    url: '/api/monitor/job',
    data: { ids }
  })
}

/** 修改任务状态（暂停/恢复） */
export function fetchChangeJobStatus(jobId: number, status: string) {
  return request.post({
    url: '/api/monitor/job/changeStatus',
    params: { jobId, status }
  })
}

/** 立即执行一次 */
export function fetchRunJobOnce(jobId: number) {
  return request.put({
    url: `/api/monitor/job/run/${jobId}`
  })
}

/** 任务日志分页列表 */
export function fetchJobLogList(params: SysJobLogQueryParams) {
  return request.post<JobPageResponse<SysJobLog>>({
    url: '/api/monitor/job/log/list',
    data: params
  })
}

/** 清空任务日志 */
export function fetchClearJobLog() {
  return request.del({
    url: '/api/monitor/job/log/clear'
  })
}
