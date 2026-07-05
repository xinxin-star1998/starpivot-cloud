import { useUserStore } from '@/store/modules/user'
import type { MessagePushPayload } from '@/api/system/message'
import { logger } from '@/utils/sys/logger'

/**
 * SSE 站内消息实时推送（fetch + Authorization，兼容网关 JWT）。
 */
export function useMessageSse(onPush: (payload: MessagePushPayload) => void) {
  let abortController: AbortController | null = null
  let reconnectTimer: ReturnType<typeof setTimeout> | null = null

  function parseBlock(block: string) {
    const lines = block.split('\n')
    let eventName = 'message'
    let data = ''
    for (const line of lines) {
      if (line.startsWith('event:')) {
        eventName = line.slice(6).trim()
      } else if (line.startsWith('data:')) {
        data += line.slice(5).trim()
      }
    }
    if (eventName === 'message' && data) {
      try {
        onPush(JSON.parse(data) as MessagePushPayload)
      } catch (error) {
        logger.warn('[MessageSSE] parse failed', error)
      }
    }
  }

  async function readStream(response: Response) {
    const reader = response.body?.getReader()
    if (!reader) return
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const parts = buffer.split('\n\n')
      buffer = parts.pop() || ''
      parts.forEach(parseBlock)
    }
  }

  async function connect() {
    const userStore = useUserStore()
    if (!userStore.isLogin || !userStore.accessToken) {
      return
    }
    disconnect(false)
    abortController = new AbortController()
    try {
      const response = await fetch('/api/message/sse/subscribe', {
        method: 'GET',
        headers: {
          Authorization: `Bearer ${userStore.accessToken}`,
          Accept: 'text/event-stream'
        },
        signal: abortController.signal
      })
      if (!response.ok || !response.body) {
        scheduleReconnect()
        return
      }
      await readStream(response)
      scheduleReconnect()
    } catch (error) {
      if ((error as Error).name !== 'AbortError') {
        scheduleReconnect()
      }
    }
  }

  function scheduleReconnect() {
    if (reconnectTimer) return
    reconnectTimer = setTimeout(() => {
      reconnectTimer = null
      connect()
    }, 5000)
  }

  function disconnect(clearReconnect = true) {
    abortController?.abort()
    abortController = null
    if (clearReconnect && reconnectTimer) {
      clearTimeout(reconnectTimer)
      reconnectTimer = null
    }
  }

  return { connect, disconnect }
}
