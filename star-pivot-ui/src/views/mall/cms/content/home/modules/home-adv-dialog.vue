<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增轮播广告' : '编辑轮播广告'"
    width="560px"
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="广告名称" prop="name">
        <ElInput v-model="formData.name" maxlength="128" show-word-limit />
      </ElFormItem>
      <ElFormItem label="轮播图片" prop="pic">
        <MallImageUpload
          v-model="formData.pic"
          :max-size-mb="5"
          picker-title="选择轮播图片"
          hint="建议宽 1920px，支持 JPG/PNG/GIF/WEBP"
        />
      </ElFormItem>
      <ElFormItem label="跳转链接">
        <ElInput v-model="formData.url" placeholder="点击跳转 URL" />
      </ElFormItem>
      <ElFormItem label="排序">
        <ElInputNumber v-model="formData.sort" :min="0" controls-position="right" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="状态">
        <ElRadioGroup v-model="formData.status">
          <ElRadio :value="1">上架</ElRadio>
          <ElRadio :value="0">下架</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="开始时间">
        <ElDatePicker
          v-model="formData.startTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="选填"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="结束时间">
        <ElDatePicker
          v-model="formData.endTime"
          type="datetime"
          value-format="YYYY-MM-DD HH:mm:ss"
          placeholder="选填"
          style="width: 100%"
        />
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
    fetchHomeAdvAdd,
    fetchHomeAdvById,
    fetchHomeAdvUpdate,
    type HomeAdvSavePayload
  } from '@/api/mall/home-adv'
  import type { DialogType } from '@/types'
  import MallImageUpload from '@/components/mall/mall-image-upload/index.vue'

  const props = defineProps<{ visible: boolean; type: DialogType; recordId?: number }>()
  const emit = defineEmits<{ 'update:visible': [boolean]; success: [] }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const formData = ref<HomeAdvSavePayload>({
    name: '',
    pic: '',
    url: '',
    sort: 0,
    status: 1,
    note: ''
  })

  const rules: FormRules = {
    name: [{ required: true, message: '请输入广告名称', trigger: 'blur' }]
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return
      formData.value = { name: '', pic: '', url: '', sort: 0, status: 1, note: '' }
      if (props.type === 'edit' && props.recordId) {
        const detail = await fetchHomeAdvById(props.recordId)
        formData.value = {
          id: detail.id,
          name: detail.name ?? '',
          pic: detail.pic,
          url: detail.url,
          sort: detail.sort,
          status: detail.status ?? 1,
          startTime: detail.startTime,
          endTime: detail.endTime,
          note: detail.note,
          clickCount: detail.clickCount
        }
      }
    }
  )

  async function handleSubmit() {
    await formRef.value?.validate()
    submitting.value = true
    try {
      if (props.type === 'add') await fetchHomeAdvAdd(formData.value)
      else await fetchHomeAdvUpdate(formData.value)
      dialogVisible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>
