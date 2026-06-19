<template>
  <MallAttrListPage :attr-type="1" page-title="基本属性" />
</template>

<script setup lang="ts">
  import MallAttrListPage from '@views/mall/attr/components/mall-attr-list-page.vue'

  defineOptions({ name: 'MallBaseAttr' })
</script>
