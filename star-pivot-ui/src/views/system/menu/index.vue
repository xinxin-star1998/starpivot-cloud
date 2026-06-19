<!-- 菜单管理页面 -->
<template>
  <div class="menu-page art-full-height">
    <!-- 搜索栏 -->
    <ArtSearchBar
      v-model="searchFilters"
      :items="formItems"
      :showExpand="false"
      @reset="handleReset"
      @search="handleSearch"
    />

    <ElCard class="art-table-card" shadow="never">
      <!-- 表格头部 -->
      <ArtTableHeader
        :showZebra="false"
        :loading="loading"
        v-model:columns="columnChecks"
        @refresh="handleRefresh"
      >
        <template #left>
          <ElButton @click="handleAddMenu" v-ripple v-auth="'system:menu:add'"> 添加菜单 </ElButton>
          <ElButton @click="toggleExpand" v-ripple>
            {{ isExpanded ? '收起' : '展开' }}
          </ElButton>
        </template>
      </ArtTableHeader>

      <ArtTable
        ref="tableRef"
        rowKey="path"
        :loading="loading"
        :columns="columns"
        :data="filteredTableData"
        :stripe="false"
        :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
        :default-expand-all="false"
      />

      <!-- 菜单弹窗 -->
      <MenuDialog
        v-model:visible="dialogVisible"
        :type="dialogType"
        :editData="editData"
        :rawMenuData="rawMenuData"
        :lockType="lockMenuType"
        @submit="handleSubmit"
      />
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { formatMenuTitle } from '@/utils/router'
  import { deepClone, safeError } from '@/utils'
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import { useTableColumns } from '@/hooks/core/useTableColumns'
  import { useAuth } from '@/hooks/core/useAuth'
  import { useMenuStore } from '@/store/modules/menu'
  import type { AppRouteRecord } from '@/types/router'
  import MenuDialog from './modules/menu-dialog.vue'
  import {
    fetchAddMenu,
    fetchDeleteMenu,
    fetchGetMenuTree,
    fetchUpdateMenu,
    type SysMenu
  } from '@/api/menu/menu'
  import { ElMessage, ElMessageBox, ElTag } from 'element-plus'
  import { Icon } from '@iconify/vue'
  import { MenuProcessor } from '@/router/core/MenuProcessor'
  import type { MenuFormData } from './types'
  import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import { INITIAL_SEARCH_STATE, MENU_TYPE_CONFIG, STATUS_CONFIG } from './constants'
  import { reloadDynamicRoutes } from '@/router/guards/dynamicRouteGuard'

  defineOptions({ name: 'Menus' })

  // 权限检查
  const { hasAuth } = useAuth()
  // 菜单状态管理
  const menuStore = useMenuStore()
  // 路由
  // 状态管理
  const loading = ref(false)
  const isExpanded = ref(false)
  const tableRef = ref()

  // 弹窗相关
  const dialogVisible = ref(false)
  const dialogType = ref<'menu' | 'button'>('menu')
  const editData = ref<Partial<MenuFormData & AppRouteRecord> | null>(null)
  const lockMenuType = ref(false)

  // 搜索相关 - 简化状态管理，只使用一个状态
  const searchFilters = reactive({ ...INITIAL_SEARCH_STATE })

  const formItems = computed(() => [
    {
      label: '菜单名称',
      key: 'menuName',
      type: 'input',
      props: { clearable: true, placeholder: '请输入菜单名称' }
    },
    {
      label: '路由地址',
      key: 'route',
      type: 'input',
      props: { clearable: true, placeholder: '请输入路由地址' }
    },
    {
      label: '权限标识',
      key: 'perms',
      type: 'input',
      props: { clearable: true, placeholder: '请输入权限标识' }
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      props: {
        clearable: true,
        placeholder: '请选择状态'
      },
      options: [
        { label: '正常', value: '0' },
        { label: '停用', value: '1' }
      ]
    }
  ])

  onMounted(() => {
    getMenuList()
  })

  // 存储原始菜单数据（用于查找parentId和原始path）
  const rawMenuData = ref<SysMenu[]>([])
  // 存储菜单ID到原始path的映射
  const menuPathMap = ref<Map<number, string>>(new Map())
  // 存储菜单ID到原始菜单项的映射（用于优化权限标识查找）
  const menuCacheMap = ref<Map<number, SysMenu>>(new Map())

  /**
   * 构建菜单路径映射和缓存映射
   * @param menuList 菜单列表
   */
  const buildMenuPathMap = (menuList: SysMenu[]): void => {
    menuPathMap.value.clear()
    menuCacheMap.value.clear()
    const traverse = (menus: SysMenu[]): void => {
      menus.forEach((menu) => {
        if (menu.menuId !== undefined) {
          // 缓存菜单ID到路径的映射
          if (menu.path) {
            menuPathMap.value.set(menu.menuId, menu.path)
          }
          // 缓存菜单ID到菜单项的映射（用于快速查找权限标识）
          menuCacheMap.value.set(menu.menuId, menu)
        }
        if (menu.children && menu.children.length > 0) {
          traverse(menu.children)
        }
      })
    }
    traverse(menuList)
  }

  /**
   * 将 SysMenu 转换为 AppRouteRecord（使用 MenuProcessor）
   */
  const convertMenuToRouteRecord = (sysMenuList: SysMenu[]): AppRouteRecord[] => {
    const menuProcessor = new MenuProcessor()
    return menuProcessor.convertSysMenuToRouteRecordPublic(sysMenuList)
  }

  /**
   * 为路由记录添加权限标识（使用缓存优化，避免递归搜索）
   */
  const addPermsToMenu = (menus: AppRouteRecord[]): void => {
    menus.forEach((menu) => {
      // 使用缓存的 Map 直接查找，避免递归遍历整棵树
      const rawMenu = menu.id !== undefined ? menuCacheMap.value.get(menu.id) : undefined
      if (rawMenu?.perms) {
        menu.perms = rawMenu.perms
      }
      if (menu.children?.length) {
        addPermsToMenu(menu.children)
      }
    })
  }

  /**
   * 获取菜单列表数据
   */
  const getMenuList = async (): Promise<void> => {
    loading.value = true

    try {
      // 菜单管理页面应该获取所有菜单树，而不是用户有权限的菜单
      const sysMenuList = await fetchGetMenuTree()
      rawMenuData.value = sysMenuList

      // 构建菜单路径映射
      buildMenuPathMap(sysMenuList)

      // 转换为路由记录格式
      const normalizedList = convertMenuToRouteRecord(sysMenuList)

      // 添加权限标识（使用缓存优化）
      addPermsToMenu(normalizedList)
      tableData.value = normalizedList
    } catch (error) {
      safeError('获取菜单列表失败:', error)
      ElMessage.error('获取菜单列表失败')
    } finally {
      loading.value = false
    }
  }

  /**
   * 从缓存中查找菜单项及其父ID（使用缓存优化）
   * @param menuId 菜单ID
   * @returns 菜单项和父ID的对象
   */
  const findMenuAndParentFromRawData = (
    menuId: number | undefined
  ): { menu: SysMenu | undefined; parentId: number | undefined } => {
    if (!menuId) return { menu: undefined, parentId: undefined }

    // 使用缓存直接查找
    const menu = menuCacheMap.value.get(menuId)
    return { menu, parentId: menu?.parentId }
  }

  /**
   * 获取菜单类型信息
   * @param row 菜单行数据
   * @returns 菜单类型信息（文本和颜色）
   */
  const getMenuTypeInfo = (
    row: AppRouteRecord
  ): { text: string; color: 'primary' | 'success' | 'warning' | 'info' | 'danger' } => {
    // 优先使用后端返回的 menuType 字段
    if (row.menuType && row.menuType in MENU_TYPE_CONFIG) {
      const config = MENU_TYPE_CONFIG[row.menuType as keyof typeof MENU_TYPE_CONFIG]
      return { text: config.text, color: config.color }
    }
    // 兼容旧逻辑：如果没有 menuType，则根据其他字段推断
    if (row.meta?.isAuthButton) return { text: '按钮', color: 'danger' }
    if (row.children?.length) return { text: '目录', color: 'info' }
    if (row.meta?.link && row.meta?.isIframe) return { text: '内嵌', color: 'success' }
    if (row.path) return { text: '菜单', color: 'primary' }
    if (row.meta?.link) return { text: '外链', color: 'warning' }
    return { text: '未知', color: 'info' }
  }

  // 表格列配置
  const { columnChecks, columns } = useTableColumns(() => [
    {
      prop: 'meta.title',
      label: '菜单名称',
      minWidth: 120,
      formatter: (row: AppRouteRecord) => {
        const title = formatMenuTitle(row.meta?.title)
        const icon = row.meta?.isAuthButton ? undefined : row.meta?.icon
        if (!icon) return title
        return h('div', { style: 'display: inline-flex; align-items: center; gap: 8px;' }, [
          h(Icon, { icon, style: 'font-size: 18px; color: var(--art-gray-700);' }),
          title
        ])
      }
    },
    {
      prop: 'type',
      label: '菜单类型',
      formatter: (row: AppRouteRecord) => {
        const typeInfo = getMenuTypeInfo(row)
        return h(ElTag, { type: typeInfo.color }, () => typeInfo.text)
      }
    },
    {
      prop: 'path',
      label: '路由地址',
      formatter: (row: AppRouteRecord) => {
        if (row.meta?.isAuthButton) return ''
        // 优先显示外链
        if (row.meta?.link) return row.meta.link
        // 显示后端返回的原始 path，而不是规范化后的 path
        if (row.id !== undefined) {
          const originalPath = menuPathMap.value.get(row.id)
          if (originalPath) return originalPath
        }
        return row.path || ''
      }
    },
    {
      prop: 'perms',
      label: '权限标识',
      minWidth: 150,
      formatter: (row: AppRouteRecord) => {
        if (row.meta?.isAuthButton) {
          return h(ElTag, { type: 'danger', size: 'small' }, () => row.meta?.authMark || '')
        }
        // 优先显示后端返回的 perms 字段
        if (row.perms) {
          return h(ElTag, { type: 'info', size: 'small' }, () => row.perms)
        }
        // 兼容旧逻辑：如果没有 perms，则显示权限列表
        if (row.meta?.authList?.length) {
          return h(
            'div',
            { style: 'display: flex; flex-wrap: wrap; gap: 4px;' },
            row.meta.authList.map((auth) =>
              h(ElTag, { type: 'info', size: 'small' }, () => auth.authMark)
            )
          )
        }
        return h('span', { style: 'color: var(--art-gray-500)' }, '无')
      }
    },
    {
      prop: 'createTime',
      label: '创建时间',
      width: 180,
      formatter: (row: AppRouteRecord) => {
        return row.createTime || h('span', { style: 'color: var(--art-gray-500)' }, '暂无')
      }
    },
    {
      prop: 'status',
      label: '状态',
      width: 100,
      formatter: (row: AppRouteRecord) => {
        // 按钮类型不显示状态
        if (row.meta?.isAuthButton) {
          return ''
        }
        const status = (row.status || '0') as keyof typeof STATUS_CONFIG
        const statusInfo = STATUS_CONFIG[status] || STATUS_CONFIG['0']
        return h(ElTag, { type: statusInfo.type }, () => statusInfo.text)
      }
    },
    {
      prop: 'operation',
      label: '操作',
      width: 180,
      align: 'right',
      formatter: (row: AppRouteRecord) => {
        const buttonStyle = { style: 'text-align: right' }
        const buttons: any[] = []

        // 如果是权限按钮（包括isAuthButton和menuType='F'），显示编辑和删除按钮
        if (row.meta?.isAuthButton || row.menuType === 'F') {
          // 编辑权限按钮权限：system:menu:edit
          if (hasAuth('system:menu:edit')) {
            buttons.push(
              h(ArtButtonTable, {
                type: 'edit',
                onClick: () => handleEditAuth(row)
              })
            )
          }

          // 删除权限按钮权限：system:menu:delete
          if (hasAuth('system:menu:delete')) {
            buttons.push(
              h(ArtButtonTable, {
                type: 'delete',
                onClick: () => handleDeleteAuth(row)
              })
            )
          }
        } else {
          // 非权限按钮菜单：显示"新增权限"和"编辑"按钮
          // 无论是否已有权限按钮子节点，都允许添加更多
          // 新增权限按钮权限：system:menu:add
          if (hasAuth('system:menu:add')) {
            buttons.push(
              h(ArtButtonTable, {
                type: 'add',
                onClick: () => handleAddAuth(row),
                title: '新增权限'
              })
            )
          }

          // 编辑菜单按钮权限：system:menu:edit
          if (hasAuth('system:menu:edit')) {
            buttons.push(
              h(ArtButtonTable, {
                type: 'edit',
                onClick: () => handleEditMenu(row)
              })
            )
          }

          // 删除菜单按钮权限：system:menu:delete
          if (hasAuth('system:menu:delete')) {
            buttons.push(
              h(ArtButtonTable, {
                type: 'delete',
                onClick: () => handleDeleteMenu(row)
              })
            )
          }
        }

        if (buttons.length === 0) {
          // 无任何操作权限时返回空占位
          return h('span', { style: 'color: var(--art-gray-500)' }, '')
        }

        return h('div', buttonStyle, buttons)
      }
    }
  ])

  // 数据相关
  const tableData = ref<AppRouteRecord[]>([])

  /**
   * 重置搜索条件
   */
  const handleReset = (): void => {
    Object.assign(searchFilters, { ...INITIAL_SEARCH_STATE })
    // 重置后不需要重新获取数据，computed会自动更新
  }

  /**
   * 执行搜索
   */
  const handleSearch = (): void => {
    // 搜索条件已通过v-model绑定到searchFilters，computed会自动更新
    // 这里可以添加其他搜索相关的逻辑，如记录搜索历史等
  }

  /**
   * 刷新菜单列表
   */
  const handleRefresh = (): void => {
    getMenuList()
  }

  /**
   * 将权限列表转换为子节点
   * @param items 菜单项数组
   * @returns 转换后的菜单项数组
   */
  const convertAuthListToChildren = (items: AppRouteRecord[]): AppRouteRecord[] => {
    return items.map((item) => {
      const clonedItem = deepClone(item)

      if (clonedItem.children?.length) {
        clonedItem.children = convertAuthListToChildren(clonedItem.children)
      }

      // 如果菜单本身有 perms 字段，则不将 authList 转换为子节点
      // 菜单本身的权限应该显示在权限标识列中，而不是作为子节点
      if (item.meta?.authList?.length && !item.perms) {
        const authChildren: AppRouteRecord[] = item.meta.authList.map(
          (auth: { title: string; authMark: string }) => ({
            path: `${item.path}_auth_${auth.authMark}`,
            name: `${String(item.name)}_auth_${auth.authMark}`,
            meta: {
              title: auth.title,
              authMark: auth.authMark,
              isAuthButton: true,
              parentPath: item.path
            }
          })
        )

        clonedItem.children = clonedItem.children?.length
          ? [...clonedItem.children, ...authChildren]
          : authChildren
      }

      return clonedItem
    })
  }

  /**
   * 检查菜单项是否匹配搜索条件
   */
  const matchesSearchFilters = (
    item: AppRouteRecord,
    searchName: string,
    searchRoute: string,
    searchPerms: string,
    searchStatus: string
  ): boolean => {
    // 名称匹配
    if (searchName) {
      const menuTitle = formatMenuTitle(item.meta?.title || '').toLowerCase()
      if (!menuTitle.includes(searchName)) return false
    }

    // 路由匹配
    if (searchRoute) {
      const menuPath = (item.path || '').toLowerCase()
      if (!menuPath.includes(searchRoute)) return false
    }

    // 权限标识匹配
    if (searchPerms) {
      const itemPerms = (item.perms || '').toLowerCase()
      const authMarks = item.meta?.authList?.map((auth) => auth.authMark.toLowerCase()) || []
      const authMark = item.meta?.authMark?.toLowerCase() || ''
      const permsMatch =
        itemPerms.includes(searchPerms) ||
        authMarks.some((mark) => mark.includes(searchPerms)) ||
        authMark.includes(searchPerms) ||
        (item.meta?.authList?.some((auth) => auth.title.toLowerCase().includes(searchPerms)) ??
          false)
      if (!permsMatch) return false
    }

    // 状态匹配
    if (searchStatus && item.status !== searchStatus) return false

    return true
  }

  /**
   * 检查是否有搜索条件
   */
  const hasSearchFilters = computed(() => {
    return Object.values(searchFilters).some((v) =>
      typeof v === 'string' ? v.trim() : v !== undefined && v !== null
    )
  })

  /**
   * 搜索菜单（优化版本：减少不必要的克隆）
   * @param items 菜单项数组
   * @returns 搜索结果数组
   */
  const searchMenu = (items: AppRouteRecord[]): AppRouteRecord[] => {
    const results: AppRouteRecord[] = []

    // 提前计算搜索条件，避免在循环中重复计算
    const searchName = searchFilters.menuName?.toLowerCase().trim() || ''
    const searchRoute = searchFilters.route?.toLowerCase().trim() || ''
    const searchPerms = searchFilters.perms?.toLowerCase().trim() || ''
    const searchStatus = searchFilters.status?.trim() || ''

    for (const item of items) {
      const itemMatches = matchesSearchFilters(
        item,
        searchName,
        searchRoute,
        searchPerms,
        searchStatus
      )

      if (item.children?.length) {
        const matchedChildren = searchMenu(item.children)
        // 如果子节点有匹配的，或者当前节点匹配，则保留
        if (matchedChildren.length > 0 || itemMatches) {
          const clonedItem = deepClone(item)
          clonedItem.children =
            matchedChildren.length > 0 ? matchedChildren : searchMenu(item.children)
          results.push(clonedItem)
        }
      } else if (itemMatches) {
        // 叶子节点且匹配，才需要克隆
        results.push(deepClone(item))
      }
    }

    return results
  }

  // 过滤后的表格数据 - 优化：无搜索条件时直接返回原始数据
  const filteredTableData = computed(() => {
    // 如果没有搜索条件，直接返回原始数据（避免不必要的搜索和克隆）
    if (!hasSearchFilters.value) {
      return convertAuthListToChildren(tableData.value)
    }

    // 有搜索条件时才执行搜索
    const searchedData = searchMenu(tableData.value)
    return convertAuthListToChildren(searchedData)
  })

  /**
   * 添加菜单
   */
  const handleAddMenu = (): void => {
    dialogType.value = 'menu'
    editData.value = null
    lockMenuType.value = false // 允许用户自主选择菜单类型
    dialogVisible.value = true
  }

  /**
   * 添加权限按钮
   * @param parentRow 父级菜单行数据
   */
  const handleAddAuth = (parentRow?: AppRouteRecord): void => {
    dialogType.value = 'button'
    // 新增权限时，只传递 parentId，不传递完整的父菜单对象，避免被误判为编辑模式
    editData.value = parentRow
      ? ({ parentId: parentRow.id } as Partial<MenuFormData & AppRouteRecord>)
      : null
    lockMenuType.value = false
    dialogVisible.value = true
  }

  /**
   * 编辑菜单
   * @param row 菜单行数据
   */
  const handleEditMenu = (row: AppRouteRecord): void => {
    dialogType.value = 'menu'
    // 从缓存中查找parentId和原始path、component
    const { menu: rawMenu, parentId } = findMenuAndParentFromRawData(row.id)

    // 使用原始数据中的 path 和 component，而不是规范化后的
    editData.value = {
      ...row,
      parentId,
      path: rawMenu?.path || row.path || '',
      component: rawMenu?.component || row.component || ''
    } as Partial<MenuFormData & AppRouteRecord>
    lockMenuType.value = false
    dialogVisible.value = true
  }

  /**
   * 编辑权限按钮
   * @param row 权限行数据
   */
  const handleEditAuth = (row: AppRouteRecord): void => {
    dialogType.value = 'button'
    editData.value = {
      id: row.id,
      title: row.meta?.title,
      authMark: row.meta?.authMark,
      perms: row.meta?.authMark,
      parentPath: row.meta?.parentPath
    } as Partial<MenuFormData & AppRouteRecord>
    lockMenuType.value = false
    dialogVisible.value = true
  }

  /**
   * 提交表单数据
   * @param formData 表单数据
   */
  const handleSubmit = async (formData: MenuFormData): Promise<void> => {
    try {
      const isEdit = !!formData.menuId
      if (isEdit) {
        await fetchUpdateMenu(formData)
        ElMessage.success('修改菜单成功')
      } else {
        await fetchAddMenu(formData)
        ElMessage.success('新增菜单成功')
      }
      dialogVisible.value = false
      await getMenuList()
      // 清除前端菜单缓存，确保下次刷新或重新登录时获取最新数据
      menuStore.clearMenuCacheMeta()
      // 重新注册动态路由，立即生效
      await reloadDynamicRoutes()
    } catch (error) {
      safeError('保存菜单失败:', error)
      ElMessage.error(formData.menuId ? '修改菜单失败' : '新增菜单失败')
    }
  }

  /**
   * 删除菜单或权限
   * @param row 菜单或权限行数据
   * @param isAuthButton 是否为权限按钮
   */
  const handleDelete = async (row: AppRouteRecord, isAuthButton = false): Promise<void> => {
    if (!row.id) {
      ElMessage.warning(`${isAuthButton ? '权限' : '菜单'}ID不存在，无法删除`)
      return
    }

    try {
      // 使用 await 确保真正等待用户在确认框中的选择，避免误删
      await ElMessageBox.confirm(
        `确定要删除该${isAuthButton ? '权限' : '菜单'}吗？删除后无法恢复`,
        '提示',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      await fetchDeleteMenu(row.id)
      ElMessage.success('删除成功')
      await getMenuList()
      // 清除前端菜单缓存，确保下次刷新或重新登录时获取最新数据
      menuStore.clearMenuCacheMeta()
      // 重新注册动态路由，立即生效
      await reloadDynamicRoutes()
    } catch (error) {
      // 用户点击取消/关闭时，Element Plus 会抛出 'cancel' 或 'close' 等错误标识，这里统一视为正常中断
      if (error !== 'cancel' && error !== 'close') {
        safeError(`删除${isAuthButton ? '权限' : '菜单'}失败:`, error)
        ElMessage.error('删除失败')
      }
    }
  }

  /**
   * 删除菜单
   * @param row 菜单行数据
   */
  const handleDeleteMenu = (row: AppRouteRecord): Promise<void> => {
    return handleDelete(row, false)
  }

  /**
   * 删除权限按钮
   * @param row 权限行数据
   */
  const handleDeleteAuth = (row: AppRouteRecord): Promise<void> => {
    return handleDelete(row, true)
  }

  /**
   * 切换展开/收起所有菜单
   */
  const toggleExpand = (): void => {
    isExpanded.value = !isExpanded.value
    nextTick(() => {
      if (tableRef.value?.elTableRef && filteredTableData.value) {
        const processRows = (rows: AppRouteRecord[]) => {
          rows.forEach((row) => {
            if (row.children?.length) {
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
  .menu-page {
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
