import request from '@/utils/http'
import { useUserStore } from '@/store/modules/user'
import { buildAbsoluteApiUrl, normalizeRequestUrl } from '@/utils/http/api-path'
import { getApiBaseUrl } from '@/utils/http/index'

export const AI_AUTO_ROUTE = 'auto'

export interface ChatSendPayload {
  message: string
  conversationId?: string
  regenerate?: boolean
  model?: string
  temperature?: number
  /** 对话场景，对应 health.promptTemplates[].id；传 auto 或省略表示自动路由 */
  promptScene?: string
}

export interface AiPromptSceneOption {
  id: string
  label: string
  description?: string
}

export interface ChatReply {
  conversationId: string
  reply: string
  sources?: RagSourceItem[]
}

export interface RagSourceItem {
  chunkId?: number
  docId?: number
  docTitle?: string
  snippet?: string
  score?: number
  pageNum?: number
}

export interface AiModelOption {
  id: string
  label: string
}

export interface AiHealthStatus {
  online: boolean
  message: string
  botAvatar?: string
  botName?: string
  welcomeMessage?: string
  models?: AiModelOption[]
  defaultModel?: string
  defaultTemperature?: number
  maxMemoryMessages?: number
  promptTemplates?: AiPromptSceneOption[]
  defaultPromptScene?: string
  queryRouterEnabled?: boolean
}

export interface AiChatStreamHandlers {
  onMeta?: (meta: {
    conversationId: string
    sources?: RagSourceItem[]
    promptScene?: string
    model?: string
    routed?: boolean
    intent?: string
    ragDegraded?: boolean
  }) => void
  onStatus?: (status: { phase: string; message: string }) => void
  onDelta?: (chunk: string) => void
  onDone?: () => void
  onError?: (message: string) => void
}

export interface ChatSession {
  conversationId: string
  title: string
  updatedAt: number
}

export interface ChatHistoryMessage {
  role: 'USER' | 'ASSISTANT' | string
  content: string
  createTime?: number
}

export interface SessionRenamePayload {
  conversationId: string
  title: string
}

const AI_REQUEST_TIMEOUT = 120000

export class StreamAbortError extends Error {
  readonly reason: 'timeout' | 'user'

  constructor(reason: 'timeout' | 'user') {
    super(reason === 'timeout' ? '生成超时，请重试' : '已停止生成')
    this.name = 'StreamAbortError'
    this.reason = reason
  }
}

export function isStreamAbortError(error: unknown): error is StreamAbortError {
  return error instanceof StreamAbortError
}

function buildAiStreamUrl(): string {
  const baseUrl = getApiBaseUrl()
  const path = normalizeRequestUrl('/api/ai/chat/stream', baseUrl)
  return buildAbsoluteApiUrl(path, baseUrl)
}

function parseSseBlock(
  block: string,
  handlers: AiChatStreamHandlers
): 'done' | 'continue' | { error: string } {
  const lines = block.split('\n')
  let eventName = 'message'
  const dataLines: string[] = []

  for (const line of lines) {
    if (line.startsWith('event:')) {
      eventName = line.slice(6).trim()
    } else if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).trimStart())
    }
  }

  const data = dataLines.join('\n')

  if (eventName === 'meta' && data) {
    try {
      const meta = JSON.parse(data) as {
        conversationId?: string
        sources?: RagSourceItem[]
        promptScene?: string
        model?: string
        routed?: boolean
        intent?: string
        ragDegraded?: boolean
      }
      if (meta.conversationId) {
        handlers.onMeta?.({
          conversationId: meta.conversationId,
          sources: meta.sources,
          promptScene: meta.promptScene,
          model: meta.model,
          routed: meta.routed,
          intent: meta.intent,
          ragDegraded: meta.ragDegraded
        })
      }
    } catch {
      // ignore malformed meta
    }
    return 'continue'
  }

  if (eventName === 'status' && data) {
    try {
      const status = JSON.parse(data) as { phase?: string; message?: string }
      if (status.message) {
        handlers.onStatus?.({
          phase: status.phase || 'preparing',
          message: status.message
        })
      }
    } catch {
      // ignore malformed status
    }
    return 'continue'
  }

  if (eventName === 'delta' && data) {
    handlers.onDelta?.(data)
    return 'continue'
  }

  if (eventName === 'error') {
    const message = data || 'AI 生成失败'
    handlers.onError?.(message)
    return { error: message }
  }

  if (eventName === 'done') {
    handlers.onDone?.()
    return 'done'
  }

  return 'continue'
}

