<template>
  <ElDialog v-model="dialogVisible" align-center destroy-on-close title="快速入库" width="480px">
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <StockSkuWareSelect
        v-model:sku-id="formData.skuId"
        v-model:ware-id="formData.wareId"
        sku-prop="skuId"
        ware-prop="wareId"
        @sku-selected="onSkuSelected"
      />
      <ElFormItem label="入库数量" prop="skuNum">
        <ElInputNumber
          v-model="formData.skuNum"
          :min="1"
          class="w-full"
          controls-position="right"
        />
      </ElFormItem>
      <ElFormItem v-if="selectedSkuName" label="SKU 名称">
        <span class="readonly-text">{{ selectedSkuName }}</span>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton :loading="submitting" type="primary" @click="handleSubmit">确认入库</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import type {FormInstance, FormRules} from 'element-plus'
import {ElMessage} from 'element-plus'
import {fetchInboundSku} from '@/api/mall/ware-sku'
import type {MallSkuVo} from '@/api/mall/sku'
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
  const selectedSkuName = ref('')

  const formData = reactive({
    skuId: undefined as number | undefined,
    wareId: undefined as number | undefined,
    skuNum: 1
  })

  const rules: FormRules = {
    skuId: [{ required: true, message: '请选择 SKU', trigger: 'change' }],
    wareId: [{ required: true, message: '请选择仓库', trigger: 'change' }],
    skuNum: [{ required: true, message: '请输入入库数量', trigger: 'blur' }]
  }

  function onSkuSelected(sku?: MallSkuVo) {
    selectedSkuName.value = sku?.skuName || ''
  }

  watch(
    () => props.visible,
    (visible) => {
      if (visible) {
        formData.skuId = undefined
        formData.wareId = undefined
        formData.skuNum = 1
        selectedSkuName.value = ''
      }
    }
  )

  async function handleSubmit() {
    await formRef.value?.validate()
    submitting.value = true
    try {
      await fetchInboundSku({
        skuId: formData.skuId!,
        wareId: formData.wareId!,
        skuNum: formData.skuNum
      })
      ElMessage.success('入库成功')
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
  .readonly-text {
    color: var(--el-text-color-secondary);
    font-size: 13px;
  }
</style>
