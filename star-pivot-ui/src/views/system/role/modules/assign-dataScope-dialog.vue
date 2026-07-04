<template>
  <ElDialog
    v-model="visible"
    title="分配数据权限"
    width="800px"
    align-center
    class="el-dialog-border"
    @close="handleClose"
  >
    <div class="permission-dialog">
      <ElForm ref="roleForm" :model="form" :rules="rules" label-width="120px">
        <ElFormItem label="角色名称" prop="roleName">
          <ElInput v-model="form.roleName" placeholder="请输入角色名称" disabled />
        </ElFormItem>
        <ElFormItem label="角色编码" prop="roleKey">
          <ElInput v-model="form.roleKey" placeholder="请输入角色编码" disabled />
        </ElFormItem>
        <ElFormItem label="数据范围" prop="dataScope">
          <ElSelect
            v-model="form.dataScope"
            placeholder="请选择数据范围"
            @change="handleDataScopeChange"
          >
            <ElOption value="1" label="全部数据权限">全部数据权限</ElOption>
            <ElOption value="2" label="自定数据权限">自定数据权限</ElOption>
            <ElOption value="3" label="本部门数据权限">本部门数据权限</ElOption>
            <ElOption value="4" label="本部门及以下数据权限">本部门及以下数据权限</ElOption>
            <ElOption value="5" label="仅本人数据权限">仅本人数据权限</ElOption>
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="部门权限" prop="deptIds" v-if="showDeptTree">
          <div class="dept-permission-wrapper">
            <!-- 控制选项 -->
            <div class="permission-controls">
              <ElCheckbox v-model="deptExpandAll" @change="toggleDeptExpandAll"
                >展开/折叠</ElCheckbox
              >
              <ElCheckbox
                v-model="deptSelectAll"
                :indeterminate="deptSelectAllIndeterminate"
                @change="toggleDeptSelectAll"
              >
                全选/全不选
              </ElCheckbox>
              <ElCheckbox v-model="isDeptParentChildLinked" @change="handleDeptCheckStrictlyChange">
                父子联动
              </ElCheckbox>
            </div>
            <!-- 树结构容器 -->
            <div
              class="permission-tree-container"
              :class="isDark ? 'dark-bg' : 'light-bg'"
              ref="deptTreeContainerRef"
            >
              <div v-loading="deptLoading" class="tree-wrapper">
                <ElTree
                  ref="deptTreeRef"
                  :data="deptTreeData"
                  show-checkbox
                  node-key="deptId"
                  :default-expand-all="deptExpandAll"
                  :check-strictly="deptCheckStrictly"
                  :props="deptTreeProps"
                  @check="handleDeptTreeCheck"
                  @node-expand="handleDeptNodeExpand"
                  @node-collapse="handleDeptNodeCollapse"
                >
                  <template #default="{ data }">
                    <span>{{ data.deptName }}</span>
                  </template>
                </ElTree>
              </div>
            </div>
          </div>
        </ElFormItem>
      </ElForm>
    </div>
    <template #footer>
      <ElButton @click="handleClose">取消</ElButton>
      <ElButton type="primary" @click="savePermission">保存</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import type {FormInstance, FormRules} from 'element-plus'
import {ElForm, ElFormItem, ElInput, ElMessage, ElOption, ElSelect} from 'element-plus'
import {fetchGetDeptTree, type SysDept} from '@/api/dept/dept'
import {fetchAssignPermission, fetchGetRoleDeptIds} from '@/api/role/role'
import {useSettingStore} from '@/store/modules/setting'
import {useCheckableTree} from '@/composables/useCheckableTree'

