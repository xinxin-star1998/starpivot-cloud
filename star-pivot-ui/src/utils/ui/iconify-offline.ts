import {addCollection} from '@iconify/vue'

export type OfflineIconSetOption = {
  label: string
  prefix: string
}

/** 图标选择器默认离线图标库（需已安装对应 @iconify-json/* 包） */
export const DEFAULT_ICON_SETS: OfflineIconSetOption[] = [
  { label: 'Heroicons Outline', prefix: 'heroicons-outline' },
  { label: 'Heroicons Solid', prefix: 'heroicons-solid' },
  { label: 'Remix Icon', prefix: 'ri' },
  { label: 'Material Design Icons', prefix: 'mdi' },
  { label: 'Element Plus', prefix: 'ep' }
]

export const offlineIconLoaders: Record<string, () => Promise<{ default: any }>> = {
  'heroicons-outline': () => import('@iconify-json/heroicons-outline/icons.json'),
  'heroicons-solid': () => import('@iconify-json/heroicons-solid/icons.json'),
  ri: () => import('@iconify-json/ri/icons.json'),
  mdi: () => import('@iconify-json/mdi/icons.json'),
  ep: () => import('@iconify-json/ep/icons.json')
}

const registeredPrefixes = new Set<string>()

/** 注册 Iconify 离线图标集并返回 `prefix:name` 列表（幂等） */
export async function ensureIconCollection(prefix: string): Promise<string[]> {
  const loader = offlineIconLoaders[prefix]
  if (!loader) {
    return []
  }
  const mod = await loader()
  const iconsJson = mod.default
  if (!registeredPrefixes.has(prefix)) {
    addCollection(iconsJson)
    registeredPrefixes.add(prefix)
  }
  const icons = iconsJson?.icons ? Object.keys(iconsJson.icons) : []
  return icons.map((name: string) => `${prefix}:${name}`)
}

/** 启动时预注册常用库，侧栏/菜单首次渲染不依赖 CDN */
export async function setupOfflineIconify(): Promise<void> {
  const prefixes = ['heroicons-outline', 'heroicons-solid', 'ri'] as const
  await Promise.all(prefixes.map((p) => ensureIconCollection(p)))
}
