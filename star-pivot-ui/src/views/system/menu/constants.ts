/**
 * 菜单管理页面常量配置
 */

/**
 * 菜单类型配置
 */
export const MENU_TYPE_CONFIG = {
  M: { text: '目录', color: 'info' as const },
  C: { text: '菜单', color: 'primary' as const },
  F: { text: '按钮', color: 'danger' as const }
} as const

/**
 * 状态配置
 */
export const STATUS_CONFIG = {
  '0': { text: '正常', type: 'success' as const },
  '1': { text: '停用', type: 'danger' as const }
} as const

/**
 * 初始搜索状态
 */
export const INITIAL_SEARCH_STATE = {
  menuName: '',
  route: '',
  perms: '',
  status: ''
} as const
