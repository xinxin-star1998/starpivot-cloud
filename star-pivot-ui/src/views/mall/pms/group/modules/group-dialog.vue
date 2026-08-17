<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? '新增属性分组' : '编辑属性分组'"
    width="520px"
    align-center
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <ElFormItem label="组名" prop="attrGroupName">
        <ElInput v-model="formData.attrGroupName" placeholder="请输入组名" />
      </ElFormItem>
      <ElFormItem label="排序" prop="sort">
        <ElInputNumber
          v-model="formData.sort"
          :min="0"
          :precision="0"
          controls-position="right"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="描述" prop="descript">
        <ElInput v-model="formData.descript" placeholder="请输入描述" />
      </ElFormItem>
      <ElFormItem label="组图标" prop="icon">
        <ArtIconPicker ref="iconPickerRef" v-model="formData.icon" :manual="true">
          <ElInput
            v-model="formData.icon"
            placeholder="如：heroicons-outline:rectangle-group"
            clearable
            style="width: 100%"
          >
            <template #prepend>
              <div class="group-icon-prepend">
                <Icon
                  :icon="formData.icon || 'heroicons-outline:rectangle-group'"
                  style="font-size: 18px"
                />
              </div>
            </template>
            <template #append>
              <ElButton @click.stop="openIconPicker">选择图标</ElButton>
            </template>
          </ElInput>
        </ArtIconPicker>
      </ElFormItem>
      <ElFormItem label="所属分类" prop="catalogPath">
        <ElCascader
          v-model="catalogPath"
          :options="categoryOptions"
          :props="cascaderProps"
          clearable
          filterable
          :show-all-levels="true"
          placeholder="试试搜索：手机"
          style="width: 100%"
        />
      </ElFormItem>
    </ElForm>
    <template #footer>
      <div class="dialog-footer">
        <ElButton @click="dialogVisible = false">取消</ElButton>
        <ElButton type="primary" @click="handleSubmit">确定</ElButton>
      </div>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { Icon } from '@iconify/vue'
  import ArtIconPicker from '@/components/core/base/art-icon-picker/index.vue'
  import { fetchAddGroup, fetchGetGroupById, fetchUpdateGroup, type Group } from '@/api/mall/group'
  import { fetchMallCategoryTree, type MallCategoryTreeNode } from '@/api/mall/category'
  import {
    filterVisibleCategoryTree,
    findCategoryNode,
    findCategoryPath,
    mapCategoryCascaderOptions
  } from '@/utils/mall/category-tree'

  interface Props {
    visible: boolean
    type: string
    groupData?: Partial<Group>
    /** 新增时由左侧树选中的三级分类 id */
    defaultCatelogId?: number
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

  const dialogType = computed(() => props.type)

  const formRef = ref<FormInstance>()
  const iconPickerRef = ref<{ open: () => void; close: () => void } | null>(null)

  const openIconPicker = () => {
    iconPickerRef.value?.open()
  }

  const categoryOptions = ref<MallCategoryTreeNode[]>([])
  const catalogPath = ref<number[]>([])

  const cascaderProps = {
    value: 'catId',
    label: 'name',
    children: 'children',
    leaf: 'leaf',
    emitPath: true,
    expandTrigger: 'click' as const,
    checkStrictly: false
  }

  const formData = reactive({
    attrGroupName: '',
    sort: 0,
    descript: '',
    icon: '',
    catelogId: undefined as number | undefined
  })

  const rules: FormRules = {
    attrGroupName: [{ required: true, message: '组名不能为空', trigger: 'blur' }],
    sort: [{ required: true, message: '排序不能为空', trigger: 'change' }],
    descript: [{ required: true, message: '描述不能为空', trigger: 'blur' }],
    catalogPath: [
      {
        validator: (_rule, _value, callback) => {
          if (!catalogPath.value?.length) {
            callback(new Error('请选择所属分类'))
            return
          }
          const last = catalogPath.value[catalogPath.value.length - 1]
          const node = last != null ? findCategoryNode(categoryOptions.value, last) : null
          const lv = node?.catLevel != null ? Number(node.catLevel) : 0
          if (!node || (lv !== 3 && catalogPath.value.length < 3)) {
            callback(new Error('请选择三级分类'))
            return
          }
          callback()
        },
        trigger: 'change'
      }
    ]
  }

  const loadCategoryOptions = async () => {
    try {
      const tree = await fetchMallCategoryTree()
      categoryOptions.value = mapCategoryCascaderOptions(filterVisibleCategoryTree(tree || []))
    } catch {
      categoryOptions.value = []
    }
  }

  const syncCatalogPathFromId = (catelogId: number | undefined) => {
    if (catelogId == null || !categoryOptions.value.length) {
      catalogPath.value = []
      return
    }
    catalogPath.value = findCategoryPath(categoryOptions.value, catelogId) ?? []
  }

  watch(catalogPath, (path) => {
    const last = path?.length ? path[path.length - 1] : undefined
    formData.catelogId = last
  })

  const initFormData = async () => {
    await loadCategoryOptions()
    const isEdit = props.type === 'edit' && props.groupData

    if (isEdit && props.groupData?.attrGroupId) {
      try {
        const detail = await fetchGetGroupById(props.groupData.attrGroupId)
        if (detail) {
          Object.assign(formData, {
            attrGroupName: detail.attrGroupName || '',
            sort: detail.sort != null ? Number(detail.sort) : 0,
            descript: detail.descript || '',
            icon: detail.icon || '',
            catelogId: detail.catelogId != null ? Number(detail.catelogId) : undefined
          })
          syncCatalogPathFromId(formData.catelogId)
        }
      } catch {
        ElMessage.error('获取属性分组详情失败')
        const row = props.groupData
        Object.assign(formData, {
          attrGroupName: row.attrGroupName || '',
          sort: row.sort != null ? Number(row.sort) : 0,
          descript: row.descript || '',
          icon: row.icon || '',
          catelogId: row.catelogId != null ? Number(row.catelogId) : undefined
        })
        syncCatalogPathFromId(formData.catelogId)
      }
    } else {
      const presetId = props.defaultCatelogId != null ? Number(props.defaultCatelogId) : undefined
      Object.assign(formData, {
        attrGroupName: '',
        sort: 0,
        descript: '',
        icon: '',
        catelogId: presetId
      })
      syncCatalogPathFromId(presetId)
    }
  }

  watch(
    () => [props.visible, props.type, props.groupData, props.defaultCatelogId],
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

    const catelogId =
      (catalogPath.value?.length ?? 0) > 0
        ? catalogPath.value[catalogPath.value.length - 1]
        : formData.catelogId
    if (catelogId == null) {
      ElMessage.warning('请选择所属分类')
      return
    }

    try {
      const submitData: Record<string, unknown> = {
        attrGroupName: formData.attrGroupName,
        sort: formData.sort,
        descript: formData.descript,
        icon: formData.icon,
        catelogId
      }

      if (dialogType.value === 'add') {
        await fetchAddGroup(submitData as Group)
      } else {
        submitData.attrGroupId = props.groupData?.attrGroupId
        await fetchUpdateGroup(submitData as Group)
      }
      ElMessage.success(dialogType.value === 'add' ? '新增成功' : '更新成功')
      dialogVisible.value = false
      emit('submit')
    } catch {
      ElMessage.error(dialogType.value === 'add' ? '新增失败' : '更新失败')
    }
  }
</script>

<style scoped lang="scss">
  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }

  .group-icon-prepend {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
  }
</style>
