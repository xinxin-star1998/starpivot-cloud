<!-- 商城-SKU 查询（菜单入口；与商品管理共用 MallSkuPage 只读模式） -->
<script setup lang="ts">
import MallSkuPage from '@/views/mall/pms/sku/MallSkuPage.vue'

defineOptions({ name: 'MallSku' })
</script>

<template>
  <MallSkuPage mode="goods-manager" />
</template>
