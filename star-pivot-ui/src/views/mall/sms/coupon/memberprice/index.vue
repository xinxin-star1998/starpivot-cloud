<!-- 营销-会员价格 -->
<template>
  <div class="sms-member-price-page art-full-height">
    <ElCard shadow="never">
      <ElForm :inline="true" @submit.prevent="handleSearch">
        <ElFormItem label="SKU ID">
          <ElInputNumber v-model="searchForm.skuId" :min="1" controls-position="right" />
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
          <ElButton v-auth="'mall:memberprice:list'" type="primary" @click="openDialog('add')">
            新增会员价
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

    <MemberPriceDialog
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
    fetchMemberPriceList,
    fetchMemberPriceRemove,
    type MemberPriceVo
  } from '@/api/mall/member-price'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import MemberPriceDialog from './modules/member-price-dialog.vue'
  import type { DialogType } from '@/types'

  defineOptions({ name: 'SmsMemberPrice' })

  const searchForm = ref({ skuId: undefined as number | undefined })
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
      apiFn: fetchMemberPriceList,
      apiParams: { pageNum: 1, pageSize: 10, ...searchForm.value },
      columnsFactory: () => [
        { type: 'index', label: '序号', width: 70 },
        { prop: 'skuId', label: 'SKU ID', width: 100 },
        { prop: 'skuName', label: 'SKU 名称', minWidth: 160 },
        { prop: 'memberLevelName', label: '会员等级', width: 120 },
        { prop: 'memberPrice', label: '会员价', width: 100 },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          fixed: 'right',
          formatter: (row: MemberPriceVo) =>
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
    searchForm.value = { skuId: undefined }
    resetSearchParams()
    getData()
  }

  const openDialog = (type: DialogType, id?: number) => {
    dialogType.value = type
    currentId.value = id
    dialogVisible.value = true
  }

  const deleteOne = (row: MemberPriceVo) => {
    if (!row.id) return
    ElMessageBox.confirm('确定删除该会员价吗？', '删除', { type: 'warning' })
      .then(async () => {
        await fetchMemberPriceRemove([row.id!])
        refreshData()
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .sms-member-price-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }
</style>
