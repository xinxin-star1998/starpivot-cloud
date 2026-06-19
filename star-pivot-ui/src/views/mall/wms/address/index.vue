<!-- 省市区地址管理（懒加载树表） -->
<template>
  <div class="address-page art-full-height">
    <AddressSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams" />
    <p v-if="isSearchMode" class="address-search-tip"
      >当前为搜索结果（最多 200 条），清空条件并查询可恢复懒加载树</p
    >

    <ElCard class="art-table-card" shadow="never" style="margin-top: 12px">
      <ArtTableHeader
        v-model:columns="columnChecks"
        :loading="loading"
        @refresh="refreshData({ resetTree: true })"
      >
        <template #left>
          <ElSpace wrap>
            <ElButton
              v-auth="'mall:address:add'"
              type="primary"
              @click="showDialog('add')"
              v-ripple
            >
              新增省份
            </ElButton>
            <ElButton
              v-if="!isSearchMode"
              :disabled="tableData.length === 0"
              @click="toggleExpand"
              v-ripple
            >
              {{ isExpanded ? '收起' : '展开' }}
            </ElButton>
            <ElButton
              v-auth="'mall:address:delete'"
              type="danger"
              :disabled="selectedRows.length === 0"
              @click="handleBatchDelete"
              v-ripple
            >
              批量删除
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <ArtTable
        :key="tableBootKey"
        ref="tableRef"
        row-key="id"
        :loading="loading"
        :data="tableData"
        :columns="columns"
        :stripe="false"
        :lazy="!isSearchMode"
        :load="isSearchMode ? undefined : loadChildren"
        :tree-props="
          isSearchMode ? undefined : { children: 'children', hasChildren: 'hasChildren' }
        "
        :default-expand-all="false"
        @selection-change="handleSelectionChange"
      />

      <AddressDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :address-data="currentAddressData"
        @submit="handleDialogSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { h } from 'vue'
  import type { TableProps } from 'element-plus'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import {
    type Address,
    fetchDeleteAddress,
    fetchGetAddressChildren,
    fetchSearchAddress
  } from '@/api/mall/address'
  import AddressSearch from './modules/address-search.vue'
  import AddressDialog from './modules/address-dialog.vue'
  import type { DialogType } from '@/types'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import { useAuth } from '@/hooks/core/useAuth'
  import {
    addressHasChildren,
    addressLevelTagType,
    formatAddressLevel,
    nextAddressLevel
  } from '@/utils/mall/address-level'

  defineOptions({ name: 'MallAddress' })

  const { hasAuth } = useAuth()

  const loading = ref(false)
  const isExpanded = ref(false)
  const isSearchMode = ref(false)
  const tableBootKey = ref(0)
  const tableRef = ref()
  const tableData = ref<Address[]>([])
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentAddressData = ref<Partial<Address>>({})
  const selectedRows = ref<Address[]>([])

  /** 懒加载过程中缓存节点，用于刷新后还原展开路径 */
  const nodeCache = new Map<string, Address>()

  /** 刷新后需展开的祖先编码（从省到父级） */
  const restoreExpandCodes = ref<string[]>([])

  const searchForm = ref({
    code: '',
    parentCode: '',
    name: '',
    level: undefined as number | undefined
  })

  const hasSearchCondition = computed(() => {
    const f = searchForm.value
    return !!(f.code.trim() || f.parentCode.trim() || f.name.trim() || f.level != null)
  })

  const cacheNode = (row: Address) => {
    if (row.code) nodeCache.set(row.code, row)
  }

  const mapRow = (item: Address): Address => {
    const row = {
      ...item,
      hasChildren: item.hasChildren ?? addressHasChildren(item.level)
    }
    cacheNode(row)
    return row
  }

  /** 收集需展开的祖先编码（展开后可见当前行所在层级） */
  const collectAncestorCodes = (row: Partial<Address>): string[] => {
    const codes: string[] = []
    let parentCode = row.parentCode
    while (parentCode && parentCode !== '0' && parentCode !== '00') {
      codes.unshift(parentCode)
      const parent = nodeCache.get(parentCode)
      if (!parent?.parentCode) break
      parentCode = parent.parentCode
    }
    return codes
  }

  /** 停留在该行同级列表（编辑/删除后） */
  const buildRestoreForSiblings = (row: Address) => {
    cacheNode(row)
    restoreExpandCodes.value = collectAncestorCodes(row)
  }

  /** 停留在该行下级列表（其下新增后） */
  const buildRestoreForChildren = (row: Address) => {
    cacheNode(row)
    const codes = collectAncestorCodes(row)
    if (row.code) codes.push(row.code)
    restoreExpandCodes.value = codes
  }

  const fetchRowChildren = async (row: Address): Promise<Address[]> => {
    const list = await fetchGetAddressChildren(row.code || '0')
    return (list || []).map(mapRow)
  }

  const expandPathCodes = async (codes: string[]) => {
    const elTable = tableRef.value?.elTableRef
    if (!elTable || !codes.length) return

    let levelRows = tableData.value
    for (const code of codes) {
      const row = levelRows.find((r) => r.code === code)
      if (!row) break
      // 清空旧 children，避免懒加载缓存导致编辑后仍显示旧名称
      row.children = undefined
      const children = await fetchRowChildren(row)
      row.children = children
      elTable.toggleRowExpansion(row, true)
      await nextTick()
      levelRows = children
    }
    // 触发树表重绘
    tableData.value = [...tableData.value]
  }

  const loadRoots = async () => {
    loading.value = true
    try {
      const list = await fetchGetAddressChildren('0')
      tableData.value = (list || []).map(mapRow)
    } catch {
      tableData.value = []
    } finally {
      loading.value = false
    }
  }

  const toggleRowExpand = (row: Address) => {
    if (isSearchMode.value || !row.hasChildren) return
    const elTable = tableRef.value?.elTableRef
    if (!elTable) return
    elTable.toggleRowExpansion(row)
  }

  const loadChildren: TableProps<Address>['load'] = (row, _treeNode, resolve) => {
    cacheNode(row)
    fetchRowChildren(row)
      .then((children) => {
        row.children = children
        resolve(children)
      })
      .catch(() => resolve([]))
  }

  const runSearch = async () => {
    isSearchMode.value = true
    loading.value = true
    try {
      const f = searchForm.value
      const list = await fetchSearchAddress({
        code: f.code.trim() || undefined,
        parentCode: f.parentCode.trim() || undefined,
        name: f.name.trim() || undefined,
        level: f.level
      })
      tableData.value = (list || []).map((item) => ({ ...item, hasChildren: false }))
    } catch {
      tableData.value = []
    } finally {
      loading.value = false
    }
  }

  const exitSearchMode = () => {
    isSearchMode.value = false
    refreshData({ resetTree: true })
  }

  const { columnChecks, columns } = useTableColumns(() => [
    { type: 'selection' },
    {
      prop: 'name',
      label: '地区名称',
      minWidth: 200,
      showOverflowTooltip: true,
      formatter: (row: Address) => {
        const name = row.name || ''
        if (isSearchMode.value || !row.hasChildren) return name
        return h(
          'span',
          {
            class: 'address-tree-name',
            onClick: (e: MouseEvent) => {
              e.stopPropagation()
              toggleRowExpand(row)
            }
          },
          name
        )
      }
    },
    { prop: 'code', label: '地区编码', width: 120, showOverflowTooltip: true },
    { prop: 'parentCode', label: '父级编码', width: 120, showOverflowTooltip: true },
    {
      prop: 'level',
      label: '层级',
      width: 90,
      formatter: (row: Address) =>
        h(ElTag, { type: addressLevelTagType(row.level), size: 'small' }, () =>
          formatAddressLevel(row.level)
        )
    },
    { prop: 'createTime', label: '创建时间', width: 170, showOverflowTooltip: true },
    {
      prop: 'operation',
      label: '操作',
      width: 200,
      fixed: 'right',
      formatter: (row: Address) => {
        const actions: ReturnType<typeof h>[] = []
        if (hasAuth('mall:address:add') && !isSearchMode.value && addressHasChildren(row.level)) {
          actions.push(
            h(ArtButtonTable, {
              type: 'add',
              title: '新增下级',
              onClick: () => showDialog('add', row)
            })
          )
        }
        if (hasAuth('mall:address:edit')) {
          actions.push(
            h(ArtButtonTable, {
              label: '编辑',
              type: 'edit',
              onClick: () => showDialog('edit', row)
            })
          )
        }
        if (hasAuth('mall:address:delete')) {
          actions.push(
            h(ArtButtonTable, {
              label: '删除',
              type: 'delete',
              onClick: () => deleteAddress(row)
            })
          )
        }
        if (actions.length === 0) {
          return h('span', { style: 'color: #999' }, '')
        }
        return h('div', actions)
      }
    }
  ])

  onMounted(() => {
    loadRoots()
  })

  const handleSearch = async () => {
    if (hasSearchCondition.value) {
      nodeCache.clear()
      tableBootKey.value++
      await runSearch()
    } else {
      exitSearchMode()
    }
  }

  const resetSearchParams = () => {
    searchForm.value = {
      code: '',
      parentCode: '',
      name: '',
      level: undefined
    }
    exitSearchMode()
  }

  const refreshData = async (options?: { resetTree?: boolean }) => {
    selectedRows.value = []
    if (isSearchMode.value) {
      if (options?.resetTree) tableBootKey.value++
      runSearch()
      return
    }

    const codesToRestore = [...restoreExpandCodes.value]
    restoreExpandCodes.value = []

    if (options?.resetTree) {
      nodeCache.clear()
      tableBootKey.value++
    }

    await loadRoots()
    if (codesToRestore.length) {
      // 重挂载表格，清除 Element Plus 懒加载内部缓存
      tableBootKey.value++
      await nextTick()
      await expandPathCodes(codesToRestore)
    }
  }

  /** 弹窗提交前暂存，用于刷新后还原展开层级 */
  const pendingRestoreRow = ref<Address | null>(null)
  const pendingRestoreMode = ref<'siblings' | 'children'>('siblings')

  const showDialog = (type: DialogType, row?: Address) => {
    dialogType.value = type
    pendingRestoreRow.value = null
    if (type === 'add' && row?.code) {
      pendingRestoreRow.value = { ...row }
      pendingRestoreMode.value = 'children'
      currentAddressData.value = {
        parentCode: row.code,
        level: nextAddressLevel(row.level)
      }
    } else if (type === 'edit' && row) {
      pendingRestoreRow.value = { ...row }
      pendingRestoreMode.value = 'siblings'
      currentAddressData.value = { ...row }
    } else {
      currentAddressData.value = { parentCode: '0', level: 0 }
    }
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  const handleDialogSubmit = async () => {
    const row = pendingRestoreRow.value
    if (row) {
      if (pendingRestoreMode.value === 'children') {
        buildRestoreForChildren(row)
      } else {
        buildRestoreForSiblings(row)
      }
    }
    pendingRestoreRow.value = null
    currentAddressData.value = {}
    await refreshData()
  }

  const handleSelectionChange = (selection: Address[]) => {
    selectedRows.value = selection
  }

  const deleteAddress = (row: Address) => {
    if (!row.id) return
    ElMessageBox.confirm(`确定删除地区「${row.name || row.code}」吗？`, '删除地区', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        await fetchDeleteAddress([row.id!])
        buildRestoreForSiblings(row)
        await refreshData()
      })
      .catch(() => {})
  }

  const handleBatchDelete = () => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的地区')
      return
    }
    const names = selectedRows.value.map((r) => r.name || r.code).join('、')
    ElMessageBox.confirm(`确定删除以下地区吗？\n${names}`, '批量删除', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        const ids = selectedRows.value.map((r) => r.id!).filter(Boolean)
        const anchor = selectedRows.value[0]
        await fetchDeleteAddress(ids)
        selectedRows.value = []
        if (anchor) buildRestoreForSiblings(anchor)
        await refreshData()
      })
      .catch(() => {})
  }

  const toggleExpand = () => {
    isExpanded.value = !isExpanded.value
    nextTick(() => {
      const elTable = tableRef.value?.elTableRef
      if (!elTable || !tableData.value.length) return
      const walk = (rows: Address[]) => {
        rows.forEach((row) => {
          if (row.hasChildren) {
            elTable.toggleRowExpansion(row, isExpanded.value)
          }
          if (row.children?.length) {
            walk(row.children)
          }
        })
      }
      walk(tableData.value)
    })
  }
</script>

<style scoped lang="scss">
  .address-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }

  .address-search-tip {
    margin: 8px 0 0;
    font-size: 13px;
    color: var(--art-gray-600);
  }

  :deep(.art-table-card) {
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: var(--art-shadow-card);
  }

  :deep(.address-tree-name) {
    cursor: pointer;

    &:hover {
      color: var(--el-color-primary);
    }
  }
</style>
