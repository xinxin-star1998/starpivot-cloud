<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增仓库' : '编辑仓库'"
    width="560px"
    align-center
    destroy-on-close
  >
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="110px"
      aria-label="仓库信息表单"
    >
      <ElFormItem label="仓库名称" prop="name">
        <ElInput v-model="formData.name" placeholder="仓库名称" maxlength="128" show-word-limit />
      </ElFormItem>
      <ElFormItem label="所在地区" prop="areaPath">
        <ElCascader
          ref="areaCascaderRef"
          v-model="areaPath"
          :props="warehouseAddressCascaderProps"
          clearable
          :show-all-levels="true"
          placeholder="请选择省 / 市 / 区县"
          style="width: 100%"
          @change="handleAreaChange"
        />
        <p v-if="formData.areacode" class="area-code-hint">区域编码：{{ formData.areacode }}</p>
      </ElFormItem>
      <ElFormItem label="详细地址" prop="address">
        <ElInput
          v-model="formData.address"
          type="textarea"
          :rows="3"
          placeholder="街道、门牌等详细地址"
          maxlength="512"
          show-word-limit
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <ElButton @click="dialogVisible = false">取消</ElButton>
      <ElButton type="primary" @click="handleSubmit">提交</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
import type {CascaderInstance, CascaderValue, FormInstance, FormRules} from 'element-plus'
import {
  fetchWmsWareInfoAdd,
  fetchWmsWareInfoById,
  fetchWmsWareInfoUpdate,
  type WmsWareInfoSavePayload,
  type WmsWareInfoVo
} from '@/api/mall/wareinfo'
import type {DialogType} from '@/types'
import {
  resolveAddressCodePath,
  WAREHOUSE_MAX_AREA_LEVEL,
  warehouseAddressCascaderProps
} from '@/utils/mall/address-cascader'

interface Props {
    visible: boolean
    type: DialogType
    wareData?: Partial<WmsWareInfoVo>
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
  const areaCascaderRef = ref<CascaderInstance>()
  const areaPath = ref<string[]>([])

  const formData = reactive({
    id: undefined as number | undefined,
    name: '',
    address: '',
    areacode: '',
    areaPath: [] as string[]
  })

  const rules: FormRules = {
    name: [{ required: true, message: '请输入仓库名称', trigger: 'blur' }],
    areaPath: [{ required: true, message: '请选择所在地区', trigger: 'change' }]
  }

  const syncAreaPathToForm = () => {
    formData.areaPath = [...areaPath.value]
  }

  const handleAreaChange = (value: CascaderValue | null | undefined) => {
    const codes = Array.isArray(value) ? value.map(String) : []
    areaPath.value = codes
    formData.areacode = codes.length ? codes[codes.length - 1] : ''
    syncAreaPathToForm()
    // 选满省 / 市 / 区县三级后再收起下拉
    if (codes.length >= WAREHOUSE_MAX_AREA_LEVEL + 1) {
      nextTick(() => areaCascaderRef.value?.togglePopperVisible(false))
    }
  }

  const initFormData = async () => {
    const isEdit = props.type === 'edit' && props.wareData?.id

    if (isEdit && props.wareData?.id) {
      try {
        const detail = await fetchWmsWareInfoById(props.wareData.id)
        Object.assign(formData, {
          id: detail.id,
          name: detail.name ?? '',
          address: detail.address ?? '',
          areacode: detail.areacode ?? ''
        })
      } catch {
        const row = props.wareData
        Object.assign(formData, {
          id: row.id,
          name: row.name ?? '',
          address: row.address ?? '',
          areacode: row.areacode ?? ''
        })
      }
    } else {
      Object.assign(formData, {
        id: undefined,
        name: '',
        address: '',
        areacode: ''
      })
    }

    areaPath.value = await resolveAddressCodePath(formData.areacode, {
      maxPathLength: WAREHOUSE_MAX_AREA_LEVEL + 1
    })
    syncAreaPathToForm()
  }

  watch(
    () => [props.visible, props.type, props.wareData],
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
    syncAreaPathToForm()
    try {
      await formRef.value.validate()
    } catch {
      return
    }
    const payload: WmsWareInfoSavePayload = {
      name: formData.name,
      address: formData.address || undefined,
      areacode: formData.areacode || undefined
    }
    try {
      if (props.type === 'add') {
        await fetchWmsWareInfoAdd(payload)
      } else {
        payload.id = formData.id
        await fetchWmsWareInfoUpdate(payload)
      }
      dialogVisible.value = false
      emit('submit')
    } catch {
      // http 拦截器提示
    }
  }
</script>

<style scoped lang="scss">
  .area-code-hint {
    margin: 6px 0 0;
    font-size: 12px;
    line-height: 1.4;
    color: var(--art-gray-600);
  }
</style>
