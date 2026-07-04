<template>
  <ElDrawer v-model="visible" destroy-on-close size="420px" title="审批通知">
    <div class="notify-toolbar">
      <span class="notify-hint">未读 {{ unreadCount }} 条</span>
      <ElButton link type="primary" @click="handleReadAll">全部已读</ElButton>
    </div>
    <ElSkeleton v-if="loading" :rows="6" animated />
    <template v-else>
      <div v-if="!items.length" class="notify-empty">暂无通知</div>
      <ul v-else class="notify-list">
        <li
          v-for="item in items"
          :key="item.notifyId"
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
import {
  type ApNotificationVo,
  fetchApprovalNotificationList,
  fetchApprovalNotificationRead,
  fetchApprovalNotificationReadAll,
  fetchApprovalUnreadCount
} from '@/api/approval/notification'
import {formatDateTime} from '@/utils/common/datetime'

const visible = defineModel<boolean>('visible', { default: false })
  const emit = defineEmits<{
    navigate: [instanceId?: number]
    'unread-change': [count: number]
  }>()

  const loading = ref(false)
  const items = ref<ApNotificationVo[]>([])
  const unreadCount = ref(0)

  async function loadData() {
    loading.value = true
    try {
      const [count, page] = await Promise.all([
        fetchApprovalUnreadCount(),
        fetchApprovalNotificationList({ pageNum: 1, pageSize: 30 })
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

  async function handleClick(item: ApNotificationVo) {
    if (item.notifyId && item.readFlag === '0') {
      await fetchApprovalNotificationRead(item.notifyId)
      item.readFlag = '1'
      unreadCount.value = Math.max(0, unreadCount.value - 1)
      emit('unread-change', unreadCount.value)
    }
    visible.value = false
    emit('navigate', item.instanceId)
  }

  async function handleReadAll() {
    await fetchApprovalNotificationReadAll()
    unreadCount.value = 0
    items.value = items.value.map((item) => ({ ...item, readFlag: '1' }))
    emit('unread-change', 0)
  }

  defineExpose({ refresh: loadData })
</script>

<style lang="scss" scoped>
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
    padding: 48px 0;
    text-align: center;
    color: var(--el-text-color-secondary);
  }

  .notify-list {
    padding: 0;
    margin: 0;
    list-style: none;
  }

  .notify-item {
    padding: 12px;
    margin-bottom: 8px;
    cursor: pointer;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;

    &.unread {
      background: var(--el-color-primary-light-9);
    }

    &:hover {
      border-color: var(--el-color-primary-light-5);
    }
  }

  .notify-title {
    font-size: 14px;
    font-weight: 500;
  }

  .notify-content {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .notify-time {
    margin-top: 6px;
    font-size: 12px;
    color: var(--el-text-color-placeholder);
  }
</style>
