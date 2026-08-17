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

<script lang="ts" setup>
  import { TEMPLATE_STATUS_OPTIONS } from '../../utils/approval-labels'

  interface Props {
    modelValue: Record<string, any>
  }
  interface Emits {
    (e: 'update:modelValue', value: Record<string, any>): void
    (e: 'search', params: Record<string, any>): void
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
      label: '模板编码',
      key: 'templateCode',
      type: 'input',
      placeholder: '模板编码',
      clearable: true
    },
    {
      label: '模板名称',
      key: 'templateName',
      type: 'input',
      placeholder: '模板名称',
      clearable: true
    },
    {
      label: '业务域',
      key: 'bizModule',
      type: 'input',
      placeholder: '如 mall',
      clearable: true
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      placeholder: '全部',
      clearable: true,
      options: TEMPLATE_STATUS_OPTIONS
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
