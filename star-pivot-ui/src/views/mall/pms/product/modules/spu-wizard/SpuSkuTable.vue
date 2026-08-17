<template>
  <div
    ref="scrollRef"
    class="sku-table-scroll"
    @mousedown="onMouseDown"
    @mouseleave="stopDrag"
    @mousemove="onMouseMove"
    @mouseup="stopDrag"
  >
    <ElTable
      :cell-class-name="() => 'sku-table__body-cell'"
      :data="skuRows"
      :header-cell-class-name="() => 'sku-table__header-cell'"
      border
      class="sku-table"
      stripe
    >
      <ElTableColumn align="center" fixed="left" label="属性组合">
        <ElTableColumn
          v-for="(col, colIdx) in saleTableColumns"
          :key="col.attrId"
          :label="col.attrName"
          align="center"
          min-width="112"
        >
          <template #default="{ row }">
            <span class="sku-attr-val">{{ row.attr[colIdx]?.attrValue }}</span>
          </template>
        </ElTableColumn>
      </ElTableColumn>
      <ElTableColumn fixed="left" label="SKU 名称" min-width="260">
        <template #default="{ row }">
          <ElInput v-model="row.skuName" class="sku-cell-input" clearable placeholder="SKU 名称" />
        </template>
      </ElTableColumn>
      <ElTableColumn align="center" label="价格" width="140">
        <template #default="{ row }">
          <ElInputNumber
            v-model="row.price"
            :min="0"
            :precision="2"
            class="sku-cell-num"
            controls-position="right"
          />
        </template>
      </ElTableColumn>
      <ElTableColumn v-if="!isEdit" align="center" label="初始库存" width="120">
        <template #default="{ row }">
          <ElInputNumber
            v-model="row.initialStock"
            :min="0"
            class="sku-cell-num"
            controls-position="right"
          />
        </template>
      </ElTableColumn>
      <ElTableColumn align="center" label="库存预警" width="120">
        <template #default="{ row }">
          <ElInputNumber
            v-model="row.stockWarning"
            :min="0"
            class="sku-cell-num"
            controls-position="right"
          />
        </template>
      </ElTableColumn>
      <ElTableColumn label="标题" min-width="240">
        <template #default="{ row }">
          <ElInput v-model="row.skuTitle" class="sku-cell-input" clearable placeholder="展示标题" />
        </template>
      </ElTableColumn>
      <ElTableColumn label="副标题" min-width="240">
        <template #default="{ row }">
          <ElInput
            v-model="row.skuSubtitle"
            class="sku-cell-input"
            clearable
            placeholder="副标题"
          />
        </template>
      </ElTableColumn>
      <ElTableColumn class-name="sku-table__promo-cell" label="满件折扣" width="232">
        <template #default="{ row }">
          <SpuSkuPromoDiscount v-model:discount="row.discount" v-model:full-count="row.fullCount" />
        </template>
      </ElTableColumn>
      <ElTableColumn class-name="sku-table__promo-cell" label="满减" width="232">
        <template #default="{ row }">
          <SpuSkuPromoReduction
            v-model:full-price="row.fullPrice"
            v-model:reduce-price="row.reducePrice"
          />
        </template>
      </ElTableColumn>
      <ElTableColumn v-if="spuImages.length" label="SKU 图" min-width="320">
        <template #default="{ row }">
          <SpuSkuImagePick
            :get-image-display-url="getImageDisplayUrl"
            :row="row"
            :spu-images="spuImages"
            @toggle="(idx, img, checked) => emit('toggleImage', row, idx, img, checked)"
            @set-default="(idx, img) => emit('setDefaultImage', row, idx, img)"
          />
        </template>
      </ElTableColumn>
    </ElTable>
  </div>
</template>

<script lang="ts" setup>
  import type { SpuWizardSaleDraft, SpuWizardSkuRow } from '@/utils/mall/spu-wizard-payload'
  import { useSkuTableDragScroll } from '../../composables/useSkuTableDragScroll'
  import SpuSkuImagePick from './SpuSkuImagePick.vue'
  import SpuSkuPromoDiscount from './SpuSkuPromoDiscount.vue'
  import SpuSkuPromoReduction from './SpuSkuPromoReduction.vue'

  defineProps<{
    isEdit: boolean
    skuRows: SpuWizardSkuRow[]
    saleTableColumns: SpuWizardSaleDraft[]
    spuImages: string[]
    getImageDisplayUrl: (objectName: string) => string
  }>()

  const emit = defineEmits<{
    toggleImage: [row: SpuWizardSkuRow, imgIdx: number, img: string, checked: boolean]
    setDefaultImage: [row: SpuWizardSkuRow, imgIdx: number, img: string]
  }>()

  const { scrollRef, onMouseDown, onMouseMove, stopDrag } = useSkuTableDragScroll()
</script>

<style lang="scss" scoped>
  .sku-table-scroll {
    width: 100%;
    overflow-x: auto;
    overflow-y: hidden;
    padding: 4px 2px 12px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
    background: var(--el-fill-color-blank);
    scrollbar-width: thin;
    scrollbar-color: var(--el-border-color) transparent;

    &.is-dragging {
      user-select: none;
    }

    &::-webkit-scrollbar {
      height: 8px;
    }

    &::-webkit-scrollbar-thumb {
      border-radius: 4px;
      background: var(--el-border-color-darker);
    }
  }

  .sku-table {
    width: max-content;
    min-width: 100%;

    :deep(.sku-table__header-cell) {
      background: var(--el-fill-color-light) !important;
      font-weight: 600;
    }

    :deep(.sku-table__body-cell) {
      padding: 12px 14px;
      vertical-align: middle;
    }

    :deep(.sku-table__promo-cell) {
      background: var(--el-fill-color-extra-light);

      .cell {
        overflow: visible;
      }
    }

    :deep(.el-input__wrapper) {
      width: 100%;
    }

    :deep(.sku-cell-num:not(.sku-cell-num--sm)) {
      width: 100%;
    }
  }

  .sku-attr-val {
    display: inline-block;
    padding: 4px 10px;
    font-size: 13px;
    background: var(--el-color-primary-light-9);
    border: 1px solid var(--el-color-primary-light-7);
    border-radius: 4px;
    white-space: nowrap;
  }

  .sku-cell-input {
    width: 100%;
    min-width: 200px;
  }

  .sku-cell-num {
    width: 100%;
  }
</style>
