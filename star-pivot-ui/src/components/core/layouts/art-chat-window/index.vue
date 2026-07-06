<!-- 系统聊天窗口 -->
<template>
  <div>
    <ElDrawer
      v-model="isDrawerVisible"
      :size="isMobile ? '100%' : '680px'"
      :with-header="false"
      @keydown.esc="handleEscapeKey"
    >
      <div class="mb-4 flex-cb">
        <div>
          <span class="text-base font-medium">{{ botDisplayName }}</span>
          <div class="mt-1.5 flex-c gap-1">
            <div
              class="h-2 w-2 rounded-full"
              :class="isOnline ? 'bg-success/100' : 'bg-danger/100'"
            ></div>
            <span class="text-xs text-g-600">{{ isOnline ? '在线' : '离线' }}</span>
          </div>
        </div>
        <div class="flex-c gap-2">
          <ElButton
            v-if="isMobile"
            text
            size="small"
            @click="sessionPanelVisible = !sessionPanelVisible"
          >
            历史
          </ElButton>
          <ElButton
            text
            size="small"
            :disabled="sending || !conversationId"
            @click="clearCurrentHistory"
          >
            清空
          </ElButton>
          <ElButton text size="small" :disabled="sending" @click="startNewConversation">
            新对话
          </ElButton>
          <ElIcon class="c-p" :size="20" @click="closeChat">
            <Close />
          </ElIcon>
        </div>
      </div>

      <div class="relative flex h-[calc(100%-64px)] overflow-hidden rounded-lg border border-g-300/50">
        <ChatSessionPanel
          v-if="!isMobile || sessionPanelVisible"
          :class="{ 'absolute inset-y-0 left-0 z-10 bg-white shadow-lg': isMobile }"
          :sessions="sessions"
          :active-id="conversationId"
          @select="switchSession"
          @delete="deleteSession"
          @rename="renameSession"
        />

        <div class="flex min-w-0 flex-1 flex-col">
          <div
            class="flex-1 overflow-y-auto px-4 py-5 [&::-webkit-scrollbar]:!w-1"
            ref="messageContainer"
          >
            <template v-for="message in messages" :key="message.id">
              <div
                :class="[
                  'mb-7.5 flex w-full items-start gap-2',
                  message.isMe ? 'flex-row-reverse' : 'flex-row'
                ]"
              >
                <ArtAvatarDisplay
                  v-if="message.isMe"
                  :key="userAvatarDisplayKey"
                  :avatar-url="userAvatarUrl"
                  :size="32"
                  avatar-class="shrink-0"
                />
                <ArtAvatarDisplay
                  v-else
                  :avatar-url="botAvatarUrl"
                  :size="32"
                  avatar-class="shrink-0"
                />
                <div
                  :class="[
                    'flex min-w-0 flex-col',
                    message.isMe ? 'max-w-[70%] items-end' : 'max-w-[92%] items-start'
                  ]"
                >
                  <div
                    :class="[
                      'mb-1 flex gap-2 text-xs',
                      message.isMe ? 'flex-row-reverse' : 'flex-row'
                    ]"
                  >
                    <span class="font-medium">{{ message.sender }}</span>
                    <span class="text-g-600">{{ message.time }}</span>
                  </div>
                  <div
                    :class="[
                      'w-full rounded-md px-3.5 py-2.5 text-sm text-g-900',
                      message.isMe
                        ? 'message-right bg-theme/15 leading-[1.4]'
                        : 'message-left bg-g-300/50 leading-normal'
                    ]"
                  >
                    <span v-if="message.loading" class="inline-flex items-center gap-1">
                      <span class="animate-pulse">正在思考</span>
                      <span class="animate-bounce">...</span>
                    </span>
                    <ChatMarkdown
                      v-else-if="!message.isMe"
                      :content="message.content"
                      :streaming="!!message.streaming"
                    />
                    <span v-else>{{ message.content }}</span>
                  </div>
                  <div
                    v-if="message.sources?.length"
                    class="mt-2 w-full rounded-md border border-g-300/60 bg-g-100/40 px-3 py-2 text-xs text-g-700"
                  >
                    <div class="mb-1 font-medium text-g-800">参考来源</div>
                    <div
                      v-for="(source, index) in message.sources"
                      :key="`${source.chunkId || index}-${source.docId || ''}`"
                      class="leading-relaxed"
                    >
                      {{ index + 1 }}. {{ source.docTitle || '未知文档'
                      }}<template v-if="source.pageNum">（第{{ source.pageNum }}页）</template>
                      <span v-if="source.snippet"> — {{ source.snippet }}</span>
                    </div>
                  </div>
                  <div
                    v-if="canShowMessageActions(message)"
                    class="mt-1.5 flex flex-wrap gap-2"
                    :class="message.isMe ? 'justify-end' : 'justify-start'"
                  >
                    <ElButton
                      link
                      type="primary"
                      size="small"
                      class="!px-0 !text-xs"
                      @click="copyMessage(message.content)"
                    >
                      复制
                    </ElButton>
                    <ElButton
                      v-if="!message.isMe && message.userPrompt"
                      link
                      type="primary"
                      size="small"
                      class="!px-0 !text-xs"
                      :disabled="sending"
                      @click="regenerateMessage(message)"
                    >
                      重新生成
                    </ElButton>
                  </div>
                </div>
              </div>
            </template>
          </div>

          <div class="border-t border-g-300/50 px-4 py-3">
            <div v-if="modelOptions.length > 1" class="mb-2 flex items-center gap-2">
              <span class="shrink-0 text-xs text-g-500">模型</span>
              <ElSelect
                v-model="selectedModel"
                size="small"
                class="!w-44"
                :disabled="sending"
              >
                <ElOption
                  v-for="model in modelOptions"
                  :key="model.id"
                  :label="model.label"
                  :value="model.id"
                />
              </ElSelect>
            </div>
            <ElInput
              v-model="messageText"
              type="textarea"
              :rows="3"
              placeholder="输入消息，Ctrl+Enter 发送"
              resize="none"
              :disabled="sending"
              @keydown="handleInputKeydown"
            />
            <div class="mt-2 flex-cb">
              <span class="text-[11px] text-g-500">Ctrl+Enter 发送 · Shift+Enter 换行 · Esc 停止</span>
              <div class="flex gap-2">
                <ElButton v-if="sending" type="danger" plain @click="stopGeneration" v-ripple>
                  停止生成
                </ElButton>
                <ElButton
                  v-else
                  type="primary"
                  :disabled="!messageText.trim()"
                  @click="sendMessage"
                  v-ripple
                >
                  发送
                </ElButton>
              </div>
            </div>
          </div>
        </div>
      </div>
    </ElDrawer>
  </div>
