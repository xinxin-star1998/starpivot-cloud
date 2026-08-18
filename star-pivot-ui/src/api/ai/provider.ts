import request from '@/utils/http'
import type { AiModelOption } from '@/api/ai/chat'

export interface AiProviderModelItem {
  id: string
  label?: string
  kind?: 'chat' | 'embedding' | 'rerank' | string
}

export interface AiProviderItem {
  providerId?: number
  providerCode?: string
  providerName?: string
  baseUrl?: string
  apiKeyMasked?: string
  apiKeyConfigured?: boolean
  completionsPath?: string
  embeddingsPath?: string
  rerankEndpoint?: string
  chatEnabled?: string
  embeddingEnabled?: string
  rerankEnabled?: string
  defaultChatModel?: string
  defaultEmbeddingModel?: string
  defaultRerankModel?: string
  models?: AiProviderModelItem[]
  isDefaultChat?: string
  isDefaultEmbedding?: string
  isDefaultRerank?: string
  status?: string
  remark?: string
  updateBy?: string
  updateTime?: string
}

export interface AiProviderPreset {
  providerCode: string
  providerName: string
  baseUrl?: string
  completionsPath?: string
  embeddingsPath?: string
  rerankEndpoint?: string
  chatEnabled?: string
  embeddingEnabled?: string
  rerankEnabled?: string
  defaultChatModel?: string
  defaultEmbeddingModel?: string
  defaultRerankModel?: string
  models?: AiProviderModelItem[]
  remark?: string
}

export interface AiProviderListParams extends Api.Common.CommonSearchParams {
  providerName?: string
  providerCode?: string
  status?: string
}

export interface AiProviderSavePayload {
  providerId?: number
  providerCode: string
  providerName: string
  baseUrl: string
  apiKey?: string
  completionsPath?: string
  embeddingsPath?: string
  rerankEndpoint?: string
  chatEnabled?: string
  embeddingEnabled?: string
  rerankEnabled?: string
  defaultChatModel?: string
  defaultEmbeddingModel?: string
  defaultRerankModel?: string
  models?: AiProviderModelItem[]
  isDefaultChat?: string
  isDefaultEmbedding?: string
  isDefaultRerank?: string
  status?: string
  remark?: string
}

export function fetchAiProviderList(params: AiProviderListParams) {
  return request.post<Api.Common.PageResponse<AiProviderItem>>({
    url: '/ai/provider/pageList',
    data: params
  })
}

export function fetchAiProviderDetail(providerId: number) {
  return request.get<AiProviderItem>({
    url: `/ai/provider/${providerId}`
  })
}

export function fetchAiProviderPresets() {
  return request.get<AiProviderPreset[]>({
    url: '/ai/provider/presets'
  })
}

export function fetchAiProviderChatModels() {
  return request.get<AiModelOption[]>({
    url: '/ai/provider/chat-models'
  })
}

export function fetchAiProviderSave(data: AiProviderSavePayload) {
  return request.post<number>({
    url: '/ai/provider/save',
    data
  })
}

export function fetchAiProviderRemove(providerId: number) {
  return request.del<void>({
    url: `/ai/provider/${providerId}`
  })
}

export function fetchAiProviderSetDefault(providerId: number, kind = 'chat') {
  return request.put<void>({
    url: `/ai/provider/${providerId}/default?kind=${encodeURIComponent(kind)}`
  })
}

export function fetchAiProviderTest(providerId: number, kind = 'chat') {
  return request.post<string>({
    url: `/ai/provider/${providerId}/test?kind=${encodeURIComponent(kind)}`,
    timeout: 30000
  })
}
