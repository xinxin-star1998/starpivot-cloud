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
  import { ADDRESS_LEVEL_OPTIONS } from '@/utils/mall/address-level'

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

  const levelOptions = ADDRESS_LEVEL_OPTIONS.map((o) => ({
    label: o.label,
    value: o.value
  }))

  const formItems = computed(() => [
    {
      label: '地区编码',
      key: 'code',
      type: 'input',
      placeholder: '精确匹配',
      clearable: true
    },
    {
      label: '父级编码',
      key: 'parentCode',
      type: 'input',
      placeholder: '精确匹配',
      clearable: true
    },
    {
      label: '地区名称',
      key: 'name',
      type: 'input',
      placeholder: '模糊查询',
      clearable: true
    },
    {
      label: '层级',
      key: 'level',
      type: 'select',
      props: {
        placeholder: '请选择',
        options: levelOptions,
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
