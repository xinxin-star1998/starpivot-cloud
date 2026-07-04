<template>
  <ElCard class="wizard-panel wizard-done" shadow="never">
    <ElResult icon="success" sub-title="商品及 SKU 信息已提交" title="保存成功">
      <template #extra>
        <ElSpace>
          <ElButton type="primary" @click="wizard.goBack">返回列表</ElButton>
          <ElButton v-if="!wizard.isEdit" @click="wizard.resetWizard">继续发布</ElButton>
        </ElSpace>
      </template>
    </ElResult>
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

  .wizard-done {
    display: flex;
    align-items: center;
    justify-content: center;
    min-height: 320px;
  }
</style>
