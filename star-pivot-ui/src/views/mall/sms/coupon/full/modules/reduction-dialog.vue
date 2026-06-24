<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="480px"
    align-center
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="SKU ID" prop="skuId">
        <ElInputNumber v-model="formData.skuId" :min="1" controls-position="right" />
      </ElFormItem>
      <template v-if="mode === 'full'">
        <ElFormItem label="满(元)">
          <ElInputNumber
            v-model="fullForm.fullPrice"
            :min="0"
            :precision="2"
            controls-position="right"
          />
        </ElFormItem>
        <ElFormItem label="减(元)">
          <ElInputNumber
            v-model="fullForm.reducePrice"
            :min="0"
            :precision="2"
            controls-position="right"
          />
        </ElFormItem>
      </template>
      <template v-else>
        <ElFormItem label="满(件)">
          <ElInputNumber v-model="ladderForm.fullCount" :min="0" controls-position="right" />
        </ElFormItem>
        <ElFormItem label="折扣">
          <ElInputNumber
            v-model="ladderForm.discount"
            :min="0"
            :max="1"
            :step="0.01"
            :precision="2"
            controls-position="right"
          />
        </ElFormItem>
        <ElFormItem label="折后价">
          <ElInputNumber
            v-model="ladderForm.price"
            :min="0"
            :precision="2"
            controls-position="right"
          />
        </ElFormItem>
      </template>
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
    fetchSkuLadderAdd,
    fetchSkuLadderById,
    fetchSkuLadderUpdate,
    type SkuFullReductionSavePayload,
    type SkuLadderSavePayload
  } from '@/api/mall/sku-promotion'
  import type { DialogType } from '@/types'

  interface Props {
    visible: boolean
    type: DialogType
    mode: 'full' | 'ladder'
    recordId?: number
  }

  const props = defineProps<Props>()
  const emit = defineEmits<{ 'update:visible': [boolean]; success: [] }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const dialogTitle = computed(() => {
    const action = props.type === 'add' ? '新增' : '编辑'
    return props.mode === 'full' ? `${action}满减` : `${action}阶梯价`
  })

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const formData = ref<{ skuId?: number }>({ skuId: undefined })
  const fullForm = ref<SkuFullReductionSavePayload>({ skuId: 0 })
  const ladderForm = ref<SkuLadderSavePayload>({ skuId: 0 })

  const rules: FormRules = {
    skuId: [{ required: true, message: '请输入 SKU ID', trigger: 'blur' }]
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return
      formData.value = { skuId: undefined }
      fullForm.value = { skuId: 0 }
      ladderForm.value = { skuId: 0 }
      if (props.type === 'edit' && props.recordId) {
        if (props.mode === 'full') {
          const detail = await fetchSkuFullReductionById(props.recordId)
          formData.value.skuId = detail.skuId
          fullForm.value = {
            id: detail.id,
            skuId: detail.skuId!,
            fullPrice: detail.fullPrice,
            reducePrice: detail.reducePrice,
            addOther: detail.addOther
          }
        } else {
          const detail = await fetchSkuLadderById(props.recordId)
          formData.value.skuId = detail.skuId
          ladderForm.value = {
            id: detail.id,
            skuId: detail.skuId!,
            fullCount: detail.fullCount,
            discount: detail.discount,
            price: detail.price,
            addOther: detail.addOther
          }
        }
      }
    }
  )

  const handleSubmit = async () => {
    await formRef.value?.validate()
    submitting.value = true
    try {
      const skuId = formData.value.skuId!
      if (props.mode === 'full') {
        const payload = { ...fullForm.value, skuId }
        if (props.type === 'add') await fetchSkuFullReductionAdd(payload)
        else await fetchSkuFullReductionUpdate(payload)
      } else {
        const payload = { ...ladderForm.value, skuId }
        if (props.type === 'add') await fetchSkuLadderAdd(payload)
        else await fetchSkuLadderUpdate(payload)
      }
      dialogVisible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>
