import { fetchPortalRegionChildren } from '@/api/portal/region'
import type { PortalRegion } from '@/api/portal/types'

export interface PortalRegionForm {
  province: string
  city: string
  region: string
}

/** C 端省市区三级联动 */
export function usePortalRegionPicker(form: PortalRegionForm) {
  const regionCodes = ref<[string, string, string]>(['', '', ''])
  const provinces = ref<PortalRegion[]>([])
  const cities = ref<PortalRegion[]>([])
  const districts = ref<PortalRegion[]>([])

  async function loadProvinces() {
    provinces.value = await fetchPortalRegionChildren('0')
  }

  async function onProvinceChange(code: string) {
    const province = provinces.value.find((item) => item.code === code)
    form.province = province?.name || ''
    form.city = ''
    form.region = ''
    regionCodes.value[1] = ''
    regionCodes.value[2] = ''
    cities.value = code ? await fetchPortalRegionChildren(code) : []
    districts.value = []
  }

  async function onCityChange(code: string) {
    const city = cities.value.find((item) => item.code === code)
    form.city = city?.name || ''
    form.region = ''
    regionCodes.value[2] = ''
    districts.value = code ? await fetchPortalRegionChildren(code) : []
  }

  function onDistrictChange(code: string) {
    const district = districts.value.find((item) => item.code === code)
    form.region = district?.name || ''
  }

  function resetRegionPicker() {
    regionCodes.value = ['', '', '']
    cities.value = []
    districts.value = []
  }

  return {
    regionCodes,
    provinces,
    cities,
    districts,
    loadProvinces,
    onProvinceChange,
    onCityChange,
    onDistrictChange,
    resetRegionPicker
  }
}
