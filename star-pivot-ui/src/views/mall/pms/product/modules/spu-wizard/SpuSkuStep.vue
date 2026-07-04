<template>
  <ElCard class="wizard-panel wizard-panel--sku" shadow="never">
    <SpuSkuWareBar
      :default-ware-id="wizard.baseForm.defaultWareId"
      :is-edit="wizard.isEdit"
      :ware-options="wizard.wareOptions"
      @update:default-ware-id="wizard.baseForm.defaultWareId = $event"
    />
    <SpuSkuTable
      :get-image-display-url="wizard.getImageDisplayUrl"
      :is-edit="wizard.isEdit"
      :sale-table-columns="wizard.saleTableColumns"
      :sku-rows="wizard.skuRows"
      :spu-images="wizard.baseForm.images"
      @toggle-image="wizard.toggleSkuImage"
      @set-default-image="wizard.setDefaultSkuImage"
    />
    <div class="wizard-actions">
      <ElButton @click="wizard.step = 2">上一步</ElButton>
      <ElButton :loading="wizard.submitting" type="primary" @click="wizard.submitSpu">
        保存商品
      </ElButton>
    </div>
  </ElCard>
</template>

<script lang="ts" setup>
import {useSpuWizardInject} from '../../composables/useSpuWizard'
import SpuSkuWareBar from './SpuSkuWareBar.vue'
import SpuSkuTable from './SpuSkuTable.vue'

const wizard = useSpuWizardInject()
</script>

<style lang="scss" scoped>
  .wizard-panel {
    flex: 1;
    min-height: 0;
    overflow: auto;
  }

  .wizard-actions {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-top: 24px;
  }

  .wizard-panel--sku {
    :deep(.el-card__body) {
      padding-top: 8px;
    }
  }
</style>
