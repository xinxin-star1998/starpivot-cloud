<script setup lang="ts">
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  defineProps<{
    icon: string
    title: string
    description: string
  }>()
</script>

<template>
  <div class="props-empty-state">
    <div class="props-empty-state__icon-wrap">
      <ArtSvgIcon :icon="icon" class="props-empty-state__icon" />
    </div>
    <p class="props-empty-state__title">{{ title }}</p>
    <span class="props-empty-state__desc">{{ description }}</span>
  </div>
</template>

<style scoped lang="scss">
  .props-empty-state {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    height: 100%;
    min-height: 240px;
    padding: 32px 24px;
    text-align: center;
  }

  .props-empty-state__icon-wrap {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 72px;
    height: 72px;
    margin-bottom: 16px;
    border-radius: 20px;
    background: linear-gradient(
      135deg,
      var(--el-color-primary-light-9) 0%,
      var(--el-fill-color-light) 100%
    );
    border: 1px solid var(--el-border-color-lighter);
    box-shadow: 0 8px 24px rgb(64 158 255 / 8%);
  }

  .props-empty-state__icon {
    font-size: 32px;
    color: var(--el-color-primary-light-3);
  }

  .props-empty-state__title {
    margin: 0 0 6px;
    font-size: 15px;
    font-weight: 600;
    color: var(--el-text-color-primary);
    letter-spacing: 0.01em;
  }

  .props-empty-state__desc {
    font-size: 13px;
    line-height: 1.5;
    color: var(--el-text-color-secondary);
    max-width: 200px;
  }
</style>
