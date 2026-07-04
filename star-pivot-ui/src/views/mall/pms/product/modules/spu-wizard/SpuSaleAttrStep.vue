<template>
  <ElCard v-loading="wizard.loadingSale" class="wizard-panel" shadow="never">
    <template v-if="wizard.saleAttrs.length === 0 && !wizard.loadingSale">
      <ElEmpty description="该分类下暂无销售属性">
        <ElButton type="primary" @click="wizard.generateDefaultSku">下一步：SKU 信息</ElButton>
      </ElEmpty>
    </template>
    <ElForm v-else label-width="120px" style="max-width: 800px; margin: 0 auto">
      <ElFormItem
        v-for="(attr, aidx) in wizard.saleAttrs"
        :key="attr.attrId"
        :label="attr.attrName"
      >
        <ElCheckboxGroup v-model="wizard.saleAttrDraft[aidx]!.attrValues">
          <ElCheckbox
            v-for="val in wizard.parseValueSelect(attr.valueSelect)"
            :key="val"
            :label="val"
            :value="val"
          />
        </ElCheckboxGroup>
        <ElButton
          v-if="!wizard.saleInputVisible[aidx]"
          size="small"
          style="margin-left: 8px"
          @click="wizard.showSaleInput(aidx)"
        >
          + 自定义
        </ElButton>
        <ElInput
          v-else
          v-model="wizard.saleInputValue[aidx]"
          placeholder="回车确认"
          size="small"
          style="width: 140px; margin-left: 8px"
          @blur="wizard.confirmSaleInput(aidx)"
          @keyup.enter="wizard.confirmSaleInput(aidx)"
        />
      </ElFormItem>
    </ElForm>
    <div v-if="wizard.saleAttrs.length > 0" class="wizard-actions">
      <ElButton @click="wizard.step = 1">上一步</ElButton>
      <ElButton type="primary" @click="wizard.generateSkus">下一步：SKU 信息</ElButton>
    </div>
  </ElCard>
</template>

<script lang="ts" setup>
import {useSpuWizardInject} from '../../composables/useSpuWizard'

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
</style>
