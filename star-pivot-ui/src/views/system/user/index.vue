<!-- 用户管理页面 -->
<!-- art-full-height 自动计算出页面剩余高度 -->
<!-- art-table-card 一个符合系统样式的 class，同时自动撑满剩余高度 -->
<!-- 更多 useTable 使用示例请移步至 功能示例 下面的高级表格示例或者查看官方文档 -->
<!-- useTable 文档：https://www.artd.pro/docs/zh/guide/hooks/use-table.html -->
<template>
  <div class="user-page art-full-height">
    <div :class="{ 'is-left-collapsed': deptPanelCollapsed }" class="user-layout">
      <!-- 左侧部门树 -->
      <div :class="{ collapsed: deptPanelCollapsed }" class="left-panel">
        <ElCard shadow="never" class="department-tree-card">
          <div class="department-tree-header">
            <div class="dept-title-row">
              <div class="dept-title">
                <el-icon class="dept-title-icon">
                  <OfficeBuilding />
                </el-icon>
                <span>{{ t('system.user.deptTree') }}</span>
              </div>
              <div class="dept-tools">
                <ElButton
                  :aria-label="
                    deptTreeExpandAll ? t('system.user.collapseDept') : t('system.user.expandDept')
                  "
                  class="dept-tool-btn"
                  text
                  @click="toggleDeptTreeExpand"
                >
                  <el-icon :class="{ 'is-collapsed': !deptTreeExpandAll }" class="dept-arrow-icon">
                    <ArrowDown />
                  </el-icon>
                </ElButton>
                <ElButton
                  :loading="deptLoading"
                  :aria-label="t('system.user.refreshDept')"
                  class="dept-tool-btn"
                  text
                  @click="handleRefreshDeptTree"
                >
                  <el-icon>
                    <RefreshRight />
                  </el-icon>
                </ElButton>
              </div>
            </div>
            <div class="dept-search-box">
              <ElInput
                v-model="deptSearchText"
                :placeholder="t('system.user.searchDept')"
                size="small"
                clearable
                @input="handleDeptSearch"
              >
                <template #prefix>
                  <el-icon class="el-input__icon"><Search /></el-icon>
                </template>
              </ElInput>
            </div>
          </div>
          <div class="department-tree-wrapper">
            <ElTree
              :key="deptTreeRenderKey"
              :data="deptTree"
              :props="deptTreeProps"
              :default-expand-all="deptTreeExpandAll"
              :expand-on-click-node="false"
              node-key="deptId"
              :default-checked-keys="selectedDeptId ? [selectedDeptId] : []"
              :filter-node-method="filterDeptNode"
              ref="deptTreeRef"
              @node-click="(data: SysDept) => handleDeptSelect(data.deptId)"
            >
              <template #default="{ node }">
                <span class="custom-tree-node">
                  <span>{{ node.label }}</span>
                </span>
              </template>
            </ElTree>
          </div>
        </ElCard>
        <ElTooltip
          :content="
            deptPanelCollapsed ? t('system.user.expandPanel') : t('system.user.collapsePanel')
          "
          :show-arrow="true"
          effect="dark"
          placement="right"
          popper-class="panel-toggle-tooltip"
        >
          <ElButton
            :aria-label="
              deptPanelCollapsed
                ? t('system.user.expandDeptPanel')
                : t('system.user.collapseDeptPanel')
            "
            class="panel-toggle-btn"
            text
            @click="toggleDeptPanel"
          >
            <el-icon class="panel-toggle-icon">
              <component :is="deptPanelCollapsed ? DArrowRight : DArrowLeft" />
            </el-icon>
          </ElButton>
        </ElTooltip>
      </div>

      <!-- 右侧用户列表 -->
      <div class="right-panel">
        <!-- 搜索栏 -->
        <UserSearch
          v-model="searchForm"
          @search="handleSearch"
          @reset="resetSearchParams"
        ></UserSearch>

        <ElCard class="art-table-card" shadow="never">
          <!-- 表格头部 -->
          <ArtTableHeader v-model:columns="columnChecks" :loading="loading" @refresh="refreshData">
            <template #left>
              <ElSpace wrap>
                <ElButton @click="showDialog('add')" v-ripple v-auth="'system:user:add'">{{
                  t('system.user.addUser')
                }}</ElButton>
                <ElButton
                  type="danger"
                  :disabled="selectedRows.length === 0"
                  @click="handleBatchDelete"
                  v-ripple
                  v-auth="'system:user:delete'"
                >
                  {{ t('system.user.batchDelete') }}
                </ElButton>
                <ElButton
                  type="primary"
                  plain
                  v-ripple
                  v-auth="'system:user:import'"
                  @click="importDialogVisible = true"
                >
                  {{ t('system.user.importUser') }}
                </ElButton>
                <ElButton
                  type="primary"
                  plain
                  v-ripple
                  v-auth="'system:user:export'"
                  @click="handleExportUsers"
                >
                  {{ t('system.user.exportUser') }}
                </ElButton>
              </ElSpace>
            </template>
          </ArtTableHeader>

          <ExcelImportDialog
            v-model="importDialogVisible"
            :title="t('system.user.importTitle')"
            :show-overwrite="true"
            :overwrite-label="t('system.user.importOverwriteLabel')"
            :import-fn="doImportUser"
            :download-template-fn="fetchDownloadUserImportTemplate"
            @success="refreshData"
          />

          <!-- 表格 -->
          <ArtTable
            :loading="loading"
            :data="data"
            :columns="columns"
            :pagination="pagination"
            @selection-change="handleSelectionChange"
            @pagination:size-change="handleSizeChange"
            @pagination:current-change="handleCurrentChange"
          >
          </ArtTable>

          <!-- 用户弹窗 -->
          <UserDialog
            v-model:visible="dialogVisible"
            :type="dialogType"
            :user-data="currentUserData"
            @submit="handleDialogSubmit"
          />
        </ElCard>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import ArtButtonTable from '@/components/core/forms/art-button-table/index.vue'
  import ExcelImportDialog from '@/components/core/forms/excel-import-dialog/index.vue'
  import {
    fetchDownloadUserImportTemplate,
    fetchExportUser,
    fetchImportUserExcel
  } from '@/api/user/user'
  import { useTable } from '@/hooks/core/useTable'
  import { useI18n } from 'vue-i18n'
  import {
    fetchDeleteUser,
    fetchGetUserList,
    fetchResetUserPassword,
    fetchUnlockUser,
    fetchUpdateUserStatus
  } from '@/api/user/user'
  import { fetchGetDeptTree, SysDept } from '@/api/dept/dept'
  import UserSearch from './modules/user-search.vue'
  import UserDialog from './modules/user-dialog.vue'
  import {
    ElButton,
    ElIcon,
    ElInput,
    ElMessage,
    ElMessageBox,
    ElSwitch,
    ElTree
  } from 'element-plus'
  import {
    ArrowDown,
    DArrowLeft,
    DArrowRight,
    OfficeBuilding,
    RefreshRight,
    Search
  } from '@element-plus/icons-vue'
  import { DialogType } from '@/types'
  import ArtTable from '@/components/core/tables/art-table/index.vue'
  import ArtTableHeader from '@/components/core/tables/art-table-header/index.vue'
  import ArtAvatarDisplay from '@/components/core/media/art-avatar-display/index.vue'
  import { useAuth } from '@/hooks/core/useAuth'
  import { useUserStore } from '@/store/modules/user'

  defineOptions({ name: 'User' })

  const { t } = useI18n()
  const { hasAuth } = useAuth()
  const userStore = useUserStore()
  const currentUserId = computed(() => {
    const info = userStore.getUserInfo as any
    return info?.userId ?? info?.user?.userId
  })

  type UserListItem = Api.SystemManage.UserListItem

  // 弹窗相关
  const dialogType = ref<DialogType>('add')
  const dialogVisible = ref(false)
  const currentUserData = ref<Partial<UserListItem>>({})
  // 导入弹窗显隐
  const importDialogVisible = ref(false)

  // 选中行
  const selectedRows = ref<UserListItem[]>([])

  // 搜索表单
  const searchForm = ref<{
    userName: string | undefined
    sex: string | undefined
    phonenumber: string | undefined
    email: string | undefined
    status: string
    deptId: number | undefined
  }>({
    userName: undefined,
    sex: undefined,
    phonenumber: undefined,
    email: undefined,
    status: '0',
    deptId: undefined
  })

  // 部门树相关
  const deptTree = ref<SysDept[]>([])
  const deptLoading = ref(false)
  const selectedDeptId = ref<number | undefined>(undefined)
  const deptTreeRef = ref()
  const deptTreeExpandAll = ref(true)
  const deptTreeRenderKey = ref(0)
  const deptPanelCollapsed = ref(false)

  // 部门树配置
  const deptTreeProps = {
    label: 'deptName',
    children: 'children',
    value: 'deptId'
  }

  // 部门搜索文本
  const deptSearchText = ref('')

  // 部门树节点过滤方法
  const filterDeptNode = (value: string, data: any) => {
    if (!value) return true
    return data.deptName?.includes(value)
  }

  // 处理部门搜索
  const handleDeptSearch = () => {
    if (deptTreeRef.value) {
      deptTreeRef.value.filter(deptSearchText.value)
    }
  }

  // 获取部门树数据
  const getDeptTree = async () => {
    try {
      deptLoading.value = true
      const data = await fetchGetDeptTree()
      deptTree.value = data
    } catch (error) {
      console.error('获取部门树失败:', error)
    } finally {
      deptLoading.value = false
    }
  }

  // 展开/收起部门树
  const toggleDeptTreeExpand = () => {
    deptTreeExpandAll.value = !deptTreeExpandAll.value
    deptTreeRenderKey.value += 1
  }

  // 刷新部门树
  const handleRefreshDeptTree = async () => {
    await getDeptTree()
  }

  // 收起/展开左侧部门面板
  const toggleDeptPanel = () => {
    deptPanelCollapsed.value = !deptPanelCollapsed.value
  }

  // 处理部门选择
  const handleDeptSelect = (deptId: number | undefined) => {
    selectedDeptId.value = deptId
    searchForm.value.deptId = deptId
    handleSearch(searchForm.value)
  }

  // 组件初始化
  onMounted(() => {
    getDeptTree()
  })

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
    // 核心配置
    core: {
      // 适配 useTable 的泛型约束（入参需可接受 any），避免 strictFunctionTypes 下推断为 never
      apiFn: (params: any) => fetchGetUserList(params as Api.SystemManage.UserSearchParams),
      apiParams: {
        pageNum: 1,
        pageSize: 20,
        ...searchForm.value
      },
      columnsFactory: () => [
        { type: 'selection' }, // 勾选列
        { type: 'index', width: 60, label: '序号' }, // 序号
        {
          prop: 'userInfo',
          label: '用户信息',
          width: 300,
          formatter: (row) => {
            const user = row as UserListItem
            const avatarUrl = user.avatar || ''
            const hasAvatar = !!avatarUrl && avatarUrl !== ''

            return h('div', { class: 'user-info flex-c items-center' }, [
              // 有头像时用 ArtAvatarDisplay 展示（支持 OSS 私有桶 presigned，列表页可正常显示）
              hasAvatar &&
                h('div', { class: 'avatar-wrapper' }, [
                  h(ArtAvatarDisplay, {
                    avatarUrl,
                    size: 40,
                    avatarClass: 'size-10'
                  })
                ]),

              // 用户信息容器，根据是否有头像调整间距
              h(
                'div',
                {
                  class: `flex-1 min-w-0 ${hasAvatar ? 'ml-3' : ''}`
                },
                [
                  h(
                    'div',
                    {
                      class: 'flex items-center gap-2',
                      style: { whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis' }
                    },
                    [
                      h(
                        'span',
                        {
                          class: 'user-name font-medium',
                          style: {
                            whiteSpace: 'nowrap',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis',
                            color: 'var(--art-gray-900)'
                          }
                        },
                        user.userName || '未知用户'
                      ),
                      h('span', {
                        class: 'status-indicator',
                        style: {
                          display: 'inline-block',
                          width: '8px',
                          height: '8px',
                          borderRadius: '50%',
                          backgroundColor:
                            user.status === '0' ? 'var(--el-color-success)' : 'var(--el-color-info)'
                        }
                      })
                    ]
                  ),
                  h(
                    'p',
                    {
                      class: 'email text-sm',
                      style: {
                        whiteSpace: 'nowrap',
                        overflow: 'hidden',
                        textOverflow: 'ellipsis',
                        color: 'var(--art-gray-500)'
                      }
                    },
                    user.email || '无邮箱'
                  )
                ]
              )
            ])
          }
        },
        {
          prop: 'deptNameText',
          label: '所属部门',
          sortable: true,
          minWidth: 140,
          showOverflowTooltip: true
        },
        {
          prop: 'roleNamesText',
          label: '关联角色',
          minWidth: 180,
          showOverflowTooltip: true
        },
        {
          prop: 'postNamesText',
          label: '所属岗位',
          minWidth: 180,
          showOverflowTooltip: true
        },
        {
          prop: 'sex',
          label: '性别',
          sortable: true,
          formatter: (row) => {
            const user = row as UserListItem
            // 性别映射：0男 1女 2未知
            const sexMap: Record<string, string> = {
              '0': '男',
              '1': '女',
              '2': '未知'
            }
            return sexMap[user.sex] || user.sex || '未知'
          }
        },
        {
          prop: 'phonenumber',
          label: '手机号',
          formatter: (row) => {
            const user = row as UserListItem
            return user.phonenumber || '未知'
          }
        },
        {
          prop: 'status',
          label: '状态',
          formatter: (row) => {
            const user = row as UserListItem
            return h(ElSwitch, {
              modelValue: user.status === '0',
              activeValue: true,
              inactiveValue: false,
              onChange: (value: string | number | boolean) => {
                handleStatusChange(user, value === true)
              }
            })
          }
        },
        {
          prop: 'createTime',
          label: '创建日期',
          sortable: true
        },
        {
          prop: 'operation',
          label: '操作',
          width: 180,
          fixed: 'right', // 固定列
          formatter: (row) => {
            const user = row as UserListItem
            const actions: any[] = []

            // 编辑用户按钮权限：system:user:edit
            if (hasAuth('system:user:edit')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'edit',
                  tooltip: '编辑用户',
                  onClick: () => showDialog('edit', user)
                })
              )

              // 解锁账户按钮（管理员操作）- 只在账户被锁定时显示
              if (user.isLocked === true) {
                actions.push(
                  h(ArtButtonTable, {
                    icon: 'ri:lock-unlock-line',
                    iconClass: 'bg-warning/12 text-warning',
                    tooltip: '解锁账户',
                    onClick: () => unlockUser(user)
                  })
                )
              }
            }

            // 删除用户按钮权限：system:user:delete
            if (hasAuth('system:user:delete')) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'delete',
                  tooltip: '删除用户',
                  onClick: () => deleteUser(user)
                })
              )
            }
            if (hasAuth('system:user:resetPwd') && user.userId !== currentUserId.value) {
              actions.push(
                h(ArtButtonTable, {
                  type: 'sync',
                  tooltip: '重置密码',
                  onClick: () => resetPwd(user)
                })
              )
            }

            if (actions.length === 0) {
              // 无任何操作权限时返回空占位
              return h('span', { style: 'color: var(--art-gray-500)' }, '')
            }

            return h('div', actions)
          }
        }
      ]
    },
    // 数据处理
    transform: {
      // 数据转换器
      dataTransformer: (records: UserListItem[]) => {
        // 类型守卫检查
        if (!Array.isArray(records)) {
          return []
        }

        return records.map((r) => {
          const user = r
          const roleNames = Array.isArray(user.roleNames) ? user.roleNames.filter(Boolean) : []
          const postNames = Array.isArray(user.postNames) ? user.postNames.filter(Boolean) : []
          return {
            ...user,
            deptNameText: user.deptName || '-',
            roleNamesText: roleNames.length ? roleNames.join('、') : '-',
            postNamesText: postNames.length ? postNames.join('、') : '-'
          }
        })
      }
    }
  })

  /**
   * 搜索处理
   * @param params 参数
   */
  const handleSearch = (params: Record<string, any>) => {
    // 搜索参数赋值
    Object.assign(searchParams, params)
    getData()
  }

  /**
   * 显示用户弹窗
   */
  const showDialog = (type: DialogType, row?: UserListItem): void => {
    dialogType.value = type
    currentUserData.value = row || {}
    nextTick(() => {
      dialogVisible.value = true
    })
  }

  /**
   * 删除用户
   */
  const deleteUser = async (row: UserListItem): Promise<void> => {
    try {
      await ElMessageBox.confirm(`确定要注销该用户吗？`, '注销用户', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'error'
      })
      await fetchDeleteUser([row.userId])
      refreshData()
      ElMessage.success('注销成功')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('删除用户失败:', error)
        ElMessage.error('注销失败')
      }
    }
  }

  /**
   * 批量删除用户
   */
  const handleBatchDelete = async (): Promise<void> => {
    if (selectedRows.value.length === 0) {
      ElMessage.warning('请选择要删除的用户')
      return
    }
    try {
      const userNames = selectedRows.value.map((row) => row.userName || '未知用户').join('、')
      await ElMessageBox.confirm(
        `确定要注销以下 ${selectedRows.value.length} 个用户吗？\n${userNames}`,
        '批量注销用户',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'error'
        }
      )
      const userIds = selectedRows.value
        .map((row) => row.userId)
        .filter((id): id is number => id !== undefined && id !== null)
      if (userIds.length === 0) {
        ElMessage.warning('所选用户中没有有效的用户ID')
        return
      }
      await fetchDeleteUser(userIds)
      selectedRows.value = []
      refreshData()
      ElMessage.success('注销成功')
    } catch (error) {
      if (error !== 'cancel') {
        console.error('批量删除用户失败:', error)
        ElMessage.error('注销失败')
      }
    }
  }

  /**
   * 处理弹窗提交事件
   */
  const handleDialogSubmit = async () => {
    try {
      dialogVisible.value = false
      currentUserData.value = {}
      // 刷新列表数据
      refreshData()
    } catch (error) {
      console.error('提交失败:', error)
    }
  }

  /**
   * 处理表格行选择变化
   */
  const handleSelectionChange = (selection: UserListItem[]): void => {
    selectedRows.value = selection
  }

  /**
   * 处理用户状态切换
   */
  const handleStatusChange = async (row: UserListItem, value: boolean) => {
    try {
      const newStatus = value ? '0' : '1'
      await fetchUpdateUserStatus(row.userId, Number(newStatus))
      ElMessage.success(value ? '启用成功' : '禁用成功')
      // 更新本地数据
      row.status = newStatus
      // 刷新列表
      refreshData()
    } catch (error) {
      console.error('更新状态失败:', error)
      ElMessage.error('更新状态失败')
      // 刷新列表以恢复原状态
      refreshData()
    }
  }

  /**
   * 解锁账户（管理员操作）
   */
  const unlockUser = async (row: UserListItem): Promise<void> => {
    try {
      await ElMessageBox.confirm(`确定要解锁用户 "${row.userName}" 的账户吗？`, '解锁账户', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await fetchUnlockUser(row.userId)
      ElMessage.success('账户解锁成功')
      refreshData()
    } catch (error: any) {
      if (error !== 'cancel') {
        console.error('解锁账户失败:', error)
        // 后端返回的消息会包含在 error 中，直接显示
        const errorMsg = error?.message || error?.response?.data?.msg || '解锁失败'
        ElMessage.error(errorMsg)
      }
    }
  }

  /**
   * 管理员重置用户密码
   */
  const resetPwd = async (row: UserListItem): Promise<void> => {
    // 使用 MutationObserver 阻止浏览器自动填充已保存的密码
    const observer = new MutationObserver((mutations) => {
      for (const mutation of mutations) {
        for (const node of Array.from(mutation.addedNodes)) {
          if (!(node instanceof HTMLElement)) continue
          const input =
            node instanceof HTMLInputElement
              ? node
              : (node.querySelector('input[type="password"]') as HTMLInputElement | null)
          if (input && input.type === 'password') {
            input.setAttribute('autocomplete', 'new-password')
            if (input.value) {
              input.value = ''
              input.dispatchEvent(new Event('input', { bubbles: true }))
            }
            observer.disconnect()
            return
          }
        }
      }
    })
    observer.observe(document.body, { childList: true, subtree: true })

    try {
      const { value } = await ElMessageBox.prompt(
        `请输入用户 "${row.userName}" 的新密码`,
        '重置密码',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          inputPattern: /^(?=.*[a-zA-Z])(?=.*\d)[a-zA-Z\d]{6,20}$/,
          inputErrorMessage: '密码长度 6-20 位，必须包含字母和数字',
          inputPlaceholder: '请输入新密码',
          inputType: 'password'
        }
      )
      if (!value) return
      await fetchResetUserPassword(row.userId, value)
      ElMessage.success('重置密码成功')

      // 清除登录页“记住密码”存储的旧密码，防止下次登录时自动填充旧密码
      try {
        const saved = localStorage.getItem('login-info')
        if (saved) {
          const info = JSON.parse(saved)
          if (info.username === row.userName) {
            delete info.password
            localStorage.setItem('login-info', JSON.stringify(info))
          }
        }
      } catch {
        /* ignore */
      }
    } catch (error: any) {
      if (error === 'cancel' || error === 'close') return
      console.error('重置密码失败:', error)
      ElMessage.error(error?.message || '重置密码失败')
    } finally {
      observer.disconnect()
    }
  }

  /**
   * 导出用户数据
   */
  const doImportUser = (file: File, updateSupport: boolean) =>
    fetchImportUserExcel(file, updateSupport)

  const handleExportUsers = async () => {
    try {
      await fetchExportUser({ ...searchForm.value })
    } catch (error) {
      console.error('导出用户失败:', error)
    }
  }
