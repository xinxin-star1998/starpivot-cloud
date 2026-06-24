<template>
  <MallImageUpload
    v-model="model"
    multiple
    :limit="limit"
    :max-size-mb="5"
    :hint="hint"
    picker-title="选择商品图片"
  />
</template>

<script setup lang="ts">
  import MallImageUpload from '@/components/mall/mall-image-upload/index.vue'

  defineOptions({ name: 'SpuImageUpload' })

  const props = withDefaults(
    defineProps<{
      modelValue: string[]
      goodsId?: number
      limit?: number
      hint?: string
    }>(),
    {
      limit: 10,
      hint: ''
    }
  )

  const emit = defineEmits<{
    (e: 'update:modelValue', value: string[]): void
  }>()

  /** goodsId 保留兼容，上传路径由文件中心统一生成 */
  void props.goodsId

  const model = computed({
    get: () => props.modelValue,
    set: (value) => emit('update:modelValue', value)
  })
</script>