</template>

<script setup lang="ts">
import { Close } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { storeToRefs } from 'pinia'
import {
  fetchAiChatClearHistory,
  fetchAiChatCreateSession,
  fetchAiChatDeleteSession,
  fetchAiChatHealth,
  fetchAiChatMessages,
  fetchAiChatRenameSession,
  fetchAiChatSessions,
  fetchAiChatStream,
  isStreamAbortError,
  type AiModelOption,
  type ChatHistoryMessage,
  type ChatSession
} from '@/api/ai/chat'
import { useUserStore } from '@/store/modules/user'
import { isAbortError, stripChatCopyContent } from '@/utils/ai/render-markdown'
import { getConversationStorageKey, getModelStorageKey } from '@/utils/ai/session-storage'
import { mittBus } from '@/utils/sys'
import ArtAvatarDisplay from '@/components/core/media/art-avatar-display/index.vue'
import ChatMarkdown from '@/components/core/layouts/art-chat-window/chat-markdown.vue'
import ChatSessionPanel from '@/components/core/layouts/art-chat-window/chat-session-panel.vue'
import defaultUserAvatar from '@imgs/user/avatar.webp'
import defaultBotAvatar from '@/assets/images/avatar/avatar10.webp'

defineOptions({ name: 'ArtChatWindow' })

interface ChatMessage {
  id: number
  sender: string
  content: string
  time: string
  isMe: boolean
  loading?: boolean
  streaming?: boolean
  stopped?: boolean
  userPrompt?: string
  isWelcome?: boolean
  sources?: import('@/api/ai/chat').RagSourceItem[]
}

const MOBILE_BREAKPOINT = 640
const SCROLL_DELAY = 100
const DEFAULT_BOT_NAME = 'AI 助手'
const DEFAULT_WELCOME_MESSAGE = '你好！我是 **AI 助手**，有什么我可以帮你的吗？'

const { width } = useWindowSize()
const isMobile = computed(() => width.value < MOBILE_BREAKPOINT)
const userStore = useUserStore()
const { getUserInfo: userInfo } = storeToRefs(userStore)

