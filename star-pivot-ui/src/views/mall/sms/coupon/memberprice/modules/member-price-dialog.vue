<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增会员价' : '编辑会员价'"
    width="480px"
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="SKU ID" prop="skuId">
        <ElInputNumber v-model="formData.skuId" :min="1" controls-position="right" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="会员等级" prop="memberLevelId">
        <ElSelect v-model="formData.memberLevelId" placeholder="选择等级" style="width: 100%">
          <ElOption v-for="lv in levelOptions" :key="lv.id" :label="lv.name" :value="lv.id!" />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="会员价">
        <ElInputNumber
          v-model="formData.memberPrice"
          :min="0"
          :precision="2"
          controls-position="right"
          style="width: 100%"
        />
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
    fetchMemberLevelList,
    fetchMemberPriceAdd,
    fetchMemberPriceById,
    fetchMemberPriceUpdate,
    type MemberLevelVo,
    type MemberPriceSavePayload
  } from '@/api/mall/member-price'
  import type { DialogType } from '@/types'

  const props = defineProps<{ visible: boolean; type: DialogType; recordId?: number }>()
  const emit = defineEmits<{ 'update:visible': [boolean]; success: [] }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const levelOptions = ref<MemberLevelVo[]>([])
  const formData = ref<MemberPriceSavePayload>({ skuId: 0, memberLevelId: 0 })

  const rules: FormRules = {
    skuId: [{ required: true, message: '请输入 SKU ID', trigger: 'blur' }],
    memberLevelId: [{ required: true, message: '请选择会员等级', trigger: 'change' }]
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return
      levelOptions.value = await fetchMemberLevelList()
      formData.value = { skuId: undefined as unknown as number, memberLevelId: undefined as unknown as number }
      if (props.type === 'edit' && props.recordId) {
        const detail = await fetchMemberPriceById(props.recordId)
        formData.value = {
          id: detail.id,
          skuId: detail.skuId!,
          memberLevelId: detail.memberLevelId!,
          memberLevelName: detail.memberLevelName,
          memberPrice: detail.memberPrice,
          addOther: detail.addOther
        }
      }
    }
  )

  const handleSubmit = async () => {
    await formRef.value?.validate()
    submitting.value = true
    try {
      if (props.type === 'add') await fetchMemberPriceAdd(formData.value)
      else await fetchMemberPriceUpdate(formData.value)
      dialogVisible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>
