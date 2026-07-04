<template>
  <ElDialog v-model="dialogVisible" title="合并到采购单" width="480px" align-center destroy-on-close>
    <ElForm ref="formRef" :model="formData" label-width="120px">
      <ElFormItem label="目标采购单 ID">
        <ElInputNumber
          v-model="formData.purchaseId"
          :min="1"
          controls-position="right"
          placeholder="留空则新建采购单"
          class="w-full"
        />
      </ElFormItem>
      <ElFormItem label="已选需求">
        <ElTag v-for="id in detailIds" :key="id" style="margin-right: 6px">{{ id }}</ElTag>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">合并</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import {fetchPurchaseMerge} from '@/api/mall/purchase'

interface Props {
    visible: boolean
    detailIds: number[]
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

  const submitting = ref(false)
  const formData = reactive({
    purchaseId: undefined as number | undefined
  })

  watch(
    () => props.visible,
    (visible) => {
      if (visible) {
        formData.purchaseId = undefined
      }
    }
  )

  async function handleSubmit() {
    if (!props.detailIds.length) return
    submitting.value = true
    try {
      await fetchPurchaseMerge({
        purchaseId: formData.purchaseId,
        items: props.detailIds
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
