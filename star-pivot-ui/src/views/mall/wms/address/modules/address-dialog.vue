<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增地区' : '编辑地区'"
    width="520px"
    align-center
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="110px">
      <ElFormItem label="地区编码" prop="code">
        <ElInput v-model="formData.code" placeholder="如 110000" maxlength="32" show-word-limit />
      </ElFormItem>
      <ElFormItem label="父级编码" prop="parentCode">
        <ElInput
          v-model="formData.parentCode"
          placeholder="省级填 0，下级填上级编码"
          maxlength="32"
          show-word-limit
        />
      </ElFormItem>
      <ElFormItem label="地区名称" prop="name">
        <ElInput v-model="formData.name" placeholder="地区名称" maxlength="255" show-word-limit />
      </ElFormItem>
      <ElFormItem label="层级" prop="level">
        <ElSelect v-model="formData.level" placeholder="请选择层级" style="width: 100%">
          <ElOption
            v-for="opt in ADDRESS_LEVEL_OPTIONS"
            :key="opt.value"
            :label="opt.label"
            :value="opt.value"
          />
        </ElSelect>
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" @click="handleSubmit">提交</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import type {FormInstance, FormRules} from 'element-plus'
import {
  type Address,
  type AddressSavePayload,
  fetchAddAddress,
  fetchGetAddressById,
  fetchUpdateAddress
} from '@/api/mall/address'
import type {DialogType} from '@/types'
import {ADDRESS_LEVEL_OPTIONS} from '@/utils/mall/address-level'

interface Props {
    visible: boolean
    type: DialogType
    addressData?: Partial<Address>
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

  const formData = reactive({
    id: undefined as number | undefined,
    code: '',
    parentCode: '0',
    name: '',
    level: 0 as number
  })

  const rules: FormRules = {
    code: [{ required: true, message: '请输入地区编码', trigger: 'blur' }],
    name: [{ required: true, message: '请输入地区名称', trigger: 'blur' }],
    level: [{ required: true, message: '请选择层级', trigger: 'change' }]
  }

  const initFormData = async () => {
    const isEdit = props.type === 'edit' && props.addressData?.id

    if (isEdit && props.addressData?.id) {
      try {
        const detail = await fetchGetAddressById(props.addressData.id)
        Object.assign(formData, {
          id: detail.id,
          code: detail.code ?? '',
          parentCode: detail.parentCode ?? '0',
          name: detail.name ?? '',
          level: detail.level ?? 0
        })
      } catch {
        const row = props.addressData
        Object.assign(formData, {
          id: row.id,
          code: row.code ?? '',
          parentCode: row.parentCode ?? '0',
          name: row.name ?? '',
          level: row.level ?? 0
        })
      }
    } else {
      const preset = props.addressData
      Object.assign(formData, {
        id: undefined,
        code: '',
        parentCode: preset?.parentCode ?? '0',
        name: '',
        level: preset?.level ?? 0
      })
    }
  }

  watch(
    () => [props.visible, props.type, props.addressData],
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

    const payload: AddressSavePayload = {
      code: formData.code,
      parentCode: formData.parentCode || '0',
      name: formData.name,
      level: formData.level
    }

    try {
      if (props.type === 'add') {
        await fetchAddAddress(payload)
      } else {
        payload.id = formData.id
        await fetchUpdateAddress(payload)
      }
      dialogVisible.value = false
      emit('submit')
    } catch {
      // http 拦截器提示
    }
  }
</script>
