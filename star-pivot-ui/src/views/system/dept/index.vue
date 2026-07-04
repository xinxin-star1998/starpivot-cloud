<!-- 部门管理页面 -->
<template>
  <div class="dept-page art-full-height">
    <!-- 搜索栏 -->
    <DeptSearch v-model="searchForm" @search="handleSearch" @reset="resetSearchParams"></DeptSearch>

    <ElCard class="art-table-card" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
        <template #left>
          <ElSpace wrap>
            <ElButton @click="showDialog('add')" v-ripple v-auth="'system:dept:add'"
              >新增部门</ElButton
            >
            <ElButton @click="toggleExpand" v-ripple>
              {{ isExpanded ? '收起' : '展开' }}
            </ElButton>
          </ElSpace>
        </template>
      </ArtTableHeader>

      <!-- 表格 -->
      <ArtTable
        ref="tableRef"
        rowKey="deptId"
        :loading="loading"
        :data="filteredTableData"
        :columns="columns"
        :stripe="false"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
      >
      </ArtTable>

      <!-- 部门弹窗 -->
      <DeptDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :dept-data="currentDeptData"
        @submit="handleDialogSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
import {useTableColumns} from '@/hooks/core/useTableColumns'
import {useAuth} from '@/hooks/core/useAuth'
import DeptSearch from './modules/dept-search.vue'
import DeptDialog from './modules/dept-dialog.vue'
import {ElMessage, ElMessageBox, ElTag} from 'element-plus'
import {DialogType} from '@/types'
import ArtTable from '@/components/core/tables/art-table/index.vue'
import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
import {fetchDeleteDept, fetchGetDeptTree, type SysDept} from '@/api/dept/dept'

