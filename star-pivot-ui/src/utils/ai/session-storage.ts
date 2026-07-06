const CONVERSATION_PREFIX = 'starpivot-ai-conversation-id'
const MODEL_PREFIX = 'starpivot-ai-selected-model'
const AI_STORAGE_PREFIX = 'starpivot-ai-'

export function getConversationStorageKey(userId?: string | number | null): string {
  if (userId != null && userId !== '') {
    return `${CONVERSATION_PREFIX}:${userId}`
  }
  return CONVERSATION_PREFIX
}

export function getModelStorageKey(userId?: string | number | null): string {
  if (userId != null && userId !== '') {
    return `${MODEL_PREFIX}:${userId}`
  }
  return MODEL_PREFIX
}

/** 登出或切换账号时清理 AI 相关 sessionStorage */
export function clearAiSessionStorage(): void {
  for (let i = sessionStorage.length - 1; i >= 0; i--) {
    const key = sessionStorage.key(i)
    if (key?.startsWith(AI_STORAGE_PREFIX)) {
      sessionStorage.removeItem(key)
    }
  }
}
