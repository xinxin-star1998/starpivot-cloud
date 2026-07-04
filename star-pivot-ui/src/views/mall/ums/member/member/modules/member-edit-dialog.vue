<template>
  <ElDialog v-model="dialogVisible" title="编辑会员" width="480px" destroy-on-close @closed="resetForm">
    <ElForm ref="formRef" :model="form" :rules="rules" label-width="88px">
      <ElFormItem label="昵称" prop="nickname">
        <ElInput v-model="form.nickname" placeholder="昵称" maxlength="64" />
      </ElFormItem>
      <ElFormItem label="状态" prop="status">
        <ElRadioGroup v-model="form.status">
          <ElRadio :value="1">启用</ElRadio>
          <ElRadio :value="0">禁用</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="积分">
        <ElInputNumber v-model="form.integration" :min="0" controls-position="right" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="成长值">
        <ElInputNumber v-model="form.growth" :min="0" controls-position="right" style="width: 100%" />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" :loading="submitting" @click="handleSubmit">保存</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import {fetchMemberById, fetchMemberUpdate, type MemberSavePayload} from '@/api/mall/member'
import type {FormInstance, FormRules} from 'element-plus'

interface Props {
    visible: boolean
    memberId?: number
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
  const form = ref<MemberSavePayload>({
    id: 0,
    nickname: '',
    status: 1,
    integration: 0,
    growth: 0
  })

  const rules: FormRules = {
    nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }]
  }

  watch(
    () => [props.visible, props.memberId] as const,
    async ([visible, id]) => {
      if (!visible || id == null) return
      const detail = await fetchMemberById(id)
      form.value = {
        id: detail.id!,
        nickname: detail.nickname,
        status: detail.status ?? 1,
        integration: detail.integration ?? 0,
        growth: detail.growth ?? 0
      }
    }
  )

  function resetForm() {
    formRef.value?.resetFields()
  }

  async function handleSubmit() {
    await formRef.value?.validate()
    submitting.value = true
    try {
      await fetchMemberUpdate(form.value)
      dialogVisible.value = false
      emit('submit')
    } finally {
      submitting.value = false
    }
  }
</script>
