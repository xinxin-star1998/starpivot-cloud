<!-- 会员等级 -->
<template>
  <div class="member-level-page art-full-height">
    <ElCard class="art-table-card" shadow="never">
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElButton v-auth="'mall:member:level'" type="primary" @click="openDialog('add')">
            新增等级
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

    <LevelDialog
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
    fetchMemberLevelPage,
    fetchMemberLevelRemove,
    PRIVILEGE_MAP,
    type MemberLevelVo
  } from '@/api/mall/member-level'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import LevelDialog from './modules/level-dialog.vue'
  import { ElTag } from 'element-plus'
  import type { DialogType } from '@/types'

  defineOptions({ name: 'MemberLevel' })

  const dialogVisible = ref(false)
  const dialogType = ref<DialogType>('add')
  const currentId = ref<number>()

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
      apiFn: fetchMemberLevelPage,
      apiParams: { pageNum: 1, pageSize: 20 },
      columnsFactory: () => [
        { prop: 'id', label: 'ID', width: 70 },
        { prop: 'name', label: '等级名称', minWidth: 120 },
        { prop: 'growthPoint', label: '成长值门槛', width: 110 },
        {
          prop: 'freeFreightPoint',
          label: '免邮积分',
          width: 100,
          formatter: (row) => (row.freeFreightPoint != null ? Number(row.freeFreightPoint).toFixed(0) : '-')
        },
        {
          prop: 'defaultStatus',
          label: '默认',
          width: 80,
          formatter: (row) =>
            h(ElTag, { type: row.defaultStatus === 1 ? 'success' : 'info', size: 'small' }, () =>
              PRIVILEGE_MAP[row.defaultStatus ?? 0] ?? row.defaultStatus
            )
        },
        {
          prop: 'privilegeFreeFreight',
          label: '免邮',
          width: 70,
          formatter: (row) => PRIVILEGE_MAP[row.privilegeFreeFreight ?? 0] ?? '-'
        },
        {
          prop: 'privilegeMemberPrice',
          label: '会员价',
          width: 80,
          formatter: (row) => PRIVILEGE_MAP[row.privilegeMemberPrice ?? 0] ?? '-'
        },
        { prop: 'note', label: '备注', minWidth: 140, showOverflowTooltip: true },
        {
          prop: 'operation',
          label: '操作',
          width: 160,
          fixed: 'right',
          formatter: (row: MemberLevelVo) =>
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

  function openDialog(type: DialogType, id?: number) {
    dialogType.value = type
    currentId.value = id
    dialogVisible.value = true
  }

  async function deleteOne(row: MemberLevelVo) {
    if (row.id == null) return
    await ElMessageBox.confirm(`确定删除等级「${row.name}」？`, '删除确认', { type: 'warning' })
    await fetchMemberLevelRemove([row.id])
    refreshData()
  }
</script>
