<!-- 评论管理 -->
<template>
  <div class="cms-comment-page art-full-height">
    <CommentSearch v-model="searchForm" @search="handleSearch" @reset="handleReset" />

    <ElCard class="art-table-card" shadow="never" style="margin-top: 12px">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData" />

      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <CommentDetailDrawer
      v-model:visible="detailVisible"
      :comment-id="currentCommentId"
      @replied="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
import {h} from 'vue'
import {useTable} from '@/hooks/core/useTable'
import {
  COMMENT_SHOW_STATUS_MAP,
  type CommentVo,
  fetchCommentList,
  fetchCommentRemove,
  fetchCommentUpdateShowStatus
} from '@/api/mall/comment'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import CommentSearch from './modules/comment-search.vue'
import CommentDetailDrawer from './modules/comment-detail-drawer.vue'
import {ElButton, ElMessageBox, ElRate, ElSpace, ElTag} from 'element-plus'
import {useAuth} from '@/hooks/core/useAuth'

defineOptions({ name: 'CmsCommentManage' })

  const { hasAuth } = useAuth()

  const searchForm = ref({
    spuId: undefined as number | undefined,
    memberNickName: undefined as string | undefined,
    showStatus: undefined as number | undefined,
    star: undefined as number | undefined
  })

  const detailVisible = ref(false)
  const currentCommentId = ref<number>()

  function normalizeParams(params: Record<string, unknown>) {
    const next = { ...params }
    const rawSpuId = next.spuId
    if (rawSpuId === '' || rawSpuId == null) {
      delete next.spuId
    } else {
      const spuId = Number(rawSpuId)
      if (Number.isFinite(spuId) && spuId > 0) next.spuId = spuId
      else delete next.spuId
    }
    return next
  }

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    getData,
    searchParams,
    resetSearchParams,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: fetchCommentList,
      apiParams: {
        pageNum: 1,
        pageSize: 10,
        ...searchForm.value
      },
      columnsFactory: () => [
        { prop: 'id', label: 'ID', width: 70 },
        { prop: 'spuName', label: '商品', minWidth: 140, showOverflowTooltip: true },
        { prop: 'memberNickName', label: '会员', width: 110, showOverflowTooltip: true },
        {
          prop: 'star',
          label: '评分',
          width: 130,
          formatter: (row) => h(ElRate, { modelValue: row.star ?? 0, disabled: true })
        },
        {
          prop: 'showStatus',
          label: '状态',
          width: 90,
          formatter: (row) =>
            h(
              ElTag,
              { type: row.showStatus === 1 ? 'success' : 'info', size: 'small' },
              () => COMMENT_SHOW_STATUS_MAP[row.showStatus ?? 0] ?? row.showStatus
            )
        },
        { prop: 'content', label: '内容', minWidth: 200, showOverflowTooltip: true },
        { prop: 'createTime', label: '时间', minWidth: 160 },
        {
          prop: 'operation',
          label: '操作',
          width: 220,
          fixed: 'right',
          formatter: (row: CommentVo) => {
            const actions: ReturnType<typeof h>[] = []
            if (hasAuth('mall:comment:query')) {
              actions.push(
                h(
                  ElButton,
                  { link: true, type: 'primary', onClick: () => openDetail(row.id) },
                  () => '详情'
                )
              )
            }
            if (hasAuth('mall:comment:edit') && row.id != null) {
              const nextStatus = row.showStatus === 1 ? 0 : 1
              actions.push(
                h(
                  ElButton,
                  { link: true, type: 'warning', onClick: () => toggleShow(row) },
                  () => (nextStatus === 1 ? '显示' : '隐藏')
                )
              )
            }
            if (hasAuth('mall:comment:delete')) {
              actions.push(
                h(
                  ElButton,
                  { link: true, type: 'danger', onClick: () => deleteOne(row) },
                  () => '删除'
                )
              )
            }
            return actions.length ? h(ElSpace, null, () => actions) : ''
          }
        }
      ]
    }
  })

  function handleSearch(params: Record<string, unknown>) {
    Object.assign(searchParams, normalizeParams(params), { pageNum: 1 })
    getData()
  }

  function handleReset() {
    searchForm.value = {
      spuId: undefined,
      memberNickName: undefined,
      showStatus: undefined,
      star: undefined
    }
    resetSearchParams()
    getData()
  }

  function openDetail(id?: number) {
    currentCommentId.value = id
    detailVisible.value = true
  }

  async function toggleShow(row: CommentVo) {
    if (row.id == null) return
    const nextStatus = row.showStatus === 1 ? 0 : 1
    await fetchCommentUpdateShowStatus({ id: row.id, showStatus: nextStatus })
    refreshData()
  }

  async function deleteOne(row: CommentVo) {
    if (row.id == null) return
    await ElMessageBox.confirm('确定删除该评论？', '删除确认', { type: 'warning' })
    await fetchCommentRemove([row.id])
    refreshData()
  }
</script>
