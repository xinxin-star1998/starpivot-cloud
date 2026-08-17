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
  import { COMMENT_SHOW_STATUS_MAP } from '@/api/mall/comment'

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
      label: 'SPU ID',
      key: 'spuId',
      type: 'input',
      placeholder: '精确查询',
      clearable: true
    },
    {
      label: '会员昵称',
      key: 'memberNickName',
      type: 'input',
      placeholder: '模糊查询',
      clearable: true
    },
    {
      label: '展示状态',
      key: 'showStatus',
      type: 'select',
      clearable: true,
      options: Object.entries(COMMENT_SHOW_STATUS_MAP).map(([value, label]) => ({
        label,
        value: Number(value)
      }))
    },
    {
      label: '评分',
      key: 'star',
      type: 'select',
      clearable: true,
      options: [1, 2, 3, 4, 5].map((n) => ({ label: `${n} 星`, value: n }))
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
