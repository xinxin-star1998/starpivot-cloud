<template>
  <div class="ai-session-page art-full-height">
    <ElCard shadow="never" class="search-card">
      <ElForm :inline="true" :model="searchForm">
        <ElFormItem label="用户 ID">
          <ElInputNumber v-model="searchForm.userId" :min="1" controls-position="right" class="!w-36" />
        </ElFormItem>
        <ElFormItem label="会话 ID">
          <ElInput v-model="searchForm.conversationId" clearable placeholder="conversationId" class="!w-56" />
        </ElFormItem>
        <ElFormItem label="标题">
          <ElInput v-model="searchForm.title" clearable placeholder="会话标题" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">查询</ElButton>
          <ElButton @click="resetSearch">重置</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData" />
      <ArtTable
        :columns="columns"
        :data="data"
        :loading="loading"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <ElDrawer v-model="messageDrawerVisible" size="520px" title="会话消息">
      <div v-if="messageLoading" class="py-10 text-center text-g-500">加载中...</div>
      <div v-else-if="!sessionMessages.length" class="py-10 text-center text-g-500">暂无消息</div>
      <div v-else class="space-y-4">
        <div
          v-for="(item, index) in sessionMessages"
          :key="index"
          class="rounded-md border border-g-300/60 p-3"
        >
          <div class="mb-1 flex items-center justify-between text-xs text-g-500">
            <span>{{ item.role === 'USER' ? '用户' : '助手' }}</span>
            <span>{{ formatMessageTime(item.createTime) }}</span>
          </div>
          <div class="whitespace-pre-wrap break-words text-sm text-g-900">{{ item.content }}</div>
        </div>
      </div>
    </ElDrawer>
  </div>
</template>

<script lang="ts" setup>
import { h } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useTable } from '@/hooks/core/useTable'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {
  fetchAiSessionAdminList,
  fetchAiSessionAdminMessages,
  fetchAiSessionAdminRemove,
  type AiChatSessionAdminItem
} from '@/api/ai/session'
import type { ChatHistoryMessage } from '@/api/ai/chat'
import { handleMutationError } from '@/utils/http/mutation'

defineOptions({ name: 'AiSession' })

const searchForm = ref<{ userId?: number; conversationId: string; title: string }>({
  conversationId: '',
  title: ''
})

const messageDrawerVisible = ref(false)
const messageLoading = ref(false)
const sessionMessages = ref<ChatHistoryMessage[]>([])

const {
  columns,
  columnChecks,
  data,
  loading,
  pagination,
  searchParams,
  getData,
  handleSizeChange,
  handleCurrentChange,
  refreshData
} = useTable({
  core: {
    apiFn: fetchAiSessionAdminList,
    apiParams: { pageNum: 1, pageSize: 20, ...searchForm.value },
    columnsFactory: () => [
      { type: 'index', width: 60, label: '序号' },
      { prop: 'sessionId', label: 'ID', width: 80 },
      { prop: 'userId', label: '用户 ID', width: 90 },
      { prop: 'conversationId', label: '会话 ID', minWidth: 180 },
      { prop: 'title', label: '标题', minWidth: 140 },
      { prop: 'messageCount', label: '消息数', width: 80 },
      { prop: 'updateTime', label: '更新时间', minWidth: 160 },
      {
        prop: 'actions',
        label: '操作',
        width: 160,
        fixed: 'right',
        formatter: (row: AiChatSessionAdminItem) =>
          h('div', { class: 'flex gap-2' }, [
            h(
              'a',
              { class: 'text-primary cursor-pointer', onClick: () => openMessages(row) },
              '查看'
            ),
            h(
              'a',
              { class: 'text-danger cursor-pointer', onClick: () => handleDelete(row) },
              '删除'
            )
          ])
      }
    ]
  }
})

function handleSearch(): void {
  Object.assign(searchParams, searchForm.value)
  getData()
}

function resetSearch(): void {
  searchForm.value = { conversationId: '', title: '' }
  handleSearch()
}

async function openMessages(row: AiChatSessionAdminItem): Promise<void> {
  if (!row.conversationId) return
  messageDrawerVisible.value = true
  messageLoading.value = true
  sessionMessages.value = []
  try {
    sessionMessages.value = (await fetchAiSessionAdminMessages(row.conversationId)) || []
  } catch (error) {
    handleMutationError(error, '加载消息失败')
  } finally {
    messageLoading.value = false
  }
}

async function handleDelete(row: AiChatSessionAdminItem): Promise<void> {
  if (!row.sessionId) return
  try {
    await ElMessageBox.confirm(`确定删除会话「${row.title || row.conversationId}」吗？`, '提示', {
      type: 'warning',
      confirmButtonText: '删除',
      cancelButtonText: '取消'
    })
  } catch {
    return
  }
  try {
    await fetchAiSessionAdminRemove(row.sessionId)
    ElMessage.success('删除成功')
    await refreshData()
  } catch (error) {
    handleMutationError(error, '删除失败')
  }
}

function formatMessageTime(timestamp?: number): string {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleString()
}
</script>

<style scoped lang="scss">
.ai-session-page {
  .search-card {
    margin-bottom: 12px;
  }
}
</style>
