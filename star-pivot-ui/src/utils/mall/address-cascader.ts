import { type Address, fetchGetAddressChildren, fetchSearchAddress } from '@/api/mall/address'
import { addressHasChildren } from '@/utils/mall/address-level'
import type { CascaderNode, CascaderOption, CascaderProps } from 'element-plus'

/** 仓库选址：仅省 / 市 / 区县（level 0～2），不含乡镇 */
export const WAREHOUSE_MAX_AREA_LEVEL = 2

export function mapAddressToCascaderOption(item: Address): CascaderOption {
  return {
    value: item.code!,
    label: item.name || item.code || '',
    leaf: !addressHasChildren(item.level)
  }
}

function mapAddressToCascaderOptionWithMaxLevel(item: Address, maxLevel: number): CascaderOption {
  const level = item.level ?? 0
  return {
    value: item.code!,
    label: item.name || item.code || '',
    leaf: level >= maxLevel || !addressHasChildren(item.level)
  }
}

function createAddressCascaderLazyLoad(
  maxAreaLevel: number
): NonNullable<CascaderProps['lazyLoad']> {
  return async (node: CascaderNode, resolve: (data: CascaderOption[]) => void) => {
    // 级联第 4 列对应乡镇，仓库不加载
    if (node.level > maxAreaLevel + 1) {
      resolve([])
      return
    }
    const parentCode = node.level === 0 ? '0' : String(node.value ?? '0')
    try {
      const list = await fetchGetAddressChildren(parentCode)
      resolve(
        (list || [])
          .filter((item) => item.code)
          .map((item) => mapAddressToCascaderOptionWithMaxLevel(item, maxAreaLevel))
      )
    } catch {
      resolve([])
    }
  }
}

/** 省市区乡镇懒加载 Cascader（value 为地区 code） */
export const addressCascaderProps: CascaderProps = {
  lazy: true,
  emitPath: true,
  expandTrigger: 'click',
  checkStrictly: true,
  lazyLoad: createAddressCascaderLazyLoad(3)
}

/** 仓库：仅省 / 市 / 区县三级 */
export const warehouseAddressCascaderProps: CascaderProps = {
  lazy: true,
  emitPath: true,
  expandTrigger: 'click',
  checkStrictly: true,
  lazyLoad: createAddressCascaderLazyLoad(WAREHOUSE_MAX_AREA_LEVEL)
}

/** 根据末级编码反查 Cascader 路径（编辑回显） */
export async function resolveAddressCodePath(
  code?: string,
  options?: { maxPathLength?: number }
): Promise<string[]> {
  if (!code?.trim()) return []
  const path: string[] = []
  let current: string | undefined = code.trim()
  for (let depth = 0; depth < 6 && current; depth++) {
    path.unshift(current)
    try {
      const rows = await fetchSearchAddress({ code: current })
      const node = rows?.find((r) => r.code === current) ?? rows?.[0]
      const parent = node?.parentCode?.trim()
      if (!parent || parent === '0' || parent === '00') break
      current = parent
    } catch {
      break
    }
  }
  const maxLen = options?.maxPathLength
  if (maxLen != null && path.length > maxLen) {
    return path.slice(0, maxLen)
  }
  return path
}
