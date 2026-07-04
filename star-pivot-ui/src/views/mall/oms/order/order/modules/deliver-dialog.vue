<template>
  <ElDialog v-model="dialogVisible" title="订单发货" width="480px" destroy-on-close @closed="resetForm">
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="96px">
      <ElFormItem label="物流公司" prop="deliveryCompany">
        <ElInput v-model="form.deliveryCompany" placeholder="如：顺丰速运" />
      </ElFormItem>
      <ElFormItem label="物流单号" prop="deliverySn">
        <ElInput v-model="form.deliverySn" placeholder="请输入物流单号" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">确认发货</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import {fetchOmsOrderDeliver} from '@/api/mall/order'
import type {FormInstance, FormRules} from 'element-plus'

interface Props {
    visible: boolean
    orderId?: number
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
    deliveryCompany: '',
    deliverySn: ''
  })

  const rules: FormRules = {
    deliveryCompany: [{ required: true, message: '请输入物流公司', trigger: 'blur' }],
    deliverySn: [{ required: true, message: '请输入物流单号', trigger: 'blur' }]
  }

  function resetForm() {
    form.value = { deliveryCompany: '', deliverySn: '' }
    formRef.value?.resetFields()
  }

  async function handleSubmit() {
    if (props.orderId == null) return
    await formRef.value?.validate()
    submitting.value = true
    try {
      await fetchOmsOrderDeliver({
        orderId: props.orderId,
        deliveryCompany: form.value.deliveryCompany,
        deliverySn: form.value.deliverySn
      })
      dialogVisible.value = false
      emit('submit')
    } finally {
      submitting.value = false
    }
  }
</script>
