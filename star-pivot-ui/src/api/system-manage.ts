// import request from '@/utils/http'
// // 获取用户列表
// export function fetchGetUserList(params: Api.SystemManage.UserSearchParams) {
//   return request.post<Api.SystemManage.UserList>({
//     url: '/api/sys/user/pageList',
//     params
//   })
// }
//
// // 获取角色列表
// export function fetchGetRoleList(params: Api.SystemManage.RoleSearchParams) {
//   return request.post<Api.SystemManage.RoleList>({
//     url: '/api/sys/role/list',
//     params
//   })
// }
//
// // 后端菜单实体类型
// export interface SysMenu {
//   menuId?: number
//   menuName: string
//   parentId?: number
//   orderNum?: number
//   path: string
//   component?: string
//   query?: string
//   routeName?: string
//   isFrame?: number // 0是外链/iframe, 1否
//   isCache?: number // 0缓存, 1不缓存
//   menuType?: string // M目录 C菜单 F按钮
//   visible?: string // 0显示 1隐藏
//   status?: string // 0正常 1停用
//   perms?: string
//   icon?: string
//   children?: SysMenu[]
// }
//
// // 获取菜单列表（从后端获取）
// export function fetchGetMenuList() {
//   return request.get<SysMenu[]>({
//     url: '/api/sys/menu/userMenuTree'
//   })
// }
