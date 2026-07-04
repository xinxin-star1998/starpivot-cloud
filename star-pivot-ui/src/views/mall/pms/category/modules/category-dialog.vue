<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogTitle"
    width="520px"
    align-center
    destroy-on-close
  >
    <ElForm
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      aria-label="商品分类表单"
    >
      <ElFormItem v-if="isAdd" label="上级类目">
        <ElInput :model-value="parentLabel" disabled />
      </ElFormItem>
      <ElFormItem label="分类名称" prop="name">
        <ElInput v-model="formData.name" placeholder="名称" maxlength="128" show-word-limit />
      </ElFormItem>
      <ElFormItem label="排序" prop="sort">
        <ElInputNumber v-model="formData.sort" :min="0" :max="99999" style="width: 100%" />
      </ElFormItem>
      <ElFormItem label="显示状态" prop="showStatus">
        <ElRadioGroup v-model="formData.showStatus">
          <ElRadio :value="1">显示</ElRadio>
          <ElRadio :value="0">隐藏</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="图标" prop="icon">
        <ElInput v-model="formData.icon" placeholder="图标 URL" maxlength="512" show-word-limit />
      </ElFormItem>
      <ElFormItem label="计量单位" prop="productUnit">
        <ElInput
          v-model="formData.productUnit"
          placeholder="如：件、箱"
          maxlength="32"
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
import type {FormInstance, FormRules} from 'element-plus'
import {
  fetchMallCategoryAdd,
  fetchMallCategoryById,
  fetchMallCategoryUpdate,
  type MallCategorySavePayload,
  type MallCategoryTreeNode
} from '@/api/mall/category'
import type {DialogType} from '@/types'

interface ParentForAdd {
    catId: number
    name?: string
  }

  interface Props {
    visible: boolean
    type: DialogType
    /** 编辑时传入当前行数据（含 catId） */
    categoryData?: Partial<MallCategoryTreeNode>
    /** 新增子类时传入父节点；新增一级时为 null */
    parentForAdd?: ParentForAdd | null
  }

  export interface CategoryDialogSubmitPayload {
    mode: 'add' | 'edit'
    catId?: number
    parentCid?: number
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit', payload: CategoryDialogSubmitPayload): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const isAdd = computed(() => props.type === 'add')

  const dialogTitle = computed(() => {
    if (props.type === 'add') {
      return props.parentForAdd ? '新增子分类' : '新增一级分类'
    }
    return '编辑分类'
  })

  const parentLabel = computed(() => {
    if (!props.parentForAdd) return '（一级 / 无上级）'
    return props.parentForAdd.name || `ID ${props.parentForAdd.catId}`
  })

  const formRef = ref<FormInstance>()

  const formData = reactive({
    catId: undefined as number | undefined,
    name: '',
    sort: 0,
    showStatus: 1,
    icon: '',
    productUnit: ''
  })

  const rules: FormRules = {
    name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
    sort: [{ required: true, message: '请输入排序', trigger: 'blur' }],
    showStatus: [{ required: true, message: '请选择显示状态', trigger: 'change' }]
  }

  const initFormData = async () => {
    if (props.type === 'edit' && props.categoryData?.catId) {
      try {
        const detail = await fetchMallCategoryById(props.categoryData.catId)
        Object.assign(formData, {
          catId: detail.catId,
          name: detail.name ?? '',
          sort: detail.sort ?? 0,
          showStatus: detail.showStatus != null ? Number(detail.showStatus) : 1,
          icon: detail.icon ?? '',
          productUnit: detail.productUnit ?? ''
        })
      } catch {
        const row = props.categoryData
        Object.assign(formData, {
          catId: row.catId,
          name: row.name ?? '',
          sort: row.sort ?? 0,
          showStatus: row.showStatus != null ? Number(row.showStatus) : 1,
          icon: row.icon ?? '',
          productUnit: row.productUnit ?? ''
        })
      }
    } else {
      Object.assign(formData, {
        catId: undefined,
        name: '',
        sort: 0,
        showStatus: 1,
        icon: '',
        productUnit: ''
      })
    }
  }

  watch(
    () => [props.visible, props.type, props.categoryData, props.parentForAdd],
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
    const payload: MallCategorySavePayload = {
      name: formData.name,
      sort: formData.sort,
      showStatus: formData.showStatus,
      icon: formData.icon || undefined,
      productUnit: formData.productUnit || undefined
    }
    try {
      if (props.type === 'add') {
        payload.parentCid = props.parentForAdd?.catId ?? 0
        await fetchMallCategoryAdd(payload)
      } else {
        payload.catId = formData.catId
        await fetchMallCategoryUpdate(payload)
      }
      dialogVisible.value = false
      emit('submit', {
        mode: props.type === 'add' ? 'add' : 'edit',
        catId: props.type === 'edit' ? formData.catId : undefined,
        parentCid: props.type === 'add' ? (props.parentForAdd?.catId ?? 0) : undefined
      })
    } catch {
      // http 拦截器提示
    }
  }
</script>
