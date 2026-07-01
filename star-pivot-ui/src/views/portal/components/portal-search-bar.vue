<!-- C 端搜索框 -->
<template>
  <div class="portal-search-bar">
    <ElInput
      v-model="keyword"
      :placeholder="placeholder"
      size="large"
      clearable
      @keyup.enter="submit"
    >
      <template #prefix>
        <ArtSvgIcon icon="ri:search-line" class="portal-search-bar__icon" />
      </template>
      <template #append>
        <ElButton type="primary" class="portal-search-bar__btn" @click="submit">搜索</ElButton>
      </template>
    </ElInput>
  </div>
</template>

<script setup lang="ts">
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

defineOptions({ name: 'PortalSearchBar' })

  withDefaults(
    defineProps<{
      placeholder?: string
    }>(),
    {
      placeholder: '搜索商品...'
    }
  )

  const keyword = defineModel<string>({ default: '' })
  const emit = defineEmits<{
    search: [keyword: string]
  }>()

  function submit() {
    emit('search', keyword.value.trim())
  }
</script>

<style scoped lang="scss">
  .portal-search-bar {
    :deep(.el-input-group__append) {
      padding: 0;
      border: none;
      background: transparent;
      box-shadow: none;
    }

    :deep(.el-input__wrapper) {
      border-radius: var(--portal-radius-sm) 0 0 var(--portal-radius-sm);
      box-shadow: 0 0 0 1px var(--portal-border-strong) inset;
    }

    &__icon {
      font-size: 18px;
      color: var(--portal-text-muted);
    }

    &__btn {
      border-radius: 0 var(--portal-radius-sm) var(--portal-radius-sm) 0;
      background: var(--portal-primary-gradient);
      border: none;
      padding: 0 20px;
      height: 40px;
      font-weight: 600;

      &:hover {
        opacity: 0.92;
      }
    }
  }
</style>
