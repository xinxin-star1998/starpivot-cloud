<template>
  <MallImageUpload
    ref="uploadRef"
    :model-value="modelValue ?? ''"
    :max-size-mb="2"
    :single-size="80"
    picker-title="选择品牌 Logo"
    hint="JPG / PNG / GIF / WEBP，不超过 2MB"
    @update:model-value="emit('update:modelValue', $event as string)"
  />
</template>

<script lang="ts" setup>
import MallImageUpload from '@/components/mall/mall-image-upload/index.vue'

defineOptions({ name: 'BrandLogoUpload' })

  defineProps<{
    modelValue?: string
    /** 保留兼容 */
    brandId?: number
  }>()

  const emit = defineEmits<{
    (e: 'update:modelValue', value: string): void
  }>()

  const uploadRef = ref<InstanceType<typeof MallImageUpload>>()

  const resolveLogo = async (): Promise<string | undefined> => {
    return uploadRef.value?.resolveObjectName()
  }

  defineExpose({ resolveLogo })
</script>
