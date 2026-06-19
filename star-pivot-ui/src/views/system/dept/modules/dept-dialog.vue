<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '添加部门' : '编辑部门'"
    width="40%"
    align-center
  >
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      aria-label="部门信息表单"
    >
      <ElFormItem label="上级部门" prop="parentId">
        <ElTreeSelect
          v-model="formData.parentId"
          :data="deptTreeData"
          :props="deptTreeProps"
          placeholder="请选择上级部门"
          clearable
          check-strictly
          :render-after-expand="false"
          :disabled="dialogType === 'edit' && formData.deptId === formData.parentId"
        />
      </ElFormItem>
      <ElFormItem label="部门名称" prop="deptName">
        <ElInput v-model="formData.deptName" placeholder="请输入部门名称" />
      </ElFormItem>
      <ElFormItem label="显示顺序" prop="orderNum">
        <ElInputNumber v-model="formData.orderNum" :min="0" placeholder="请输入显示顺序" />
      </ElFormItem>
      <ElFormItem label="负责人" prop="leader">
        <ElInput v-model="formData.leader" placeholder="请输入负责人" />
      </ElFormItem>
      <ElFormItem label="联系电话" prop="phone">
        <ElInput v-model="formData.phone" placeholder="请输入联系电话" />
      </ElFormItem>
      <ElFormItem label="邮箱" prop="email">
        <ElInput v-model="formData.email" placeholder="请输入邮箱" />
      </ElFormItem>
      <ElFormItem label="状态" prop="status">
        <ElRadioGroup v-model="formData.status">
          <ElRadio :value="'0'">正常</ElRadio>
          <ElRadio :value="'1'">停用</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="备注" prop="remark">
        <ElInput type="textarea" v-model="formData.remark" placeholder="请输入备注" :rows="3" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">提交</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { ElMessage } from 'element-plus'
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElTreeSelect } from 'element-plus'
  import {
    fetchGetDeptTree,
    fetchAddDept,
    fetchUpdateDept,
    fetchGetDeptById,
    type SysDept
  } from '@/api/dept/dept'
  import { DialogType } from '@/types'

  interface Props {
    visible: boolean
    type: DialogType
    deptData?: Partial<SysDept>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  // 部门树数据
  const deptTreeData = ref<SysDept[]>([])
  // 部门树配置
  const deptTreeProps = {
    value: 'deptId',
    label: 'deptName',
    children: 'children'
  }
  // 对话框显示控制
  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const dialogType = computed(() => props.type)

  // 表单实例
  const formRef = ref<FormInstance>()

  // 表单数据
  const formData = reactive({
    deptId: undefined as number | undefined,
    parentId: 0,
    deptName: '',
    orderNum: 0,
    leader: '',
    phone: '',
    email: '',
    status: '0',
    remark: ''
  })

  // 表单验证规则
  const rules: FormRules = {
    deptName: [
      { required: true, message: '请输入部门名称', trigger: 'blur' },
      { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
    ],
    phone: [{ pattern: /^1[3-9]\d{9}$|^$/, message: '请输入正确的手机号格式', trigger: 'blur' }],
    email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
  }

  /**
   * 检查部门是否为指定部门的子部门
   * @param dept 要检查的部门
   * @param excludeId 要排除的部门ID
   * @param allDepts 所有部门列表（用于查找）
   * @returns 是否为子部门
   */
  const isChildOf = (dept: SysDept, excludeId: number, allDepts: SysDept[]): boolean => {
    if (dept.deptId === excludeId) return true
    if (dept.parentId === excludeId) return true

    // 递归检查父部门
    if (dept.parentId) {
      const parent = findDeptById(dept.parentId, allDepts)
      if (parent) {
        return isChildOf(parent, excludeId, allDepts)
      }
    }
    return false
  }

  /**
   * 根据ID查找部门
   * @param deptId 部门ID
   * @param depts 部门列表
   * @returns 找到的部门
   */
  const findDeptById = (deptId: number, depts: SysDept[]): SysDept | null => {
    for (const dept of depts) {
      if (dept.deptId === deptId) return dept
      if (dept.children && dept.children.length > 0) {
        const found = findDeptById(deptId, dept.children)
        if (found) return found
      }
    }
    return null
  }

  /**
   * 过滤部门树，排除当前编辑的部门及其所有子部门
   * @param tree 部门树
   * @param excludeId 要排除的部门ID
   * @param allDepts 所有部门列表（用于查找关系）
   * @returns 过滤后的部门树
   */
  const filterDeptTree = (tree: SysDept[], excludeId?: number, allDepts?: SysDept[]): SysDept[] => {
    if (!excludeId) return tree

    const allDeptsList = allDepts || tree

    return tree
      .filter((dept) => {
        // 排除自己
        if (dept.deptId === excludeId) return false
        // 排除所有子部门
        return !isChildOf(dept, excludeId, allDeptsList)
      })
      .map((dept) => {
        const cloned = { ...dept }
        if (dept.children && dept.children.length > 0) {
          cloned.children = filterDeptTree(dept.children, excludeId, allDeptsList)
        }
        return cloned
      })
  }

  /**
   * 初始化表单数据
   * 根据对话框类型（新增/编辑）填充表单
   */
  const initFormData = async () => {
    const isEdit = props.type === 'edit' && props.deptData

    if (isEdit && props.deptData?.deptId) {
      // 编辑模式：获取完整的部门详情
      try {
        const deptDetail = await fetchGetDeptById(props.deptData.deptId)
        if (deptDetail) {
          Object.assign(formData, {
            deptId: deptDetail.deptId,
            parentId: deptDetail.parentId || 0,
            deptName: deptDetail.deptName || '',
            orderNum: deptDetail.orderNum || 0,
            leader: deptDetail.leader || '',
            phone: deptDetail.phone || '',
            email: deptDetail.email || '',
            status: deptDetail.status || '0',
            remark: deptDetail.remark || ''
          })
        }
      } catch (error) {
        console.error('获取部门详情失败:', error)
        ElMessage.error('获取部门详情失败')
        // 如果获取详情失败，使用列表数据作为回退
        Object.assign(formData, {
          deptId: props.deptData.deptId,
          parentId: props.deptData.parentId || 0,
          deptName: props.deptData.deptName || '',
          orderNum: props.deptData.orderNum || 0,
          leader: props.deptData.leader || '',
          phone: props.deptData.phone || '',
          email: props.deptData.email || '',
          status: props.deptData.status || '0',
          remark: props.deptData.remark || ''
        })
      }
    } else {
      // 新增模式：重置表单
      Object.assign(formData, {
        deptId: undefined,
        parentId: props.deptData?.parentId || 0,
        deptName: '',
        orderNum: 0,
        leader: '',
        phone: '',
        email: '',
        status: '0',
        remark: ''
      })
    }
  }

  /**
   * 获取部门树数据
   */
  const getDeptTree = async () => {
    try {
      const res = await fetchGetDeptTree()
      if (Array.isArray(res) && res.length > 0) {
        // 编辑模式下，过滤掉当前部门及其所有子部门
        if (props.type === 'edit' && formData.deptId) {
          deptTreeData.value = filterDeptTree(res, formData.deptId, res)
        } else {
          deptTreeData.value = res
        }
      } else {
        deptTreeData.value = []
      }
    } catch (error) {
      console.error('获取部门树失败:', error)
      deptTreeData.value = []
      ElMessage.error('获取部门树失败')
    }
  }

  /**
   * 监听对话框状态变化
   * 当对话框打开时初始化表单数据并清除验证状态
   */
  watch(
    () => [props.visible, props.type, props.deptData],
    async ([visible]) => {
      if (visible) {
        // 先初始化表单数据（编辑模式需要先获取详情）
        await initFormData()
        // 再获取部门树（编辑模式需要过滤）
        await getDeptTree()
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  /**
   * 提交表单
   * 验证通过后触发提交事件
   */
  const handleSubmit = async () => {
    if (!formRef.value) return

    await formRef.value.validate(async (valid) => {
      if (valid) {
        try {
          const submitData = {
            ...formData,
            parentId: formData.parentId || 0
          }

          if (dialogType.value === 'add') {
            await fetchAddDept(submitData)
            ElMessage.success('添加成功')
          } else {
            await fetchUpdateDept(submitData)
            ElMessage.success('更新成功')
          }
          dialogVisible.value = false
          emit('submit')
        } catch (error) {
          console.error('提交失败:', error)
          ElMessage.error(dialogType.value === 'add' ? '添加失败' : '更新失败')
        }
      }
    })
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
      padding: 24px;
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

  :deep(.el-input-number) {
    width: 100%;

    .el-input__wrapper {
      border-radius: 8px;
    }
  }

  :deep(.el-select) {
    width: 100%;

    .el-select__wrapper {
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: var(--art-shadow-sm);
      }
    }
  }

  :deep(.el-tree-select) {
    width: 100%;
  }

  :deep(.el-radio-group) {
    .el-radio {
      margin-right: 20px;
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

  .dialog-footer {
    display: flex;
    gap: 12px;
    justify-content: flex-end;
  }
</style>
