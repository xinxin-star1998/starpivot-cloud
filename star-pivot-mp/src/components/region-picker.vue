<template>
  <view class="region-picker">
    <picker :range="provinces" range-key="name" @change="onProvinceChange">
      <view class="picker-cell" :class="{ filled: !!modelValue.province }">
        <text class="cell-label">省份</text>
        <text class="cell-value">{{ modelValue.province || '请选择省份' }}</text>
        <text class="arrow">›</text>
      </view>
    </picker>
    <picker :range="cities" range-key="name" :disabled="!modelValue.province" @change="onCityChange">
      <view class="picker-cell" :class="{ filled: !!modelValue.city, disabled: !modelValue.province }">
        <text class="cell-label">城市</text>
        <text class="cell-value">{{ modelValue.city || '请选择城市' }}</text>
        <text class="arrow">›</text>
      </view>
    </picker>
    <picker :range="districts" range-key="name" :disabled="!modelValue.city" @change="onDistrictChange">
      <view class="picker-cell" :class="{ filled: !!modelValue.region, disabled: !modelValue.city }">
        <text class="cell-label">区县</text>
        <text class="cell-value">{{ modelValue.region || '请选择区县' }}</text>
        <text class="arrow">›</text>
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

<style scoped lang="scss">
.region-picker {
  display: flex;
  flex-direction: column;
  gap: 12rpx;
}

.picker-cell {
  display: flex;
  align-items: center;
  gap: 16rpx;
  padding: 20rpx 24rpx;
  background: $sp-bg-page;
  border: 2rpx solid transparent;
  border-radius: $sp-radius-sm;
  font-size: 28rpx;

  &.filled {
    background: $sp-primary-light;
    border-color: rgba(225, 37, 27, 0.15);

    .cell-value {
      color: $sp-text;
      font-weight: 500;
    }
  }

  &.disabled {
    opacity: 0.5;
  }
}

.cell-label {
  width: 72rpx;
  flex-shrink: 0;
  font-size: 26rpx;
  color: $sp-text-secondary;
}

.cell-value {
  flex: 1;
  color: $sp-text-muted;
}

.arrow {
  font-size: 32rpx;
  color: #ccc;
}
</style>
