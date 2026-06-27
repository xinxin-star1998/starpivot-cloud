<!-- 营销-优惠券管理 -->
<template>
  <div class="sms-coupon-page art-full-height">
    <ElCard shadow="never">
      <ElForm :inline="true" @submit.prevent="handleSearch">
        <ElFormItem label="优惠券名称">
          <ElInput
            v-model="searchForm.couponName"
            clearable
            placeholder="名称"
            style="width: 160px"
          />
        </ElFormItem>
        <ElFormItem label="发布状态">
          <ElSelect v-model="searchForm.publish" clearable placeholder="全部" style="width: 120px">
            <ElOption
              v-for="opt in COUPON_PUBLISH_OPTIONS"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="适用范围">
          <ElSelect v-model="searchForm.useType" clearable placeholder="全部" style="width: 120px">
            <ElOption
              v-for="opt in COUPON_USE_TYPE_OPTIONS"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="有效期">
          <ElDatePicker
            v-model="validityRange"
            end-placeholder="结束"
            start-placeholder="开始"
            style="width: 360px"
            type="datetimerange"
            value-format="YYYY-MM-DD HH:mm:ss"
          />
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

    <CouponDetailDrawer
      v-model:visible="detailVisible"
      :coupon-id="detailId"
      @edit="handleDetailEdit"
      @submit="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import { useTable } from '@/hooks/core/useTable'
  import {
    COUPON_PUBLISH_OPTIONS,
    COUPON_TYPE_OPTIONS,
    COUPON_USE_TYPE_OPTIONS,
    type CouponVo,
    fetchCouponList,
    fetchCouponPublishStatus,
    fetchCouponRemove
  } from '@/api/mall/coupon'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import CouponDialog from './modules/coupon-dialog.vue'
  import CouponDetailDrawer from './modules/coupon-detail-drawer.vue'
  import type { DialogType } from '@/types'
  import { useAuth } from '@/hooks/core/useAuth'
  import { handleMutationError } from '@/utils/http/mutation'
  import {
    COUPON_RUN_STATUS_MAP,
    formatCouponDateRange,
    formatCouponMoney,
    formatCouponStock,
    getCouponRunStatus
  } from '@/utils/mall/coupon'

  defineOptions({ name: 'SmsCoupon' })

  const { hasAuth } = useAuth()
  const validityRange = ref<[string, string] | null>(null)
  const searchForm = ref({
    couponName: undefined as string | undefined,
    publish: undefined as number | undefined,
    useType: undefined as number | undefined
  })
  const dialogVisible = ref(false)
  const dialogType = ref<DialogType>('add')
  const currentId = ref<number>()
  const detailVisible = ref(false)
  const detailId = ref<number>()

  const useTypeLabel = (v?: number) =>
    COUPON_USE_TYPE_OPTIONS.find((o) => o.value === v)?.label || '-'

  const couponTypeLabel = (v?: number) =>
    COUPON_TYPE_OPTIONS.find((o) => o.value === v)?.label || '-'

  const buildSearchPayload = () => {
    const payload: Record<string, unknown> = { ...searchForm.value }
    if (validityRange.value?.length === 2) {
      payload.validityStart = validityRange.value[0]
      payload.validityEnd = validityRange.value[1]
    } else {
      delete payload.validityStart
      delete payload.validityEnd
    }
    for (const key of ['couponName', 'publish', 'useType'] as const) {
      if (payload[key] === '' || payload[key] == null) delete payload[key]
    }
    return payload
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
      apiFn: fetchCouponList,
      apiParams: { pageNum: 1, pageSize: 10, ...buildSearchPayload() },
      columnsFactory: () => [
        { type: 'index', label: '序号', width: 70 },
        { prop: 'couponName', label: '名称', minWidth: 140 },
        {
          prop: 'couponType',
          label: '券类型',
          width: 100,
          formatter: (row: CouponVo) => couponTypeLabel(row.couponType)
        },
        {
          prop: 'amount',
          label: '面额',
          width: 100,
          formatter: (row: CouponVo) => formatCouponMoney(row.amount)
        },
        {
          prop: 'minPoint',
          label: '门槛',
          width: 100,
          formatter: (row: CouponVo) => formatCouponMoney(row.minPoint)
        },
        {
          prop: 'useType',
          label: '适用',
          width: 100,
          formatter: (row: CouponVo) => useTypeLabel(row.useType)
        },
        {
          prop: 'validity',
          label: '有效期',
          minWidth: 180,
          formatter: (row: CouponVo) => formatCouponDateRange(row.startTime, row.endTime)
        },
        {
          prop: 'enableTime',
          label: '领取时间',
          minWidth: 180,
          formatter: (row: CouponVo) =>
            formatCouponDateRange(row.enableStartTime, row.enableEndTime)
        },
        {
          prop: 'stock',
          label: '已领/总量',
          width: 100,
          formatter: (row: CouponVo) => formatCouponStock(row)
        },
        {
          prop: 'runStatus',
          label: '状态',
          width: 90,
          formatter: (row: CouponVo) => {
            const status = getCouponRunStatus(row)
            const meta = COUPON_RUN_STATUS_MAP[status]
            return h(ElTag, { type: meta.tagType }, () => meta.label)
          }
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
          width: 280,
          fixed: 'right',
          formatter: (row: CouponVo) => {
            const actions: ReturnType<typeof h>[] = []
            if (hasAuth('mall:coupon:list') || hasAuth('mall:coupon:query')) {
              actions.push(
                h(ArtButtonTable, {
                  label: '详情',
                  type: 'view',
                  onClick: () => openDetail(row.id)
                })
              )
            }
            if (hasAuth('mall:coupon:edit')) {
              const published = row.publish === 1
              actions.push(
                h(ArtButtonTable, {
                  label: published ? '下架' : '发布',
                  icon: published ? 'ri:arrow-down-circle-line' : 'ri:arrow-up-circle-line',
                  iconClass: published
                    ? 'bg-warning/12 text-warning'
                    : 'bg-success/12 text-success',
                  onClick: () => togglePublishStatus(row)
                })
              )
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
                  tooltip: (row.receiveCount ?? 0) > 0 ? '已有用户领取，不可删除' : '删除',
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
    Object.assign(searchParams, { ...buildSearchPayload(), pageNum: 1 })
    getData()
  }

  const resetSearch = () => {
    searchForm.value = { couponName: undefined, publish: undefined, useType: undefined }
    validityRange.value = null
    resetSearchParams()
    Object.assign(searchParams, buildSearchPayload())
    getData()
  }

  const openDialog = (type: DialogType, id?: number) => {
    dialogType.value = type
    currentId.value = id
    dialogVisible.value = true
  }

  const openDetail = (id?: number) => {
    if (id == null) return
    detailId.value = id
    detailVisible.value = true
  }

  const handleDetailEdit = (id?: number) => {
    detailVisible.value = false
    openDialog('edit', id)
  }

  const togglePublishStatus = async (row: CouponVo) => {
    if (row.id == null) return
    const published = row.publish === 1
    const nextPublish = (published ? 0 : 1) as 0 | 1
    const action = published ? '下架' : '发布'
    try {
      await ElMessageBox.confirm(
        `确定${action}优惠券「${row.couponName}」吗？${published ? '下架后用户将无法继续领取。' : ''}`,
        `${action}优惠券`,
        { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
      )
      await fetchCouponPublishStatus(row.id!, nextPublish)
      refreshData()
    } catch (error) {
      handleMutationError(error, `${action}失败`)
    }
  }

  const deleteOne = async (row: CouponVo) => {
    if (!row.id) return
    const received = row.receiveCount ?? 0
    if (received > 0) {
      ElMessage.warning('该优惠券已有用户领取，不能删除')
      return
    }
    try {
      await ElMessageBox.confirm(`确定删除优惠券「${row.couponName}」吗？`, '删除', {
        type: 'warning'
      })
      await fetchCouponRemove([row.id!])
      refreshData()
    } catch (error) {
      handleMutationError(error, '删除失败')
    }
  }
</script>

<style scoped lang="scss">
  .sms-coupon-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }
</style>
