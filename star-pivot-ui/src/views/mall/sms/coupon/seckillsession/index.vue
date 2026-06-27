<!-- 营销-秒杀场次 -->
<template>
  <div class="sms-seckill-session-page art-full-height">
    <ElCard shadow="never">
      <ElForm :inline="true" @submit.prevent="handleSearch">
        <ElFormItem label="场次名称">
          <ElInput v-model="searchForm.name" clearable placeholder="场次名称" />
        </ElFormItem>
        <ElFormItem>
          <ElButton type="primary" @click="handleSearch">查询</ElButton>
          <ElButton @click="resetSearch">重置</ElButton>
        </ElFormItem>
      </ElForm>
    </ElCard>

    <ElCard class="art-table-card" shadow="never" style="margin-top: 12px">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElButton v-auth="'mall:seckill:session'" type="primary" @click="openDialog('add')">
            新增场次
          </ElButton>
        </template>
      </ArtTableHeader>
      <ArtTable
        :loading="loading"
        :data="data"
        :columns="columns"
        :pagination="pagination"
        @pagination:size-change="handleSizeChange"
        @pagination:current-change="handleCurrentChange"
      />
    </ElCard>

    <SessionDialog
      v-model:visible="dialogVisible"
      :type="dialogType"
      :session-id="currentId"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { ElMessageBox, ElTag } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchSeckillSessionList,
    fetchSeckillSessionRemove,
    type SeckillSessionVo
  } from '@/api/mall/seckill-session'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import SessionDialog from './modules/session-dialog.vue'
  import type { DialogType } from '@/types'
  import { handleMutationError } from '@/utils/http/mutation'

  defineOptions({ name: 'SmsSeckillSession' })

  const searchForm = ref({ name: undefined as string | undefined })
  const dialogVisible = ref(false)
  const dialogType = ref<DialogType>('add')
  const currentId = ref<number>()

  const formatTime = (t?: string) => (t ? t.replace('T', ' ').slice(0, 16) : '-')

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
      apiFn: fetchSeckillSessionList,
      apiParams: { pageNum: 1, pageSize: 10, ...searchForm.value },
      columnsFactory: () => [
        { type: 'index', label: '序号', width: 70 },
        { prop: 'name', label: '场次名称', minWidth: 140 },
        {
          prop: 'startTime',
          label: '开始时间',
          minWidth: 160,
          formatter: (row: SeckillSessionVo) => formatTime(row.startTime)
        },
        {
          prop: 'endTime',
          label: '结束时间',
          minWidth: 160,
          formatter: (row: SeckillSessionVo) => formatTime(row.endTime)
        },
        {
          prop: 'status',
          label: '状态',
          width: 90,
          formatter: (row: SeckillSessionVo) =>
            h(ElTag, { type: row.status === 1 ? 'success' : 'info' }, () =>
              row.status === 1 ? '启用' : '禁用'
            )
        },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          fixed: 'right',
          formatter: (row: SeckillSessionVo) =>
            h('div', [
              h(ArtButtonTable, {
                label: '编辑',
                type: 'edit',
                onClick: () => openDialog('edit', row.id)
              }),
              h(ArtButtonTable, {
                label: '删除',
                type: 'delete',
                onClick: () => deleteOne(row)
              })
            ])
        }
      ]
    }
  })

  const handleSearch = () => {
    Object.assign(searchParams, { ...searchForm.value, pageNum: 1 })
    getData()
  }

  const resetSearch = () => {
    searchForm.value = { name: undefined }
    resetSearchParams()
    getData()
  }

  const openDialog = (type: DialogType, id?: number) => {
    dialogType.value = type
    currentId.value = id
    dialogVisible.value = true
  }

  const deleteOne = async (row: SeckillSessionVo) => {
    if (!row.id) return
    try {
      await ElMessageBox.confirm(`确定删除场次「${row.name}」吗？`, '删除场次', { type: 'warning' })
      await fetchSeckillSessionRemove([row.id!])
      refreshData()
    } catch (error) {
      handleMutationError(error, '删除失败')
    }
  }
</script>

<style scoped lang="scss">
  .sms-seckill-session-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }
</style>
