<template>
  <ElDialog v-model="dialogVisible" title="新增 SKU" width="480px" align-center destroy-on-close>
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="SPU ID" prop="spuId">
        <ElInputNumber v-model="formData.spuId" :min="1" :controls="false" class="w-full" />
      </ElFormItem>
      <ElFormItem label="SKU 名称" prop="skuName">
        <ElInput v-model="formData.skuName" placeholder="如：华为 Mate 30 星河银 8GB+256GB" />
      </ElFormItem>
      <ElFormItem label="价格" prop="price">
        <ElInputNumber
          v-model="formData.price"
          :min="0"
          :precision="2"
          :step="0.01"
          :controls="false"
          class="w-full"
        />
      </ElFormItem>
      <ElFormItem label="标题" prop="skuTitle">
        <ElInput v-model="formData.skuTitle" placeholder="可选" />
      </ElFormItem>
      <ElFormItem label="副标题" prop="skuSubtitle">
        <ElInput v-model="formData.skuSubtitle" type="textarea" :rows="2" placeholder="可选" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">确定</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import type {FormInstance, FormRules} from 'element-plus'
import {fetchMallSkuCreate} from '@/api/mall/sku'

interface Props {
    visible: boolean
    defaultSpuId?: number
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'success'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const formRef = ref<FormInstance>()
  const submitting = ref(false)

  const formData = reactive({
    spuId: undefined as number | undefined,
    skuName: '',
    price: undefined as number | undefined,
    skuTitle: '',
    skuSubtitle: ''
  })

  const rules: FormRules = {
    spuId: [{ required: true, message: '请输入 SPU ID', trigger: 'blur' }],
    skuName: [{ required: true, message: '请输入 SKU 名称', trigger: 'blur' }],
    price: [{ required: true, message: '请输入价格', trigger: 'blur' }]
  }

  watch(
    () => props.visible,
    (visible) => {
      if (!visible) return
      formData.spuId = props.defaultSpuId
      formData.skuName = ''
      formData.price = undefined
      formData.skuTitle = ''
      formData.skuSubtitle = ''
      nextTick(() => formRef.value?.clearValidate())
    }
  )

  async function handleSubmit() {
    await formRef.value?.validate()
    if (formData.spuId == null || formData.price == null) return
    submitting.value = true
    try {
      await fetchMallSkuCreate({
        spuId: formData.spuId,
        skuName: formData.skuName.trim(),
        price: formData.price,
        skuTitle: formData.skuTitle || undefined,
        skuSubtitle: formData.skuSubtitle || undefined
      })
      dialogVisible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped lang="scss">
  .w-full {
    width: 100%;
  }
</style>
