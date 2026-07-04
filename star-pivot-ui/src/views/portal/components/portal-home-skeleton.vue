<!-- 首页骨架屏 -->
<template>
  <div class="portal-home-skeleton">
    <section class="portal-home-skeleton__hero">
      <ElSkeleton animated class="portal-home-skeleton__nav">
        <template #template>
          <ElSkeletonItem variant="rect" style="width: 100%; height: 100%; border-radius: 16px 0 0 16px" />
        </template>
      </ElSkeleton>
      <ElSkeleton animated class="portal-home-skeleton__banner">
        <template #template>
          <ElSkeletonItem variant="image" style="width: 100%; height: 100%" />
        </template>
      </ElSkeleton>
      <ElSkeleton animated class="portal-home-skeleton__user">
        <template #template>
          <div style="display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 16px">
            <ElSkeletonItem variant="circle" style="width: 56px; height: 56px" />
            <ElSkeletonItem variant="text" style="width: 80%" />
            <ElSkeletonItem variant="button" style="width: 100%; height: 36px" />
            <ElSkeletonItem variant="text" style="width: 100%" />
          </div>
        </template>
      </ElSkeleton>
    </section>

    <section class="portal-home-skeleton__grid">
      <ElSkeleton v-for="i in 4" :key="i" animated class="portal-home-skeleton__block">
        <template #template>
          <ElSkeletonItem variant="h3" style="width: 40%; margin-bottom: 12px" />
          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px">
            <ElSkeletonItem variant="image" style="width: 100%; height: 80px" />
            <ElSkeletonItem variant="image" style="width: 100%; height: 80px" />
          </div>
        </template>
      </ElSkeleton>
    </section>

    <section class="portal-home-skeleton__products">
      <ElSkeleton animated>
        <template #template>
          <ElSkeletonItem variant="h3" style="width: 120px; margin-bottom: 20px" />
          <div class="portal-home-skeleton__product-grid">
            <div v-for="j in 8" :key="j" class="portal-home-skeleton__product-card">
              <ElSkeletonItem variant="image" style="width: 100%; aspect-ratio: 1" />
              <ElSkeletonItem variant="text" style="margin-top: 12px" />
              <ElSkeletonItem variant="text" style="width: 60%; margin-top: 8px" />
            </div>
          </div>
        </template>
      </ElSkeleton>
    </section>
  </div>
</template>

<script setup lang="ts">
defineOptions({ name: 'PortalHomeSkeleton' })
</script>

<style scoped lang="scss">
  .portal-home-skeleton {
    &__hero {
      display: flex;
      min-height: 480px;
      margin-bottom: 20px;
      border-radius: var(--portal-radius-lg);
      overflow: hidden;
      background: var(--portal-bg-elevated);
      box-shadow: var(--portal-shadow);
    }

    &__nav {
      width: 200px;
      flex-shrink: 0;
    }

    &__banner {
      flex: 1;
      min-width: 0;
      height: 480px;
    }

    &__user {
      width: 200px;
      flex-shrink: 0;
      border-left: 1px solid var(--portal-border);
    }

    &__grid {
      display: grid;
      grid-template-columns: repeat(4, minmax(0, 1fr));
      gap: 16px;
      margin-bottom: 20px;
    }

    &__block {
      padding: 16px;
      background: var(--portal-bg-elevated);
      border-radius: var(--portal-radius);
      border: 1px solid var(--portal-border);
    }

    &__products {
      padding: 24px;
      background: var(--portal-bg-elevated);
      border-radius: var(--portal-radius-lg);
      box-shadow: var(--portal-shadow-sm);
    }

    &__product-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
      gap: 20px;
    }

    &__product-card {
      border: 1px solid var(--portal-border);
      border-radius: var(--portal-radius);
      padding: 0 0 14px;
      overflow: hidden;
    }

    @media (width <= 900px) {
      &__hero {
        flex-direction: column;
        min-height: 0;
      }

      &__nav {
        display: none;
      }

      &__banner {
        order: 1;
        height: 220px;
      }

      &__user {
        order: 2;
        width: 100%;
        border-left: none;
        border-top: 1px solid var(--portal-border);
      }

      &__grid {
        grid-template-columns: repeat(2, minmax(0, 1fr));
      }
    }

    @media (width <= 640px) {
      &__grid {
        grid-template-columns: 1fr;
      }
    }
  }
</style>
