<!-- C 端省市区三级联动表单项 -->
<template>
  <ElFormItem label="省份" prop="province">
    <ElSelect v-model="regionCodes[0]" placeholder="省" @change="onProvinceChange">
      <ElOption v-for="p in provinces" :key="p.code" :label="p.name" :value="p.code!" />
    </ElSelect>
  </ElFormItem>
  <ElFormItem label="城市" prop="city">
    <ElSelect
      v-model="regionCodes[1]"
      placeholder="市"
      :disabled="!cities.length"
      @change="onCityChange"
    >
      <ElOption v-for="c in cities" :key="c.code" :label="c.name" :value="c.code!" />
    </ElSelect>
  </ElFormItem>
  <ElFormItem label="区/县" prop="region">
    <ElSelect
      v-model="regionCodes[2]"
      placeholder="区/县"
      :disabled="!districts.length"
      @change="onDistrictChange"
    >
      <ElOption v-for="d in districts" :key="d.code" :label="d.name" :value="d.code!" />
    </ElSelect>
  </ElFormItem>
</template>

<script setup lang="ts">
import {type PortalRegionForm, usePortalRegionPicker} from '@/hooks/portal/usePortalRegionPicker'

defineOptions({ name: 'PortalRegionFields' })

  const props = defineProps<{
    form: PortalRegionForm
  }>()

  const {
    regionCodes,
    provinces,
    cities,
    districts,
    loadProvinces,
    onProvinceChange,
    onCityChange,
    onDistrictChange,
    resetRegionPicker
  } = usePortalRegionPicker(props.form)

  onMounted(() => {
    loadProvinces()
  })

  defineExpose({ resetRegionPicker, loadProvinces })
</script>
