/** 审批模板状态 */
export interface ApTemplate {
  templateId?: number
  templateCode?: string
  templateName?: string
  bizModule?: string
  version?: number
  status?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface ApTemplateStep {
  stepId?: number
  templateId?: number
  stepCode?: string
  stepName?: string
  stepOrder?: number
  assigneeType?: string
  assigneeValue?: string
  approveMode?: string
  skipExpression?: string
  timeoutHours?: number
  timeoutAction?: string
}

export interface ApTemplateRoute {
  routeId?: number
  fromStepCode?: string
  fromStepName?: string
  toStepCode?: string
  toStepName?: string
  priority?: number
  conditionExpr?: string
}

export interface ApTemplateBind {
  bindId?: number
  bizModule?: string
  bizType?: string
  matchExpr?: string
  templateCode?: string
  priority?: number
  status?: string
  createTime?: string
}

export interface ApTaskVo {
  taskId?: number
  instanceId?: number
  title?: string
  bizModule?: string
  bizType?: string
  bizKey?: string
  stepCode?: string
  stepName?: string
  status?: string
  action?: string
  comment?: string
  createTime?: string
  finishTime?: string
}

export interface ApInstanceVo {
  instanceId?: number
  templateCode?: string
  bizModule?: string
  bizType?: string
  bizKey?: string
  title?: string
  starterId?: number
  starterName?: string
  status?: string
  createTime?: string
  finishTime?: string
}

export interface ApprovalTimelineVo {
  instanceId?: number
  title?: string
  status?: string
  bizModule?: string
  bizType?: string
  bizKey?: string
  steps?: TimelineStepVo[]
}

export interface TimelineStepVo {
  stepCode?: string
  stepName?: string
  status?: string
  assignees?: string[]
  records?: TimelineRecordVo[]
}

export interface TimelineRecordVo {
  operatorName?: string
  action?: string
  comment?: string
  time?: string
}

export interface ApTemplateSaveDto {
  templateId?: number
  templateCode?: string
  templateName?: string
  bizModule?: string
  remark?: string
  steps: ApTemplateStep[]
  routes?: ApTemplateRoute[]
}

export interface ApTemplateBindSaveDto {
  bindId?: number
  bizModule?: string
  bizType?: string
  matchExpr?: string
  templateCode?: string
  priority?: number
  status?: string
}

export interface ApTaskActionDto {
  taskId: number
  comment?: string
}

export interface ApprovalSubmitRequest {
  bizModule: string
  bizType: string
  bizKey: string
  title: string
  templateCode?: string
  context?: Record<string, unknown>
}
