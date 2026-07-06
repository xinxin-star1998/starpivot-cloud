import request from '@/utils/http'

export interface AiKnowledgeBaseItem {
  kbId?: number
  kbName?: string
  description?: string
  topK?: number
  chunkSize?: number
  chunkOverlap?: number
  status?: string
  updateTime?: string
}

export interface AiKnowledgeDocumentItem {
  docId?: number
  kbId?: number
  title?: string
  content?: string
  chunkCount?: number
  status?: string
  sourceType?: string
  originalFileName?: string
  fileType?: string
  fileSize?: number
  indexStatus?: string
  errorMsg?: string
  indexedAt?: string
  updateBy?: string
  updateTime?: string
}

export interface AiKnowledgeBaseListParams extends Api.Common.CommonSearchParams {
  kbName?: string
  status?: string
}

export interface AiKnowledgeBaseSavePayload {
  kbId?: number
  kbName: string
  description?: string
  topK?: number
  chunkSize?: number
  chunkOverlap?: number
  status?: string
}

export interface AiKnowledgeDocumentListParams extends Api.Common.CommonSearchParams {
  kbId: number
  title?: string
  status?: string
}

export interface AiKnowledgeDocumentSavePayload {
  docId?: number
  kbId: number
  title: string
  content: string
  status?: string
}

export function fetchAiKnowledgeBaseList(params: AiKnowledgeBaseListParams) {
  return request.post<Api.Common.PageResponse<AiKnowledgeBaseItem>>({
    url: '/ai/knowledge/base/pageList',
    data: params
  })
}

export function fetchAiKnowledgeBaseSave(data: AiKnowledgeBaseSavePayload) {
  return request.post<number>({
    url: '/ai/knowledge/base/save',
    data
  })
}

export function fetchAiKnowledgeBaseRemove(kbId: number) {
  return request.delete<void>({
    url: `/ai/knowledge/base/${kbId}`
  })
}

export function fetchAiKnowledgeDocumentList(params: AiKnowledgeDocumentListParams) {
  return request.post<Api.Common.PageResponse<AiKnowledgeDocumentItem>>({
    url: '/ai/knowledge/document/pageList',
    data: params
  })
}

export function fetchAiKnowledgeDocumentDetail(docId: number) {
  return request.get<AiKnowledgeDocumentItem>({
    url: `/ai/knowledge/document/${docId}`
  })
}

export function fetchAiKnowledgeDocumentSave(data: AiKnowledgeDocumentSavePayload) {
  return request.post<number>({
    url: '/ai/knowledge/document/save',
    data
  })
}

export function fetchAiKnowledgeDocumentRemove(docId: number) {
  return request.delete<void>({
    url: `/ai/knowledge/document/${docId}`
  })
}

export function fetchAiKnowledgeDocumentReindex(docId: number) {
  return request.post<void>({
    url: `/ai/knowledge/document/${docId}/reindex`
  })
}

export function fetchAiKnowledgeDocumentUpload(kbId: number, file: File) {
  const formData = new FormData()
  formData.append('kbId', String(kbId))
  formData.append('file', file)
  return request.post<number>({
    url: '/ai/knowledge/document/upload',
    data: formData,
    timeout: 120000
  })
}
