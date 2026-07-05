import {fetchPortalRegionChildren} from '@/api/portal/region'
import type {PortalRegion} from '@/api/portal/types'

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

  function findRegionByName(list: PortalRegion[], name: string) {
    const trimmed = name.trim()
    if (!trimmed) return undefined
    return list.find((item) => item.name === trimmed)
  }

  /** 编辑地址：按已保存的省/市/区名称反查 code 并回填下拉 */
  async function restoreRegionFromNames(names: PortalRegionForm) {
    resetRegionPicker()
    if (!names.province?.trim()) return

    if (!provinces.value.length) {
      await loadProvinces()
    }

    const province = findRegionByName(provinces.value, names.province)
    if (!province?.code) return

    regionCodes.value[0] = province.code
    form.province = province.name || names.province

    cities.value = await fetchPortalRegionChildren(province.code)
    if (!names.city?.trim()) return

    const city = findRegionByName(cities.value, names.city)
    if (!city?.code) return

    regionCodes.value[1] = city.code
    form.city = city.name || names.city

    districts.value = await fetchPortalRegionChildren(city.code)
    if (!names.region?.trim()) return

    const district = findRegionByName(districts.value, names.region)
    if (!district?.code) return

    regionCodes.value[2] = district.code
    form.region = district.name || names.region
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
    resetRegionPicker,
    restoreRegionFromNames
  }
}
