<!-- 布局容器 -->
<template>
  <div class="app-layout">
    <aside id="app-sidebar">
      <ArtSidebarMenu />
    </aside>

    <main id="app-main">
      <div id="app-header">
        <ArtHeaderBar />
      </div>
      <div id="app-content">
        <ArtPageContent />
      </div>
    </main>

    <div id="app-global">
      <ArtGlobalComponent />
    </div>
  </div>
</template>

<script setup lang="ts">
  const ArtSidebarMenu = defineAsyncComponent(
    () => import('@/components/core/layouts/art-menus/art-sidebar-menu/index.vue')
  )
  const ArtHeaderBar = defineAsyncComponent(
    () => import('@/components/core/layouts/art-header-bar/index.vue')
  )
  const ArtPageContent = defineAsyncComponent(
    () => import('@/components/core/layouts/art-page-content/index.vue')
  )
  const ArtGlobalComponent = defineAsyncComponent(
    () => import('@/components/core/layouts/art-global-component/index.vue')
  )

  defineOptions({ name: 'AppLayout', inheritAttrs: false })
</script>

<style lang="scss" scoped>
  @use './style';
</style>
