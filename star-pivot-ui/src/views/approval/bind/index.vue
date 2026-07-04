<!-- 审批中心-业务绑定 -->
<template>
  <div class="approval-bind-page art-full-height">
    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElButton v-auth="'approval:bind:edit'" v-ripple type="primary" @click="openEdit()">
            新建绑定
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

    <BindEditDialog
      v-model:record="editRecord"
      v-model:visible="editVisible"
      @success="refreshData"
    />
  </div>
</template>

<script lang="ts" setup>
import {h} from 'vue'
import {ElMessage, ElMessageBox, ElTag} from 'element-plus'
import {useTable} from '@/hooks/core/useTable'
import {useAuth} from '@/hooks/core/useAuth'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {fetchApprovalBindList, fetchApprovalBindRemove} from '@/api/approval/template'
import type {ApTemplateBind} from '@/api/approval/types'
import BindEditDialog from './modules/bind-edit-dialog.vue'
import {handleMutationError} from '@/utils/http/mutation'

defineOptions({ name: 'ApprovalBind' })

  const { hasAuth } = useAuth()

  const editVisible = ref(false)
  const editRecord = ref<ApTemplateBind | null>(null)

  const {
    columns,
    columnChecks,
    data,
    loading,
    pagination,
    handleSizeChange,
    handleCurrentChange,
    refreshData
  } = useTable({
    core: {
      apiFn: fetchApprovalBindList,
      apiParams: { pageNum: 1, pageSize: 20 },
      columnsFactory: () => [
        { type: 'index', width: 60, label: '序号' },
        { prop: 'bizModule', label: '业务域', width: 90 },
        { prop: 'bizType', label: '单据类型', width: 120 },
        { prop: 'templateCode', label: '模板编码', minWidth: 160, showOverflowTooltip: true },
        {
          prop: 'matchExpr',
          label: '匹配表达式',
          minWidth: 180,
          showOverflowTooltip: true,
          formatter: (row: ApTemplateBind) => row.matchExpr || '(总是匹配)'
        },
        { prop: 'priority', label: '优先级', width: 80 },
        {
          prop: 'status',
          label: '状态',
          width: 80,
          formatter: (row: ApTemplateBind) =>
            h(ElTag, { type: row.status === '0' ? 'success' : 'info' }, () =>
              row.status === '0' ? '启用' : '停用'
            )
        },
        {
          prop: 'operation',
          label: '操作',
          width: 140,
          fixed: 'right',
          formatter: (row: ApTemplateBind) => {
            if (!hasAuth('approval:bind:edit')) return null
            return h('div', [
              h(ArtButtonTable, {
                type: 'edit',
                label: '编辑',
                onClick: () => openEdit(row)
              }),
              h(ArtButtonTable, {
                type: 'delete',
                label: '删除',
                onClick: () => handleDelete(row)
              })
            ])
          }
        }
      ]
    }
  })

  function openEdit(row?: ApTemplateBind) {
    editRecord.value = row || null
    editVisible.value = true
  }

  async function handleDelete(row: ApTemplateBind) {
    if (!row.bindId) return
    try {
      await ElMessageBox.confirm('确定删除该绑定规则吗？', '删除', { type: 'warning' })
      await fetchApprovalBindRemove([row.bindId])
      ElMessage.success('已删除')
      refreshData()
    } catch (error) {
      handleMutationError(error, '删除失败')
    }
  }
</script>
