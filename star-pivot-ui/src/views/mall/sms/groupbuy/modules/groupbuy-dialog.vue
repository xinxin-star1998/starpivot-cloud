<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增拼团活动' : '编辑拼团活动'"
    width="620px"
    align-center
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="110px">
      <ElFormItem label="活动标题" prop="title">
        <ElInput v-model="formData.title" maxlength="255" />
      </ElFormItem>
      <ElFormItem label="SKU ID" prop="skuId">
        <ElInputNumber
          v-model="formData.skuId"
          :min="1"
          :controls="false"
          placeholder="请输入 SKU ID"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="拼团价" prop="groupPrice">
        <ElInputNumber
          v-model="formData.groupPrice"
          :min="0.01"
          :precision="2"
          :step="1"
          controls-position="right"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="成团人数" prop="groupSize">
        <ElInputNumber v-model="formData.groupSize" :min="2" controls-position="right" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="活动库存" prop="stockCount">
        <ElInputNumber v-model="formData.stockCount" :min="0" controls-position="right" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="每人限购">
        <ElInputNumber v-model="formData.limitBuy" :min="1" controls-position="right" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="成团有效期">
        <ElInputNumber v-model="formData.validHours" :min="1" controls-position="right" style="width: 100%">
          <template #append>小时</template>
        </ElInputNumber>
      </ElFormItem>
      <ElFormItem label="活动时间">
        <ElDatePicker
          v-model="dateRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始"
          end-placeholder="结束"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="状态">
        <ElRadioGroup v-model="formData.status">
          <ElRadio :value="1">上线</ElRadio>
          <ElRadio :value="0">下线</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="排序">
        <ElInputNumber v-model="formData.sort" :min="0" controls-position="right" style="width: 100%" />
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
    fetchGroupBuyActivityAdd,
    fetchGroupBuyActivityById,
    fetchGroupBuyActivityUpdate,
    type GroupBuyActivitySavePayload
  } from '@/api/mall/groupbuy'
  import type { DialogType } from '@/types'

  interface Props {
    visible: boolean
    type: DialogType
    activityId?: number
  }

  const props = defineProps<Props>()
  const emit = defineEmits<{ 'update:visible': [boolean]; success: [] }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const dateRange = ref<[string, string] | null>(null)

  const defaultForm = (): GroupBuyActivitySavePayload => ({
    title: '',
    skuId: 0,
    groupPrice: 0,
    groupSize: 2,
    stockCount: 0,
    limitBuy: 1,
    validHours: 24,
    status: 0,
    sort: 0
  })

  const formData = ref<GroupBuyActivitySavePayload>(defaultForm())

  const rules: FormRules = {
    title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
    skuId: [{ required: true, message: '请输入 SKU ID', trigger: 'blur' }],
    groupPrice: [{ required: true, message: '请输入拼团价', trigger: 'blur' }],
    groupSize: [{ required: true, message: '请输入成团人数', trigger: 'blur' }],
    stockCount: [{ required: true, message: '请输入活动库存', trigger: 'blur' }]
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return
      formData.value = defaultForm()
      dateRange.value = null
      if (props.type === 'edit' && props.activityId) {
        const detail = await fetchGroupBuyActivityById(props.activityId)
        formData.value = {
          id: detail.id,
          title: detail.title || '',
          skuId: detail.skuId || 0,
          groupPrice: detail.groupPrice || 0,
          groupSize: detail.groupSize || 2,
          stockCount: detail.stockCount || 0,
          limitBuy: detail.limitBuy || 1,
          validHours: detail.validHours || 24,
          status: detail.status ?? 0,
          sort: detail.sort || 0
        }
        if (detail.startTime && detail.endTime) {
          dateRange.value = [detail.startTime, detail.endTime]
        }
      }
    }
  )

  const handleSubmit = async () => {
    await formRef.value?.validate()
    submitting.value = true
    try {
      const payload: GroupBuyActivitySavePayload = {
        ...formData.value,
        startTime: dateRange.value?.[0],
        endTime: dateRange.value?.[1]
      }
      if (props.type === 'add') {
        await fetchGroupBuyActivityAdd(payload)
      } else {
        await fetchGroupBuyActivityUpdate(payload)
      }
      dialogVisible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>