</script>

<style lang="scss" scoped>
  .user-page {
    --panel-radius: 12px;
    --panel-border: 1px solid var(--art-card-border);
    --panel-shadow: 0 4px 14px rgb(15 23 42 / 6%);
    --panel-shadow-hover: 0 8px 20px rgb(15 23 42 / 10%);
  }

  .user-page {
    padding: 0;
    background-color: var(--default-bg-color);
  }

  .user-layout {
    position: relative;
    display: flex;
    gap: 0;
    height: 100%;
    overflow: hidden;
  }

  .left-panel {
    position: relative;
    display: flex;
    flex-direction: column;
    width: 280px;
    min-width: 280px;
    overflow: visible;
    background-color: var(--default-box-color);
    border: var(--panel-border);
    border-radius: var(--panel-radius);
    box-shadow: var(--panel-shadow);
    transition:
      width 0.22s ease,
      min-width 0.22s ease,
      opacity 0.2s ease,
      border-color 0.2s ease,
      box-shadow 0.2s ease;
  }

  .left-panel:hover {
    box-shadow: var(--panel-shadow-hover);
  }

  .left-panel.collapsed {
    width: 0;
    min-width: 0;
    border: none;
  }

  .left-panel.collapsed .department-tree-card {
    opacity: 0;
    pointer-events: none;
  }

  .dark .left-panel {
    box-shadow: none;
  }

  .panel-toggle-btn {
    position: absolute;
    top: 50%;
    left: 100%;
    z-index: 3;
    width: 22px;
    height: 40px;
    padding: 0;
    color: var(--el-color-primary);
    border: var(--panel-border);
    border-left: none;
    border-radius: 0 10px 10px 0;
    background: var(--el-color-primary-light-9);
    transform: translateY(-50%);
    transition:
      left 0.22s ease,
      color 0.2s ease,
      background-color 0.2s ease;
  }

  .panel-toggle-btn:hover {
    color: var(--el-color-primary-light-3);
    background: var(--el-color-primary-light-8);
    transform: none;
  }

  .panel-toggle-icon {
    font-size: 14px;
  }

  .right-panel {
    display: flex;
    flex: 1;
    flex-direction: column;
    overflow: hidden;
    background-color: var(--default-box-color);
    border: var(--panel-border);
    border-radius: var(--panel-radius);
    box-shadow: var(--panel-shadow);
    transition: box-shadow 0.25s ease;

    &:hover {
      box-shadow: var(--panel-shadow-hover);
    }
  }

  .dark .right-panel {
    box-shadow: none;
  }

  .department-tree-card {
    display: flex;
    flex-direction: column;
    height: 100%;
    border: none;
    box-shadow: none;
  }

  :deep(.department-tree-card > .el-card__body) {
    display: flex;
    flex: 1;
    flex-direction: column;
    height: 100%;
    padding: 0;
  }

  .department-tree-header {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding: 10px 10px 8px;
    border-bottom: var(--panel-border);
    background: linear-gradient(180deg, rgb(64 158 255 / 6%) 0%, transparent 100%);
  }

  .dept-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    width: 100%;
    padding-bottom: 2px;
  }

  .dept-title {
    display: inline-flex;
    gap: 6px;
    align-items: center;
    font-size: 14px;
    font-weight: 600;
    color: var(--art-gray-800);
  }

  .dept-title-icon {
    font-size: 13px;
    color: var(--art-gray-500);
  }

  .dept-tools {
    display: inline-flex;
    gap: 6px;
    align-items: center;
  }

  .dept-tool-btn {
    width: 20px;
    height: 20px;
    padding: 0;
    color: var(--art-gray-500);
    border: none;
    border-radius: 4px;
    background: transparent;

    &:hover {
      color: var(--art-gray-700);
      background: var(--art-gray-100);
      transform: none;
    }
  }

  .dept-arrow-icon {
    font-size: 14px;
    transition: transform 0.2s ease;
  }

  .dept-arrow-icon.is-collapsed {
    transform: rotate(-90deg);
  }

  .dept-search-box {
    width: 100%;
  }

  :deep(.el-input) {
    width: 100%;
  }

  .dept-search-box :deep(.el-input__wrapper) {
    border-radius: 8px;
    box-shadow: inset 0 0 0 1px var(--art-card-border);
  }

  .dept-search-box :deep(.el-input__wrapper:hover) {
    box-shadow: inset 0 0 0 1px var(--art-gray-400);
  }

  .department-tree-wrapper {
    flex: 1;
    padding: 6px 8px 8px;
    overflow-y: auto;
    background-color: var(--default-box-color);
  }

  .department-tree-wrapper::-webkit-scrollbar {
    width: 6px;
  }

  .department-tree-wrapper::-webkit-scrollbar-thumb {
    border-radius: 999px;
    background: rgb(148 163 184 / 45%);
  }

  :deep(.el-tree) {
    width: 100%;
    height: 100%;
    overflow: auto;
  }

  :deep(.el-tree-node__content) {
    height: 32px;
    line-height: 32px;
    padding-right: 8px;
    border-radius: 6px;
    transition: all 0.2s ease;

    &:hover {
      background-color: var(--art-gray-100);
    }
  }

  :deep(.el-tree-node.is-current > .el-tree-node__content) {
    color: var(--art-primary);
    background-color: rgb(64 158 255 / 12%);
  }

  :deep(.el-tree-node__expand-icon) {
    font-size: 14px;
  }

  .custom-tree-node {
    display: flex;
    align-items: center;
    width: 100%;
  }

  .art-table-card {
    display: flex;
    flex: 1;
    flex-direction: column;
    border: none;
    box-shadow: none;
  }

  :deep(.el-table) {
    border-radius: 8px;
    border: 1px solid var(--art-card-border);

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

  :deep(.panel-toggle-tooltip) {
    padding: 8px 12px;
    border-radius: 8px;
    font-size: 13px;
    line-height: 1;
  }

  :deep(.el-tag) {
    font-weight: 500;
    border-radius: 6px;
  }

  .status-indicator {
    animation: pulse 2s infinite;
  }

  @keyframes pulse {
    0%,
    100% {
      opacity: 1;
    }

    50% {
      opacity: 0.5;
    }
  }
</style>
