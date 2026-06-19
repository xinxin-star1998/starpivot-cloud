<!-- 工作流-流程定义列表 -->
<template>
  <div class="workflow-def-page art-full-height">
    <DefSearch v-model="searchForm" @reset="resetSearchParams" @search="handleSearch" />

    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton v-auth="'workflow:def:edit'" v-ripple type="primary" @click="goDesigner()">
              新建流程
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :columns="columns"
        :data="data"
        :loading="loading"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <DefPreviewDialog v-model:visible="previewVisible" :def-id="previewDefId" />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { useRouter } from 'vue-router'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { useAuth } from '@/hooks/core/useAuth'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import {
    fetchWorkflowDefDetail,
    fetchWorkflowDefDisable,
    fetchWorkflowDefList,
    fetchWorkflowDefPublish,
    fetchWorkflowDefRemove,
    fetchWorkflowDefSave,
    type ProcessDefVo
  } from '@/api/workflow/def'
  import { defStatusLabel, defStatusTagType } from '../utils/workflow-labels'
  import DefSearch from './modules/def-search.vue'
  import DefPreviewDialog from './modules/def-preview-dialog.vue'

  defineOptions({ name: 'WorkflowDef' })

  const router = useRouter()
  const { hasAuth } = useAuth()

  const previewVisible = ref(false)
  const previewDefId = ref<number>()

  const searchForm = ref({
    processCode: undefined as string | undefined,
    processName: undefined as string | undefined,
    status: undefined as string | undefined
  })

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
      apiFn: fetchWorkflowDefList,
      apiParams: {
        pageNum: 1,
        pageSize: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'index', width: 60, label: '序号' },
        {
          prop: 'processCode',
          label: '流程编码',
          minWidth: 160,
          showOverflowTooltip: true
        },
        {
          prop: 'processName',
          label: '流程名称',
          minWidth: 140,
          showOverflowTooltip: true
        },
        { prop: 'bizModule', label: '模块', width: 80 },
        { prop: 'version', label: '版本', width: 70 },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: ProcessDefVo) =>
            h(ElTag, { type: defStatusTagType(row.status) }, () => defStatusLabel(row.status))
        },
        {
          prop: 'updateTime',
          label: '更新时间',
          minWidth: 160,
          showOverflowTooltip: true
        },
        {
          prop: 'operation',
          label: '操作',
          width: 280,
          fixed: 'right',
          formatter: (row: ProcessDefVo) => {
            const actions: any[] = []

            if (hasAuth('workflow:def:query')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'view',
                  label: '预览',
                  onClick: () => openPreview(row.defId)
                })
              )
            }

            if (hasAuth('workflow:def:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'edit',
                  label: '设计',
                  onClick: () => goDesigner(row.defId)
                }),
                h(ArtButtonTable, {
                  type: 'sync',
                  label: '复制',
                  onClick: () => handleCopy(row)
                })
              )
            }

            if (row.status === 'draft' && hasAuth('workflow:def:publish')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'generate',
                  label: '发布',
                  onClick: () => handlePublish(row.defId)
                })
              )
            }

            if (row.status === 'published' && hasAuth('workflow:def:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'pause',
                  label: '停用',
                  onClick: () => handleDisable(row.defId)
                })
              )
            }

            if (row.status !== 'published' && hasAuth('workflow:def:delete')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'delete',
                  label: '删除',
                  onClick: () => handleRemove(row)
                })
              )
            }

            if (actions.length === 0) {
              return h('span', { style: 'color: var(--art-gray-500)' }, '')
            }

            return h('div', actions)
          }
        }
      ]
    }
  })

  function handleSearch(params: Record<string, any>) {
    Object.assign(searchParams, params)
    getData()
  }

  function goDesigner(defId?: number) {
    router.push({
      path: '/workflow/designer',
      query: defId ? { defId: String(defId) } : undefined
    })
  }

  function openPreview(defId?: number) {
    if (!defId) return
    previewDefId.value = defId
    previewVisible.value = true
  }

  async function handleCopy(row: ProcessDefVo) {
    if (!row.defId) return
    try {
      const detail = await fetchWorkflowDefDetail(row.defId)
      const suffix = `_copy_${Date.now().toString(36).slice(-4)}`
      await fetchWorkflowDefSave({
        processCode: `${detail.processCode || 'flow'}${suffix}`,
        processName: `${detail.processName || '流程'}（副本）`,
        bizModule: detail.bizModule || 'mall',
        defJson: detail.defJson || '{}'
      })
      refreshData()
      ElMessage.success('已复制为新草稿')
    } catch {
      ElMessage.error('复制失败')
    }
  }

  async function handlePublish(defId?: number) {
    if (!defId) return
    await fetchWorkflowDefPublish(defId)
    ElMessage.success('发布成功')
    refreshData()
  }

  async function handleDisable(defId?: number) {
    if (!defId) return
    await fetchWorkflowDefDisable(defId)
    ElMessage.success('已停用')
    refreshData()
  }

  async function handleRemove(row: ProcessDefVo) {
    if (!row.defId) return
    try {
      await ElMessageBox.confirm(
        `确定要删除流程「${row.processName || row.processCode}」吗？`,
        '删除流程',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )
      await fetchWorkflowDefRemove([row.defId])
      refreshData()
      ElMessage.success('删除成功')
    } catch (error) {
      if (error !== 'cancel') {
        ElMessage.error('删除失败')
      }
    }
  }
</script>
