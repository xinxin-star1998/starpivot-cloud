<template>
  <ElDialog
    v-model="dialogVisible"
    :title="`绑定分类 — ${brandName || ''}`"
    width="560px"
    align-center
    destroy-on-close
  >
    <div class="bind-toolbar">
      <ElButton type="primary" @click="openPicker">新增关联</ElButton>
    </div>
    <ElTable
      v-loading="loading"
      :data="boundList"
      border
      stripe
      max-height="360px"
      empty-text="暂无关联分类"
    >
      <ElTableColumn type="index" label="序号" width="60" />
      <ElTableColumn prop="catelogName" label="三级分类" min-width="200" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.catelogName || '-' }}
        </template>
      </ElTableColumn>
      <ElTableColumn label="操作" width="80" fixed="right">
        <template #default="{ row }">
          <ElButton
            link
            type="danger"
            :loading="removingId === row.catelogId"
            @click="handleRemove(row)"
          >
            移除
          </ElButton>
        </template>
      </ElTableColumn>
    </ElTable>

    <ElDialog
      v-model="pickerVisible"
      title="选择三级分类"
      width="480px"
      align-center
      append-to-body
      destroy-on-close
    >
      <p class="bind-tip">请依次点击一级、二级展开，再选择三级类目（仅三级可确认）</p>
      <ElCascader
        v-model="pickerPath"
        :options="categoryOptions"
        :props="cascaderProps"
        clearable
        filterable
        :show-all-levels="true"
        placeholder="请选择三级分类"
        style="width: 100%"
      />
      <template #footer>
        <ElButton @click="pickerVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="adding" @click="confirmAdd">确定</ElButton>
      </template>
    </ElDialog>

    <template #footer>
      <ElButton @click="dialogVisible = false">关闭</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import { fetchMallCategoryTree, type MallCategoryTreeNode } from '@/api/mall/category'
  import {
    fetchMallBrandBindCategories,
    fetchMallBrandBoundCategories,
    type MallBrandCategoryRelation
  } from '@/api/mall/brand'
  import {
    filterVisibleCategoryTree,
    findCategoryNode,
    mapCategoryCascaderOptions
  } from '@/utils/mall/category-tree'
  import { ElMessage, ElMessageBox } from 'element-plus'

  interface Props {
    visible: boolean
    brandId?: number
    brandName?: string
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

  const loading = ref(false)
  const adding = ref(false)
  const removingId = ref<number | null>(null)
  const boundList = ref<MallBrandCategoryRelation[]>([])
  const changed = ref(false)

  const pickerVisible = ref(false)
  const pickerPath = ref<number[]>([])
  const categoryOptions = ref<MallCategoryTreeNode[]>([])

  const cascaderProps = {
    value: 'catId',
    label: 'name',
    children: 'children',
    leaf: 'leaf',
    emitPath: true,
    expandTrigger: 'click' as const,
    checkStrictly: false
  }

  const loadBoundList = async () => {
    const brandId = props.brandId
    if (brandId == null) return
    loading.value = true
    try {
      const res = await fetchMallBrandBoundCategories(brandId)
      boundList.value = (res || []).filter((r) => r.catelogId != null)
    } catch {
      boundList.value = []
    } finally {
      loading.value = false
    }
  }

  const boundCatIds = computed(() =>
    boundList.value.map((r) => Number(r.catelogId)).filter((id) => Number.isFinite(id))
  )

  const saveBindings = async (catIds: number[]) => {
    const brandId = props.brandId
    if (brandId == null) return
    await fetchMallBrandBindCategories(brandId, catIds)
    changed.value = true
    await loadBoundList()
  }

  const openPicker = async () => {
    pickerPath.value = []
    pickerVisible.value = true
    try {
      const tree = await fetchMallCategoryTree()
      categoryOptions.value = mapCategoryCascaderOptions(filterVisibleCategoryTree(tree || []), {
        boundCatIds: boundCatIds.value
      })
    } catch {
      categoryOptions.value = []
    }
  }

  const confirmAdd = async () => {
    const path = pickerPath.value
    if (!path?.length) {
      ElMessage.warning('请选择三级分类')
      return
    }
    const catId = Number(path[path.length - 1])
    if (!Number.isFinite(catId)) {
      ElMessage.warning('请选择有效的三级分类')
      return
    }
    const node = findCategoryNode(categoryOptions.value, catId)
    if (!node || Number(node.catLevel) !== 3) {
      ElMessage.warning('请选择三级分类，不能选择一级或二级')
      return
    }
    if (boundCatIds.value.includes(catId)) {
      ElMessage.warning('该分类已关联，请勿重复添加')
      return
    }
    adding.value = true
    try {
      await saveBindings([...boundCatIds.value, catId])
      pickerVisible.value = false
    } catch {
      // 拦截器提示
    } finally {
      adding.value = false
    }
  }

  const handleRemove = async (row: MallBrandCategoryRelation) => {
    const catId = row.catelogId
    if (catId == null) return
    try {
      await ElMessageBox.confirm(
        `确定移除关联分类「${row.catelogName || catId}」吗？`,
        '移除确认',
        { type: 'warning', confirmButtonText: '移除', cancelButtonText: '取消' }
      )
    } catch {
      return
    }
    removingId.value = catId
    try {
      const next = boundCatIds.value.filter((id) => id !== catId)
      await saveBindings(next)
    } catch {
      // 拦截器提示
    } finally {
      removingId.value = null
    }
  }

  watch(
    () => [props.visible, props.brandId],
    ([visible]) => {
      if (visible) {
        changed.value = false
        loadBoundList()
      }
    }
  )

  watch(dialogVisible, (visible) => {
    if (!visible && changed.value) {
      emit('submit')
    }
  })
</script>

<style scoped lang="scss">
  .bind-toolbar {
    margin-bottom: 12px;
  }

  .bind-tip {
    margin: 0 0 12px;
    font-size: 13px;
    color: var(--art-gray-600);
  }
</style>
