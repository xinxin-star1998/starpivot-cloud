<!-- 审批中心-审批模板 -->
<template>
  <div class="approval-template-page art-full-height">
    <TemplateSearch v-model="searchForm" @reset="resetSearchParams" @search="handleSearch" />

    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElButton v-auth="'approval:template:edit'" v-ripple type="primary" @click="openEdit()">
            新建模板
          </ElButton>
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

    <TemplateEditDialog
      v-model:template-id="editTemplateId"
      v-model:visible="editVisible"
      @success="refreshData"
    />
  </div>
</template>

<script lang="ts" setup>
  import { h } from 'vue'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import { useAuth } from '@/hooks/core/useAuth'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import {
    fetchApprovalTemplateDisable,
    fetchApprovalTemplateList,
    fetchApprovalTemplatePublish
  } from '@/api/approval/template'
  import type { ApTemplate } from '@/api/approval/types'
  import { templateStatusLabel, templateStatusTagType } from '../utils/approval-labels'
  import TemplateSearch from './modules/template-search.vue'
  import TemplateEditDialog from './modules/template-edit-dialog.vue'
  import { handleMutationError } from '@/utils/http/mutation'

  defineOptions({ name: 'ApprovalTemplate' })

  const { hasAuth } = useAuth()

  const editVisible = ref(false)
  const editTemplateId = ref<number>()

  const searchForm = ref({
    templateCode: undefined as string | undefined,
    templateName: undefined as string | undefined,
    bizModule: undefined as string | undefined,
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
      apiFn: fetchApprovalTemplateList,
      apiParams: {
        pageNum: 1,
        pageSize: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'index', width: 60, label: '序号' },
        { prop: 'templateCode', label: '模板编码', minWidth: 160, showOverflowTooltip: true },
        { prop: 'templateName', label: '模板名称', minWidth: 140, showOverflowTooltip: true },
        { prop: 'bizModule', label: '业务域', width: 90 },
        { prop: 'version', label: '版本', width: 70 },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          formatter: (row: ApTemplate) =>
            h(ElTag, { type: templateStatusTagType(row.status) }, () =>
              templateStatusLabel(row.status)
            )
        },
        { prop: 'updateTime', label: '更新时间', minWidth: 160, showOverflowTooltip: true },
        {
          prop: 'operation',
          label: '操作',
          width: 260,
          fixed: 'right',
          formatter: (row: ApTemplate) => {
            const actions: any[] = []
            if (row.status === 'DRAFT' && hasAuth('approval:template:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'edit',
                  label: '编辑',
                  onClick: () => openEdit(row.templateId)
                })
              )
            }
            if (row.status === 'DRAFT' && hasAuth('approval:template:publish')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'execute',
                  label: '发布',
                  onClick: () => handlePublish(row)
                })
              )
            }
            if (row.status === 'PUBLISHED' && hasAuth('approval:template:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'pause',
                  label: '停用',
                  onClick: () => handleDisable(row)
                })
              )
            }
            if (hasAuth('approval:template:query')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'view',
                  label: '查看',
                  onClick: () => openEdit(row.templateId)
                })
              )
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

  function openEdit(id?: number) {
    editTemplateId.value = id
    editVisible.value = true
  }

  async function handlePublish(row: ApTemplate) {
    if (!row.templateId) return
    try {
      await ElMessageBox.confirm(`确定发布模板「${row.templateName}」吗？`, '发布模板', {
        type: 'warning'
      })
      await fetchApprovalTemplatePublish(row.templateId)
      ElMessage.success('发布成功')
      refreshData()
    } catch (error) {
      handleMutationError(error, '发布失败')
    }
  }

  async function handleDisable(row: ApTemplate) {
    if (!row.templateId) return
    try {
      await ElMessageBox.confirm(`确定停用模板「${row.templateName}」吗？`, '停用模板', {
        type: 'warning'
      })
      await fetchApprovalTemplateDisable(row.templateId)
      ElMessage.success('已停用')
      refreshData()
    } catch (error) {
      handleMutationError(error, '停用失败')
    }
  }
</script>
