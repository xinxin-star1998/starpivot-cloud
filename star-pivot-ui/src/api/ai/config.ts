import request from '@/utils/http'
import type { AiModelOption } from '@/api/ai/chat'

export interface AiConfigItem {
  configId?: number
  configName?: string
  botName?: string
  botAvatar?: string
  welcomeMessage?: string
  systemPrompt?: string
  defaultModel?: string
  defaultTemperature?: number
  maxMemoryMessages?: number
  models?: AiModelOption[]
  ragEnabled?: string
  ragTopK?: number
  isDefault?: string
  status?: string
  remark?: string
  updateBy?: string
  updateTime?: string
}

export interface AiConfigListParams extends Api.Common.CommonSearchParams {
  configName?: string
  botName?: string
  status?: string
}

export interface AiConfigSavePayload {
  configId?: number
  configName: string
  botName: string
  botAvatar?: string
  welcomeMessage?: string
  systemPrompt: string
  defaultModel: string
  defaultTemperature?: number
  maxMemoryMessages?: number
  models?: AiModelOption[]
  ragEnabled?: string
  ragTopK?: number
  isDefault?: string
  status?: string
  remark?: string
}

export function fetchAiConfigList(params: AiConfigListParams) {
  return request.post<Api.Common.PageResponse<AiConfigItem>>({
    url: '/ai/config/pageList',
    data: params
  })
}

export function fetchAiConfigDetail(configId: number) {
  return request.get<AiConfigItem>({
    url: `/ai/config/${configId}`
  })
}

export function fetchAiConfigSave(data: AiConfigSavePayload) {
  return request.post<number>({
    url: '/ai/config/save',
    data
  })
}

export function fetchAiConfigRemove(configId: number) {
  return request.delete<void>({
    url: `/ai/config/${configId}`
  })
}

export function fetchAiConfigSetDefault(configId: number) {
  return request.put<void>({
    url: `/ai/config/${configId}/default`
  })
}
