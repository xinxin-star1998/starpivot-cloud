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
    label: '会员 ID',
    key: 'memberId',
    type: 'input',
    placeholder: '精确查询',
    clearable: true
  },
  {
    label: '收货人',
    key: 'name',
    type: 'input',
    placeholder: '模糊匹配',
    clearable: true
  },
  {
    label: '手机号',
    key: 'phone',
    type: 'input',
    placeholder: '模糊匹配',
    clearable: true
  },
  {
    label: '省份',
    key: 'province',
    type: 'input',
    placeholder: '模糊匹配',
    clearable: true
  },
  {
    label: '默认地址',
    key: 'defaultStatus',
    type: 'select',
    clearable: true,
    options: [
      { label: '默认', value: 1 },
      { label: '非默认', value: 0 }
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