defineOptions({ name: 'Dept' })

  // 权限检查
  const { hasAuth } = useAuth()

  // 状态管理
  const loading = ref(false)
  const isExpanded = ref(false)
  const tableRef = ref()

  // 弹窗相关
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentDeptData = ref<Partial<SysDept>>({})

  // 搜索表单
  const searchForm = ref({
    deptName: '',
    leader: '',
    status: ''
  })

  // 表格数据
  const tableData = ref<SysDept[]>([])

  /**
   * 获取部门列表数据
   */
  const getDeptList = async (): Promise<void> => {
    loading.value = true
    try {
      const res = await fetchGetDeptTree()
      tableData.value = res || []
    } catch (error) {
      console.error('获取部门列表失败:', error)
      ElMessage.error('获取部门列表失败')
      tableData.value = []
    } finally {
      loading.value = false
    }
  }

  /**
   * 深度克隆对象
   */
  const deepClone = <T,>(obj: T): T => {
    if (obj === null || typeof obj !== 'object') return obj
    if (obj instanceof Date) return new Date(obj) as T
    if (Array.isArray(obj)) return obj.map((item) => deepClone(item)) as T

    const cloned = {} as T
    for (const key in obj) {
      if (Object.prototype.hasOwnProperty.call(obj, key)) {
        cloned[key] = deepClone(obj[key])
      }
    }
    return cloned
  }

  /**
   * 搜索部门
   * @param items 部门项数组
   * @returns 搜索结果数组
   */
  const searchDept = (items: SysDept[]): SysDept[] => {
    const results: SysDept[] = []

    for (const item of items) {
      const searchName = searchForm.value.deptName.toLowerCase().trim()
      const searchLeader = searchForm.value.leader.toLowerCase().trim()
      const searchStatus = searchForm.value.status.trim()

      const deptName = (item.deptName || '').toLowerCase()
      const leader = (item.leader || '').toLowerCase()

      // 状态匹配
      const statusMatch = !searchStatus || item.status === searchStatus

      // 名称和负责人匹配
      const nameMatch = !searchName || deptName.includes(searchName)
      const leaderMatch = !searchLeader || leader.includes(searchLeader)

      // 如果所有搜索条件都匹配
      const allMatch = nameMatch && leaderMatch && statusMatch

      if (item.children && item.children.length > 0) {
        const matchedChildren = searchDept(item.children)
        if (matchedChildren.length > 0) {
          const clonedItem = deepClone(item)
          clonedItem.children = matchedChildren
          results.push(clonedItem)
          continue
        }
        // 如果父节点本身匹配，即使子节点不匹配也要显示
        if (allMatch) {
          const clonedItem = deepClone(item)
          clonedItem.children = searchDept(item.children)
          results.push(clonedItem)
          continue
        }
      }

      if (allMatch) {
        results.push(deepClone(item))
      }
    }

    return results
  }

  // 过滤后的表格数据
  const filteredTableData = computed(() => {
    return searchDept(tableData.value)
  })

  // 表格列配置
  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'deptName',
      label: '部门名称',
      minWidth: 200
    },
    {
      prop: 'orderNum',
      label: '排序',
      width: 100,
      sortable: true
    },
    {
      prop: 'leader',
      label: '负责人',
      width: 120,
      formatter: (row: SysDept) => {
        return row.leader || '暂无'
      }
    },
    {
      prop: 'phone',
      label: '联系电话',
      width: 150,
      formatter: (row: SysDept) => {
        return row.phone || '暂无'
      }
    },
    {
      prop: 'email',
      label: '邮箱',
      minWidth: 180,
      formatter: (row: SysDept) => {
        return row.email || '暂无'
      }
    },
    {
      prop: 'status',
      label: '状态',
      width: 100,
      formatter: (row: SysDept) => {
        const status = row.status || '0'
        const statusMap = {
          '0': { type: 'success' as const, text: '正常' },
          '1': { type: 'danger' as const, text: '停用' }
        }
        const statusInfo = statusMap[status as keyof typeof statusMap] || statusMap['0']
        return h(ElTag, { type: statusInfo.type }, () => statusInfo.text)
      }
    },
    {
      prop: 'createTime',
      label: '创建时间',
      width: 180,
      formatter: (row: SysDept) => {
        return row.createTime || '暂无'
      }
    },
    {
      prop: 'operation',
      label: '操作',
      width: 180,
      align: 'right',
      fixed: 'right',
      formatter: (row: SysDept) => {
        const buttonStyle = { style: 'text-align: right' }
        const actions: any[] = []

        // 新增子部门按钮权限：system:dept:add
        if (hasAuth('system:dept:add')) {
          actions.push(
            h(ArtButtonTable, {
              type: 'add',
              onClick: () => showDialog('add', row),
              title: '新增子部门'
            })
          )
        }

        // 编辑部门按钮权限：system:dept:edit
        if (hasAuth('system:dept:edit')) {
          actions.push(
            h(ArtButtonTable, {
              type: 'edit',
              onClick: () => showDialog('edit', row)
            })
          )
        }

        // 删除部门按钮权限：system:dept:remove
        if (hasAuth('system:dept:delete')) {
          actions.push(
            h(ArtButtonTable, {
              type: 'delete',
              onClick: () => deleteDept(row)
            })
          )
        }

        if (actions.length === 0) {
          // 无任何操作权限时返回空占位
          return h('span', { style: 'color: var(--art-gray-500)' }, '')
        }

        return h('div', buttonStyle, actions)
      }
    }
  ])

  onMounted(() => {
    getDeptList()
  })

  /**
   * 搜索处理
   * @param params 参数
   */
  const handleSearch = (params: Record<string, any>) => {
    Object.assign(searchForm.value, params)
  }

  /**
   * 重置搜索参数
   */
  const resetSearchParams = () => {
    searchForm.value = {
      deptName: '',
      leader: '',
      status: ''
    }
  }

  /**
   * 刷新数据
   */
  const refreshData = () => {
    getDeptList()
  }

  /**
   * 显示部门弹窗
   */
  const showDialog = (type: DialogType, row?: SysDept): void => {
    dialogType.value = type
    if (type === 'add' && row) {
      // 新增子部门，设置父部门ID
      currentDeptData.value = { parentId: row.deptId }
    } else if (type === 'edit' && row) {
      // 编辑部门
      currentDeptData.value = { ...row }
    } else {
      // 新增顶级部门
      currentDeptData.value = {}
    }
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  /**
   * 删除部门
   */
  const deleteDept = (row: SysDept): void => {
    if (!row.deptId) {
      ElMessage.warning('部门ID不存在，无法删除')
      return
    }

    // 检查是否有子部门
    const hasChildren = row.children && row.children.length > 0
    const confirmMessage = hasChildren
      ? `该部门下存在子部门，删除后子部门也将被删除，确定要删除该部门吗？`
      : `确定要删除该部门吗？删除后无法恢复`

    ElMessageBox.confirm(confirmMessage, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(async () => {
        try {
          await fetchDeleteDept(row.deptId!)
          ElMessage.success('删除成功')
          await getDeptList()
        } catch (error) {
          console.error('删除部门失败:', error)
          ElMessage.error('删除失败')
        }
      })
      .catch(() => {
        // 用户取消删除
      })
  }

  /**
   * 处理弹窗提交事件
   */
  const handleDialogSubmit = async () => {
    try {
      dialogVisible.value = false
      currentDeptData.value = {}
      // 刷新列表数据
      await getDeptList()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  /**
   * 切换展开/收起所有部门
   */
  const toggleExpand = (): void => {
    isExpanded.value = !isExpanded.value
    nextTick(() => {
      if (tableRef.value?.elTableRef && filteredTableData.value) {
        const processRows = (rows: SysDept[]) => {
          rows.forEach((row) => {
            if (row.children && row.children.length > 0) {
              tableRef.value.elTableRef.toggleRowExpansion(row, isExpanded.value)
              processRows(row.children)
            }
          })
        }
        processRows(filteredTableData.value)
      }
    })
  }
</script>

<style scoped lang="scss">
  .dept-page {
    padding: var(--art-page-padding);
    background-color: var(--default-bg-color);
  }

  :deep(.art-table-card) {
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    box-shadow: var(--art-shadow-card);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: var(--art-shadow-card-hover);
    }
  }

  :deep(.el-table) {
    border-radius: 8px;

    .el-table__header-wrapper {
      th {
        font-weight: 600;
        color: var(--art-gray-800);
        background-color: var(--art-gray-100) !important;
      }
    }

    .el-table__body-wrapper {
      tr {
        transition: all 0.2s ease;

        &:hover > td {
          background-color: var(--art-gray-50) !important;
        }
      }
    }
  }

  :deep(.el-button) {
    font-weight: 500;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-1px);
    }
  }

  :deep(.el-tag) {
    font-weight: 500;
    border-radius: 6px;
  }
</style>
