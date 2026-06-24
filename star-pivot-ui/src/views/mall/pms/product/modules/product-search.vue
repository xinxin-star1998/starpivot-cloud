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

  const publishStatusOptions = [
    { label: '上架', value: 1 },
    { label: '下架', value: 0 }
  ]

  const formItems = computed(() => [
    {
      label: 'SPU名称',
      key: 'spuName',
      type: 'input',
      placeholder: '模糊查询',
      clearable: true
    },
    {
      label: '上架状态',
      key: 'publishStatus',
      type: 'select',
      props: {
        placeholder: '请选择',
        options: publishStatusOptions,
        clearable: true
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
