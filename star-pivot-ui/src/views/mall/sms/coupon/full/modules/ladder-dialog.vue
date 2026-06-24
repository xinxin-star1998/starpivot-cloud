<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增阶梯价' : '编辑阶梯价'"
    width="480px"
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="SKU ID" prop="skuId">
        <ElInputNumber v-model="formData.skuId" :min="1" controls-position="right" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="满(件)">
        <ElInputNumber v-model="formData.fullCount" :min="1" controls-position="right" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="折扣">
        <ElInputNumber
          v-model="formData.discount"
          :min="0"
          :max="1"
          :step="0.01"
          :precision="2"
          controls-position="right"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="折后价">
        <ElInputNumber v-model="formData.price" :min="0" :precision="2" controls-position="right" style="width: 100%" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">提交</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import {
    fetchSkuLadderAdd,
    fetchSkuLadderById,
    fetchSkuLadderUpdate,
    type SkuLadderSavePayload
  } from '@/api/mall/sku-promotion'
  import type { DialogType } from '@/types'

  const props = defineProps<{ visible: boolean; type: DialogType; recordId?: number }>()
  const emit = defineEmits<{ 'update:visible': [boolean]; success: [] }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const formData = ref<SkuLadderSavePayload>({ skuId: 0 })

  const rules: FormRules = {
    skuId: [{ required: true, message: '请输入 SKU ID', trigger: 'blur' }]
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return
      formData.value = { skuId: undefined as unknown as number }
      if (props.type === 'edit' && props.recordId) {
        const detail = await fetchSkuLadderById(props.recordId)
        formData.value = {
          id: detail.id,
          skuId: detail.skuId!,
          fullCount: detail.fullCount,
          discount: detail.discount,
          price: detail.price,
          addOther: detail.addOther
        }
      }
    }
  )

  const handleSubmit = async () => {
    await formRef.value?.validate()
    submitting.value = true
    try {
      if (props.type === 'add') await fetchSkuLadderAdd(formData.value)
      else await fetchSkuLadderUpdate(formData.value)
      dialogVisible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>
