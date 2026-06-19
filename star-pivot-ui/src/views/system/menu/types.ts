/**
 * 菜单管理相关的类型定义
 */

/**
 * 菜单表单数据类型（与后端MenuDTO对应）
 */
export interface MenuFormData {
  menuId?: number
  menuName: string
  parentId?: number
  orderNum?: number
  path?: string
  component?: string
  query?: string
  routeName?: string
  isFrame?: number // 0是外链/iframe, 1否
  isCache?: number // 0缓存, 1不缓存
  menuType: 'M' | 'C' | 'F' // M目录 C菜单 F按钮
  visible?: string // 0显示 1隐藏
  status?: string // 0正常 1停用
  perms?: string
  icon?: string
  remark?: string
}
