<template>
  <div class="portal-page-header">
    <div class="portal-page-header__main">
      <h1 class="portal-page-header__title">{{ title }}</h1>
      <p v-if="subtitle" class="portal-page-header__subtitle">{{ subtitle }}</p>
    </div>
    <div v-if="$slots.extra" class="portal-page-header__extra">
      <slot name="extra" />
    </div>
  </div>
</template>

<script setup lang="ts">
  defineOptions({ name: 'PortalPageHeader' })

  defineProps<{
    title: string
    subtitle?: string
  }>()
</script>

<style scoped lang="scss">
  .portal-page-header {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--portal-border);

    &__title {
      margin: 0;
      font-size: 24px;
      font-weight: 700;
      color: var(--portal-text);
      letter-spacing: -0.02em;
    }

    &__subtitle {
      margin: 6px 0 0;
      font-size: 14px;
      color: var(--portal-text-secondary);
    }

    &__extra {
      flex-shrink: 0;
    }
  }
</style>