type RoleListItem = Api.SystemManage.RoleListItem

  interface Props {
    modelValue: boolean
    roleData?: RoleListItem
  }

  interface Emits {
    (e: 'update:modelValue', value: boolean): void
    (e: 'success'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    modelValue: false,
    roleData: undefined
  })

  const emit = defineEmits<Emits>()

  // 主题状态
  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)

  // 表单引用
  const roleForm = ref<FormInstance>()

  // 部门树（通用封装）
  const deptTree = useCheckableTree<SysDept>({ keyField: 'deptId', childrenField: 'children' })
  const deptLoading = ref(false)

  /**
   * 表单数据
   */
  const form = reactive({
    roleId: 0,
    roleName: '',
    roleKey: '',
    dataScope: '1', // 默认全部数据权限
    deptIds: [] as number[]
  })

  /**
   * 表单验证规则
   */
  const rules = reactive<FormRules>({
    roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
    roleKey: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
    dataScope: [{ required: true, message: '请选择数据范围', trigger: 'change' }]
  })

  /**
   * 是否显示部门树（只有自定数据权限时显示）
   */
  const showDeptTree = computed(() => {
    return form.dataScope === '2'
  })

  // 兼容原模板变量命名（保持模板不动，脚本大幅收敛）
  const deptTreeRef = deptTree.treeRef
  const deptTreeContainerRef = deptTree.containerRef
  const deptExpandAll = deptTree.expandAll
  const deptSelectAll = deptTree.selectAll
  const deptSelectAllIndeterminate = deptTree.indeterminate
  const isDeptParentChildLinked = deptTree.parentChildLinked
  const deptCheckStrictly = deptTree.checkStrictly
  const deptTreeData = deptTree.data

  /**
   * 弹窗显示状态双向绑定
   */
  const visible = computed({
    get: () => props.modelValue,
    set: (value: boolean) => emit('update:modelValue', value)
  })

  /**
   * 部门树形组件配置
   */
  // Tree 组件 props（模板仍使用）
  const deptTreeProps = { children: 'children', label: 'deptName' }

  // 防止重复加载的标记
  const isLoadingDeptTree = ref(false)
  // 标记是否需要加载角色部门权限
  const shouldLoadRoleDeptIds = ref(false)

  /**
   * 使用 ResizeObserver 监听容器尺寸变化，自动调整高度
   */
  let resizeObserver: ResizeObserver | null = null
  let resizeRafId = 0

  const stopResizeObserve = () => {
    if (resizeObserver) {
      resizeObserver.disconnect()
      resizeObserver = null
    }
    if (resizeRafId) {
      cancelAnimationFrame(resizeRafId)
      resizeRafId = 0
    }
  }

  const startResizeObserve = () => {
    const el = deptTreeContainerRef.value
    if (!el) return

    stopResizeObserve()

    resizeObserver = new ResizeObserver(() => {
      if (resizeRafId) cancelAnimationFrame(resizeRafId)
      resizeRafId = requestAnimationFrame(() => {
        // 容器/树渲染完成后再计算高度
        if (deptTreeData.value.length > 0) {
          deptTree.adjustContainerHeight()
        }
      })
    })

    resizeObserver.observe(el)
  }

  onMounted(() => {
    // 这里不直接绑定：容器可能被 v-if 控制，首次 mounted 时 ref 可能为 undefined
  })

  onUnmounted(() => {
    stopResizeObserve()
  })

  /**
   * 当弹窗打开 + 显示部门树 + 容器挂载后，再绑定 ResizeObserver
   * - 关闭弹窗或切换为非自定权限时，自动解绑，避免观察已卸载节点
   */
  watch(
    [() => props.modelValue, showDeptTree, deptTreeContainerRef],
    async ([isVisible, isShowTree, containerEl]: [boolean, boolean, HTMLElement | undefined]) => {
      if (isVisible && isShowTree && containerEl) {
        await nextTick()
        startResizeObserve()
        deptTree.adjustContainerHeight()
      } else {
        stopResizeObserve()
      }
    },
    { immediate: true }
  )

  /**
   * 监听树组件和数据就绪状态，自动加载角色部门权限
   */
  watch(
    [deptTreeRef, deptTreeData, () => shouldLoadRoleDeptIds.value],
    ([tree, data, shouldLoad]: [unknown, SysDept[], boolean]) => {
      // 当树组件已初始化、数据已加载且需要加载角色部门权限时
      if (tree && Array.isArray(data) && data.length > 0 && shouldLoad) {
        // 先置为 false，避免多次触发重复调度
        shouldLoadRoleDeptIds.value = false
        nextTick(() => {
          loadRoleDeptIds()
        })
      }
    },
    { immediate: false }
  )

  /**
   * 监听部门树数据变化，自动调整容器高度
   */
  watch(
    deptTreeData,
    (newData: SysDept[]) => {
      if (Array.isArray(newData) && newData.length > 0) {
        // 等待 DOM 更新完成后再调整高度
        nextTick(() => {
          deptTree.adjustContainerHeight()
        })
      }
    },
    { immediate: false }
  )

  /**
   * 加载部门树数据
   */
  const loadDeptTree = async () => {
    // 防止重复调用
    if (isLoadingDeptTree.value) {
      return
    }
    isLoadingDeptTree.value = true
    deptLoading.value = true
    try {
      const deptList = await fetchGetDeptTree()
      if (Array.isArray(deptList) && deptList.length > 0) {
        deptTree.data.value = deptList
        // 等待 DOM 更新
        await nextTick()
        // 如果是编辑模式，标记需要加载角色部门权限
        // watch 会自动监听并加载
        if (props.roleData?.roleId) {
          shouldLoadRoleDeptIds.value = true
        }
      }
    } catch (error) {
      // API 调用失败的错误已在 HTTP 拦截器中统一处理并显示错误消息
      if (import.meta.env.DEV) {
        console.error('加载部门树失败:', error)
      }
    } finally {
      deptLoading.value = false
      isLoadingDeptTree.value = false
    }
  }

  /**
   * 加载角色已分配的部门ID列表
   */
  const loadRoleDeptIds = async () => {
    if (!props.roleData?.roleId) return
    try {
      const deptIds = await fetchGetRoleDeptIds(props.roleData.roleId)
      // 确保deptIds是数组类型，处理null或undefined情况
      const safeDeptIds = Array.isArray(deptIds) ? deptIds : []
      await deptTree.applyCheckedKeys(safeDeptIds)
      deptTree.adjustContainerHeight()
    } catch (error) {
      // API 调用失败的错误已在 HTTP 拦截器中统一处理并显示错误消息
      if (import.meta.env.DEV) {
        console.error('加载角色部门ID失败:', error)
      }
    }
  }

  /**
   * 监听弹窗打开，初始化权限数据
   */
  watch(
    () => props.modelValue,
    async (newVal: boolean) => {
      if (newVal) {
        // 初始化表单数据
        if (props.roleData) {
          Object.assign(form, {
            roleId: props.roleData.roleId || 0,
            roleName: props.roleData.roleName || '',
            roleKey: props.roleData.roleKey || '',
            dataScope: props.roleData.dataScope || '1'
          })
        } else {
          Object.assign(form, {
            roleId: 0,
            roleName: '',
            roleKey: '',
            dataScope: '1'
          })
        }
        // 重置状态
        deptTree.data.value = []
        deptTree.expandAll.value = false
        deptTree.selectAll.value = false
        deptTree.indeterminate.value = false
        isLoadingDeptTree.value = false
        // 重置树组件选中状态
        deptTree.treeRef.value?.setCheckedKeys([])
        // 如果数据范围为自定数据权限，加载部门树
        if (form.dataScope === '2') {
          // 等待弹窗完全打开后再加载数据
          await nextTick()
          loadDeptTree()
        }
      } else {
        // 关闭时清空数据和重置加载标记
        deptTree.data.value = []
        isLoadingDeptTree.value = false
        if (deptTree.containerRef.value) {
          deptTree.containerRef.value.style.height = 'auto'
        }
        // 重置表单
        roleForm.value?.resetFields()
      }
    }
  )

  /**
   * 数据范围变化处理
   */
  const handleDataScopeChange = (value: string) => {
    if (value === '2') {
      // 自定数据权限，加载部门树
      if (deptTreeData.value.length === 0) {
        loadDeptTree()
      }
    } else {
      // 其他数据范围，清空部门树选中状态
      deptTree.treeRef.value?.setCheckedKeys([])
    }
  }

  /**
   * 关闭弹窗并清空选中状态
   */
  const handleClose = () => {
    visible.value = false
    deptTree.treeRef.value?.setCheckedKeys([])
    roleForm.value?.resetFields()
  }

  /**
   * 保存权限配置
   */
  const savePermission = async () => {
    if (!roleForm.value) return

    try {
      // 表单验证
      await roleForm.value.validate()

      // 获取选中的部门ID（仅自定数据权限时需要）
      let deptIds: number[] = []
      if (form.dataScope === '2') {
        const deptCheckedKeys = deptTree.treeRef.value?.getCheckedKeys() || []
        const deptHalfCheckedKeys = deptTree.treeRef.value?.getHalfCheckedKeys() || []
        // 合并完全选中和半选中的节点ID，确保父级部门ID被传递
        const allDeptKeys = [...deptCheckedKeys, ...deptHalfCheckedKeys]
        // 去重并过滤出数字类型的ID
        deptIds = Array.from(new Set(allDeptKeys)).filter(
          (key: any) => typeof key === 'number'
        ) as number[]
      }

      // 调用更新角色接口，保存数据权限（含 dataScope）
      const updateData: Api.SystemManage.RolePermissionAssignDTO = {
        roleId: form.roleId,
        menuIds: [], // 菜单权限为空数组（此弹窗只处理数据权限）
        deptIds,
        dataScope: form.dataScope
      }
      await fetchAssignPermission(updateData)

      ElMessage.success('权限保存成功')
      emit('success')
      handleClose()
    } catch (error) {
      // 表单验证失败：Element Plus 会通过 UI 显示错误，这里不需要处理
      // API 调用失败：错误已在 HTTP 拦截器中统一处理并显示错误消息
      if (import.meta.env.DEV) {
        console.error('保存权限失败:', error)
      }
    }
  }

  /**
   * 切换部门树全部展开/收起状态
   */
  const toggleDeptExpandAll = () => {
    deptTree.toggleExpandAll()
  }

  /**
   * 处理部门树节点展开
   */
  const handleDeptNodeExpand = () => {
    deptTree.handleNodeExpand()
  }

  /**
   * 处理部门树节点收起
   */
  const handleDeptNodeCollapse = () => {
    deptTree.handleNodeCollapse()
  }

  /**
   * 切换部门树全选/取消全选状态
   */
  const toggleDeptSelectAll = () => {
    deptTree.toggleSelectAll()
  }

  /**
   * 处理部门树父子联动变化
   */
  const handleDeptCheckStrictlyChange = () => {
    deptTree.handleCheckStrictlyChange()
  }

  /**
   * 处理部门树节点选中状态变化
   * 同步更新全选按钮状态
   */
  const handleDeptTreeCheck = () => {
    deptTree.syncSelectState()
  }
</script>

<style scoped lang="scss">
  .permission-dialog {
    .dept-permission-wrapper {
      width: 100%;

      .permission-controls {
        display: flex;
        gap: 20px;
        padding-bottom: 12px;
        margin-bottom: 16px;
        color: var(--el-text-color-primary);
        border-bottom: 1px solid var(--el-border-color-lighter);
        transition: color 0.3s ease;
      }

      .permission-tree-container {
        height: auto;
        min-height: 100px;
        max-height: 600px;
        padding: 12px;
        overflow: auto;
        border: 1px solid var(--el-border-color-lighter);
        border-radius: 4px;
        transition:
          height 0.3s ease,
          background-color 0.3s ease;

        &.light-bg {
          background: var(--default-box-color);
        }

        &.dark-bg {
          background: var(--el-bg-color-page);
        }

        .tree-wrapper {
          min-height: 100px;
        }
      }
    }
  }
</style>
