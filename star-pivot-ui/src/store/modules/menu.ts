/**
 * 菜单状态管理模块
 *
 * 提供菜单数据和动态路由的状态管理
 *
 * ## 主要功能
 *
 * - 菜单列表存储和管理
 * - 首页路径配置
 * - 动态路由注册和移除
 * - 路由移除函数管理
 * - 菜单宽度配置
 *
 * ## 使用场景
 *
 * - 动态菜单加载和渲染
 * - 路由权限控制
 * - 首页路径动态设置
 * - 登出时清理动态路由
 *
 * ## 工作流程
 *
 * 1. 获取菜单数据（前端/后端模式）
 * 2. 设置菜单列表和首页路径
 * 3. 注册动态路由并保存移除函数
 * 4. 登出时调用移除函数清理路由
 *
 * @module store/modules/menu
 * @author Art Design Pro Team
 */
import {defineStore} from 'pinia'
import {AppRouteRecord} from '@/types/router'
import {getFirstMenuPath} from '@/utils'
import {HOME_PAGE_PATH} from '@/router'
import type {SysMenu} from '@/api/menu/menu'

const MENU_CACHE_SCHEMA_VERSION = 1
const MENU_CACHE_TTL_MS = 5 * 60 * 1000

/**
 * 菜单状态管理
 * 管理应用的菜单列表、首页路径、菜单宽度和动态路由移除函数
 */
export const useMenuStore = defineStore(
  'menuStore',
  () => {
    /** 首页路径 */
    const homePath = ref(HOME_PAGE_PATH)
    /** 菜单列表（转换后的路由数据） */
    const menuList = ref<AppRouteRecord[]>([])
    /** 原始菜单数据（后端返回的SysMenu数组） */
    const rawMenuList = ref<SysMenu[]>([])
    /** 菜单宽度 */
    const menuWidth = ref('')
    /** 菜单缓存时间戳（毫秒） */
    const cacheTimestamp = ref(0)
    /** 菜单缓存所属用户 ID（用于隔离不同账号） */
    const cacheUserId = ref('')
    /** 菜单缓存结构版本（用于升级后自动失效） */
    const cacheSchemaVersion = ref(MENU_CACHE_SCHEMA_VERSION)
    /** 存储路由移除函数的数组 */
    const removeRouteFns = ref<(() => void)[]>([])

    /**
     * 设置菜单列表
     * @param list 菜单路由记录数组
     */
    const setMenuList = (list: AppRouteRecord[]) => {
      menuList.value = list
      setHomePath(HOME_PAGE_PATH || getFirstMenuPath(list))
    }

    /**
     * 设置原始菜单数据（后端返回的SysMenu数组）
     * @param list 原始菜单数据
     */
    const setRawMenuList = (list: SysMenu[]) => {
      rawMenuList.value = list
    }

    /**
     * 获取原始菜单数据
     * @returns 原始菜单数据数组
     */
    const getRawMenuList = () => rawMenuList.value

    /**
     * 递归获取所有菜单的权限标识
     * @param menus 菜单列表
     * @returns 权限标识数组
     */
    const getAllPerms = (menus: SysMenu[]): string[] => {
      const perms: string[] = []
      for (const menu of menus) {
        if (menu.perms) {
          // perms 可能是逗号分隔的多个权限标识
          const permsArray = menu.perms
            .split(',')
            .map((p) => p.trim())
            .filter(Boolean)
          perms.push(...permsArray)
        }
        if (menu.children && menu.children.length > 0) {
          perms.push(...getAllPerms(menu.children))
        }
      }
      return perms
    }

    /**
     * 获取指定前缀的权限列表
     * @param prefix 权限前缀，如 'system:data'
     * @returns 匹配前缀的权限标识数组
     */
    const getPermsByPrefix = (prefix: string): string[] => {
      const allPerms = getAllPerms(rawMenuList.value)
      if (!prefix) return allPerms
      return allPerms.filter((perm) => perm.startsWith(prefix))
    }

    /**
     * 判断当前用户是否拥有指定权限（全局菜单权限，不依赖当前路由）
     */
    const hasPerm = (perm: string): boolean => {
      if (!perm) return false
      return getAllPerms(rawMenuList.value).includes(perm)
    }

    /**
     * 是否拥有任一权限
     */
    const hasAnyPerm = (perms: string[]): boolean => {
      return perms.some((perm) => hasPerm(perm))
    }

    /**
     * 获取首页路径
     * @returns 首页路径字符串
     */
    const getHomePath = () => homePath.value

    /**
     * 设置主页路径
     * @param path 主页路径
     */
    const setHomePath = (path: string) => {
      homePath.value = path
    }

    /**
     * 添加路由移除函数
     * @param fns 要添加的路由移除函数数组
     */
    const addRemoveRouteFns = (fns: (() => void)[]) => {
      removeRouteFns.value.push(...fns)
    }

    /**
     * 移除所有动态路由
     * 执行所有存储的路由移除函数并清空数组
     */
    const removeAllDynamicRoutes = () => {
      // removeRouteFn 由 router.addRoute 返回，不同实现/重复调用下可能抛错
      // 这里做容错，保证清理流程不中断
      removeRouteFns.value.forEach((fn) => {
        try {
          fn()
        } catch (error) {
          console.error('[menuStore] 移除动态路由失败:', error)
        }
      })
      removeRouteFns.value = []
    }

    /**
     * 清空路由移除函数数组
     */
    const clearRemoveRouteFns = () => {
      removeRouteFns.value = []
    }

    /**
     * 标记菜单缓存信息
     * @param userId 当前登录用户 ID
     */
    const markMenuCache = (userId?: string | number) => {
      cacheTimestamp.value = Date.now()
      cacheUserId.value = userId !== undefined && userId !== null ? String(userId) : ''
      cacheSchemaVersion.value = MENU_CACHE_SCHEMA_VERSION
    }

    /**
     * 判断菜单缓存是否有效
     * 条件：有菜单、版本匹配、未过期、且用户一致
     * @param userId 当前登录用户 ID
     */
    const isMenuCacheValid = (userId?: string | number): boolean => {
      if (!menuList.value.length) return false
      if (cacheSchemaVersion.value !== MENU_CACHE_SCHEMA_VERSION) return false
      const isExpired =
        !cacheTimestamp.value || Date.now() - cacheTimestamp.value > MENU_CACHE_TTL_MS
      if (isExpired) return false

      const currentUserId = userId !== undefined && userId !== null ? String(userId) : ''
      if (!currentUserId || !cacheUserId.value) return false

      return currentUserId === cacheUserId.value
    }

    /**
     * 清空菜单缓存元数据
     */
    const clearMenuCacheMeta = () => {
      cacheTimestamp.value = 0
      cacheUserId.value = ''
      cacheSchemaVersion.value = MENU_CACHE_SCHEMA_VERSION
    }

    return {
      homePath,
      menuList,
      rawMenuList,
      menuWidth,
      cacheTimestamp,
      cacheUserId,
      cacheSchemaVersion,
      setMenuList,
      setRawMenuList,
      getRawMenuList,
      getPermsByPrefix,
      hasPerm,
      hasAnyPerm,
      getHomePath,
      setHomePath,
      addRemoveRouteFns,
      removeAllDynamicRoutes,
      clearRemoveRouteFns,
      markMenuCache,
      isMenuCacheValid,
      clearMenuCacheMeta
    }
  },
  {
    persist: {
      key: 'menu',
      storage: localStorage
    }
  }
)
