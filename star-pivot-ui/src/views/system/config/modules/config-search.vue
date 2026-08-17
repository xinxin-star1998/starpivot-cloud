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

<script lang="ts" setup>
  import ArtSearchBar from '@/components/core/forms/art-search-bar/index.vue'

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
  const configTypeOptions = ref([
    { label: '是', value: 'Y' },
    { label: '否', value: 'N' }
  ])

  const formItems = computed(() => [
    {
      label: '参数名称',
      key: 'configName',
      type: 'input',
      placeholder: '请输入参数名称',
      clearable: true
    },
    {
      label: '参数键名',
      key: 'configKey',
      type: 'input',
      placeholder: '请输入参数键名',
      clearable: true
    },
    {
      label: '参数键值',
      key: 'configValue',
      type: 'input',
      placeholder: '请输入参数键值',
      clearable: true
    },
    {
      label: '系统内置',
      key: 'configType',
      type: 'select',
      props: {
        placeholder: '请选择系统内置',
        options: configTypeOptions.value,
        clearable: true
      }
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
