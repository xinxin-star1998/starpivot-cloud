<!-- C 端商城布局 -->
<template>
  <div class="portal-layout">
    <PortalHeader />
    <main class="portal-main">
      <RouterView />
    </main>
    <footer class="portal-footer">StarPivot 商城 · C 端演示</footer>
  </div>
</template>

<script setup lang="ts">
  import PortalHeader from './portal-header.vue'

  defineOptions({ name: 'PortalLayout' })
</script>

<style scoped lang="scss">
  .portal-layout {
    min-height: 100vh;
    display: flex;
    flex-direction: column;
    background: #f5f5f5;
  }

  .portal-main {
    flex: 1;
    width: 100%;
    max-width: 1200px;
    margin: 0 auto;
    padding: 16px;
    box-sizing: border-box;
  }

  .portal-footer {
    text-align: center;
    padding: 24px;
    color: #999;
    font-size: 13px;
    background: #fff;
    border-top: 1px solid #eee;
  }
</style>
