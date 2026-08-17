<template>
  <ElDialog
    v-model="dialogVisible"
    align-center
    append-to-body
    destroy-on-close
    title="选择商品"
    width="820px"
    @closed="handleClosed"
  >
    <div class="picker-toolbar">
      <ElInput
        v-model="keyword"
        class="picker-search"
        clearable
        placeholder="商品名称"
        @keyup.enter="handleSearch"
      />
      <ElButton :loading="loading" type="primary" @click="handleSearch">查询</ElButton>
    </div>

    <ElTable
      ref="tableRef"
      v-loading="loading"
      :data="list"
      border
      max-height="400px"
      row-key="id"
      stripe
      @selection-change="handleSelectionChange"
    >
      <ElTableColumn :selectable="isRowSelectable" reserve-selection type="selection" width="48" />
      <ElTableColumn label="ID" prop="id" width="80" />
      <ElTableColumn label="商品名称" min-width="220" prop="spuName" show-overflow-tooltip />
      <ElTableColumn label="状态" width="88">
        <template #default="{ row }">
          <ElTag :type="row.publishStatus === 1 ? 'success' : 'info'" size="small">
            {{ row.publishStatus === 1 ? '上架' : '下架' }}
          </ElTag>
        </template>
      </ElTableColumn>
    </ElTable>

    <div class="picker-pagination">
      <ElPagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        background
        layout="total, sizes, prev, pager, next"
        @current-change="loadList"
        @size-change="handleSizeChange"
      />
    </div>

    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton :disabled="selectedRows.length === 0" type="primary" @click="handleConfirm">
        确定（已选 {{ selectedRows.length }}）
      </ElButton>
    </template>
  </ElDialog>
</template>

<script lang="ts" setup>
  import type { TableInstance } from 'element-plus'
  import { fetchMallProductList, type MallProductVo } from '@/api/mall/product'

  export interface MallSpuPickerItem {
    spuId: number
    spuName?: string
  }

  interface Props {
    visible: boolean
    /** 已关联的 SPU ID，列表中不可重复选择 */
    excludeIds?: number[]
  }

  const props = withDefaults(defineProps<Props>(), {
    excludeIds: () => []
  })

  const emit = defineEmits<{
    'update:visible': [boolean]
    confirm: [MallSpuPickerItem[]]
  }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const tableRef = ref<TableInstance>()
  const loading = ref(false)
  const keyword = ref('')
  const list = ref<MallProductVo[]>([])
  const selectedRows = ref<MallProductVo[]>([])
  const page = ref(1)
  const pageSize = ref(10)
  const total = ref(0)

  const excludeIdSet = computed(() => new Set(props.excludeIds.map((id) => Number(id))))

  const isRowSelectable = (row: MallProductVo) => {
    const id = row.id
    return id != null && !excludeIdSet.value.has(Number(id))
  }

  const loadList = async () => {
    loading.value = true
    try {
      const res = await fetchMallProductList({
        current: page.value,
        size: pageSize.value,
        spuName: keyword.value.trim() || undefined
      })
      list.value = res.rows || []
      total.value = res.total ?? 0
    } catch {
      list.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  const handleSearch = () => {
    page.value = 1
    loadList()
  }

  const handleSizeChange = () => {
    page.value = 1
    loadList()
  }

  const handleSelectionChange = (rows: MallProductVo[]) => {
    selectedRows.value = rows
  }

  const handleConfirm = () => {
    const items: MallSpuPickerItem[] = selectedRows.value
      .filter((row) => row.id != null)
      .map((row) => ({
        spuId: Number(row.id),
        spuName: row.spuName
      }))
    emit('confirm', items)
    dialogVisible.value = false
  }

  const handleClosed = () => {
    keyword.value = ''
    list.value = []
    selectedRows.value = []
    page.value = 1
    pageSize.value = 10
    total.value = 0
    tableRef.value?.clearSelection()
  }

  watch(
    () => props.visible,
    (visible) => {
      if (visible) {
        handleSearch()
      }
    }
  )
</script>

<style lang="scss" scoped>
  .picker-toolbar {
    display: flex;
    gap: 8px;
    margin-bottom: 12px;
  }

  .picker-search {
    flex: 1;
  }

  .picker-pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 12px;
  }
</style>
