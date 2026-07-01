<!-- 商品分享菜单 -->
<template>
  <ElPopover trigger="click" placement="bottom-end" :width="220" popper-class="portal-share-popover">
    <template #reference>
      <button type="button" class="portal-share-trigger">
        <ArtSvgIcon icon="ri:share-forward-line" />
        分享
      </button>
    </template>
    <div class="portal-share-menu">
      <button type="button" class="portal-share-menu__item" @click="copyLink">
        <ArtSvgIcon icon="ri:link" />
        复制链接
      </button>
      <button type="button" class="portal-share-menu__item" @click="copyPromo">
        <ArtSvgIcon icon="ri:file-copy-line" />
        复制文案
      </button>
      <button v-if="canNativeShare" type="button" class="portal-share-menu__item" @click="nativeShare">
        <ArtSvgIcon icon="ri:share-line" />
        系统分享
      </button>
    </div>
  </ElPopover>
</template>

<script setup lang="ts">
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import {ElMessage} from 'element-plus'

defineOptions({ name: 'PortalSharePopover' })

  const props = withDefaults(
    defineProps<{
      title?: string
      promoText?: string
      url?: string
    }>(),
    {
      title: 'StarPivot 商品',
      promoText: '',
      url: ''
    }
  )

  const canNativeShare = computed(() => typeof navigator !== 'undefined' && !!navigator.share)

  const shareUrl = computed(() => props.url || (typeof window !== 'undefined' ? window.location.href : ''))

  const sharePromoText = computed(() => {
    if (props.promoText) return props.promoText
    return `${props.title}\n${shareUrl.value}`
  })

  async function copyText(text: string, successMsg: string) {
    try {
      await navigator.clipboard.writeText(text)
      ElMessage.success(successMsg)
    } catch {
      ElMessage.info(text)
    }
  }

  function copyLink() {
    copyText(shareUrl.value, '链接已复制')
  }

  function copyPromo() {
    copyText(sharePromoText.value, '文案已复制，快去分享吧')
  }

  async function nativeShare() {
    try {
      await navigator.share({
        title: props.title,
        text: props.promoText || props.title,
        url: shareUrl.value
      })
    } catch (error) {
      if (error instanceof DOMException && error.name === 'AbortError') return
      copyLink()
    }
  }
</script>

<style scoped lang="scss">
  .portal-share-trigger {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 6px 10px;
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius-sm);
    background: var(--portal-bg-elevated);
    color: var(--portal-text-secondary);
    font-size: 13px;
    cursor: pointer;
    flex-shrink: 0;
    transition: all var(--portal-transition);

    &:hover {
      color: var(--portal-primary);
      border-color: var(--portal-primary);
    }
  }

  .portal-share-menu {
    display: flex;
    flex-direction: column;
    gap: 4px;

    &__item {
      display: flex;
      align-items: center;
      gap: 8px;
      width: 100%;
      padding: 10px 12px;
      border: none;
      border-radius: var(--portal-radius-sm);
      background: transparent;
      color: var(--portal-text);
      font-size: 14px;
      cursor: pointer;
      text-align: left;
      transition: background var(--portal-transition);

      &:hover {
        background: var(--portal-primary-light);
        color: var(--portal-primary);
      }
    }
  }
</style>
