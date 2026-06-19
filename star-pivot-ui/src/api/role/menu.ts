import request from '@/utils/http'

/**
 * 菜单管理实体类型
 */
export interface Menu {
  id?: number
  pid?: Long
  name?: String
  url?: String
  permissions?: String
  menuType?: Integer
  icon?: String
  sort?: Long
  creator?: Long
  createDate?: LocalDateTime
  updater?: Long
  updateDate?: LocalDateTime
}

/**
 * 菜单管理搜索参数
 */
export interface MenuSearchParams {
  pid?: Long
  name?: String
  url?: String
  permissions?: String
  menuType?: Integer
  icon?: String
  sort?: Long
  creator?: Long
  createDate?: LocalDateTime
  updater?: Long
  updateDate?: LocalDateTime
}

/**
 * 获取菜单管理列表（树形，不分页）
 */
export function fetchGetMenuList(params: MenuSearchParams) {
  return request.post<Menu[]>({
    url: '/api/role/menu/list',
    data: params
  })
}

/**
 * 根据ID获取菜单管理详情
 */
export function fetchGetMenuById(id: number) {
  return request.get<Menu>({
    url: `/api/role/menu/\${id}`
  })
}

/**
 * 新增菜单管理
 */
export function fetchAddMenu(data: Menu) {
  return request.post({
    url: '/api/role/menu',
    data
  })
}

/**
 * 修改菜单管理
 */
export function fetchUpdateMenu(data: Menu) {
  return request.put({
    url: '/api/role/menu',
    data
  })
}

/**
 * 删除菜单管理（支持单删和批量删除，请求体 ids）
 */
export function fetchDeleteMenu(ids: number[]) {
  return request.del({
    url: '/api/role/menu/delete',
    data: { ids: ids }
  })
}
