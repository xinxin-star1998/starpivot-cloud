<template>
  <ElDialog
    v-model="visible"
    :title="form.bindId ? '编辑绑定规则' : '新建绑定规则'"
    destroy-on-close
    width="560px"
    @open="handleOpen"
  >
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="100px">
      <ElFormItem label="业务域" prop="bizModule">
        <ElInput v-model="form.bizModule" placeholder="如 mall" />
      </ElFormItem>
      <ElFormItem label="单据类型" prop="bizType">
        <ElInput v-model="form.bizType" placeholder="如 purchase / return" />
      </ElFormItem>
      <ElFormItem label="模板编码" prop="templateCode">
        <ElInput v-model="form.templateCode" placeholder="已发布的 templateCode" />
      </ElFormItem>
      <ElFormItem label="匹配表达式">
        <ElInput
          v-model="form.matchExpr"
          :rows="3"
          placeholder="SpEL，空表示总是匹配。例：#context['amount'] > 10000"
          type="textarea"
        />
      </ElFormItem>
      <ElFormItem label="优先级">
        <ElInputNumber v-model="form.priority" :min="0" />
      </ElFormItem>
      <ElFormItem label="状态">
        <ElRadioGroup v-model="form.status">
          <ElRadio value="0">启用</ElRadio>
          <ElRadio value="1">停用</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
    </ElForm>

    <template #footer>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton :loading="submitting" type="primary" @click="handleSubmit">保存</ElButton>
    </template>
  </ElDialog>
</template>

<script lang="ts" setup>
import type {FormInstance, FormRules} from 'element-plus'
import {ElMessage} from 'element-plus'
import {handleMutationError} from '@/utils/http/mutation'
import {fetchApprovalBindSave} from '@/api/approval/template'
import type {ApTemplateBind} from '@/api/approval/types'

const visible = defineModel<boolean>('visible', { default: false })
  const record = defineModel<ApTemplateBind | null>('record', { default: null })

  const emit = defineEmits<{ success: [] }>()

  const formRef = ref<FormInstance>()
  const submitting = ref(false)

  const form = reactive({
    bindId: undefined as number | undefined,
    bizModule: 'mall',
    bizType: '',
    templateCode: '',
    matchExpr: '',
    priority: 0,
    status: '0'
  })

  const rules: FormRules = {
    bizModule: [{ required: true, message: '请输入业务域', trigger: 'blur' }],
    bizType: [{ required: true, message: '请输入单据类型', trigger: 'blur' }],
    templateCode: [{ required: true, message: '请输入模板编码', trigger: 'blur' }]
  }

  function handleOpen() {
    if (record.value) {
      form.bindId = record.value.bindId
      form.bizModule = record.value.bizModule || 'mall'
      form.bizType = record.value.bizType || ''
      form.templateCode = record.value.templateCode || ''
      form.matchExpr = record.value.matchExpr || ''
      form.priority = record.value.priority ?? 0
      form.status = record.value.status || '0'
    } else {
      form.bindId = undefined
      form.bizModule = 'mall'
      form.bizType = ''
      form.templateCode = ''
      form.matchExpr = ''
      form.priority = 0
      form.status = '0'
    }
  }

  async function handleSubmit() {
    await formRef.value?.validate()
    submitting.value = true
    try {
      await fetchApprovalBindSave({ ...form })
      ElMessage.success('保存成功')
      visible.value = false
      emit('success')
    } catch (error) {
      handleMutationError(error, '保存失败')
    } finally {
      submitting.value = false
    }
  }
</script>
