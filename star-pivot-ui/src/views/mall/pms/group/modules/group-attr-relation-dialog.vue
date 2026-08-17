<template>
  <ElDialog
    v-model="dialogVisible"
    :title="`关联属性 — ${groupName || ''}`"
    width="720px"
    align-center
    destroy-on-close
  >
    <div class="relation-toolbar">
      <ElInput
        v-model="searchKeyword"
        clearable
        placeholder="按属性名搜索"
        style="width: 240px"
        @keyup.enter="handleBoundSearch"
        @clear="handleBoundSearch"
      />
      <ElButton @click="handleBoundSearch">查询</ElButton>
      <ElButton type="primary" @click="openPicker">新增属性</ElButton>
    </div>

    <ElTable
      v-loading="loading"
      :data="paginatedBoundList"
      border
      stripe
      row-key="attrId"
      empty-text="暂无关联属性，请点击「新增属性」"
    >
      <ElTableColumn type="index" label="序号" width="60" :index="boundIndexMethod" />
      <ElTableColumn prop="attrName" label="属性名" min-width="180" show-overflow-tooltip />
      <ElTableColumn label="组内排序" width="140">
        <template #default="{ row }">
          <ElInputNumber
            v-model="sortMap[row.attrId!]"
            :min="0"
            :precision="0"
            controls-position="right"
            size="small"
            style="width: 100%"
            @change="markSortDirty"
          />
        </template>
      </ElTableColumn>
      <ElTableColumn label="操作" width="88" fixed="right">
        <template #default="{ row }">
          <ElButton
            link
            type="danger"
            :loading="removingId === row.attrId"
            @click="handleRemove(row)"
          >
            移除
          </ElButton>
        </template>
      </ElTableColumn>
    </ElTable>

    <div class="relation-pagination">
      <ElPagination
        v-model:current-page="boundPage.pageNum"
        v-model:page-size="boundPage.pageSize"
        :total="boundPageTotal"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        background
        small
        @current-change="handleBoundPageChange"
        @size-change="handleBoundSizeChange"
      />
    </div>

    <ElDialog
      v-model="pickerVisible"
      title="新增属性"
      width="640px"
      align-center
      append-to-body
      destroy-on-close
    >
      <div class="relation-toolbar">
        <ElInput
          v-model="pickerKeyword"
          clearable
          placeholder="按属性名搜索"
          style="width: 220px"
          @keyup.enter="handlePickerSearch"
          @clear="handlePickerSearch"
        />
        <ElButton @click="handlePickerSearch">查询</ElButton>
      </div>
      <ElTable
        ref="pickerTableRef"
        v-loading="pickerLoading"
        :data="paginatedPickerList"
        border
        stripe
        row-key="attrId"
        empty-text="暂无可添加的基本属性"
        @selection-change="handlePickerSelectionChange"
      >
        <ElTableColumn
          type="selection"
          width="48"
          :selectable="pickerRowSelectable"
          reserve-selection
        />
        <ElTableColumn type="index" label="序号" width="60" :index="pickerIndexMethod" />
        <ElTableColumn prop="attrName" label="属性名" min-width="200" show-overflow-tooltip />
        <ElTableColumn prop="icon" label="属性图标" width="100" show-overflow-tooltip />
        <ElTableColumn label="可选值" min-width="160">
          <template #default="{ row }">
            <template v-if="!valueSelectDisplay(row).tags.length">
              <span class="text-placeholder">—</span>
            </template>
            <ElTooltip
              v-else-if="valueSelectDisplay(row).restCount > 0"
              :content="valueSelectDisplay(row).full"
              placement="top"
              :show-after="300"
            >
              <span class="value-select-tags">
                <ElTag type="success" size="small">{{ valueSelectDisplay(row).tags[0] }}</ElTag>
                <ElTag type="success" size="small">+{{ valueSelectDisplay(row).restCount }}</ElTag>
              </span>
            </ElTooltip>
            <span v-else class="value-select-tags">
              <ElTag type="success" size="small">{{ valueSelectDisplay(row).tags[0] }}</ElTag>
            </span>
          </template>
        </ElTableColumn>
      </ElTable>
      <div class="relation-pagination">
        <ElPagination
          v-model:current-page="pickerPage.pageNum"
          v-model:page-size="pickerPage.pageSize"
          :total="pickerPageTotal"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          background
          small
          @current-change="handlePickerPageChange"
          @size-change="handlePickerSizeChange"
        />
      </div>
      <template #footer>
        <ElButton @click="pickerVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="adding" @click="confirmAdd">确定</ElButton>
      </template>
    </ElDialog>

    <template #footer>
      <ElButton @click="dialogVisible = false">关闭</ElButton>
      <ElButton v-if="sortDirty" type="primary" :loading="saving" @click="saveBindings">
        保存排序
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { TableInstance } from 'element-plus'
  import { ElMessage, ElMessageBox } from 'element-plus'
  import {
    fetchGroupAttrRelations,
    fetchSaveGroupAttrRelations,
    type GroupAttrRelation
  } from '@/api/mall/group'
  import { formatValueSelectBrief, getAttrValueSelect } from '@/utils/mall/attr-value-select'

  const props = defineProps<{
    visible: boolean
    attrGroupId?: number
    groupName?: string
  }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const loading = ref(false)
  const saving = ref(false)
  const adding = ref(false)
  const removingId = ref<number | null>(null)
  const changed = ref(false)
  const sortDirty = ref(false)

  const searchKeyword = ref('')
  const boundList = ref<GroupAttrRelation[]>([])
  const allCandidates = ref<GroupAttrRelation[]>([])
  const sortMap = reactive<Record<number, number>>({})

  const boundPage = reactive({ pageNum: 1, pageSize: 10 })
  const pickerPage = reactive({ pageNum: 1, pageSize: 10 })

  const pickerVisible = ref(false)
  const pickerLoading = ref(false)
  const pickerKeyword = ref('')
  const pickerTableRef = ref<TableInstance>()
  const pickerSelection = ref<GroupAttrRelation[]>([])

  const boundAttrIds = computed(() =>
    boundList.value.map((r) => Number(r.attrId)).filter((id) => Number.isFinite(id))
  )

  const valueSelectDisplay = (row: GroupAttrRelation) =>
    formatValueSelectBrief(getAttrValueSelect(row))

  const matchAttrName = (name: string | undefined, keyword: string) => {
    const kw = keyword.trim().toLowerCase()
    if (!kw) return true
    return (name || '').toLowerCase().includes(kw)
  }

  const filteredBoundList = computed(() =>
    boundList.value.filter((row) => matchAttrName(row.attrName, searchKeyword.value))
  )

  const pickerCandidates = computed(() =>
    allCandidates.value.filter(
      (row) => row.attrId != null && !boundAttrIds.value.includes(row.attrId!)
    )
  )

  const filteredPickerList = computed(() =>
    pickerCandidates.value.filter((row) => matchAttrName(row.attrName, pickerKeyword.value))
  )

  const boundPageTotal = computed(() => filteredBoundList.value.length)
  const pickerPageTotal = computed(() => filteredPickerList.value.length)

  const paginatedBoundList = computed(() => {
    const start = (boundPage.pageNum - 1) * boundPage.pageSize
    return filteredBoundList.value.slice(start, start + boundPage.pageSize)
  })

  const paginatedPickerList = computed(() => {
    const start = (pickerPage.pageNum - 1) * pickerPage.pageSize
    return filteredPickerList.value.slice(start, start + pickerPage.pageSize)
  })

  const clampPage = (pageNum: { pageNum: number; pageSize: number }, total: number) => {
    const maxPage = Math.max(1, Math.ceil(total / pageNum.pageSize) || 1)
    if (pageNum.pageNum > maxPage) {
      pageNum.pageNum = maxPage
    }
  }

  watch(boundPageTotal, (total) => clampPage(boundPage, total))
  watch(pickerPageTotal, (total) => clampPage(pickerPage, total))

  const boundIndexMethod = (index: number) =>
    (boundPage.pageNum - 1) * boundPage.pageSize + index + 1

  const pickerIndexMethod = (index: number) =>
    (pickerPage.pageNum - 1) * pickerPage.pageSize + index + 1

  const handleBoundSearch = () => {
    boundPage.pageNum = 1
  }

  const handleBoundPageChange = () => {}

  const handleBoundSizeChange = () => {
    boundPage.pageNum = 1
  }

  const handlePickerSearch = () => {
    pickerPage.pageNum = 1
  }

  const handlePickerPageChange = () => {}

  const handlePickerSizeChange = () => {
    pickerPage.pageNum = 1
  }

  const syncSortMapFromBound = () => {
    Object.keys(sortMap).forEach((k) => delete sortMap[Number(k)])
    boundList.value.forEach((row, index) => {
      if (row.attrId == null) return
      sortMap[row.attrId] = row.attrSort ?? index
    })
  }

  const buildSaveItems = () =>
    boundList.value
      .filter((r) => r.attrId != null)
      .map((r, index) => ({
        attrId: r.attrId!,
        attrSort: sortMap[r.attrId!] ?? index
      }))

  const saveBindings = async () => {
    const id = props.attrGroupId
    if (id == null) return
    saving.value = true
    try {
      await fetchSaveGroupAttrRelations(id, buildSaveItems())
      changed.value = true
      sortDirty.value = false
      await loadData()
    } finally {
      saving.value = false
    }
  }

  const loadData = async () => {
    const id = props.attrGroupId
    if (id == null) return
    loading.value = true
    try {
      const list = await fetchGroupAttrRelations(id)
      allCandidates.value = list || []
      boundList.value = allCandidates.value.filter((r) => r.linked && r.attrId != null)
      syncSortMapFromBound()
      boundPage.pageNum = 1
    } catch {
      allCandidates.value = []
      boundList.value = []
    } finally {
      loading.value = false
    }
  }

  const markSortDirty = () => {
    sortDirty.value = true
  }

  const pickerRowSelectable = (row: GroupAttrRelation) => row.attrId != null

  const handlePickerSelectionChange = (rows: GroupAttrRelation[]) => {
    pickerSelection.value = rows
  }

  const openPicker = async () => {
    pickerKeyword.value = ''
    pickerSelection.value = []
    pickerPage.pageNum = 1
    pickerVisible.value = true
    pickerLoading.value = true
    try {
      if (!allCandidates.value.length) {
        await loadData()
      }
      await nextTick()
      pickerTableRef.value?.clearSelection()
    } finally {
      pickerLoading.value = false
    }
  }

  const confirmAdd = async () => {
    if (!pickerSelection.value.length) {
      ElMessage.warning('请勾选要添加的属性')
      return
    }
    const baseSort = boundList.value.length
    pickerSelection.value.forEach((row, i) => {
      if (row.attrId == null) return
      boundList.value.push({
        ...row,
        linked: true,
        attrSort: baseSort + i
      })
      sortMap[row.attrId] = baseSort + i
    })
    adding.value = true
    try {
      await saveBindings()
      ElMessage.success('添加成功')
      pickerVisible.value = false
    } catch {
      await loadData()
    } finally {
      adding.value = false
    }
  }

  const handleRemove = async (row: GroupAttrRelation) => {
    const attrId = row.attrId
    if (attrId == null) return
    try {
      await ElMessageBox.confirm(`确定移除属性「${row.attrName || attrId}」吗？`, '移除确认', {
        type: 'warning',
        confirmButtonText: '移除',
        cancelButtonText: '取消'
      })
    } catch {
      return
    }
    removingId.value = attrId
    boundList.value = boundList.value.filter((r) => r.attrId !== attrId)
    delete sortMap[attrId]
    try {
      await saveBindings()
      ElMessage.success('已移除')
    } catch {
      await loadData()
    } finally {
      removingId.value = null
    }
  }

  watch(
    () => [props.visible, props.attrGroupId],
    ([visible]) => {
      if (visible) {
        changed.value = false
        sortDirty.value = false
        searchKeyword.value = ''
        boundPage.pageNum = 1
        loadData()
      }
    }
  )

  watch(dialogVisible, (visible) => {
    if (!visible && changed.value) {
      emit('submit')
    }
  })
</script>

<style scoped lang="scss">
  .relation-toolbar {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: center;
    margin-bottom: 12px;
  }

  .relation-pagination {
    display: flex;
    justify-content: flex-end;
    margin-top: 12px;
  }

  .value-select-tags {
    display: inline-flex;
    flex-wrap: wrap;
    gap: 4px;
    align-items: center;
  }

  .text-placeholder {
    color: var(--el-text-color-placeholder);
  }
</style>
