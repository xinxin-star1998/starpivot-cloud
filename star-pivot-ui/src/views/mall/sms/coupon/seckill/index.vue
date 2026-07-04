<!-- 营销-秒杀活动 -->
<template>
  <div class="sms-seckill-page art-full-height">
    <ElCard shadow="never">
      <ElForm :inline="true" @submit.prevent="handleSearch">
        <ElFormItem label="活动标题">
          <ElInput v-model="searchForm.title" clearable placeholder="活动标题" />
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
          <ElButton v-auth="'mall:seckill:list'" type="primary" @click="openDialog('add')">
            新增活动
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

    <SeckillDialog
      v-model:visible="dialogVisible"
      :type="dialogType"
      :promotion-id="currentId"
      @success="refreshData"
    />
  </div>
</template>

<script setup lang="ts">
import {h} from 'vue'
import {ElMessageBox, ElTag} from 'element-plus'
import {useTable} from '@/hooks/core/useTable'
import {
  fetchSeckillPromotionList,
  fetchSeckillPromotionRemove,
  type SeckillPromotionVo
} from '@/api/mall/seckill-promotion'
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import SeckillDialog from './modules/seckill-dialog.vue'
import type {DialogType} from '@/types'
import {handleMutationError} from '@/utils/http/mutation'

defineOptions({ name: 'SmsSeckillPromotion' })

  const searchForm = ref({ title: undefined as string | undefined })
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
      apiFn: fetchSeckillPromotionList,
      apiParams: { pageNum: 1, pageSize: 10, ...searchForm.value },
      columnsFactory: () => [
        { type: 'index', label: '序号', width: 70 },
        { prop: 'title', label: '活动标题', minWidth: 180 },
        {
          prop: 'startTime',
          label: '开始',
          minWidth: 160,
          formatter: (row: SeckillPromotionVo) => formatTime(row.startTime)
        },
        {
          prop: 'endTime',
          label: '结束',
          minWidth: 160,
          formatter: (row: SeckillPromotionVo) => formatTime(row.endTime)
        },
        {
          prop: 'status',
          label: '状态',
          width: 90,
          formatter: (row: SeckillPromotionVo) =>
            h(ElTag, { type: row.status === 1 ? 'success' : 'info' }, () =>
              row.status === 1 ? '上线' : '下线'
            )
        },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          fixed: 'right',
          formatter: (row: SeckillPromotionVo) =>
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
    searchForm.value = { title: undefined }
    resetSearchParams()
    getData()
  }

  const openDialog = (type: DialogType, id?: number) => {
    dialogType.value = type
    currentId.value = id
    dialogVisible.value = true
  }

  const deleteOne = async (row: SeckillPromotionVo) => {
    if (!row.id) return
    try {
      await ElMessageBox.confirm(`确定删除活动「${row.title}」吗？`, '删除活动', {
        type: 'warning'
      })
      await fetchSeckillPromotionRemove([row.id!])
      refreshData()
    } catch (error) {
      handleMutationError(error, '删除失败')
    }
  }
</script>

<style scoped lang="scss">
  .sms-seckill-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }
</style>
