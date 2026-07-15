<template>
  <ElDialog
    v-model="dialogVisible"
    :title="dialogType === 'add' ? `新增${typeLabel}` : `编辑${typeLabel}`"
    width="640px"
    align-center
    destroy-on-close
  >
    <ElForm ref="formRef" :model="formData" :rules="rules" label-width="110px">
      <ElFormItem label="属性名" prop="attrName">
        <ElInput
          v-model="formData.attrName"
          placeholder="请输入属性名"
          maxlength="64"
          show-word-limit
        />
      </ElFormItem>

      <ElFormItem label="属性类型">
        <ElSelect :model-value="props.attrType" disabled style="width: 100%">
          <ElOption :value="1" label="规格参数" />
          <ElOption :value="0" label="销售属性" />
        </ElSelect>
      </ElFormItem>

      <ElFormItem label="值类型" prop="valueType">
        <ElSwitch
          v-model="valueTypeMultiple"
          inline-prompt
          active-text="多选"
          inactive-text="单选"
        />
        <span class="value-type-hint">
          {{
            Number(formData.valueType) === 1 ? '用户可同时选多个' : '用户只能选一个，且必须选一个'
          }}
        </span>
      </ElFormItem>

      <ElFormItem label="可选值" prop="valueSelect">
        <ElInputTag
          v-model="valueSelectTags"
          clearable
          trigger="Enter"
          placeholder="输入后回车添加，多个值以分号存储"
          style="width: 100%"
        />
      </ElFormItem>

      <ElFormItem label="属性图标" prop="icon">
        <ArtIconPicker ref="iconPickerRef" v-model="formData.icon" :manual="true">
          <ElInput
            v-model="formData.icon"
            placeholder="如：heroicons-outline:tag"
            clearable
            style="width: 100%"
          >
            <template #prepend>
              <div class="attr-icon-prepend">
                <Icon :icon="formData.icon || 'heroicons-outline:tag'" style="font-size: 18px" />
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
          v-model="formData.catalogPath"
          :options="categoryOptions"
          :props="cascaderProps"
          clearable
          filterable
          :show-all-levels="true"
          placeholder="请选择三级分类"
          style="width: 100%"
          @change="handleCatalogChange"
        />
      </ElFormItem>

      <ElFormItem label="所属分组" prop="attrGroupId">
        <ElSelect
          v-model="formData.attrGroupId"
          clearable
          filterable
          placeholder="请先选择分类，再选分组"
          style="width: 100%"
          :disabled="!formData.catelogId"
        >
          <ElOption
            v-for="g in groupOptions"
            :key="g.attrGroupId!"
            :label="g.attrGroupName ?? ''"
            :value="g.attrGroupId!"
          />
        </ElSelect>
      </ElFormItem>

      <ElFormItem label="可检索" prop="searchType">
        <ElSwitch
          v-model="formData.searchType"
          :active-value="1"
          :inactive-value="0"
          inline-prompt
          active-text="是"
          inactive-text="否"
        />
      </ElFormItem>

      <ElFormItem label="快速展示" prop="showDesc">
        <ElSwitch
          v-model="formData.showDesc"
          :active-value="1"
          :inactive-value="0"
          inline-prompt
          active-text="是"
          inactive-text="否"
        />
      </ElFormItem>

      <ElFormItem label="启用状态" prop="enable">
        <ElSwitch
          v-model="formData.enable"
          :active-value="1"
          :inactive-value="0"
          inline-prompt
          active-text="启用"
          inactive-text="禁用"
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
import type {FormInstance, FormRules} from 'element-plus'
/**
 * 属性新增/编辑弹窗。
 * attrGroupId 提交后由后端写入 pms_attr_attrgroup_relation，非 pms_attr 字段。
 * attrType 由父页面传入（0 销售 / 1 基本），弹窗内只读展示。
 */
