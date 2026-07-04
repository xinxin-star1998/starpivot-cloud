<template>
  <ElDialog
    :title="dialogTitle"
    :model-value="visible"
    @update:model-value="handleCancel"
    width="700px"
    align-center
    class="dict-data-dialog"
    @closed="handleClosed"
  >
    <ArtForm
      ref="formRef"
      v-model="form"
      :items="formItems"
      :rules="rules"
      :span="width > 640 ? 12 : 24"
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
import {useWindowSize} from '@vueuse/core'
import type {DictDataFormData} from '@/api/dict/data'

const { width } = useWindowSize()

  interface Props {
    visible: boolean
    editData?: DictDataFormData | null
    dictType?: string
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit', data: DictDataFormData): void
  }

  const props = withDefaults(defineProps<Props>(), {
    visible: false,
    editData: null,
    dictType: ''
  })

  const emit = defineEmits<Emits>()

  const formRef = ref()
  const isEdit = ref(false)

  const form = reactive<DictDataFormData>({
    dictSort: 0,
    dictLabel: '',
    dictValue: '',
    dictType: '',
    cssClass: '',
    listClass: '',
    isDefault: 'N',
    status: '0',
    remark: ''
  })

  const dialogTitle = computed(() => {
    return isEdit.value ? '编辑字典数据' : '新增字典数据'
  })

  const rules = reactive<FormRules>({
    dictLabel: [
      { required: true, message: '请输入字典标签', trigger: 'blur' },
      { max: 100, message: '字典标签长度不能超过100个字符', trigger: 'blur' }
    ],
    dictValue: [
      { required: true, message: '请输入字典键值', trigger: 'blur' },
      { max: 100, message: '字典键值长度不能超过100个字符', trigger: 'blur' }
    ],
    dictType: [{ required: true, message: '请选择字典类型', trigger: 'change' }],
    dictSort: [
      { required: true, message: '请输入字典排序', trigger: 'blur' },
      { type: 'number', min: 0, message: '字典排序必须大于等于0', trigger: 'blur' }
    ],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }]
  })

  const formItems = computed<FormItem[]>(() => {
    const switchSpan = width.value < 768 ? 24 : 12
    return [
      {
        label: '字典类型',
        key: 'dictType',
        type: 'input',
        props: {
          placeholder: '请输入字典类型',
          disabled: !!props.dictType || isEdit.value
        }
      },
      {
        label: '字典标签',
        key: 'dictLabel',
        type: 'input',
        props: { placeholder: '请输入字典标签' }
      },
      {
        label: '字典键值',
        key: 'dictValue',
        type: 'input',
        props: { placeholder: '请输入字典键值' }
      },
      {
        label: '字典排序',
        key: 'dictSort',
        type: 'number',
        props: {
          min: 0,
          controlsPosition: 'right',
          style: { width: '100%' }
        }
      },
      {
        label: '样式属性',
        key: 'cssClass',
        type: 'select',
        props: {
          placeholder: '请选择样式属性',
          options: [
            { label: '主要', value: 'primary' },
            { label: '成功', value: 'success' },
            { label: '警告', value: 'warning' },
            { label: '危险', value: 'danger' }
          ]
        }
      },
      {
        label: '回显样式',
        key: 'listClass',
        type: 'select',
        props: {
          placeholder: '请选择回显样式',
          options: [
            { label: '默认', value: 'default' },
            { label: '主要', value: 'primary' },
            { label: '成功', value: 'success' },
            { label: '信息', value: 'info' },
            { label: '警告', value: 'warning' },
            { label: '危险', value: 'danger' }
          ]
        }
      },
      {
        label: '是否默认',
        key: 'isDefault',
        type: 'radiogroup',
        span: switchSpan,
        props: {
          options: [
            { label: '是', value: 'Y' },
            { label: '否', value: 'N' }
          ],
          size: 'default',
          direction: 'horizontal'
        }
      },
      {
        label: '状态',
        key: 'status',
        type: 'radiogroup',
        span: switchSpan,
        props: {
          options: [
            { label: '正常', value: '0' },
            { label: '停用', value: '1' }
          ],
          size: 'default',
          direction: 'horizontal'
        }
      },
      {
        label: '备注',
        key: 'remark',
        type: 'input',
        span: 24,
        props: { type: 'textarea', rows: 3, placeholder: '请输入备注' }
      }
    ]
  })

  /**
   * 加载表单数据（编辑模式）
   */
  const loadFormData = (): void => {
    if (!props.editData) return

    isEdit.value = true
    Object.assign(form, {
      dictCode: props.editData.dictCode,
      dictSort: props.editData.dictSort || 0,
      dictLabel: props.editData.dictLabel,
      dictValue: props.editData.dictValue,
      dictType: props.editData.dictType,
      cssClass: props.editData.cssClass || '',
      listClass: props.editData.listClass || '',
      isDefault: props.editData.isDefault || 'N',
      status: props.editData.status || '0',
      remark: props.editData.remark || ''
    })
  }

  /**
   * 重置表单数据
   */
  const resetForm = (): void => {
    Object.assign(form, {
      dictCode: undefined,
      dictSort: 0,
      dictLabel: '',
      dictValue: '',
      dictType: props.dictType || '',
      cssClass: '',
      listClass: '',
      isDefault: 'N',
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

      const submitData: DictDataFormData = {
        dictSort: form.dictSort || 0,
        dictLabel: form.dictLabel,
        dictValue: form.dictValue,
        dictType: form.dictType || props.dictType || '',
        cssClass: form.cssClass || '',
        listClass: form.listClass || '',
        isDefault: form.isDefault || 'N',
        status: form.status || '0',
        remark: form.remark || ''
      }

      // 如果是编辑模式，添加dictCode
      if (isEdit.value && form.dictCode) {
        submitData.dictCode = form.dictCode
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

  /**
   * 监听字典类型变化
   */
  watch(
    () => props.dictType,
    (newVal) => {
      if (newVal && !isEdit.value) {
        form.dictType = newVal
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
        box-shadow: 0 2px 8px 0 rgb(0 0 0 / 8%);
      }
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
