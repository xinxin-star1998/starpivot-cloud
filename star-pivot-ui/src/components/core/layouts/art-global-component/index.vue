<!-- 全局组件 -->
<template>
  <component
    v-for="componentConfig in enabledComponents"
    :key="componentConfig.key"
    :is="componentConfig.component"
  />
</template>

<script setup lang="ts">
  import { getEnabledGlobalComponents } from '@/config/modules/component'
  import { useMenuStore } from '@/store/modules/menu'

  defineOptions({ name: 'ArtGlobalComponent' })

  const menuStore = useMenuStore()

  const enabledComponents = computed(() =>
    getEnabledGlobalComponents().filter((config) => {
      if (config.key === 'chat-window') {
        return menuStore.hasPerm('ai:chat:use')
      }
      return true
    })
  )
</script>