import {ElMessage} from 'element-plus'
import {Icon} from '@iconify/vue'
import ArtIconPicker from '@/components/core/base/art-icon-picker/index.vue'
import {fetchAddAttr, fetchGetAttrById, fetchUpdateAttr, type MallAttr} from '@/api/mall/attr'
import {fetchGetGroupList, type Group} from '@/api/mall/group'
import {fetchMallCategoryTree, type MallCategoryTreeNode} from '@/api/mall/category'
import {
  filterVisibleCategoryTree,
  findCategoryNode,
  findCategoryPath,
  mapCategoryCascaderOptions
} from '@/utils/mall/category-tree'
import {joinValueSelect, normalizeAttrValueFields, parseValueSelect} from '@/utils/mall/attr-value-select'
import type {DialogType} from '@/types'

const props = defineProps<{
    visible: boolean
    type: DialogType
    attrType: 0 | 1
    attrData?: Partial<MallAttr>
    defaultCatelogId?: number
  }>()

  const emit = defineEmits<{
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }>()

  const typeLabel = computed(() => (props.attrType === 1 ? '基本属性' : '销售属性'))

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
  const groupOptions = ref<Group[]>([])

  /** 与 formData.valueSelect 双向同步（分号存储） */
  const valueSelectTags = computed({
    get: () => parseValueSelect(formData.valueSelect),
    set: (tags: string[]) => {
      formData.valueSelect = joinValueSelect(tags) ?? ''
    }
  })

  /** 值类型：0 单选，1 多选 */
  const valueTypeMultiple = computed({
    get: () => Number(formData.valueType) === 1,
    set: (on: boolean) => {
      formData.valueType = on ? 1 : 0
      nextTick(() => formRef.value?.clearValidate('valueSelect'))
    }
  })

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
    attrId: undefined as number | undefined,
    attrName: '',
    searchType: 1,
    valueType: 0,
    icon: '',
    valueSelect: '',
    enable: 1,
    showDesc: 1,
    catelogId: undefined as number | undefined,
    attrGroupId: undefined as number | undefined,
    /** 与 ElCascader 绑定，供表单校验 */
    catalogPath: [] as number[]
  })

  const rules: FormRules = {
    attrName: [{ required: true, message: '属性名不能为空', trigger: 'blur' }],
    catalogPath: [
      {
        validator: (_rule, value, callback) => {
          const path = (value as number[] | undefined) ?? formData.catalogPath
          if (!path?.length) {
            callback(new Error('请选择三级分类'))
            return
          }
          const last = path[path.length - 1]
          const node = last != null ? findCategoryNode(categoryOptions.value, last) : null
          const lv = node?.catLevel != null ? Number(node.catLevel) : 0
          if (!node || (lv !== 3 && path.length < 3)) {
            callback(new Error('请选择三级分类'))
            return
          }
          callback()
        },
        trigger: 'change'
      }
    ],
    valueSelect: [
      {
        validator: (_rule, _value, callback) => {
          if (parseValueSelect(formData.valueSelect).length === 0) {
            callback(new Error('请添加至少一个可选值'))
            return
          }
          callback()
        },
        trigger: 'blur'
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

  const loadGroupOptions = async (catelogId?: number) => {
    if (catelogId == null) {
      groupOptions.value = []
      return
    }
    try {
      const page = await fetchGetGroupList({
        catelogId,
        pageNum: 1,
        pageSize: 200
      })
      const list = page?.rows ?? []
      groupOptions.value = list.filter((g) => g.attrGroupId != null)
    } catch {
      groupOptions.value = []
    }
  }

  const syncCatalogPathFromId = (catelogId: number | undefined) => {
    if (catelogId == null || !categoryOptions.value.length) {
      formData.catalogPath = []
      return
    }
    formData.catalogPath = findCategoryPath(categoryOptions.value, catelogId) ?? []
  }

  const handleCatalogChange = () => {
    const last = formData.catalogPath?.length
      ? formData.catalogPath[formData.catalogPath.length - 1]
      : undefined
    formData.catelogId = last
    formData.attrGroupId = undefined
    loadGroupOptions(last)
    nextTick(() => formRef.value?.validateField('catalogPath').catch(() => {}))
  }

  const applyDetail = (detail: Partial<MallAttr>, fallback?: Partial<MallAttr>) => {
    const d = detail as Partial<MallAttr> & Record<string, unknown>
    const f = fallback as (Partial<MallAttr> & Record<string, unknown>) | undefined
    const raw = d.valueSelect ?? d.value_select ?? f?.valueSelect ?? f?.value_select
    const valueTypeRaw = d.valueType ?? d.value_type ?? f?.valueType ?? f?.value_type
    const { valueType, valueSelect } = normalizeAttrValueFields(
      valueTypeRaw != null ? Number(valueTypeRaw) : 0,
      raw != null ? String(raw) : ''
    )
    Object.assign(formData, {
      attrId: detail.attrId ?? fallback?.attrId,
      attrName: detail.attrName ?? fallback?.attrName ?? '',
      searchType: detail.searchType ?? fallback?.searchType ?? 1,
      valueType,
      icon: detail.icon ?? fallback?.icon ?? '',
      valueSelect,
      enable:
        detail.enable != null
          ? Number(detail.enable)
          : fallback?.enable != null
            ? Number(fallback.enable)
            : 1,
      showDesc: detail.showDesc ?? fallback?.showDesc ?? 1,
      catelogId:
        detail.catelogId != null
          ? Number(detail.catelogId)
          : fallback?.catelogId != null
            ? Number(fallback.catelogId)
            : undefined,
      attrGroupId:
        detail.attrGroupId != null
          ? Number(detail.attrGroupId)
          : fallback?.attrGroupId != null
            ? Number(fallback.attrGroupId)
            : undefined
    })
    syncCatalogPathFromId(formData.catelogId)
    loadGroupOptions(formData.catelogId)
  }

  const resetForm = () => {
    const presetId = props.defaultCatelogId != null ? Number(props.defaultCatelogId) : undefined
    Object.assign(formData, {
      attrId: undefined,
      attrName: '',
      searchType: 1,
      valueType: 0,
      icon: '',
      valueSelect: '',
      enable: 1,
      showDesc: 1,
      catelogId: presetId,
      attrGroupId: undefined,
      catalogPath: []
    })
    syncCatalogPathFromId(presetId)
    loadGroupOptions(presetId)
  }

  const initFormData = async () => {
    await loadCategoryOptions()
    const row = props.attrData
    const isEdit = props.type === 'edit' && row?.attrId

    if (isEdit && row?.attrId) {
      applyDetail(row)
      try {
        const detail = await fetchGetAttrById(row.attrId)
        applyDetail(detail, row)
      } catch {
        ElMessage.error('获取属性详情失败')
        applyDetail(row)
      }
    } else {
      resetForm()
    }
  }

  watch(
    () => [props.visible, props.type, props.attrData, props.defaultCatelogId],
    async ([visible]) => {
      if (visible) {
        await initFormData()
        nextTick(() => formRef.value?.clearValidate())
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
      formData.catalogPath?.length > 0
        ? formData.catalogPath[formData.catalogPath.length - 1]
        : formData.catelogId
    if (catelogId == null) {
      ElMessage.warning('请选择所属分类')
      return
    }

    const payload: MallAttr = {
      attrName: formData.attrName,
      searchType: formData.searchType,
      valueType: formData.valueType,
      icon: formData.icon,
      valueSelect: joinValueSelect(parseValueSelect(formData.valueSelect)),
      attrType: props.attrType,
      enable: formData.enable,
      catelogId,
      attrGroupId: formData.attrGroupId,
      showDesc: formData.showDesc
    }

    try {
      if (dialogType.value === 'add') {
        await fetchAddAttr(payload)
        ElMessage.success('新增成功')
      } else {
        payload.attrId = formData.attrId
        await fetchUpdateAttr(payload)
        ElMessage.success('更新成功')
      }
      dialogVisible.value = false
      emit('submit')
    } catch {
      ElMessage.error(dialogType.value === 'add' ? '新增失败' : '更新失败')
    }
  }
</script>

<style scoped lang="scss">
  .value-type-hint {
    margin-left: 12px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .dialog-footer {
    display: flex;
    justify-content: flex-end;
    gap: 10px;
  }

  .attr-icon-prepend {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 32px;
  }
</style>
