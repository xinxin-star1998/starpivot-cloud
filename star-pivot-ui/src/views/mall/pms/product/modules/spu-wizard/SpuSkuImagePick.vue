<template>
  <div class="sku-image-pick">
    <div v-for="(img, imgIdx) in spuImages" :key="imgIdx" class="sku-image-pick__item">
      <img :src="getImageDisplayUrl(img)" alt="" />
      <div class="sku-image-pick__actions">
        <ElCheckbox
          :model-value="row.images[imgIdx]?.imgUrl === img"
          @change="(val) => emit('toggle', imgIdx, img, val === true)"
        >
          选用
        </ElCheckbox>
        <ElRadio
          :model-value="row.images[imgIdx]?.defaultImg === 1"
          :name="`default-${row.descarKey}`"
          @change="emit('setDefault', imgIdx, img)"
        >
          默认
        </ElRadio>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import type { SpuWizardSkuRow } from '@/utils/mall/spu-wizard-payload'

  defineProps<{
    row: SpuWizardSkuRow
    spuImages: string[]
    getImageDisplayUrl: (objectName: string) => string
  }>()

  const emit = defineEmits<{
    toggle: [imgIdx: number, img: string, checked: boolean]
    setDefault: [imgIdx: number, img: string]
  }>()
</script>

<style lang="scss" scoped>
  .sku-image-pick {
    display: flex;
    flex-wrap: nowrap;
    gap: 10px;
  }

  .sku-image-pick__item {
    flex-shrink: 0;
    width: 96px;
    padding: 6px;
    background: var(--el-fill-color-light);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 6px;
    text-align: center;

    img {
      width: 100%;
      height: 72px;
      object-fit: cover;
      border-radius: 4px;
    }
  }

  .sku-image-pick__actions {
    display: flex;
    flex-direction: column;
    align-items: flex-start;
    gap: 2px;
    margin-top: 6px;

    :deep(.el-checkbox),
    :deep(.el-radio) {
      height: auto;
      margin-right: 0;
    }

    :deep(.el-checkbox__label),
    :deep(.el-radio__label) {
      font-size: 12px;
      padding-left: 6px;
    }
  }
</style>
