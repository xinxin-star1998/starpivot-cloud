<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增满减' : '编辑满减'"
    width="480px"
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="SKU ID" prop="skuId">
        <ElInputNumber v-model="formData.skuId" :min="1" controls-position="right" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="满(元)">
        <ElInputNumber v-model="formData.fullPrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="减(元)">
        <ElInputNumber v-model="formData.reducePrice" :min="0" :precision="2" controls-position="right" style="width: 100%" />
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
    fetchSkuFullReductionAdd,
    fetchSkuFullReductionById,
    fetchSkuFullReductionUpdate,
    type SkuFullReductionSavePayload
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
  const formData = ref<SkuFullReductionSavePayload>({ skuId: 0 })

  const rules: FormRules = {
    skuId: [{ required: true, message: '请输入 SKU ID', trigger: 'blur' }]
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return
      formData.value = { skuId: undefined as unknown as number }
      if (props.type === 'edit' && props.recordId) {
        const detail = await fetchSkuFullReductionById(props.recordId)
        formData.value = {
          id: detail.id,
          skuId: detail.skuId!,
          fullPrice: detail.fullPrice,
          reducePrice: detail.reducePrice,
          addOther: detail.addOther
        }
      }
    }
  )

  const handleSubmit = async () => {
    await formRef.value?.validate()
    submitting.value = true
    try {
      if (props.type === 'add') await fetchSkuFullReductionAdd(formData.value)
      else await fetchSkuFullReductionUpdate(formData.value)
      dialogVisible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>
