<template>
  <ElDialog
    :title="dialogTitle"
    :model-value="visible"
    @update:model-value="handleCancel"
    width="600px"
    align-center
    class="dict-type-dialog"
    @closed="handleClosed"
  >
    <ArtForm
      ref="formRef"
      v-model="form"
      :items="formItems"
      :rules="rules"
      :span="24"
      :gutter="20"
      label-width="100px"
      :show-reset="false"
      :show-submit="false"
    />

    <template #footer>
      <span class="dialog-footer">
        <ElButton @click="handleCancel">取 消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确 定</ElButton>
      </span>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import type {FormRules} from 'element-plus'
import {ElMessage} from 'element-plus'
import type {FormItem} from '@/components/core/forms/art-form/index.vue'
import ArtForm from '@/components/core/forms/art-form/index.vue'
import type {DictTypeFormData} from '@/api/dict/type'

interface Props {
    visible: boolean
    editData?: DictTypeFormData | null
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit', data: DictTypeFormData): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    editData: null
  })

  const emit = defineEmits<Emits>()

  const formRef = ref()
  const isEdit = ref(false)

  const form = reactive<DictTypeFormData>({
    dictName: '',
    dictType: '',
    status: '0',
    remark: ''
  })

  const dialogTitle = computed(() => {
    return isEdit.value ? '编辑字典类型' : '新增字典类型'
  })

  // 字典类型验证函数
  const validateDictType = (rule: any, value: string, callback: any) => {
    if (!value) {
      callback(new Error('请输入字典类型'))
      return
    }
    // 字典类型只能包含字母、数字、下划线和横线
    if (!/^[a-zA-Z0-9_-]+$/.test(value)) {
      callback(new Error('字典类型只能包含字母、数字、下划线和横线'))
      return
    }
    callback()
  }

  const rules = reactive<FormRules>({
    dictName: [
      { required: true, message: '请输入字典名称', trigger: 'blur' },
      { max: 100, message: '字典名称长度不能超过100个字符', trigger: 'blur' }
    ],
    dictType: [{ validator: validateDictType, trigger: 'blur' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  })

  const formItems = computed<FormItem[]>(() => [
    {
      label: '字典名称',
      key: 'dictName',
      type: 'input',
      props: { placeholder: '请输入字典名称' }
    },
    {
      label: '字典类型',
      key: 'dictType',
      type: 'input',
      props: {
        placeholder: '请输入字典类型，如：sys_user_sex',
        disabled: isEdit.value
      }
    },
    {
      label: '状态',
      key: 'status',
      type: 'radiogroup',
      props: {
        options: [
          { label: '正常', value: '0' },
          { label: '停用', value: '1' }
        ]
      }
    },
    {
      label: '备注',
      key: 'remark',
      type: 'input',
      span: 24,
      props: { type: 'textarea', rows: 3, placeholder: '请输入备注' }
    }
  ])

  /**
   * 加载表单数据（编辑模式）
   */
  const loadFormData = (): void => {
    if (!props.editData) return

    isEdit.value = true
    Object.assign(form, {
      dictId: props.editData.dictId,
      dictName: props.editData.dictName,
      dictType: props.editData.dictType,
      status: props.editData.status || '0',
      remark: props.editData.remark || ''
    })
  }

  /**
   * 重置表单数据
   */
  const resetForm = (): void => {
    Object.assign(form, {
      dictId: undefined,
      dictName: '',
      dictType: '',
      status: '0',
      remark: ''
    })
    nextTick(() => {
      if (formRef.value?.ref) {
        formRef.value.ref.resetFields()
      }
    })
    isEdit.value = false
  }

  /**
   * 提交表单
   */
  const handleSubmit = async (): Promise<void> => {
    if (!formRef.value) return

    try {
      await formRef.value.validate()

      const submitData: DictTypeFormData = {
        dictName: form.dictName,
        dictType: form.dictType,
        status: form.status || '0',
        remark: form.remark || ''
      }

      // 如果是编辑模式，添加dictId
      if (isEdit.value && form.dictId) {
        submitData.dictId = form.dictId
      }

      emit('submit', submitData)
      handleCancel()
    } catch {
      ElMessage.error('表单校验失败，请检查输入')
    }
  }

  /**
   * 取消操作
   */
  const handleCancel = (): void => {
    emit('update:visible', false)
  }

  /**
   * 对话框关闭后的回调
   */
  const handleClosed = (): void => {
    resetForm()
  }

  /**
   * 监听对话框显示状态
   */
  watch(
    () => props.visible,
    async (newVal) => {
      if (newVal) {
        await nextTick()
        if (props.editData) {
          loadFormData()
        } else {
          resetForm()
        }
      }
    }
  )
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
      box-shadow: 0 2px 8px 0 rgb(0 0 0 / 8%);
    }
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
