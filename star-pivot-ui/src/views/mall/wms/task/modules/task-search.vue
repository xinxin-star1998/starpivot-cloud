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
      label: '订单号',
      key: 'orderSn',
      type: 'input',
      placeholder: '模糊查询',
      clearable: true
    },
    {
      label: '任务状态',
      key: 'taskStatus',
      type: 'select',
      clearable: true,
      options: [
        { label: '待处理', value: 0 },
        { label: '处理中', value: 1 },
        { label: '已完成', value: 2 },
        { label: '无效', value: 3 }
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
