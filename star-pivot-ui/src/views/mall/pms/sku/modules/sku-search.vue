<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :rules="rules"
    @reset="handleReset"
    @search="handleSearch"
  />
</template>

<script setup lang="ts">
  import { fetchCategoryNameMap } from '@/utils/mall/category-tree'
  import { fetchBrandNameMap } from '@/utils/mall/brand-map'

  interface Props {
    modelValue: Record<string, unknown>
  }
  interface Emits {
    (e: 'update:modelValue', value: Record<string, unknown>): void
    (e: 'search', params: Record<string, unknown>): void
    (e: 'reset'): void
  }
  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const searchBarRef = ref()
  const categoryOptions = ref<{ label: string; value: number }[]>([])
  const brandOptions = ref<{ label: string; value: number }[]>([])

  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  const rules = {}

  onMounted(async () => {
    const [catMap, brandMap] = await Promise.all([fetchCategoryNameMap(), fetchBrandNameMap()])
    categoryOptions.value = Object.entries(catMap)
      .map(([id, name]) => ({ label: name, value: Number(id) }))
      .filter((item) => Number.isFinite(item.value))
      .sort((a, b) => a.label.localeCompare(b.label, 'zh-CN'))
    brandOptions.value = Object.entries(brandMap)
      .map(([id, name]) => ({ label: name, value: Number(id) }))
      .filter((item) => Number.isFinite(item.value))
      .sort((a, b) => a.label.localeCompare(b.label, 'zh-CN'))
  })

  const formItems = computed(() => [
    {
      label: 'SKU名称',
      key: 'skuName',
      type: 'input',
      placeholder: '模糊查询',
      clearable: true
    },
    {
      label: 'SPU名称',
      key: 'spuName',
      type: 'input',
      placeholder: '模糊查询',
      clearable: true
    },
    {
      label: 'SPU ID',
      key: 'spuId',
      type: 'input',
      props: {
        placeholder: '精确查询',
        clearable: true
      }
    },
    {
      label: '分类',
      key: 'catalogId',
      type: 'select',
      props: {
        placeholder: '全部',
        clearable: true,
        filterable: true,
        options: categoryOptions.value
      }
    },
    {
      label: '品牌',
      key: 'brandId',
      type: 'select',
      props: {
        placeholder: '全部',
        clearable: true,
        filterable: true,
        options: brandOptions.value
      }
    },
    {
      label: '最低价',
      key: 'minPrice',
      type: 'input',
      props: {
        placeholder: '元',
        clearable: true
      }
    },
    {
      label: '最高价',
      key: 'maxPrice',
      type: 'input',
      props: {
        placeholder: '元',
        clearable: true
      }
    },
    {
      label: '上架状态',
      key: 'spuPublishStatus',
      type: 'select',
      props: {
        placeholder: '全部',
        clearable: true,
        options: [
          { label: '已上架', value: 1 },
          { label: '已下架', value: 0 }
        ]
      }
    }
  ])

  function handleReset() {
    emit('reset')
  }

  async function handleSearch() {
    await searchBarRef.value.validate()
    emit('search', formData.value)
  }
</script>
