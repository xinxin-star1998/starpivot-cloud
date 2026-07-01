<template>
  <ElDialog
    v-model="dialogVisible"
    :title="type === 'add' ? '新增分类热门' : '编辑分类热门'"
    width="560px"
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="关联分类" prop="catId">
        <ElCascader
          v-model="categoryPath"
          :options="categoryOptions"
          :props="cascaderProps"
          clearable
          filterable
          placeholder="选择商品分类（三级）"
          style="width: 100%"
          @change="handleCategoryChange"
        />
      </ElFormItem>
      <ElFormItem label="展示标题">
        <ElInput v-model="formData.title" maxlength="200" show-word-limit placeholder="留空则使用分类名称" />
      </ElFormItem>
      <ElFormItem label="展示图标">
        <MallImageUpload
          v-model="formData.icon"
          :max-size-mb="2"
          picker-title="选择分类图标"
          hint="建议 128×128，支持 JPG/PNG/GIF/WEBP；留空则使用分类图标"
        />
      </ElFormItem>
      <ElFormItem label="跳转链接">
        <ElInput v-model="formData.url" placeholder="留空则跳转分类搜索页" />
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
import type {FormInstance, FormRules} from 'element-plus'
import {
  fetchCategoryHotAdd,
  fetchCategoryHotById,
  fetchCategoryHotUpdate,
  type HomeCategoryHotSavePayload
} from '@/api/mall/category-hot'
import {fetchMallCategoryTree, type MallCategoryTreeNode} from '@/api/mall/category'
import {filterVisibleCategoryTree, findCategoryPath, mapCategoryCascaderOptions} from '@/utils/mall/category-tree'
import type {DialogType} from '@/types'
import MallImageUpload from '@/components/mall/mall-image-upload/index.vue'

const props = defineProps<{ visible: boolean; type: DialogType; recordId?: number }>()
  const emit = defineEmits<{ 'update:visible': [boolean]; success: [] }>()

  const dialogVisible = computed({
    get: () => props.visible,
    set: (v) => emit('update:visible', v)
  })

  const formRef = ref<FormInstance>()
  const submitting = ref(false)
  const categoryOptions = ref<MallCategoryTreeNode[]>([])
  const categoryPath = ref<number[]>([])
  const formData = ref<HomeCategoryHotSavePayload>({
    title: '',
    icon: '',
    url: '',
    sort: 0,
    status: 1,
    note: ''
  })

  const cascaderProps = {
    value: 'catId',
    label: 'name',
    children: 'children',
    emitPath: true
  }

  const rules: FormRules = {
    catId: [{ required: true, message: '请选择关联分类', trigger: 'change' }]
  }

  async function loadCategoryOptions() {
    const tree = await fetchMallCategoryTree()
    categoryOptions.value = mapCategoryCascaderOptions(filterVisibleCategoryTree(tree || []))
  }

  function handleCategoryChange(path?: number[]) {
    const ids = path || []
    formData.value.catId = ids.length ? ids[ids.length - 1] : undefined
  }

  watch(
    () => props.visible,
    async (visible) => {
      if (!visible) return
      await loadCategoryOptions()
      categoryPath.value = []
      formData.value = {
        title: '',
        icon: '',
        url: '',
        sort: 0,
        status: 1,
        note: ''
      }
      if (props.type === 'edit' && props.recordId) {
        const detail = await fetchCategoryHotById(props.recordId)
        formData.value = {
          id: detail.id,
          catId: detail.catId ?? 0,
          title: detail.title ?? '',
          icon: detail.icon,
          url: detail.url,
          sort: detail.sort,
          status: detail.status ?? 1,
          note: detail.note
        }
        if (detail.catId != null) {
          const path = findCategoryPath(categoryOptions.value, detail.catId)
          categoryPath.value = path || [detail.catId]
        }
      }
    }
  )

  async function handleSubmit() {
    if (!formData.value.catId && categoryPath.value.length) {
      formData.value.catId = categoryPath.value[categoryPath.value.length - 1]
    }
    await formRef.value?.validate()
    submitting.value = true
    try {
      if (props.type === 'add') await fetchCategoryHotAdd(formData.value)
      else await fetchCategoryHotUpdate(formData.value)
      dialogVisible.value = false
      emit('success')
    } finally {
      submitting.value = false
    }
  }
</script>