const isDrawerVisible = ref(false)
const sessionPanelVisible = ref(false)
const isOnline = ref(false)
const sending = ref(false)
const messageText = ref('')
const messageId = ref(1)
const conversationId = ref('')
const sessions = ref<ChatSession[]>([])
const messageContainer = ref<HTMLElement | null>(null)
const streamAbortController = ref<AbortController | null>(null)
const botDisplayName = ref(DEFAULT_BOT_NAME)
const botAvatarUrl = ref(defaultBotAvatar)
const welcomeMessage = ref(DEFAULT_WELCOME_MESSAGE)
const modelOptions = ref<AiModelOption[]>([])
const selectedModel = ref('')

const userName = computed(() => {
  const user = userInfo.value?.user
  return user?.nickName || user?.username || '我'
})

const userAvatarUrl = computed(() => {
  const u = userInfo.value as any
  const topAvatar = u?.avatar
  const userAvatar = u?.user?.avatar
  const url = topAvatar && String(topAvatar).trim() ? topAvatar : userAvatar
  return url && String(url).trim() ? url : defaultUserAvatar
})

const userAvatarDisplayKey = computed(() => {
  const u = userInfo.value as any
  const v = u?.avatarUpdatedAt ?? u?.user?.avatarUpdatedAt ?? ''
  return `${String(userAvatarUrl.value)}|${v}`
})

