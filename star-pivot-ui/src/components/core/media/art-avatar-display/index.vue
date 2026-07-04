<template>
  <img
    v-if="displayUrl"
    :src="displayUrl"
    :class="avatarClass"
    :style="{ width: size + 'px', height: size + 'px' }"
    class="rounded-full object-cover"
    alt="头像"
  />
</template>

<script setup lang="ts">
import {fetchGetAvatarPresignedUrl} from '@/api/user/user'
import {extractOssObjectPath, needsOssPresignedDisplay} from '@/utils/storage/oss-object-path'

const props = withDefaults(
    defineProps<{
      /** 头像地址（OSS 私有桶永久地址时，组件内部会请求临时 URL 用于展示） */
      avatarUrl?: string
      /** 尺寸（像素） */
      size?: number
      /** 额外 class */
      avatarClass?: string
    }>(),
    {
      avatarUrl: '',
      size: 40,
      avatarClass: ''
    }
  )

  const displayUrl = ref('')

  async function resolveDisplayUrl(url: string) {
    if (!url) {
      displayUrl.value = ''
      return
    }
    const filePath = extractOssObjectPath(url)
    if (needsOssPresignedDisplay(url) && filePath) {
      try {
        const response = (await fetchGetAvatarPresignedUrl(filePath)) as any
        const presigned = response?.presignedUrl ?? response?.data?.presignedUrl
        displayUrl.value = presigned || ''
      } catch (e) {
        if (import.meta.env.DEV) console.error('获取头像临时链接失败：', e)
        displayUrl.value = ''
      }
    } else {
      displayUrl.value = url
    }
  }

  watch(
    () => props.avatarUrl,
    (val) => resolveDisplayUrl(val ?? ''),
    { immediate: true }
  )
</script>
