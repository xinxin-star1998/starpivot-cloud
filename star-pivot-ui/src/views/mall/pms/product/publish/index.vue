<!-- 发布商品：跳转至 SPU 发布向导 -->
<script setup lang="ts">
import {onMounted} from 'vue'
import {useRouter} from 'vue-router'

defineOptions({ name: 'PublishSPU' })

  const router = useRouter()

  onMounted(() => {
    router.replace({ path: '/mall/product/add' })
  })
</script>

<template>
  <div v-loading="true" class="publish-redirect" element-loading-text="正在打开发布向导..." />
</template>

<style scoped>
  .publish-redirect {
    min-height: 200px;
  }
</style>
