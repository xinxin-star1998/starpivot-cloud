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
    activeTab: 'spu' | 'subject'
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

  const formItems = computed(() => {
    const items = [
      {
        label: '会员 ID',
        key: 'memberId',
        type: 'input',
        placeholder: '精确查询',
        clearable: true
      }
    ]
    if (props.activeTab === 'spu') {
      items.push({
        label: '商品名称',
        key: 'spuName',
        type: 'input',
        placeholder: '模糊匹配',
        clearable: true
      })
    } else {
      items.push({
        label: '专题名称',
        key: 'subjectName',
        type: 'input',
        placeholder: '模糊匹配',
        clearable: true
      })
    }
    return items
  })

  function handleReset() {
    emit('reset')
  }

  async function handleSearch() {
    await searchBarRef.value.validate()
    emit('search', formData.value)
  }
</script>
