/**
 * 字典数据状态管理模块
 *
 * 提供字典数据的全局状态管理和持久化存储
 *
 * ## 主要功能
 *
 * - 字典数据缓存管理
 * - 字典数据加载和更新
 * - 字典标签和选项格式化
 * - 字典数据持久化（localStorage）
 * - 批量字典加载
 * - 缓存过期管理
 *
 * ## 使用场景
 *
 * - 表格列字典值格式化显示
 * - 表单下拉选择框选项
 * - 标签显示（带样式）
 * - 全局字典数据共享
 *
 * ## 持久化
 *
 * - 使用 localStorage 存储
 * - 存储键：sys-v{version}-dict
 * - 自动持久化所有字典数据
 * - 页面刷新后自动恢复
 *
 * @module store/modules/dict
 * @author Art Design Pro Team
 */

import {defineStore} from 'pinia'
import {fetchGetDictDataByType, type SysDictData} from '@/api/dict/data'

/**
 * 字典数据存储结构
 * Map<字典类型, Map<字典值, 字典项>>
 */
type DictCache = Map<string, Map<string, SysDictData>>

/**
 * 字典数据状态管理
 * 管理所有字典数据的加载、缓存和格式化
 */
export const useDictStore = defineStore(
  'dictStore',
  () => {
    const dictCache = ref<DictCache>(new Map())

    const loading = ref<Set<string>>(new Set())

    const getDictMap = (dictType: string): Map<string, SysDictData> => {
      return dictCache.value.get(dictType) || new Map()
    }

    const loadDict = async (dictType: string, force = false): Promise<boolean> => {
      if (!force && dictCache.value.has(dictType)) {
        return true
      }

      if (loading.value.has(dictType)) {
        return false
      }

      loading.value.add(dictType)

      try {
        const data = await fetchGetDictDataByType(dictType)
        const dictMap = new Map<string, SysDictData>()
        data.forEach((item) => {
          dictMap.set(item.dictValue, item)
        })
        dictCache.value.set(dictType, dictMap)
        return true
      } catch (error) {
        console.error(`加载字典数据失败 - ${dictType}:`, error)
        return false
      } finally {
        loading.value.delete(dictType)
      }
    }

    const loadDicts = async (dictTypes: string[], force = false): Promise<void> => {
      const promises = dictTypes.map((type) => loadDict(type, force))
      await Promise.all(promises)
    }

    const getDictLabel = (
      dictType: string,
      dictValue: string | number | undefined,
      defaultValue = '-'
    ): string => {
      if (dictValue === undefined || dictValue === null) {
        return defaultValue
      }
      const dictMap = dictCache.value.get(dictType)
      if (!dictMap) {
        console.warn(`字典类型 ${dictType} 未加载`)
        return String(dictValue)
      }
      const item = dictMap.get(String(dictValue))
      return item?.dictLabel || defaultValue
    }

    const getDictItem = (
      dictType: string,
      dictValue: string | number | undefined
    ): SysDictData | undefined => {
      if (dictValue === undefined || dictValue === null) {
        return undefined
      }
      const dictMap = dictCache.value.get(dictType)
      if (!dictMap) {
        console.warn(`字典类型 ${dictType} 未加载`)
        return undefined
      }
      return dictMap.get(String(dictValue))
    }

    const getDictOptions = (
      dictType: string,
      addEmpty = false,
      emptyLabel = '请选择'
    ): { label: string; value: string }[] => {
      const dictMap = dictCache.value.get(dictType)
      if (!dictMap) {
        console.warn(`字典类型 ${dictType} 未加载`)
        return addEmpty ? [{ label: emptyLabel, value: '' }] : []
      }

      const options: { label: string; value: string }[] = []

      if (addEmpty) {
        options.push({ label: emptyLabel, value: '' })
      }

      const sortedItems = Array.from(dictMap.values()).sort((a, b) => {
        const sortA = Number(a.dictSort) || 0
        const sortB = Number(b.dictSort) || 0
        return sortA - sortB
      })

      sortedItems.forEach((item) => {
        options.push({
          label: item.dictLabel,
          value: item.dictValue
        })
      })

      return options
    }

    const getTagType = (cssClass?: string): string => {
      const typeMap: Record<string, string> = {
        primary: 'primary',
        success: 'success',
        warning: 'warning',
        danger: 'danger',
        info: 'info'
      }
      return typeMap[cssClass || ''] || 'info'
    }

    const clearDictCache = (dictType?: string): void => {
      if (dictType) {
        dictCache.value.delete(dictType)
      } else {
        dictCache.value.clear()
      }
    }

    const isDictLoaded = (dictType: string): boolean => {
      return dictCache.value.has(dictType)
    }

    const isLoading = (dictType: string): boolean => {
      return loading.value.has(dictType)
    }

    return {
      dictCache,
      loading,
      getDictMap,
      loadDict,
      loadDicts,
      getDictLabel,
      getDictItem,
      getDictOptions,
      getTagType,
      clearDictCache,
      isDictLoaded,
      isLoading
    }
  },
  {
    persist: {
      key: 'dict',
      storage: localStorage,
      serializer: {
        serialize: (state) => {
          const dictCache = state.dictCache
          const serializedCache: Record<string, Record<string, SysDictData>> = {}
          dictCache.forEach((dictMap, dictType) => {
            serializedCache[dictType] = {}
            dictMap.forEach((item, dictValue) => {
              serializedCache[dictType][dictValue] = item
            })
          })
          return JSON.stringify({
            dictCache: serializedCache,
            loading: Array.from(state.loading)
          })
        },
        deserialize: (str) => {
          try {
            const parsed = JSON.parse(str)
            const dictCache = new Map<string, Map<string, SysDictData>>()
            if (parsed.dictCache) {
              Object.entries(parsed.dictCache).forEach(([dictType, dictMap]) => {
                const map = new Map<string, SysDictData>()
                Object.entries(dictMap as Record<string, SysDictData>).forEach(
                  ([dictValue, item]) => {
                    map.set(dictValue, item as SysDictData)
                  }
                )
                dictCache.set(dictType, map)
              })
            }
            return {
              dictCache,
              loading: new Set(parsed.loading || [])
            }
          } catch (error) {
            console.error('反序列化字典数据失败:', error)
            return {
              dictCache: new Map(),
              loading: new Set()
            }
          }
        }
      }
    }
  }
)
