<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增积分配置' : '编辑积分配置'"
    width="480px"
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="SPU ID" prop="spuId">
        <ElInputNumber v-model="formData.spuId" :min="1" controls-position="right" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="成长值">
        <ElInputNumber
          v-model="formData.growBounds"
          :min="0"
          :precision="2"
          controls-position="right"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="购物积分">
        <ElInputNumber
          v-model="formData.buyBounds"
          :min="0"
          :precision="2"
          controls-position="right"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="状态位">
        <ElInputNumber v-model="formData.work" :min="0" controls-position="right" style="width: 100%" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">提交</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import type {FormInstance, FormRules} from 'element-plus'
import {
  fetchSpuBoundsAdd,
  fetchSpuBoundsById,
  fetchSpuBoundsUpdate,
  type SpuBoundsSavePayload
} from '@/api/mall/spu-bounds'
import type {DialogType} from '@/types'

const props = defineProps<{ visible: boolean; type: DialogType; recordId?: number }>()
  const emit = defineEmits<{ 'update:visible': [boolean]; success: [] }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const formData = ref<SpuBoundsSavePayload>({ spuId: 0 })

  const rules: FormRules = {
    spuId: [{ required: true, message: '请输入 SPU ID', trigger: 'blur' }]
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return
      formData.value = { spuId: undefined as unknown as number }
      if (props.type === 'edit' && props.recordId) {
        const detail = await fetchSpuBoundsById(props.recordId)
        formData.value = {
          id: detail.id,
          spuId: detail.spuId!,
          growBounds: detail.growBounds,
          buyBounds: detail.buyBounds,
          work: detail.work
        }
      }
    }
  )

  const handleSubmit = async () => {
    await formRef.value?.validate()
    submitting.value = true
    try {
      if (props.type === 'add') await fetchSpuBoundsAdd(formData.value)
      else await fetchSpuBoundsUpdate(formData.value)
      dialogVisible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>
