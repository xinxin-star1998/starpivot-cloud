<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新增参数配置' : '编辑参数配置'"
    align-center
    width="30%"
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="参数名称" prop="configName">
        <ElInput v-model="formData.configName" placeholder="请输入参数名称" />
      </ElFormItem>
      <ElFormItem label="参数键名" prop="configKey">
        <ElInput v-model="formData.configKey" placeholder="请输入参数键名" />
      </ElFormItem>
      <ElFormItem label="参数键值" prop="configValue">
        <ElInput
          v-model="formData.configValue"
          :rows="4"
          placeholder="请输入参数键值"
          type="textarea"
        />
      </ElFormItem>
      <ElFormItem label="系统内置" prop="configType">
        <ElRadioGroup v-model="formData.configType">
          <ElRadio value="Y">是</ElRadio>
          <ElRadio value="N">否</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="备注" prop="remark">
        <ElInput v-model="formData.remark" :rows="4" placeholder="请输入备注" type="textarea" />
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

<script lang="ts" setup>
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import {
    type Config,
    fetchAddConfig,
    fetchGetConfigById,
    fetchUpdateConfig
  } from '@/api/system/config/config'

  interface Props {
    visible: boolean
    type: string
    configData?: Partial<Config>
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void

    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

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
    configId: undefined as number | undefined,
    configName: '',
    configKey: '',
    configValue: '',
    configType: 'N',
    remark: ''
  })

  // 表单验证规则
  const rules: FormRules = {
    configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
    configKey: [{ required: true, message: '请输入参数键名', trigger: 'blur' }],
    configValue: [{ required: true, message: '请输入参数键值', trigger: 'blur' }],
    configType: [{ required: true, message: '请选择系统内置', trigger: 'change' }]
  }

  /**
   * 初始化表单数据
   * 根据对话框类型（新增/编辑）填充表单
   */
  const initFormData = async () => {
    const isEdit = props.type === 'edit' && props.configData

    if (isEdit && props.configData?.configId) {
      // 编辑模式：获取完整的参数配置详情
      try {
        const detail = await fetchGetConfigById(props.configData.configId)
        console.log('参数配置详情数据:', detail)
        if (detail) {
          Object.assign(formData, {
            configId: detail.configId,
            configName: detail.configName || '',
            configKey: detail.configKey || '',
            configValue: detail.configValue || '',
            configType: detail.configType || 'N',
            remark: detail.remark || ''
          })
        }
      } catch (error) {
        console.error('获取参数配置详情失败:', error)
        ElMessage.error('获取参数配置详情失败')
        // 如果获取详情失败，使用列表数据作为回退
        const row = props.configData
        Object.assign(formData, {
          configId: row.configId,
          configName: row.configName || '',
          configKey: row.configKey || '',
          configValue: row.configValue || '',
          configType: row.configType || 'N',
          remark: row.remark || ''
        })
      }
    } else {
      // 新增模式：重置表单
      Object.assign(formData, {
        configId: undefined,
        configName: '',
        configKey: '',
        configValue: '',
        configType: 'N',
        remark: ''
      })
    }
  }

  /**
   * 监听对话框状态变化
   * 当对话框打开时初始化表单数据并清除验证状态
   */
  watch(
    () => [props.visible, props.type, props.configData],
    async ([visible]) => {
      if (visible) {
        await initFormData()
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
          const submitData: any = {
            configId: formData.configId,
            configName: formData.configName,
            configKey: formData.configKey,
            configValue: formData.configValue,
            configType: formData.configType,
            remark: formData.remark
          }

          if (dialogType.value === 'add') {
            await fetchAddConfig(submitData)
          } else {
            submitData.configId = props.configData?.configId || formData.configId
            await fetchUpdateConfig(submitData)
          }
          ElMessage.success(dialogType.value === 'add' ? '新增成功' : '更新成功')
          dialogVisible.value = false
          emit('submit')
        } catch (error) {
          console.error('提交失败:', error)
          ElMessage.error(dialogType.value === 'add' ? '新增失败' : '更新失败')
        }
      }
    })
  }
</script>

<style lang="scss" scoped>
  .dialog-footer {
    display: flex;
    gap: 10px;
    justify-content: flex-end;
  }
</style>
