<template>
  <ArtSearchBar
    ref="searchBarRef"
    v-model="formData"
    :items="formItems"
    :rules="rules"
    @reset="handleReset"
    @search="handleSearch"
  >
  </ArtSearchBar>
</template>

<script setup lang="ts">
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

  // 表单数据双向绑定
  const searchBarRef = ref()
  const formData = computed({
    get: () => props.modelValue,
    set: (val) => emit('update:modelValue', val)
  })

  // 校验规则
  const rules = {}

  // 表单配置
  const formItems = computed(() => [
    {
      label: 'sku_id',
      key: 'skuId',
      type: 'input',
      placeholder: '请输入sku_id',
      clearable: true
    },
    {
      label: '仓库id',
      key: 'wareId',
      type: 'input',
      placeholder: '请输入仓库id',
      clearable: true
    },
    {
      label: '库存数',
      key: 'stock',
      type: 'input',
      placeholder: '请输入库存数',
      clearable: true
    },
    {
      label: 'sku_name',
      key: 'skuName',
      type: 'input',
      placeholder: '请输入sku_name',
      clearable: true
    },
    {
      label: '锁定库存',
      key: 'stockLocked',
      type: 'input',
      placeholder: '请输入锁定库存',
      clearable: true
    }
  ])

  // 事件
  function handleReset() {
    emit('reset')
  }

  async function handleSearch() {
    await searchBarRef.value.validate()
    emit('search', formData.value)
  }
</script>
