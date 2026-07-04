<template>
  <ElCard class="wizard-panel" shadow="never">
    <ElForm
      :ref="(el) => (wizard.baseFormRef = el as FormInstance)"
      :model="wizard.baseForm"
      :rules="wizard.baseRules"
      label-width="120px"
      style="max-width: 720px; margin: 0 auto"
    >
      <ElFormItem label="商品名称" prop="spuName">
        <ElInput v-model="wizard.baseForm.spuName" maxlength="200" show-word-limit />
      </ElFormItem>
      <ElFormItem label="商品描述" prop="spuDescription">
        <ElInput v-model="wizard.baseForm.spuDescription" :rows="2" type="textarea" />
      </ElFormItem>
      <ElFormItem label="商品分类" prop="catalogPath">
        <ElCascader
          v-model="wizard.catalogPath"
          :options="wizard.categoryOptions"
          :props="wizard.cascaderProps"
          clearable
          filterable
          placeholder="请选择三级类目"
          style="width: 100%"
        />
      </ElFormItem>
      <ElFormItem label="品牌" prop="brandId">
        <ElSelect
          v-model="wizard.baseForm.brandId"
          clearable
          filterable
          placeholder="选填"
          style="width: 100%"
        >
          <ElOption
            v-for="b in wizard.brandOptions"
            :key="b.brandId"
            :label="b.name"
            :value="b.brandId!"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="重量 (Kg)" prop="weight">
        <ElInputNumber
          v-model="wizard.baseForm.weight"
          :min="0"
          :precision="3"
          :step="0.1"
          style="width: 200px"
        />
      </ElFormItem>
      <ElFormItem label="积分设置">
        <ElSpace wrap>
          <span>金币</span>
          <ElInputNumber
            v-model="wizard.baseForm.bounds.buyBounds"
            :min="0"
            controls-position="right"
            style="width: 140px"
          />
          <span>成长值</span>
          <ElInputNumber
            v-model="wizard.baseForm.bounds.growBounds"
            :min="0"
            controls-position="right"
            style="width: 140px"
          />
        </ElSpace>
      </ElFormItem>
      <ElFormItem label="上架状态" prop="publishStatus">
        <ElRadioGroup v-model="wizard.baseForm.publishStatus">
          <ElRadio :value="1">上架</ElRadio>
          <ElRadio :value="0">下架</ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="商品介绍图" prop="decript">
        <SpuImageUpload
          v-model="wizard.baseForm.decript"
          :goods-id="wizard.spuId"
          :limit="20"
          hint="详情页展示图（可选）"
        />
      </ElFormItem>
      <ElFormItem label="商品图集" prop="images">
        <SpuImageUpload
          v-model="wizard.baseForm.images"
          :goods-id="wizard.spuId"
          :limit="10"
          hint="SPU 主图，SKU 可从中勾选"
        />
      </ElFormItem>
      <ElFormItem>
        <ElButton :loading="wizard.loadingAttrs" type="primary" @click="wizard.onBaseNext">
          下一步：规格参数
        </ElButton>
      </ElFormItem>
    </ElForm>
  </ElCard>
</template>

<script lang="ts" setup>
import type {FormInstance} from 'element-plus'
import {useSpuWizardInject} from '../../composables/useSpuWizard'
import SpuImageUpload from '../spu-image-upload.vue'

const wizard = useSpuWizardInject()
</script>

<style lang="scss" scoped>
  .wizard-panel {
    flex: 1;
    min-height: 0;
    overflow: auto;
  }
</style>
