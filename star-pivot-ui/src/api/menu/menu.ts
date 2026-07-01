import request from '@/utils/http'
import type {MenuFormData} from '@/views/system/menu/types'

// 后端菜单实体类型
export interface SysMenu {
  menuId?: number
  menuName: string
  parentId?: number
  orderNum?: number
  path: string
  component?: string
  query?: string
  routeName?: string
  isFrame?: number // 0是外链/iframe, 1否
  isCache?: number // 0缓存, 1不缓存
  menuType?: string // M目录 C菜单 F按钮
  visible?: string // 0显示 1隐藏
  status?: string // 0正常 1停用
  perms?: string
  icon?: string
  createBy?: string
  createTime?: string // 创建时间
  updateBy?: string
  updateTime?: string // 更新时间
  remark?: string
  label: string
  value: number
  children?: SysMenu[]
}

/** 后端 RouterController 返回的动态路由节点（与 RouterVo 对齐） */
export interface DynamicRouteVo {
  menuId?: number
  name?: string
  path?: string
  hidden?: boolean
  redirect?: string
  component?: string
  query?: string
  alwaysShow?: boolean
  perms?: string
  menuType?: string
  isFrame?: number
  meta?: {
    title?: string
    icon?: string
    noCache?: boolean
    link?: string
  }
  children?: DynamicRouteVo[]
}

/**
 * 获取当前用户动态路由（后端菜单树转 RouterVo，用于前端注册路由）
 */
export function fetchGetDynamicRoutes() {
  return request.get<DynamicRouteVo[]>({
    url: '/api/router/dynamic-routes'
  })
}

/**
 * 获取当前用户菜单树（SysMenu，菜单管理等场景仍可直接调用）
 */
export function fetchGetMenuList() {
  return request.get<SysMenu[]>({
    url: '/api/router/userMenuTree'
  })
}

/**
 * 获取所有菜单树（管理员使用）
 */
export function fetchGetMenuTree() {
  return request.get<SysMenu[]>({
    url: '/api/sys/menu/menuTree'
  })
}

/**
 * 获取上级菜单树
 */
export function fetchGetParentMenu() {
  return request.get<SysMenu[]>({
    url: '/api/sys/menu/getParent'
  })
}

/**
 * 新增菜单
 */
export function fetchAddMenu(data: MenuFormData) {
  return request.post({
    url: '/api/sys/menu/add',
    data
  })
}

/**
 * 修改菜单
 */
export function fetchUpdateMenu(data: MenuFormData) {
  return request.put({
    url: '/api/sys/menu',
    data
  })
}

/**
 * 删除菜单（支持单删和批量删除）
 */
export function fetchDeleteMenu(menuIds: number | number[]) {
  // 统一转换为数组格式
  const ids = Array.isArray(menuIds) ? menuIds : [menuIds]
  return request.del({
    url: '/api/sys/menu/removeMenu',
    data: { ids }
  })
}
/**
 * 根据ID获取菜单接口
 */
export function fetchGetMenuById(menuId: number) {
  return request.get<SysMenu>({
    url: `/api/sys/menu/getById/${menuId}`
  })
}
