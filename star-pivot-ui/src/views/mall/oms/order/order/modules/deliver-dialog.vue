<template>
  <ElDialog v-model="dialogVisible" title="订单发货" width="480px" destroy-on-close @closed="resetForm">
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="96px">
      <ElFormItem label="承运商" prop="carrierId">
        <ElSelect v-model="form.carrierId" placeholder="请选择承运商" filterable style="width: 100%">
          <ElOption
            v-for="item in carriers"
            :key="item.id"
            :label="item.carrierName"
            :value="item.id!"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="物流单号" prop="trackingNo">
        <ElInput v-model="form.trackingNo" placeholder="请输入物流单号" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">确认发货</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import {fetchTmsCarrierEnabled} from '@/api/tms/carrier'
import type {TmsCarrier} from '@/api/tms/carrier'
import {fetchTmsShipmentShip} from '@/api/tms/shipment'
import type {FormInstance, FormRules} from 'element-plus'
import {handleMutationError} from '@/utils/http/mutation'

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
const carriers = ref<TmsCarrier[]>([])
const form = ref({
  carrierId: undefined as number | undefined,
  trackingNo: ''
})

const rules: FormRules = {
  carrierId: [{ required: true, message: '请选择承运商', trigger: 'change' }],
  trackingNo: [{ required: true, message: '请输入物流单号', trigger: 'blur' }]
}

watch(
  () => props.visible,
  async (visible) => {
    if (!visible) return
    try {
      carriers.value = await fetchTmsCarrierEnabled()
    } catch (error) {
      handleMutationError(error, '加载承运商失败')
    }
  }
)

function resetForm() {
  form.value = { carrierId: undefined, trackingNo: '' }
  formRef.value?.resetFields()
}

async function handleSubmit() {
  if (props.orderId == null || form.value.carrierId == null) return
  await formRef.value?.validate()
  submitting.value = true
  try {
    await fetchTmsShipmentShip({
      orderId: props.orderId,
      carrierId: form.value.carrierId,
      trackingNo: form.value.trackingNo
    })
    dialogVisible.value = false
    emit('submit')
  } finally {
    submitting.value = false
  }
}
</script>
