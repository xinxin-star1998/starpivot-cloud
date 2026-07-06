<template>
  <div ref="panelRef" class="chat-session-panel flex h-full flex-col border-r border-g-300/60 bg-g-100/30">
    <div class="border-b border-g-300/60 px-3 py-2.5 text-xs font-medium text-g-600">对话历史</div>
    <div class="flex-1 overflow-y-auto px-2 py-2 [&::-webkit-scrollbar]:!w-1">
      <div
        v-for="session in sessions"
        :key="session.conversationId"
        :class="[
          'group mb-1 flex cursor-pointer items-start gap-1 rounded-md px-2 py-2 transition-colors',
          session.conversationId === activeId ? 'bg-theme/10' : 'hover:bg-g-300/40'
        ]"
        @click="emit('select', session.conversationId)"
      >
        <div class="min-w-0 flex-1">
          <ElTooltip
            :content="sessionTitle(session)"
            placement="top"
            :show-after="200"
            :disabled="!titleOverflow[session.conversationId]"
          >
            <div
              :ref="(el) => setTitleRef(session.conversationId, el as HTMLElement | null)"
              class="truncate text-sm text-g-900"
            >
              {{ sessionTitle(session) }}
            </div>
          </ElTooltip>
          <div class="mt-0.5 text-[11px] text-g-500">{{ formatSessionTime(session.updatedAt) }}</div>
        </div>
        <ElButton
          link
          type="primary"
          class="!px-1 opacity-0 group-hover:opacity-100"
          @click.stop="handleRename(session)"
        >
          <ArtSvgIcon icon="ri:edit-line" class="text-sm" />
        </ElButton>
        <ElButton
          link
          type="danger"
          class="!px-1 opacity-0 group-hover:opacity-100"
          @click.stop="emit('delete', session.conversationId)"
        >
          <ArtSvgIcon icon="ri:delete-bin-line" class="text-sm" />
        </ElButton>
      </div>
      <div v-if="!sessions.length" class="px-2 py-6 text-center text-xs text-g-500">暂无历史对话</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, onUnmounted, reactive, ref, watch } from 'vue'
import { ElMessageBox } from 'element-plus'
import type { ChatSession } from '@/api/ai/chat'

const props = defineProps<{
  sessions: ChatSession[]
  activeId: string
}>()

const emit = defineEmits<{
  select: [conversationId: string]
  delete: [conversationId: string]
  rename: [payload: { conversationId: string; title: string }]
}>()

const titleRefs = new Map<string, HTMLElement>()
const titleOverflow = reactive<Record<string, boolean>>({})
const panelRef = ref<HTMLElement | null>(null)

function sessionTitle(session: ChatSession): string {
  return session.title?.trim() || '新对话'
}

function setTitleRef(conversationId: string, el: HTMLElement | null): void {
  if (el) {
    titleRefs.set(conversationId, el)
    updateTitleOverflow(conversationId)
    return
  }
  titleRefs.delete(conversationId)
  delete titleOverflow[conversationId]
}

function updateTitleOverflow(conversationId: string): void {
  const el = titleRefs.get(conversationId)
  if (!el) {
    delete titleOverflow[conversationId]
    return
  }
  titleOverflow[conversationId] = el.scrollWidth > el.clientWidth
}

function refreshAllTitleOverflow(): void {
  titleRefs.forEach((_el, conversationId) => updateTitleOverflow(conversationId))
}

let resizeObserver: ResizeObserver | null = null

onMounted(() => {
  resizeObserver = new ResizeObserver(() => refreshAllTitleOverflow())
  if (panelRef.value) {
    resizeObserver.observe(panelRef.value)
  }
})

onUnmounted(() => {
  resizeObserver?.disconnect()
  resizeObserver = null
})

watch(
  () => props.sessions,
  async () => {
    await nextTick()
    refreshAllTitleOverflow()
  },
  { deep: true }
)

async function handleRename(session: ChatSession): Promise<void> {
  try {
    const { value } = await ElMessageBox.prompt('请输入新的对话标题', '重命名', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      inputValue: session.title || '新对话',
      inputValidator: (value) => {
        if (!value?.trim()) return '标题不能为空'
        if (value.trim().length > 64) return '标题不能超过64字'
        return true
      }
    })
    emit('rename', {
      conversationId: session.conversationId,
      title: value.trim()
    })
  } catch {
    // cancelled
  }
}

function formatSessionTime(timestamp?: number): string {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const now = new Date()
  const isToday =
    date.getFullYear() === now.getFullYear() &&
    date.getMonth() === now.getMonth() &&
    date.getDate() === now.getDate()
  if (isToday) {
    return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
  }
  return date.toLocaleDateString([], { month: '2-digit', day: '2-digit' })
}
</script>

<style scoped lang="scss">
.chat-session-panel {
  width: 168px;
  flex-shrink: 0;
}
</style>
