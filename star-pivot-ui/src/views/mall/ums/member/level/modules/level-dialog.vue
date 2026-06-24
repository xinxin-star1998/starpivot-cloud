<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增会员等级' : '编辑会员等级'"
    width="560px"
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="120px">
      <ElFormItem label="等级名称" prop="name">
        <ElInput v-model="formData.name" placeholder="如：黄金会员" maxlength="64" />
      </ElFormItem>
      <ElFormItem label="成长值门槛">
        <ElInputNumber v-model="formData.growthPoint" :min="0" controls-position="right" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="免邮积分门槛">
        <ElInputNumber
          v-model="formData.freeFreightPoint"
          :min="0"
          :precision="2"
          controls-position="right"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="评论成长值">
        <ElInputNumber v-model="formData.commentGrowthPoint" :min="0" controls-position="right" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="默认等级">
        <ElRadioGroup v-model="formData.defaultStatus">
          <ElRadio :value="1">是</ElRadio>
          <ElRadio :value="0">否</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="免邮特权">
        <ElRadioGroup v-model="formData.privilegeFreeFreight">
          <ElRadio :value="1">是</ElRadio>
          <ElRadio :value="0">否</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="会员价特权">
        <ElRadioGroup v-model="formData.privilegeMemberPrice">
          <ElRadio :value="1">是</ElRadio>
          <ElRadio :value="0">否</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="生日特权">
        <ElRadioGroup v-model="formData.privilegeBirthday">
          <ElRadio :value="1">是</ElRadio>
          <ElRadio :value="0">否</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="备注">
        <ElInput v-model="formData.note" type="textarea" :rows="2" maxlength="512" />
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
    fetchMemberLevelAdd,
    fetchMemberLevelById,
    fetchMemberLevelUpdate,
    type MemberLevelSavePayload
  } from '@/api/mall/member-level'
  import type { DialogType } from '@/types'

  const props = defineProps<{ visible: boolean; type: DialogType; recordId?: number }>()
  const emit = defineEmits<{ 'update:visible': [boolean]; success: [] }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const formData = ref<MemberLevelSavePayload>({
    name: '',
    growthPoint: 0,
    defaultStatus: 0,
    freeFreightPoint: 0,
    commentGrowthPoint: 0,
    privilegeFreeFreight: 0,
    privilegeMemberPrice: 0,
    privilegeBirthday: 0,
    note: ''
  })

  const rules: FormRules = {
    name: [{ required: true, message: '请输入等级名称', trigger: 'blur' }]
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return
      formData.value = {
        name: '',
        growthPoint: 0,
        defaultStatus: 0,
        freeFreightPoint: 0,
        commentGrowthPoint: 0,
        privilegeFreeFreight: 0,
        privilegeMemberPrice: 0,
        privilegeBirthday: 0,
        note: ''
      }
      if (props.type === 'edit' && props.recordId) {
        const detail = await fetchMemberLevelById(props.recordId)
        formData.value = {
          id: detail.id,
          name: detail.name ?? '',
          growthPoint: detail.growthPoint,
          defaultStatus: detail.defaultStatus ?? 0,
          freeFreightPoint: detail.freeFreightPoint,
          commentGrowthPoint: detail.commentGrowthPoint,
          privilegeFreeFreight: detail.privilegeFreeFreight ?? 0,
          privilegeMemberPrice: detail.privilegeMemberPrice ?? 0,
          privilegeBirthday: detail.privilegeBirthday ?? 0,
          note: detail.note
        }
      }
    }
  )

  async function handleSubmit() {
    await formRef.value?.validate()
    submitting.value = true
    try {
      if (props.type === 'add') await fetchMemberLevelAdd(formData.value)
      else await fetchMemberLevelUpdate(formData.value)
      dialogVisible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>
