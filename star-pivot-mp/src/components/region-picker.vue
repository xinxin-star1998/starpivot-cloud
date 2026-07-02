<template>
  <view class="region-picker">
    <picker :range="provinces" range-key="name" @change="onProvinceChange">
      <view class="picker-cell" :class="{ placeholder: !modelValue.province }">
        {{ modelValue.province || '请选择省份' }}
      </view>
    </picker>
    <picker :range="cities" range-key="name" :disabled="!modelValue.province" @change="onCityChange">
      <view class="picker-cell" :class="{ placeholder: !modelValue.city }">
        {{ modelValue.city || '请选择城市' }}
      </view>
    </picker>
    <picker :range="districts" range-key="name" :disabled="!modelValue.city" @change="onDistrictChange">
      <view class="picker-cell" :class="{ placeholder: !modelValue.region }">
        {{ modelValue.region || '请选择区县' }}
      </view>
    </picker>
  </view>
</template>

<script setup lang="ts">
import {onMounted, ref, watch} from 'vue'
import {fetchRegionChildren} from '@/api/region'
import type {PortalRegion} from '@/api/types'

const props = defineProps<{
  modelValue: {
    province: string
    city: string
    region: string
  }
}>()

const emit = defineEmits<{
  'update:modelValue': [value: { province: string; city: string; region: string }]
}>()

const provinces = ref<PortalRegion[]>([])
const cities = ref<PortalRegion[]>([])
const districts = ref<PortalRegion[]>([])

const provinceCode = ref('')
const cityCode = ref('')

async function loadProvinces() {
  provinces.value = await fetchRegionChildren('0')
}

async function loadCities(code: string) {
  cities.value = code ? await fetchRegionChildren(code) : []
}

async function loadDistricts(code: string) {
  districts.value = code ? await fetchRegionChildren(code) : []
}

function onProvinceChange(e: { detail: { value: string } }) {
  const idx = Number(e.detail.value)
  const item = provinces.value[idx]
  if (!item?.name) return
  provinceCode.value = item.code || ''
  emit('update:modelValue', { province: item.name, city: '', region: '' })
  loadCities(provinceCode.value)
  districts.value = []
}

function onCityChange(e: { detail: { value: string } }) {
  const idx = Number(e.detail.value)
  const item = cities.value[idx]
  if (!item?.name) return
  cityCode.value = item.code || ''
  emit('update:modelValue', { ...props.modelValue, city: item.name, region: '' })
  loadDistricts(cityCode.value)
}

function onDistrictChange(e: { detail: { value: string } }) {
  const idx = Number(e.detail.value)
  const item = districts.value[idx]
  if (!item?.name) return
  emit('update:modelValue', { ...props.modelValue, region: item.name })
}

watch(
  () => props.modelValue.province,
  async (name) => {
    if (!name || provinceCode.value) return
    const found = provinces.value.find((p) => p.name === name)
    if (found?.code) {
      provinceCode.value = found.code
      await loadCities(found.code)
    }
  }
)

onMounted(loadProvinces)
</script>

<style scoped>
.region-picker {
  display: flex;
  flex-direction: column;
  gap: 16rpx;
}
.picker-cell {
  padding: 20rpx;
  background: #f5f5f5;
  border-radius: 12rpx;
  font-size: 28rpx;
}
.picker-cell.placeholder {
  color: #999;
}
</style>
