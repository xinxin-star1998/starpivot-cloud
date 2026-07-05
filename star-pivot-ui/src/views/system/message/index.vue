<!-- 消息中心：收件箱列表 + 筛选 + 分页 -->
<template>
  <div class="message-center-page art-full-height">
    <MessageSearch
      v-model="searchForm"
      @search="handleSearch"
      @reset="handleResetSearch"
    />

    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElTag v-if="unreadCount > 0" effect="plain" type="danger">未读 {{ unreadCount }}</ElTag>
            <ElButton v-auth="'system:message:send'" type="warning" @click="broadcastVisible = true">
              群发消息
            </ElButton>
            <ElButton :disabled="unreadCount <= 0" @click="handleReadAll">全部已读</ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :columns="columns"
        :data="data"
        :loading="loading"
        :pagination="pagination"
        :pagination-options="{ hideOnSinglePage: false }"
        :row-class-name="rowClassName"
        @pagination:current-change="handleCurrentChange"
        @pagination:size-change="handleSizeChange"
        @row-click="handleRowClick"
      />
    </ElCard>

    <BroadcastDialog v-model:visible="broadcastVisible" @success="handleBroadcastSuccess" />
  </div>
</template>

<script lang="ts" setup>
import { h } from 'vue'
import { useRouter } from 'vue-router'
import { ElTag } from 'element-plus'
import { useTable } from '@/hooks/core/useTable'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {
  fetchUserMessageList,
  fetchUserMessageRead,
  fetchUserMessageReadAll,
  fetchUserMessageUnreadCount,
  type UserMessageVo
} from '@/api/system/message'
import { formatDateTime } from '@/utils/common/datetime'
import { MSG_TYPE_LABEL, resolveMessageBizNav } from '@/utils/system/message-nav'
import BroadcastDialog from './modules/broadcast-dialog.vue'
import MessageSearch from './modules/message-search.vue'

defineOptions({ name: 'MessageCenter' })

const router = useRouter()
const unreadCount = ref(0)
const broadcastVisible = ref(false)
const searchForm = ref<{ readFlag?: string; msgType?: string }>({
  readFlag: undefined,
  msgType: undefined
})

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
  refreshData,
  resetSearchParams
} = useTable({
  core: {
    apiFn: fetchUserMessageList,
    immediate: false,
    apiParams: {
      pageNum: 1,
      pageSize: 10,
      readFlag: undefined as string | undefined,
      msgType: undefined as string | undefined
    },
    columnsFactory: () => [
      { type: 'globalIndex', width: 60, label: '序号' },
      {
        prop: 'msgType',
        label: '类型',
        width: 110,
        formatter: (row: UserMessageVo) =>
          h(ElTag, { size: 'small', effect: 'plain' }, () => typeLabel(row.msgType))
      },
      {
        prop: 'title',
        label: '标题',
        minWidth: 180,
        showOverflowTooltip: true,
        formatter: (row: UserMessageVo) =>
          h(
            'span',
            { class: row.readFlag === '0' ? 'msg-title is-unread' : 'msg-title' },
            row.title || '-'
          )
      },
      {
        prop: 'content',
        label: '内容',
        minWidth: 220,
        showOverflowTooltip: true
      },
      {
        prop: 'readFlag',
        label: '状态',
        width: 90,
        formatter: (row: UserMessageVo) =>
          h(
            ElTag,
            { size: 'small', type: row.readFlag === '0' ? 'danger' : 'info' },
            () => (row.readFlag === '0' ? '未读' : '已读')
          )
      },
      {
        prop: 'createTime',
        label: '时间',
        width: 170,
        formatter: (row: UserMessageVo) => formatDateTime(row.createTime)
      }
    ]
  }
})

function typeLabel(msgType?: string) {
  return (msgType && MSG_TYPE_LABEL[msgType]) || msgType || '消息'
}

function rowClassName({ row }: { row: UserMessageVo }) {
  return row.readFlag === '0' ? 'message-row-unread' : ''
}

async function loadUnreadCount() {
  unreadCount.value = Number(await fetchUserMessageUnreadCount()) || 0
}

function handleSearch(params: { readFlag?: string; msgType?: string }) {
  searchParams.readFlag = params.readFlag || undefined
  searchParams.msgType = params.msgType || undefined
  pagination.current = 1
  getData()
}

async function handleResetSearch() {
  searchForm.value = { readFlag: undefined, msgType: undefined }
  await resetSearchParams()
}

async function handleRowClick(row: UserMessageVo) {
  if (row.messageId && row.readFlag === '0') {
    await fetchUserMessageRead(row.messageId)
    row.readFlag = '1'
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  const nav = resolveMessageBizNav(row.bizModule, row.bizType, row.bizKey, row.linkPath)
  if (nav) {
    await router.push({ path: nav.path, query: nav.query })
  }
}

async function handleReadAll() {
  await fetchUserMessageReadAll()
  unreadCount.value = 0
  refreshData()
}

async function handleBroadcastSuccess() {
  await refreshData()
  await loadUnreadCount()
}

onMounted(async () => {
  await getData()
  await loadUnreadCount()
})
</script>

<style scoped>
:deep(.art-table .el-table__row) {
  cursor: pointer;
}

:deep(.message-row-unread) {
  --el-table-tr-bg-color: color-mix(in srgb, var(--el-color-primary) 6%, var(--el-bg-color));
}

:deep(.msg-title.is-unread) {
  font-weight: 600;
  color: var(--el-text-color-primary);
}
</style>
