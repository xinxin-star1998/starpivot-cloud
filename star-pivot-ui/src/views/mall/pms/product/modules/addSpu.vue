<!-- SPU 发布向导：新增 / 编辑（独立页） -->
<template>
  <div class="mall-add-spu-page art-full-height">
    <SpuWizardToolbar />
    <SpuBaseStep v-show="wizard.step === 0" />
    <SpuBaseAttrStep v-show="wizard.step === 1" />
    <SpuSaleAttrStep v-show="wizard.step === 2" />
    <SpuSkuStep v-show="wizard.step === 3" />
    <SpuDoneStep v-show="wizard.step === 4" />
  </div>
</template>

<script setup lang="ts">
  import { SPU_WIZARD_KEY, useSpuWizard } from '../composables/useSpuWizard'
  import SpuWizardToolbar from './spu-wizard/SpuWizardToolbar.vue'
  import SpuBaseStep from './spu-wizard/SpuBaseStep.vue'
  import SpuBaseAttrStep from './spu-wizard/SpuBaseAttrStep.vue'
  import SpuSaleAttrStep from './spu-wizard/SpuSaleAttrStep.vue'
  import SpuSkuStep from './spu-wizard/SpuSkuStep.vue'
  import SpuDoneStep from './spu-wizard/SpuDoneStep.vue'

  defineOptions({ name: 'MallProductAddSpu' })

  const wizard = useSpuWizard()
  provide(SPU_WIZARD_KEY, wizard)
</script>

<style scoped lang="scss">
  .mall-add-spu-page {
    display: flex;
    flex-direction: column;
    gap: 12px;
    padding: 12px;
    min-height: 0;
  }
</style>
