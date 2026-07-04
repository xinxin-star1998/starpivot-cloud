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
import {MEMBER_STATUS_MAP} from '@/api/mall/member'

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
      label: '用户名',
      key: 'username',
      type: 'input',
      placeholder: '模糊查询',
      clearable: true
    },
    {
      label: '手机号',
      key: 'mobile',
      type: 'input',
      placeholder: '模糊查询',
      clearable: true
    },
    {
      label: '状态',
      key: 'status',
      type: 'select',
      clearable: true,
      options: Object.entries(MEMBER_STATUS_MAP).map(([value, label]) => ({
        label,
        value: Number(value)
      }))
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