export async function fetchAiChatStream(
  payload: ChatSendPayload,
  handlers: AiChatStreamHandlers,
  signal?: AbortSignal
): Promise<void> {
  const userStore = useUserStore()
  const token = userStore.accessToken
  if (!token) {
    throw new Error('请先登录后再使用 AI 对话')
  }

  const controller = new AbortController()
  let abortReason: 'timeout' | 'user' | null = null
  const timeoutId = window.setTimeout(() => {
    abortReason = 'timeout'
    controller.abort()
  }, AI_REQUEST_TIMEOUT)
  if (signal) {
    signal.addEventListener(
      'abort',
      () => {
        if (abortReason !== 'timeout') {
          abortReason = 'user'
        }
        controller.abort()
      },
      { once: true }
    )
  }

  try {
    const response = await fetch(buildAiStreamUrl(), {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
        Accept: 'text/event-stream'
      },
      body: JSON.stringify(payload),
      signal: controller.signal
    })

    if (!response.ok) {
      let message = '流式请求失败'
      try {
        const errorBody = (await response.json()) as { message?: string }
        if (errorBody?.message) {
          message = errorBody.message
        }
      } catch {
        // ignore non-json error body
      }
      throw new Error(message)
    }

    const reader = response.body?.getReader()
    if (!reader) {
      throw new Error('无法读取 AI 流式响应')
    }

    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const parts = buffer.split('\n\n')
      buffer = parts.pop() || ''

      for (const part of parts) {
        const result = parseSseBlock(part, handlers)
        if (result === 'done') {
          return
        }
        if (typeof result === 'object' && 'error' in result) {
          throw new Error(result.error)
        }
      }
    }

    if (buffer.trim()) {
      const result = parseSseBlock(buffer, handlers)
      if (result === 'done') {
        return
      }
      if (typeof result === 'object' && 'error' in result) {
        throw new Error(result.error)
      }
    }

    handlers.onDone?.()
  } catch (error) {
    if (controller.signal.aborted) {
      throw new StreamAbortError(abortReason === 'timeout' ? 'timeout' : 'user')
    }
    throw error
  } finally {
    window.clearTimeout(timeoutId)
  }
}

export function fetchAiChatSend(data: ChatSendPayload) {
  return request.post<ChatReply>({
    url: '/ai/chat/send',
    data,
    timeout: AI_REQUEST_TIMEOUT
  })
}

export function fetchAiChatHealth() {
  return request.get<AiHealthStatus>({
    url: '/ai/chat/health'
  })
}

export function fetchAiChatClearHistory(conversationId?: string) {
  return request.del<void>({
    url: '/ai/chat/history',
    params: conversationId ? { conversationId } : undefined
  })
}

export function fetchAiChatCreateSession() {
  return request.post<ChatSession>({
    url: '/ai/chat/sessions'
  })
}

export function fetchAiChatSessions() {
  return request.get<ChatSession[]>({
    url: '/ai/chat/sessions'
  })
}

export function fetchAiChatRenameSession(data: SessionRenamePayload) {
  return request.put<ChatSession>({
    url: '/ai/chat/sessions/rename',
    data
  })
}

export function fetchAiChatMessages(conversationId: string) {
  return request.get<ChatHistoryMessage[]>({
    url: '/ai/chat/messages',
    params: { conversationId }
  })
}

export function fetchAiChatDeleteSession(conversationId: string) {
  return request.del<void>({
    url: '/ai/chat/sessions',
    params: { conversationId }
  })
}
