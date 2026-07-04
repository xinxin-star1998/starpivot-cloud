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
import {OMS_ORDER_STATUS_MAP} from '@/api/mall/order'

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
      label: '会员用户名',
      key: 'memberUsername',
      type: 'input',
      placeholder: '模糊查询',
      clearable: true
    },
    {
      label: '订单状态',
      key: 'status',
      type: 'select',
      clearable: true,
      options: Object.entries(OMS_ORDER_STATUS_MAP).map(([value, label]) => ({
        label,
        value: Number(value)
      }))
    },
    {
      label: '开始时间',
      key: 'startTime',
      type: 'datetime',
      clearable: true
    },
    {
      label: '结束时间',
      key: 'endTime',
      type: 'datetime',
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
