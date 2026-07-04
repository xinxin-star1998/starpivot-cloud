import { ref } from 'vue'

/** 移动端全部分类抽屉（全局共享状态） */
export const portalCategoryDrawerVisible = ref(false)

export function openPortalCategoryDrawer() {
  portalCategoryDrawerVisible.value = true
}

export function closePortalCategoryDrawer() {
  portalCategoryDrawerVisible.value = false
}
