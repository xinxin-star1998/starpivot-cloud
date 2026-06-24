<!-- 营销-优惠券管理 -->
<template>
  <div class="sms-coupon-page art-full-height">
    <ElCard shadow="never">
      <ElForm :inline="true" @submit.prevent="handleSearch">
        <ElFormItem label="优惠券名称">
          <ElInput v-model="searchForm.couponName" clearable placeholder="名称" />
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
          <ElButton v-auth="'mall:coupon:add'" type="primary" @click="openDialog('add')">
            新增优惠券
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

    <CouponDialog
      v-model:visible="dialogVisible"
      :type="dialogType"
      :coupon-id="currentId"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { ElMessageBox, ElTag } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import {
    COUPON_USE_TYPE_OPTIONS,
    fetchCouponList,
    fetchCouponRemove,
    type CouponVo
  } from '@/api/mall/coupon'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import CouponDialog from './modules/coupon-dialog.vue'
  import type { DialogType } from '@/types'
  import { useAuth } from '@/hooks/core/useAuth'

  defineOptions({ name: 'SmsCoupon' })

  const { hasAuth } = useAuth()
  const searchForm = ref({ couponName: undefined as string | undefined })
  const dialogVisible = ref(false)
  const dialogType = ref<DialogType>('add')
  const currentId = ref<number>()

  const useTypeLabel = (v?: number) =>
    COUPON_USE_TYPE_OPTIONS.find((o) => o.value === v)?.label || '-'

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
      apiFn: fetchCouponList,
      apiParams: { pageNum: 1, pageSize: 10, ...searchForm.value },
      columnsFactory: () => [
        { type: 'index', label: '序号', width: 70 },
        { prop: 'couponName', label: '名称', minWidth: 140 },
        { prop: 'amount', label: '面额', width: 90 },
        { prop: 'minPoint', label: '门槛', width: 90 },
        {
          prop: 'useType',
          label: '适用',
          width: 100,
          formatter: (row: CouponVo) => useTypeLabel(row.useType)
        },
        {
          prop: 'publish',
          label: '发布',
          width: 90,
          formatter: (row: CouponVo) =>
            h(ElTag, { type: row.publish === 1 ? 'success' : 'info' }, () =>
              row.publish === 1 ? '已发布' : '未发布'
            )
        },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          fixed: 'right',
          formatter: (row: CouponVo) => {
            const actions: ReturnType<typeof h>[] = []
            if (hasAuth('mall:coupon:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  label: '编辑',
                  type: 'edit',
                  onClick: () => openDialog('edit', row.id)
                })
              )
            }
            if (hasAuth('mall:coupon:delete')) {
              actions.push(
                h(ArtButtonTable, {
                  label: '删除',
                  type: 'delete',
                  onClick: () => deleteOne(row)
                })
              )
            }
            return h('div', actions)
          }
        }
      ]
    }
  })

  const handleSearch = () => {
    Object.assign(searchParams, { ...searchForm.value, pageNum: 1 })
    getData()
  }

  const resetSearch = () => {
    searchForm.value = { couponName: undefined }
    resetSearchParams()
    getData()
  }

  const openDialog = (type: DialogType, id?: number) => {
    dialogType.value = type
    currentId.value = id
    dialogVisible.value = true
  }

  const deleteOne = (row: CouponVo) => {
    if (!row.id) return
    ElMessageBox.confirm(`确定删除优惠券「${row.couponName}」吗？`, '删除', { type: 'warning' })
      .then(async () => {
        await fetchCouponRemove([row.id!])
        refreshData()
      })
      .catch(() => {})
  }
</script>

<style scoped lang="scss">
  .sms-coupon-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }
</style>
