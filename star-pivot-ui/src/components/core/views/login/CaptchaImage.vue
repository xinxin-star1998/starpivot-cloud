<template>
  <div
    class="captcha-image-wrapper"
    role="button"
    tabindex="0"
    title="点击刷新验证码"
    @click="emit('refresh')"
    @keydown.enter="emit('refresh')"
  >
    <Transition name="captcha-fade" mode="out-in">
      <img
        v-if="image"
        :key="image"
        :src="image"
        alt="图形验证码，点击刷新"
        class="captcha-image"
        :class="{ 'is-dimmed': loading }"
      />
      <div v-else key="placeholder" class="captcha-placeholder">
        <ArtSvgIcon icon="ri:refresh-line" class="text-lg" />
        <span>获取验证码</span>
      </div>
    </Transition>

    <div v-if="loading" class="captcha-loading">
      <div class="loading-spinner"></div>
    </div>

    <div v-if="image && !loading" class="captcha-refresh-overlay" aria-hidden="true">
      <ArtSvgIcon icon="ri:refresh-line" class="captcha-refresh-icon" />
      <span class="captcha-refresh-text">刷新</span>
    </div>
  </div>
</template>

<script setup lang="ts">
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

defineOptions({ name: 'CaptchaImage' })

  defineProps<{
    image: string
    loading?: boolean
  }>()

  const emit = defineEmits<{
    refresh: []
  }>()
</script>

<style scoped lang="scss">
  .captcha-image-wrapper {
    position: relative;
    flex-shrink: 0;
    width: 160px;
    height: 52px;
    overflow: hidden;
    cursor: pointer;
    background: #fff;
    border: 1px solid var(--art-gray-200);
    border-radius: var(--el-border-radius-base, 8px);
    box-shadow: 0 0 0 1px var(--art-gray-200) inset;
    transition: border-color 0.3s ease, box-shadow 0.3s ease;

    &:hover {
      border-color: var(--theme-color);
      box-shadow: 0 0 0 1px var(--theme-color) inset;
    }

    &:hover .captcha-refresh-overlay {
      opacity: 1;
    }
  }

  .dark .captcha-image-wrapper {
    background: rgb(26 26 30 / 80%);
    border-color: var(--art-gray-600);
    box-shadow: 0 0 0 1px var(--art-gray-600) inset;
  }

  .captcha-image {
    display: block;
    width: 100%;
    height: 100%;
    object-fit: contain;
    background: #fff;
    transition: opacity 0.3s ease;

    &.is-dimmed {
      opacity: 0.5;
    }
  }

  .captcha-placeholder {
    display: flex;
    gap: 8px;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    font-size: 14px;
    color: var(--art-gray-400);
    transition: all 0.3s ease;
  }

  .captcha-loading {
    position: absolute;
    inset: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    backdrop-filter: blur(2px);
    background: rgb(255 255 255 / 90%);
  }

  .dark .captcha-loading {
    background: rgb(17 24 39 / 90%);
  }

  .loading-spinner {
    width: 24px;
    height: 24px;
    border: 3px solid var(--art-gray-200);
    border-top-color: var(--theme-color);
    border-radius: 50%;
    animation: captcha-spin 0.8s linear infinite;
  }

  .dark .loading-spinner {
    border-color: var(--art-gray-600);
    border-top-color: var(--theme-color);
  }

  .captcha-refresh-overlay {
    position: absolute;
    inset: 0;
    display: flex;
    flex-direction: column;
    gap: 2px;
    align-items: center;
    justify-content: center;
    pointer-events: none;
    background: rgb(255 255 255 / 72%);
    opacity: 0;
    transition: opacity 0.2s ease;
  }

  .dark .captcha-refresh-overlay {
    background: rgb(17 24 39 / 72%);
  }

  .captcha-refresh-icon {
    font-size: 18px;
    color: var(--theme-color);
  }

  .captcha-refresh-text {
    font-size: 12px;
    line-height: 1;
    color: var(--art-gray-500);
  }

  .dark .captcha-refresh-text {
    color: var(--art-gray-400);
  }

  .captcha-fade-enter-active,
  .captcha-fade-leave-active {
    transition: opacity 0.2s ease;
  }

  .captcha-fade-enter-from,
  .captcha-fade-leave-to {
    opacity: 0;
  }

  @keyframes captcha-spin {
    to {
      transform: rotate(360deg);
    }
  }
</style>
