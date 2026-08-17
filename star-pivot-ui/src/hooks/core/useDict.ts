/**
 * useDict - 字典数据管理 Hook
 *
 * 提供统一的字典数据管理功能，支持字典数据的获取、缓存和格式化显示。
 * 基于 Pinia Store 实现，支持持久化存储。
 *
 * ## 主要功能
 *
 * 1. 字典数据获取 - 根据字典类型获取字典列表
 * 2. 字典缓存 - 自动缓存已获取的字典数据，避免重复请求
 * 3. 持久化存储 - 使用 localStorage 持久化，页面刷新后自动恢复
 * 4. 标签格式化 - 将字典值转换为对应的标签显示
 * 5. 下拉选项格式化 - 将字典数据转换为下拉选择框选项格式
 *
 * ## 使用示例
 *
 * ```typescript
 * import { useDict } from '@/hooks/core/useDict'
 *
 * // 获取字典 hook
 * const { getDictLabel, getDictOptions, loadDict } = useDict()
 *
 * // 在组件挂载时加载字典
 * onMounted(() => {
 *   await loadDict('sys_notice_type')
 *   await loadDict('sys_notice_status')
 * })
 *
 * // 获取字典标签（单个值）
 * const label = getDictLabel('sys_notice_type', '1')
 * // 返回: '通知'
 *
 * // 获取字典下拉选项
 * const options = getDictOptions('sys_notice_type')
 * // 返回: [{ label: '通知', value: '1' }, { label: '公告', value: '2' }]
 *
 * // 在表格列 formatter 中使用
 * formatter: (row) => {
 *   return h(ElTag, { type: getTagType(row.status) }, () => getDictLabel('sys_notice_status', row.status))
 * }
 * ```
 *
 * @module useDict
 */

import { useDictStore } from '@/store/modules/dict'

export const useDict = () => {
  const dictStore = useDictStore()

  return {
    dictCache: dictStore.dictCache,
    loading: dictStore.loading,
    getDictMap: dictStore.getDictMap,
    loadDict: dictStore.loadDict,
    loadDicts: dictStore.loadDicts,
    getDictLabel: dictStore.getDictLabel,
    getDictItem: dictStore.getDictItem,
    getDictOptions: dictStore.getDictOptions,
    getTagType: dictStore.getTagType,
    clearDictCache: dictStore.clearDictCache,
    isDictLoaded: dictStore.isDictLoaded,
    isLoading: dictStore.isLoading
  }
}
