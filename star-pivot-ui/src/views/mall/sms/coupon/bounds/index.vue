<!-- 营销-SPU 积分维护 -->
<template>
  <div class="sms-bounds-page art-full-height">
    <ElCard shadow="never">
      <ElForm :inline="true" @submit.prevent="handleSearch">
        <ElFormItem label="SPU ID">
          <ElInputNumber v-model="searchForm.spuId" :min="1" controls-position="right" />
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
          <ElButton v-auth="'mall:bounds:list'" type="primary" @click="openDialog('add')">
            新增积分配置
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

    <BoundsDialog
      v-model:visible="dialogVisible"
      :type="dialogType"
      :record-id="currentId"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { ElMessageBox } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import {
    fetchSpuBoundsList,
    fetchSpuBoundsRemove,
    type SpuBoundsVo
  } from '@/api/mall/spu-bounds'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import BoundsDialog from './modules/bounds-dialog.vue'
  import type { DialogType } from '@/types'

  defineOptions({ name: 'SmsSpuBounds' })

  const searchForm = ref({ spuId: undefined as number | undefined })
  const dialogVisible = ref(false)
  const dialogType = ref<DialogType>('add')
  const currentId = ref<number>()

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
      apiFn: fetchSpuBoundsList,
      apiParams: { pageNum: 1, pageSize: 10, ...searchForm.value },
      columnsFactory: () => [
        { type: 'index', label: '序号', width: 70 },
        { prop: 'spuId', label: 'SPU ID', width: 100 },
        { prop: 'spuName', label: 'SPU 名称', minWidth: 160 },
        { prop: 'growBounds', label: '成长值', width: 100 },
        { prop: 'buyBounds', label: '购物积分', width: 100 },
        { prop: 'work', label: '状态位', width: 90 },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          fixed: 'right',
          formatter: (row: SpuBoundsVo) =>
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
    searchForm.value = { spuId: undefined }
    resetSearchParams()
    getData()
  }

  const openDialog = (type: DialogType, id?: number) => {
    dialogType.value = type
    currentId.value = id
    dialogVisible.value = true
  }

  const deleteOne = (row: SpuBoundsVo) => {
    if (!row.id) return
    ElMessageBox.confirm(`确定删除 SPU「${row.spuName || row.spuId}」的积分配置吗？`, '删除', {
      type: 'warning'
    })
      .then(async () => {
        await fetchSpuBoundsRemove([row.id!])
        refreshData()
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .sms-bounds-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }
</style>
