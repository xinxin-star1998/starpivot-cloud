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
      label: '公告标题',
      key: 'noticeTitle',
      type: 'input',
      placeholder: '请输入公告标题',
      clearable: true
    },
    {
      label: '公告类型',
      key: 'noticeType',
      type: 'select',
      props: {
        placeholder: '请选择公告类型',
        clearable: true
        // 字典类型：sys_notice_type
        // options: 需要从字典服务获取
      }
    },
    {
      label: '公告内容',
      key: 'noticeContent'
    },
    {
      label: '公告状态',
      key: 'status',
      type: 'select',
      props: {
        placeholder: '请选择公告状态',
        clearable: true
        // 字典类型：sys_notice_status
        // options: 需要从字典服务获取
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
