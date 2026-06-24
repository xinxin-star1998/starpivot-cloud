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
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  const rules = {}

  const formItems = computed(() => [
    {
      label: 'SKU ID',
      key: 'skuId',
      type: 'input',
      placeholder: '精确匹配',
      clearable: true
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      clearable: true,
      options: [
        { label: '新建', value: 0 },
        { label: '已分配', value: 1 },
        { label: '采购中', value: 2 },
        { label: '完成', value: 3 },
        { label: '失败', value: 4 }
      ]
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
