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
      label: '采购人',
      key: 'assigneeName',
      type: 'input',
      placeholder: '模糊查询',
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
        { label: '已领取', value: 2 },
        { label: '已完成', value: 3 },
        { label: '有异常', value: 4 }
      ]
    },
    {
      label: '仓库 ID',
      key: 'wareId',
      type: 'input',
      clearable: true
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
