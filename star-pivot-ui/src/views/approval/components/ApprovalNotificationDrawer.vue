<template>
  <ElDrawer v-model="visible" destroy-on-close size="420px" title="我的消息">
    <div class="notify-toolbar">
      <span class="notify-hint">未读 {{ unreadCount }} 条</span>
      <ElButton link type="primary" @click="handleReadAll">全部已读</ElButton>
    </div>
    <ElSkeleton v-if="loading" :rows="6" animated />
    <template v-else>
      <div v-if="!items.length" class="notify-empty">暂无消息</div>
      <ul v-else class="notify-list">
        <li
          v-for="item in items"
          :key="item.messageId"
          :class="{ unread: item.readFlag === '0' }"
          class="notify-item"
          @click="handleClick(item)"
        >
          <div class="notify-title">{{ item.title }}</div>
          <div class="notify-content">{{ item.content }}</div>
          <div class="notify-time">{{ formatDateTime(item.createTime) }}</div>
        </li>
      </ul>
    </template>
  </ElDrawer>
</template>

<script lang="ts" setup>
import { useRouter } from 'vue-router'
import {
  fetchUserMessageList,
  fetchUserMessageRead,
  fetchUserMessageReadAll,
  fetchUserMessageUnreadCount,
  type UserMessageVo
} from '@/api/system/message'
import { formatDateTime } from '@/utils/common/datetime'
import { resolveMessageBizNav } from '@/utils/system/message-nav'

const visible = defineModel<boolean>('visible', { default: false })
const emit = defineEmits<{
  navigate: [instanceId?: number]
  'unread-change': [count: number]
}>()

const router = useRouter()
const loading = ref(false)
const items = ref<UserMessageVo[]>([])
const unreadCount = ref(0)

async function loadData() {
  loading.value = true
  try {
    const [count, page] = await Promise.all([
      fetchUserMessageUnreadCount(),
      fetchUserMessageList({ pageNum: 1, pageSize: 30 })
    ])
    unreadCount.value = Number(count) || 0
    items.value = page.rows || []
    emit('unread-change', unreadCount.value)
  } finally {
    loading.value = false
  }
}

watch(visible, (open) => {
  if (open) loadData()
})

async function handleClick(item: UserMessageVo) {
  if (item.messageId && item.readFlag === '0') {
    await fetchUserMessageRead(item.messageId)
    item.readFlag = '1'
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    emit('unread-change', unreadCount.value)
  }
  visible.value = false
  if (item.bizId) {
    emit('navigate', item.bizId)
    return
  }
  const nav = resolveMessageBizNav(item.bizModule, item.bizType, item.bizKey, item.linkPath)
  if (nav) {
    await router.push({ path: nav.path, query: nav.query })
  }
}

async function handleReadAll() {
  await fetchUserMessageReadAll()
  unreadCount.value = 0
  items.value = items.value.map((item) => ({ ...item, readFlag: '1' }))
  emit('unread-change', 0)
}

defineExpose({ loadData })
</script>

<style scoped>
.notify-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.notify-hint {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.notify-empty {
  padding: 32px 0;
  text-align: center;
  color: var(--el-text-color-secondary);
}

.notify-list {
  list-style: none;
  margin: 0;
  padding: 0;
}

.notify-item {
  padding: 12px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
  cursor: pointer;
}

.notify-item.unread .notify-title {
  font-weight: 600;
}

.notify-title {
  font-size: 14px;
  margin-bottom: 4px;
}

.notify-content {
  font-size: 13px;
  color: var(--el-text-color-regular);
  margin-bottom: 4px;
}

.notify-time {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}
</style>
