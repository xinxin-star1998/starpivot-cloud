<!-- 系统 Logo：星枢枢纽星标 -->
<template>
  <div class="art-logo flex-cc" :style="wrapStyle">
    <img :src="logoSrc" alt="星枢" class="art-logo__img" :style="logoStyle" />
  </div>
</template>

<script setup lang="ts">
import logoSrc from '@imgs/svg/star-pivot-logo.svg'

defineOptions({ name: 'ArtLogo' })
  interface Props {
    /** logo 大小（px） */
    size?: number | string
  }

  const props = withDefaults(defineProps<Props>(), {
    size: 36
  })

  const sizePx = computed(() => `${props.size}px`)

  const wrapStyle = computed(() => ({
    width: sizePx.value,
    height: sizePx.value,
    flexShrink: 0
  }))

  const logoStyle = computed(() => ({
    width: sizePx.value,
    height: sizePx.value
  }))
</script>

<style scoped lang="scss">
  .art-logo__img {
    display: block;
    object-fit: contain;
  }
</style>
