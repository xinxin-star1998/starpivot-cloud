<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增场次' : '编辑场次'"
    width="520px"
    align-center
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="场次名称" prop="name">
        <ElInput v-model="formData.name" maxlength="200" />
      </ElFormItem>
      <ElFormItem label="开始时间">
        <ElDatePicker
          v-model="formData.startTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="每日开始时间"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="结束时间">
        <ElDatePicker
          v-model="formData.endTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="每日结束时间"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="状态">
        <ElRadioGroup v-model="formData.status">
          <ElRadio :value="1">启用</ElRadio>
          <ElRadio :value="0">禁用</ElRadio>
        </ElRadioGroup>
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
  fetchSeckillSessionAdd,
  fetchSeckillSessionById,
  fetchSeckillSessionUpdate,
  type SeckillSessionSavePayload
} from '@/api/mall/seckill-session'
import type {DialogType} from '@/types'

interface Props {
    visible: boolean
    type: DialogType
    sessionId?: number
  }

  const props = defineProps<Props>()
  const emit = defineEmits<{ 'update:visible': [boolean]; success: [] }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const formData = ref<SeckillSessionSavePayload>({ name: '', status: 1 })

  const rules: FormRules = {
    name: [{ required: true, message: '请输入场次名称', trigger: 'blur' }]
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return
      formData.value = { name: '', status: 1 }
      if (props.type === 'edit' && props.sessionId) {
        const detail = await fetchSeckillSessionById(props.sessionId)
        formData.value = {
          id: detail.id,
          name: detail.name || '',
          startTime: detail.startTime,
          endTime: detail.endTime,
          status: detail.status ?? 1
        }
      }
    }
  )

  const handleSubmit = async () => {
    await formRef.value?.validate()
    submitting.value = true
    try {
      if (props.type === 'add') {
        await fetchSeckillSessionAdd(formData.value)
      } else {
        await fetchSeckillSessionUpdate(formData.value)
      }
      dialogVisible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>
