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
import {MEMBER_LOGIN_TYPE_MAP} from '@/api/mall/member-login-log'

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
      label: '登录方式',
      key: 'loginType',
      type: 'select',
      clearable: true,
      options: Object.entries(MEMBER_LOGIN_TYPE_MAP).map(([value, label]) => ({
        label,
        value: Number(value)
      }))
    },
    {
      label: 'IP',
      key: 'ip',
      type: 'input',
      placeholder: '模糊匹配',
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
