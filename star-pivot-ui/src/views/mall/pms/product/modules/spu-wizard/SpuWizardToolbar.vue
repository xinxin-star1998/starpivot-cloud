<template>
  <ElCard class="wizard-toolbar" shadow="never">
    <div class="wizard-toolbar__row">
      <ElButton @click="wizard.goBack">返回列表</ElButton>
      <span class="wizard-toolbar__title">{{ wizard.pageTitle }}</span>
    </div>
    <ElSteps :active="wizard.step" align-center class="wizard-steps" finish-status="success">
      <ElStep title="基本信息" />
      <ElStep title="规格参数" />
      <ElStep title="销售属性" />
      <ElStep title="SKU 信息" />
      <ElStep title="完成" />
    </ElSteps>
  </ElCard>
</template>

<script lang="ts" setup>
import {useSpuWizardInject} from '../../composables/useSpuWizard'

const wizard = useSpuWizardInject()
</script>

<style lang="scss" scoped>
  .wizard-toolbar__row {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 16px;
  }

  .wizard-toolbar__title {
    font-size: 16px;
    font-weight: 600;
  }

  .wizard-steps {
    margin-top: 8px;
  }
</style>
