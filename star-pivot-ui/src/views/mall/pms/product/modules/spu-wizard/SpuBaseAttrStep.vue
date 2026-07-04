<template>
  <ElCard v-loading="wizard.loadingAttrs" class="wizard-panel" shadow="never">
    <template v-if="wizard.attrGroups.length === 0 && !wizard.loadingAttrs">
      <ElEmpty description="该分类下暂无基本属性，可直接下一步" />
    </template>
    <ElTabs v-else style="min-height: 280px" tab-position="left">
      <ElTabPane
        v-for="(group, gidx) in wizard.attrGroups"
        :key="group.attrGroupId"
        :label="group.attrGroupName"
      >
        <ElForm label-width="140px">
          <ElFormItem v-for="(attr, aidx) in group.attrs" :key="attr.attrId" :label="attr.attrName">
            <ElSelect
              v-model="wizard.baseAttrValues[gidx]![aidx]!.attrValues"
              :multiple="attr.valueType === 1"
              allow-create
              clearable
              default-first-option
              filterable
              placeholder="请选择或输入"
              style="min-width: 240px"
            >
              <ElOption
                v-for="(val, vidx) in wizard.parseValueSelect(attr.valueSelect)"
                :key="vidx"
                :label="val"
                :value="val"
              />
            </ElSelect>
            <ElCheckbox
              v-model="wizard.baseAttrValues[gidx]![aidx]!.showDesc"
              :false-value="0"
              :true-value="1"
              style="margin-left: 12px"
            >
              快速展示
            </ElCheckbox>
          </ElFormItem>
        </ElForm>
      </ElTabPane>
    </ElTabs>
    <div class="wizard-actions">
      <ElButton @click="wizard.step = 0">上一步</ElButton>
      <ElButton :loading="wizard.loadingSale" type="primary" @click="wizard.onBaseAttrsNext">
        下一步：销售属性
      </ElButton>
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
