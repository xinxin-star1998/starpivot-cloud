<template>
  <ElDialog
    v-model="visible"
    :title="dialogType === 'add' ? '新增角色' : '编辑角色'"
    width="30%"
    align-center
    @close="handleClose"
  >
    <ElForm ref="roleForm" :model="form" :rules="rules" label-width="120px">
      <ElFormItem label="角色名称" prop="roleName">
        <ElInput v-model="form.roleName" placeholder="请输入角色名称" />
      </ElFormItem>
      <ElFormItem label="角色编码" prop="roleKey">
        <ElInput v-model="form.roleKey" placeholder="请输入角色编码" />
      </ElFormItem>
      <ElFormItem label="显示顺序" prop="roleSort">
        <ElInput v-model="form.roleSort" placeholder="请输入显示顺序" />
      </ElFormItem>
      <ElFormItem label="备注" prop="description">
        <ElInput v-model="form.remark" type="textarea" :rows="3" placeholder="请输入角色描述" />
      </ElFormItem>
      <ElFormItem label="状态">
        <ElSwitch v-model="statusSwitch" />
      </ElFormItem>
      <ElFormItem label="菜单权限">
        <div class="permission-tree-wrapper">
          <!-- 控制选项 -->
          <div class="permission-controls">
            <ElCheckbox v-model="menuExpandAll" @change="toggleMenuExpandAll">展开/折叠</ElCheckbox>
            <ElCheckbox
              v-model="menuSelectAll"
              :indeterminate="menuSelectAllIndeterminate"
              @change="toggleMenuSelectAll"
            >
              全选/全不选
            </ElCheckbox>
            <ElCheckbox v-model="isMenuParentChildLinked" @change="handleMenuCheckStrictlyChange">
              父子联动
            </ElCheckbox>
          </div>
          <!-- 树结构容器 -->
          <div
            class="permission-tree-container"
            :class="isDark ? 'dark-bg' : 'light-bg'"
            ref="menuTreeContainerRef"
          >
            <ElTree
              ref="menuTreeRef"
              :data="menuTreeData"
              show-checkbox
              node-key="menuId"
              :default-expand-all="menuExpandAll"
              :check-strictly="menuCheckStrictly"
              :props="menuTreeProps"
              @check="handleMenuTreeCheck"
              @node-expand="handleMenuNodeExpand"
              @node-collapse="handleMenuNodeCollapse"
            >
              <template #default="{ data }">
                <span>{{ data.label || data.menuName }}</span>
              </template>
            </ElTree>
          </div>
        </div>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" @click="handleSubmit">提交</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchAddRole, fetchUpdateRole } from '@/api/role/role'
  import { fetchGetMenuTree, type SysMenu } from '@/api/menu/menu'
  import { fetchGetRoleMenus } from '@/api/role/role'
  import { useSettingStore } from '@/store/modules/setting'
  import { useCheckableTree } from '@/composables/useCheckableTree'

  type RoleListItem = Api.SystemManage.RoleListItem

  interface Props {
    modelValue: boolean
    dialogType: 'add' | 'edit'
    roleData?: RoleListItem
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    dialogType: 'add',
    roleData: undefined
  })

  const emit = defineEmits<Emits>()

  const roleForm = ref<FormInstance>()
  const menuTree = useCheckableTree<SysMenu>({ keyField: 'menuId', childrenField: 'children' })

  // 兼容原模板变量命名（保持模板不动）
  const menuTreeRef = menuTree.treeRef
  const menuTreeContainerRef = menuTree.containerRef

  // 主题状态
  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)

  // 菜单树数据
  const menuTreeData = menuTree.data
  const menuTreeProps = {
    children: 'children',
    label: 'menuName'
  }

  // 菜单树控制状态
  const menuExpandAll = menuTree.expandAll
  const menuSelectAll = menuTree.selectAll
  const menuSelectAllIndeterminate = menuTree.indeterminate
  const isMenuParentChildLinked = menuTree.parentChildLinked
  const menuCheckStrictly = menuTree.checkStrictly
  /**
   * 弹窗显示状态双向绑定
   */
  const visible = computed({
    get: () => props.modelValue,
    set: (value: boolean) => emit('update:modelValue', value)
  })

  /**
   * 状态开关双向绑定
   * status为0时选中（true），为1时不选中（false）
   */
  const statusSwitch = computed({
    get: () => {
      // 兼容字符串和数字类型
      const status = Number(form.status)
      return status === 0
    },
    set: (value: boolean) => {
      ;(form as any).status = value ? 0 : 1
    }
  })
  /**
   * 表单验证规则
   */
  const rules = reactive<FormRules>({
    roleName: [
      { required: true, message: '请输入角色名称', trigger: 'blur' },
      { min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur' }
    ],
    roleKey: [
      { required: true, message: '请输入角色编码', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    remark: [{ required: true, message: '请输入角色描述', trigger: 'blur' }]
  })

  /**
   * 表单数据
   */
  const form = reactive({
    roleId: 0,
    roleName: '',
    roleKey: '',
    roleSort: 0,
    remark: '',
    createTime: '',
    status: 0,
    menuCheckStrictly: 1,
    deptCheckStrictly: 1,
    menuIds: [] as number[] // 菜单ID列表
  } as unknown as RoleListItem & { menuIds: number[] })

  // 防止重复加载的标记
  const isLoadingMenuTree = ref(false)

  /**
   * 监听弹窗打开，初始化表单数据
   */
  watch(
    () => props.modelValue,
    async (newVal: boolean) => {
      if (newVal) {
        initForm()
        // 重置树数据
        menuTreeData.value = []
        // 重置加载标记
        isLoadingMenuTree.value = false
        // 等待弹窗完全打开后再加载数据
        await nextTick()
        // 防止重复调用
        if (!isLoadingMenuTree.value) {
          isLoadingMenuTree.value = true
          loadMenuTree().finally(() => {
            isLoadingMenuTree.value = false
          })
        }
      } else {
        // 关闭时清空数据
        menuTreeData.value = []
        isLoadingMenuTree.value = false
        if (menuTreeContainerRef.value) {
          menuTreeContainerRef.value.style.height = 'auto'
        }
      }
    }
  )

  /**
   * 监听角色数据变化，更新表单
   * 注意：只在弹窗已打开且角色数据变化时更新，避免与 modelValue watch 重复调用
   */
  watch(
    () => props.roleData,
    (newData: RoleListItem | undefined) => {
      // 只在弹窗已打开且角色数据存在时更新，避免重复初始化
      if (newData && props.modelValue && props.dialogType === 'edit') {
        // 只更新表单数据，不重新加载树（树已在 modelValue watch 中加载）
        const status = newData.status != null ? Number(newData.status) : 0
        Object.assign(form, {
          ...newData,
          status: isNaN(status) ? 0 : status,
          menuCheckStrictly: newData.menuCheckStrictly ?? 0,
          menuIds: []
        })
        // 如果树数据已加载，重新加载角色关联的菜单
        if (menuTreeData.value.length > 0 && form.roleId) {
          loadRoleMenuIds()
        }
      }
    },
    { deep: true }
  )

  /**
   * 加载菜单树数据
   */
  const loadMenuTree = async () => {
    try {
      const menuList = await fetchGetMenuTree()
      if (Array.isArray(menuList) && menuList.length > 0) {
        menuTreeData.value = menuList
        // 等待 DOM 更新
        await nextTick()
        // 如果是编辑模式，加载已选中的菜单
        if (props.dialogType === 'edit' && form.roleId) {
          await loadRoleMenuIds()
          // 等待选中状态更新
          await nextTick()
        }
        // 延迟调整容器高度，确保 DOM 完全渲染
        setTimeout(() => {
          menuTree.adjustContainerHeight()
        }, 100)
      }
    } catch (error) {
      // API 调用失败的错误已在 HTTP 拦截器中统一处理并显示错误消息
      if (import.meta.env.DEV) {
        console.error('加载菜单树失败:', error)
      }
    }
  }

  /**
   * 加载角色已分配的菜单ID列表
   */
  const loadRoleMenuIds = async () => {
    try {
      const menuIds = await fetchGetRoleMenus(form.roleId)
      // 确保menuIds是数组类型，处理null或undefined情况
      const safeMenuIds = Array.isArray(menuIds) ? menuIds : []

      await menuTree.applyCheckedKeys(safeMenuIds)
      // 同步表单 + 全选/半选
      handleMenuTreeCheck()
    } catch (error) {
      // API 调用失败的错误已在 HTTP 拦截器中统一处理并显示错误消息
      if (import.meta.env.DEV) {
        console.error('加载角色菜单ID失败:', error)
      }
    }
  }

  /**
   * 处理菜单树选中变化
   */
  const handleMenuTreeCheck = () => {
    // 获取树的选中状态
    const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
    const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
    form.menuIds = [...checkedKeys, ...halfCheckedKeys].filter(
      (key: any) => typeof key === 'number'
    ) as number[]
    menuTree.syncSelectState()
  }

  /**
   * 切换菜单树展开/收起
   */
  const toggleMenuExpandAll = () => {
    menuTree.toggleExpandAll()
  }

  /**
   * 处理菜单树节点展开
   */
  const handleMenuNodeExpand = () => {
    menuTree.handleNodeExpand()
  }

  /**
   * 处理菜单树节点收起
   */
  const handleMenuNodeCollapse = () => {
    menuTree.handleNodeCollapse()
  }

  /**
   * 切换菜单树全选/全不选
   */
  const toggleMenuSelectAll = () => {
    menuTree.toggleSelectAll()
    handleMenuTreeCheck()
  }

  /**
   * 处理菜单树父子联动变化
   */
  const handleMenuCheckStrictlyChange = () => {
    menuTree.handleCheckStrictlyChange()
    handleMenuTreeCheck()
  }

  /**
   * 初始化表单数据
   * 根据弹窗类型填充表单或重置表单
   */
  const initForm = () => {
    if (props.dialogType === 'edit' && props.roleData) {
      // 确保 status 字段被正确赋值，兼容字符串和数字类型
      const status = props.roleData.status != null ? Number(props.roleData.status) : 0
      Object.assign(form, {
        ...props.roleData,
        status: isNaN(status) ? 0 : status,
        menuCheckStrictly: props.roleData.menuCheckStrictly ?? 0,
        menuIds: []
      })
    } else {
      Object.assign(form, {
        roleId: 0,
        roleName: '',
        roleKey: '',
        roleSort: 0,
        remark: '',
        createTime: '',
        status: 0,
        menuCheckStrictly: 0,
        menuIds: []
      })
    }
  }

  /**
   * 关闭弹窗并重置表单
   */
  const handleClose = () => {
    visible.value = false
    roleForm.value?.resetFields()
    // 清空树选中状态
    menuTreeRef.value?.setCheckedKeys([])
    // 重置控制状态
    menuExpandAll.value = false
    menuSelectAll.value = false
  }

  /**
   * 提交表单
   * 验证通过后调用接口保存数据
   */
  const handleSubmit = async () => {
    if (!roleForm.value) return

    try {
      await roleForm.value.validate()
      // 获取选中的菜单ID和半选中的菜单ID
      const checkedKeys = menuTreeRef.value?.getCheckedKeys() || []
      const halfCheckedKeys = menuTreeRef.value?.getHalfCheckedKeys() || []
      // 合并完全选中和半选中的节点ID，确保父级菜单ID被传递
      const allMenuKeys = [...checkedKeys, ...halfCheckedKeys]
      // 去重并过滤出数字类型的ID
      const menuIds = Array.from(new Set(allMenuKeys)).filter(
        (key: any) => typeof key === 'number'
      ) as number[]

      const submitData = {
        ...form,
        menuIds
      }

      // 根据弹窗类型调用对应的API
      if (props.dialogType === 'add') {
        await fetchAddRole(submitData)
      } else {
        await fetchUpdateRole(submitData)
      }
      emit('success')
      handleClose()
    } catch (error) {
      // 表单验证失败：Element Plus 会通过 UI 显示错误，这里不需要处理
      // API 调用失败：错误已在 HTTP 拦截器中统一处理并显示错误消息
      if (import.meta.env.DEV) {
        console.error('提交失败:', error)
      }
    }
  }
</script>

<style scoped lang="scss">
  :deep(.el-dialog) {
    overflow: hidden;
    border-radius: 16px;

    .el-dialog__header {
      padding: 20px 24px;
      margin: 0;
      background: linear-gradient(
        135deg,
        var(--el-color-primary-light-9) 0%,
        var(--el-color-primary-light-8) 100%
      );
      border-bottom: 1px solid var(--art-card-border);

      .el-dialog__title {
        font-size: 18px;
        font-weight: 600;
        color: var(--art-gray-900);
      }
    }

    .el-dialog__body {
      max-height: 60vh;
      padding: 24px;
      overflow-y: auto;
    }

    .el-dialog__footer {
      padding: 16px 24px;
      background-color: var(--art-gray-50);
      border-top: 1px solid var(--art-card-border);
    }
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: var(--art-gray-700);
  }

  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: var(--art-shadow-sm);
    }
  }

  :deep(.el-button) {
    padding: 10px 24px;
    font-weight: 500;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-1px);
    }
  }

  .permission-tree-wrapper {
    .permission-controls {
      display: flex;
      gap: 20px;
      padding-bottom: 12px;
      margin-bottom: 12px;
      color: var(--el-text-color-primary);
      border-bottom: 1px solid var(--el-border-color-lighter);
      transition: color 0.3s ease;
    }

    .permission-tree-container {
      height: auto;
      min-height: 100px;
      max-height: 600px;
      padding: 16px;
      overflow: auto;
      border: 1px solid var(--el-border-color-lighter);
      border-radius: 8px;
      transition:
        height 0.3s ease,
        background-color 0.3s ease;

      &.light-bg {
        background: var(--default-box-color);
      }

      &.dark-bg {
        background: var(--el-bg-color-page);
      }
    }
  }

  :deep(.el-tree) {
    .el-tree-node__content {
      height: 36px;
      border-radius: 6px;
      transition: all 0.2s ease;

      &:hover {
        background-color: var(--art-gray-100);
      }
    }
  }

  :deep(.el-switch) {
    --el-switch-on-color: var(--el-color-primary);
  }
</style>
