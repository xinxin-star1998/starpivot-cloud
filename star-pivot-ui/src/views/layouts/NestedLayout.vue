<!-- 嵌套布局容器（用于多级路由） -->
<!-- 只包含 RouterView，不包含侧边栏、头部等完整布局 -->
<template>
  <div class="nested-layout">
    <RouterView />
  </div>
</template>

<script setup lang="ts">
  defineOptions({ name: 'NestedLayout', inheritAttrs: false })
</script>

<style lang="scss" scoped>
  .nested-layout {
    width: 100%;
    height: 100%;
  }
</style>
