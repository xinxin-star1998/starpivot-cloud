<template>
  <ElDialog
    v-model="dialogVisible"
    align-center
    destroy-on-close
    title="新增采购需求"
    width="520px"
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <StockSkuWareSelect
        v-model:sku-id="formData.skuId"
        v-model:ware-id="formData.wareId"
        sku-prop="skuId"
        ware-prop="wareId"
        @sku-selected="onSkuSelected"
      />
      <ElFormItem label="采购数量" prop="skuNum">
        <ElInputNumber
          v-model="formData.skuNum"
          :min="1"
          controls-position="right"
          class="w-full"
        />
      </ElFormItem>
      <ElFormItem label="采购单价" prop="skuPrice">
        <ElInputNumber
          v-model="formData.skuPrice"
          :min="0"
          :precision="2"
          controls-position="right"
          class="w-full"
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">确定</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { fetchPurchaseDetailAdd } from '@/api/mall/purchase'
  import type { MallSkuVo } from '@/api/mall/sku'
  import StockSkuWareSelect from '@/views/mall/wms/modules/stock-sku-ware-select.vue'

  interface Props {
    visible: boolean
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
  const formData = reactive({
    skuId: undefined as number | undefined,
    skuNum: 1,
    skuPrice: undefined as number | undefined,
    wareId: undefined as number | undefined
  })

  const rules: FormRules = {
    skuId: [{ required: true, message: '请选择 SKU', trigger: 'change' }],
    skuNum: [{ required: true, message: '请输入采购数量', trigger: 'blur' }]
  }

  function onSkuSelected(sku?: MallSkuVo) {
    if (sku?.price != null && formData.skuPrice == null) {
      formData.skuPrice = Number(sku.price)
    }
  }

  watch(
    () => props.visible,
    (visible) => {
      if (visible) {
        formData.skuId = undefined
        formData.skuNum = 1
        formData.skuPrice = undefined
        formData.wareId = undefined
      }
    }
  )

  async function handleSubmit() {
    await formRef.value?.validate()
    submitting.value = true
    try {
      await fetchPurchaseDetailAdd({
        skuId: formData.skuId!,
        skuNum: formData.skuNum,
        skuPrice: formData.skuPrice,
        wareId: formData.wareId
      })
      dialogVisible.value = false
      emit('submit')
    } finally {
      submitting.value = false
    }
  }
</script>

<style scoped>
  .w-full {
    width: 100%;
  }
</style>
