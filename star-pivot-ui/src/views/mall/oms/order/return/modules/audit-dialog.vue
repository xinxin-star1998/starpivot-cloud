<template>
  <ElDialog v-model="dialogVisible" title="退货审核" width="480px" destroy-on-close @closed="resetForm">
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="96px">
      <ElFormItem label="审核结果" prop="status">
        <ElRadioGroup v-model="form.status">
          <ElRadio :value="1">同意退货</ElRadio>
          <ElRadio :value="3">拒绝</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="处理备注" prop="handleNote">
        <ElInput v-model="form.handleNote" type="textarea" :rows="3" placeholder="选填" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">提交</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchReturnAudit } from '@/api/mall/order-return'
  import type { FormInstance, FormRules } from 'element-plus'

  interface Props {
    visible: boolean
    returnId?: number
  }
  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const form = ref({
    status: 1 as number,
    handleNote: ''
  })

  const rules: FormRules = {
    status: [{ required: true, message: '请选择审核结果', trigger: 'change' }]
  }

  function resetForm() {
    form.value = { status: 1, handleNote: '' }
    formRef.value?.resetFields()
  }

  async function handleSubmit() {
    if (props.returnId == null) return
    await formRef.value?.validate()
    submitting.value = true
    try {
      await fetchReturnAudit({
        id: props.returnId,
        status: form.value.status,
        handleNote: form.value.handleNote || undefined
      })
      dialogVisible.value = false
      emit('submit')
    } finally {
      submitting.value = false
    }
  }
</script>