const formatCurrentTime = (): string => {
  return new Date().toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatMessageTime = (timestamp?: number): string => {
  if (!timestamp) {
    return formatCurrentTime()
  }
  return new Date(timestamp).toLocaleTimeString([], {
    hour: '2-digit',
    minute: '2-digit'
  })
}

const createWelcomeMessage = (): ChatMessage => ({
  id: messageId.value++,
  sender: botDisplayName.value,
  content: welcomeMessage.value,
  time: formatCurrentTime(),
  isMe: false,
  isWelcome: true
})

const messages = ref<ChatMessage[]>([createWelcomeMessage()])

const scrollToBottom = (): void => {
  nextTick(() => {
    setTimeout(() => {
      if (messageContainer.value) {
        messageContainer.value.scrollTop = messageContainer.value.scrollHeight
      }
    }, SCROLL_DELAY)
  })
}

const loadConversationId = (): void => {
  const userId = userInfo.value?.user?.userId
  conversationId.value = sessionStorage.getItem(getConversationStorageKey(userId)) || ''
}

const saveConversationId = (id: string): void => {
  conversationId.value = id
  const userId = userInfo.value?.user?.userId
  sessionStorage.setItem(getConversationStorageKey(userId), id)
}

const applyBotProfile = (health?: {
  botAvatar?: string
  botName?: string
  welcomeMessage?: string
  models?: AiModelOption[]
  defaultModel?: string
}): void => {
  if (health?.botName && String(health.botName).trim()) {
    botDisplayName.value = health.botName.trim()
  }
  if (health?.botAvatar && String(health.botAvatar).trim()) {
    botAvatarUrl.value = health.botAvatar.trim()
  } else {
    botAvatarUrl.value = defaultBotAvatar
  }
  if (health?.welcomeMessage && String(health.welcomeMessage).trim()) {
    welcomeMessage.value = health.welcomeMessage.trim()
  }
  if (health?.models?.length) {
    modelOptions.value = health.models
    const userId = userInfo.value?.user?.userId
    const storedModel = sessionStorage.getItem(getModelStorageKey(userId)) || ''
    const defaultModel = health.defaultModel || health.models[0]?.id || ''
    selectedModel.value =
      storedModel && health.models.some((item) => item.id === storedModel)
        ? storedModel
        : defaultModel
  } else {
    modelOptions.value = []
    selectedModel.value = health?.defaultModel || ''
  }
}

watch(selectedModel, (model) => {
  if (model) {
    const userId = userInfo.value?.user?.userId
    sessionStorage.setItem(getModelStorageKey(userId), model)
  }
})

watch(
  () => userInfo.value?.user?.userId,
  () => {
    loadConversationId()
  }
)

const checkHealth = async (): Promise<void> => {
  try {
    const health = await fetchAiChatHealth()
    isOnline.value = !!health?.online
    applyBotProfile(health)
  } catch {
    isOnline.value = false
    ElMessage.warning('AI 服务连接失败')
  }
}

const loadSessions = async (): Promise<void> => {
  try {
    sessions.value = (await fetchAiChatSessions()) || []
  } catch {
    sessions.value = []
    ElMessage.warning('加载对话列表失败')
  }
}

const mapHistoryToMessages = (history: ChatHistoryMessage[]): ChatMessage[] => {
  if (!history.length) {
    return [createWelcomeMessage()]
  }

  const result: ChatMessage[] = []
  let lastUser = ''

  for (const item of history) {
    if (item.role === 'USER') {
      lastUser = item.content
      result.push({
        id: messageId.value++,
        sender: userName.value,
        content: item.content,
        time: formatMessageTime(item.createTime),
        isMe: true
      })
    } else if (item.role === 'ASSISTANT') {
      result.push({
        id: messageId.value++,
        sender: botDisplayName.value,
        content: item.content,
        time: formatMessageTime(item.createTime),
        isMe: false,
        userPrompt: lastUser
      })
    }
  }
  return result
}

const loadConversationMessages = async (targetConversationId: string): Promise<void> => {
  if (!targetConversationId) {
    messages.value = [createWelcomeMessage()]
    return
  }
  try {
    const history = await fetchAiChatMessages(targetConversationId)
    messages.value = mapHistoryToMessages(history || [])
  } catch {
    messages.value = [createWelcomeMessage()]
    ElMessage.warning('加载对话历史失败')
  }
  scrollToBottom()
}

const canShowMessageActions = (message: ChatMessage): boolean => {
  return !message.loading && !message.streaming && !!message.content.trim() && !message.isWelcome
}

const appendStoppedHint = (message: ChatMessage): void => {
  message.stopped = true
  if (message.content.trim()) {
    message.content += '\n\n---\n*已停止生成*'
  } else {
    message.content = '*已停止生成*'
  }
  message.time = formatCurrentTime()
}

const appendTimeoutHint = (message: ChatMessage): void => {
  message.stopped = true
  if (message.content.trim()) {
    message.content += '\n\n---\n*生成超时，请重试*'
  } else {
    message.content = '*生成超时，请重试*'
  }
  message.time = formatCurrentTime()
}

const stopGeneration = (): void => {
  streamAbortController.value?.abort()
}

const handleInputKeydown = (event: KeyboardEvent): void => {
  if (event.key === 'Enter' && (event.ctrlKey || event.metaKey)) {
    event.preventDefault()
    sendMessage()
  }
}

const handleEscapeKey = (): void => {
  if (sending.value) {
    stopGeneration()
  }
}

const copyMessage = async (content: string): Promise<void> => {
  const text = stripChatCopyContent(content)
  if (!text) {
    ElMessage.warning('没有可复制的内容')
    return
  }
  try {
    await navigator.clipboard.writeText(text)
    ElMessage.success('已复制到剪贴板')
  } catch {
    ElMessage.error('复制失败，请手动选择文本')
  }
}

const runAssistantStream = async (
  userText: string,
  botMessage: ChatMessage,
  options?: { regenerate?: boolean }
): Promise<void> => {
  botMessage.sender = botDisplayName.value
  botMessage.loading = true
  botMessage.streaming = false
  botMessage.stopped = false
  botMessage.content = ''
  botMessage.sources = undefined
  botMessage.time = formatCurrentTime()
  botMessage.userPrompt = userText

  sending.value = true
  streamAbortController.value?.abort()
  streamAbortController.value = new AbortController()
  scrollToBottom()

  try {
    await fetchAiChatStream(
      {
        message: userText,
        conversationId: conversationId.value || undefined,
        regenerate: options?.regenerate,
        model: selectedModel.value || undefined
      },
      {
        onMeta: (meta) => {
          if (meta.conversationId) {
            saveConversationId(meta.conversationId)
          }
          if (meta.sources?.length) {
            botMessage.sources = meta.sources
          }
        },
        onDelta: (chunk) => {
          botMessage.loading = false
          botMessage.streaming = true
          botMessage.content += chunk
          scrollToBottom()
        },
        onError: (message) => {
          botMessage.loading = false
          botMessage.streaming = false
          if (!botMessage.content) {
            botMessage.content = message || 'AI 服务暂时不可用，请稍后再试。'
          }
        }
      },
      streamAbortController.value.signal
    )

    botMessage.loading = false
    botMessage.streaming = false
    if (!botMessage.content) {
      botMessage.content = '抱歉，我暂时无法回答这个问题。'
    }
    botMessage.time = formatCurrentTime()
    isOnline.value = true
  } catch (error) {
    botMessage.loading = false
    botMessage.streaming = false

    if (isStreamAbortError(error)) {
      if (error.reason === 'timeout') {
        appendTimeoutHint(botMessage)
      } else {
        appendStoppedHint(botMessage)
      }
      return
    }

    if (isAbortError(error)) {
      appendStoppedHint(botMessage)
      return
    }

    if (!botMessage.content) {
      botMessage.content = 'AI 服务暂时不可用，请稍后再试。'
    }
    botMessage.time = formatCurrentTime()
    isOnline.value = false
    ElMessage.error(error instanceof Error ? error.message : '发送失败')
  } finally {
    streamAbortController.value = null
    sending.value = false
    await loadSessions()
    scrollToBottom()
  }
}

const sendMessage = async (): Promise<void> => {
  const text = messageText.value.trim()
  if (!text || sending.value) return

  messages.value.push({
    id: messageId.value++,
    sender: userName.value,
    content: text,
    time: formatCurrentTime(),
    isMe: true
  })

  const botMessage: ChatMessage = {
    id: messageId.value++,
    sender: botDisplayName.value,
    content: '',
    time: formatCurrentTime(),
    isMe: false,
    loading: true,
    streaming: false
  }

  messages.value.push(botMessage)
  messageText.value = ''
  await runAssistantStream(text, botMessage)
}

const regenerateMessage = async (botMessage: ChatMessage): Promise<void> => {
  if (!botMessage.userPrompt || sending.value) return
  await runAssistantStream(botMessage.userPrompt, botMessage, { regenerate: true })
}

const switchSession = async (targetConversationId: string): Promise<void> => {
  if (sending.value || targetConversationId === conversationId.value) {
    sessionPanelVisible.value = false
    return
  }
  saveConversationId(targetConversationId)
  await loadConversationMessages(targetConversationId)
  sessionPanelVisible.value = false
}

const deleteSession = async (targetConversationId: string): Promise<void> => {
  try {
    await ElMessageBox.confirm('确定删除这条对话吗？', '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  try {
    await fetchAiChatDeleteSession(targetConversationId)
    if (targetConversationId === conversationId.value) {
      await startNewConversation(false)
    } else {
      await loadSessions()
    }
    ElMessage.success('对话已删除')
  } catch {
    ElMessage.error('删除失败')
  }
}

const renameSession = async (payload: { conversationId: string; title: string }): Promise<void> => {
  try {
    await fetchAiChatRenameSession(payload)
    await loadSessions()
    ElMessage.success('已重命名')
  } catch {
    ElMessage.error('重命名失败')
  }
}

const clearCurrentHistory = async (): Promise<void> => {
  if (!conversationId.value || sending.value) return

  try {
    await ElMessageBox.confirm('确定清空当前对话的消息记录吗？', '提示', {
      type: 'warning',
      confirmButtonText: '清空',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }

  try {
    await fetchAiChatClearHistory(conversationId.value)
    messages.value = [createWelcomeMessage()]
    scrollToBottom()
    ElMessage.success('对话已清空')
  } catch {
    ElMessage.error('清空失败')
  }
}

const startNewConversation = async (showToast = true): Promise<void> => {
  if (sending.value) {
    stopGeneration()
  }

  try {
    const session = await fetchAiChatCreateSession()
    saveConversationId(session.conversationId)
    messages.value = [createWelcomeMessage()]
    messageText.value = ''
    await loadSessions()
    scrollToBottom()
    if (showToast) {
      ElMessage.success('已开始新对话')
    }
  } catch {
    ElMessage.error('创建新对话失败')
  }
}

const openChat = async (): Promise<void> => {
  isDrawerVisible.value = true
  await Promise.all([checkHealth(), loadSessions()])
  if (conversationId.value) {
    await loadConversationMessages(conversationId.value)
  } else {
    messages.value = [createWelcomeMessage()]
  }
  scrollToBottom()
}

const closeChat = (): void => {
  isDrawerVisible.value = false
  sessionPanelVisible.value = false
}

onMounted(async () => {
  loadConversationId()
  await checkHealth()
  mittBus.on('openChat', openChat)
})

onUnmounted(() => {
  streamAbortController.value?.abort()
  mittBus.off('openChat', openChat)
})
</script>
