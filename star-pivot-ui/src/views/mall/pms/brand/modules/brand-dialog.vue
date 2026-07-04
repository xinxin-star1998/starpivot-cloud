<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增品牌' : '编辑品牌'"
    align-center
    destroy-on-close
    width="560px"
  >
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="rules"
      aria-label="品牌信息表单"
      label-width="110px"
    >
      <ElFormItem label="品牌名称" prop="name">
        <ElInput v-model="formData.name" maxlength="128" placeholder="名称" show-word-limit />
      </ElFormItem>
      <ElFormItem label="Logo" prop="logo">
        <BrandLogoUpload
          ref="logoUploadRef"
          v-model="formData.logo"
          :brand-id="formData.brandId"
        />
      </ElFormItem>
      <ElFormItem label="介绍" prop="descript">
        <ElInput v-model="formData.descript" :rows="3" placeholder="品牌介绍" type="textarea" />
      </ElFormItem>
      <ElFormItem label="排序" prop="sort">
        <ElInputNumber v-model="formData.sort" :max="99999" :min="0" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="显示状态" prop="showStatus">
        <ElRadioGroup v-model="formData.showStatus">
          <ElRadio :value="1">显示</ElRadio>
          <ElRadio :value="0">不显示</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="首字母" prop="firstLetter">
        <ElInput
          v-model="formData.firstLetter"
          maxlength="1"
          placeholder="检索首字母"
          show-word-limit
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton :loading="submitting" type="primary" @click="handleSubmit">提交</ElButton>
    </template>
  </ElDialog>
</template>

<script lang="ts" setup>
import type {FormInstance, FormRules} from 'element-plus'
import {
  fetchMallBrandAdd,
  fetchMallBrandById,
  fetchMallBrandUpdate,
  type MallBrandSavePayload,
  type MallBrandVo
} from '@/api/mall/brand'
import type {DialogType} from '@/types'
import BrandLogoUpload from './brand-logo-upload.vue'

interface Props {
    visible: boolean
    type: DialogType
    brandData?: Partial<MallBrandVo>
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
  const logoUploadRef = ref<InstanceType<typeof BrandLogoUpload>>()
  const submitting = ref(false)

  const formData = reactive({
    brandId: undefined as number | undefined,
    name: '',
    logo: '',
    descript: '',
    sort: 0,
    showStatus: 1,
    firstLetter: ''
  })

  const rules: FormRules = {
    name: [{ required: true, message: '请输入品牌名称', trigger: 'blur' }],
    sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
    showStatus: [{ required: true, message: '请选择显示状态', trigger: 'change' }]
  }

  const initFormData = async () => {
    const isEdit = props.type === 'edit' && props.brandData?.brandId

    if (isEdit && props.brandData?.brandId) {
      try {
        const detail = await fetchMallBrandById(props.brandData.brandId)
        Object.assign(formData, {
          brandId: detail.brandId,
          name: detail.name ?? '',
          logo: detail.logo ?? '',
          descript: detail.descript ?? '',
          sort: detail.sort ?? 0,
          showStatus: detail.showStatus ?? 1,
          firstLetter: detail.firstLetter ?? ''
        })
      } catch {
        const row = props.brandData
        Object.assign(formData, {
          brandId: row.brandId,
          name: row.name ?? '',
          logo: row.logo ?? '',
          descript: row.descript ?? '',
          sort: row.sort ?? 0,
          showStatus: row.showStatus ?? 1,
          firstLetter: row.firstLetter ?? ''
        })
      }
    } else {
      Object.assign(formData, {
        brandId: undefined,
        name: '',
        logo: '',
        descript: '',
        sort: 0,
        showStatus: 1,
        firstLetter: ''
      })
    }
  }

  watch(
    () => [props.visible, props.type, props.brandData],
    async ([visible]) => {
      if (visible) {
        await initFormData()
        nextTick(() => {
          formRef.value?.clearValidate()
        })
      }
    },
    { immediate: true }
  )

  const handleSubmit = async () => {
    if (!formRef.value) return
    try {
      await formRef.value.validate()
    } catch {
      return
    }
    submitting.value = true
    try {
      const logo = await logoUploadRef.value?.resolveLogo()
      const payload: MallBrandSavePayload = {
        name: formData.name,
        logo,
        descript: formData.descript || undefined,
        sort: formData.sort,
        showStatus: formData.showStatus,
        firstLetter: formData.firstLetter || undefined
      }
      if (props.type === 'add') {
        await fetchMallBrandAdd(payload)
      } else {
        payload.brandId = formData.brandId
        await fetchMallBrandUpdate(payload)
      }
      dialogVisible.value = false
      emit('submit')
    } catch {
      // http 拦截器提示或 Logo 上传失败
    } finally {
      submitting.value = false
    }
  }
</script>
