import request from '@/utils/http'

/**
 * 获取角色列表（分页）
 */
export function fetchGetRoleList(params: Api.SystemManage.RoleSearchParams) {
  return request.post<Api.SystemManage.RoleList>({
    url: '/api/sys/role/list',
    data: params
  })
}

/**
 * 新增角色
 */
export function fetchAddRole(data: Api.SystemManage.RoleListItem) {
  return request.post<Api.SystemManage.RoleListItem>({
    url: '/api/sys/role/addRole',
    data,
    showSuccessMessage: true
  })
}

/**
 * 修改角色
 */
export function fetchUpdateRole(data: Api.SystemManage.RoleListItem) {
  return request.put<Api.SystemManage.RoleListItem>({
    url: '/api/sys/role/updateRole',
    data,
    showSuccessMessage: true
  })
}

/**
 * 删除角色（支持单删和批量删除）
 */
export function fetchDeleteRole(roleIds: number[]) {
  return request.del({
    url: '/api/sys/role/delete',
    data: { ids: roleIds },
    showSuccessMessage: true
  })
}

/**
 * 修改角色状态
 */
export function fetchChangeRoleStatus(data: Api.SystemManage.RoleListItem) {
  return request.put({
    url: '/api/sys/role/changeStatus',
    data,
    showSuccessMessage: true
  })
}

/**
 * 获取角色详情
 */
export function fetchGetRoleById(roleId: number) {
  return request.get<Api.SystemManage.RoleListItem>({
    url: `/api/sys/role/${roleId}`
  })
}

/**
 * 获取角色下拉列表
 */
export function fetchGetRoleSelect() {
  return request.get<Api.SystemManage.RoleListItem[]>({
    url: '/api/sys/role/select'
  })
}

/**
 * 分配菜单权限 和 部门权限
 */
export function fetchAssignPermission(data: Api.SystemManage.RolePermissionAssignDTO) {
  return request.post({
    url: '/api/sys/role/assignPermission',
    data,
    showSuccessMessage: true
  })
}
/**
 * 根据角色ID获取已分配的菜单列表
 */
export function fetchGetRoleMenus(roleId: number) {
  return request.get<number[]>({
    url: `/api/sys/role/getMenuIdsByRoleId/${roleId}`
  })
}
/**
 * 根据角色ID获取已分配的部门ID列表
 */
export function fetchGetRoleDeptIds(roleId: number) {
  return request.get<number[]>({
    url: `/api/sys/role/${roleId}/deptIds`
  })
}
type AssignUserReqBo = {
  roleId: string | number
  userName?: string
  phonenumber?: string
  pageNum?: number
  pageSize?: number
}
/**
 * 当前角色ID已分配的用户列表（分页）
 */
export function fetchGetUserListByRoleId(data: AssignUserReqBo) {
  return request.post<Api.SystemManage.UserList>({
    url: '/api/sys/role/allocatedList',
    data
  })
}
//当前角色id未分配的用户 分页 unallocatedList
export function fetchGetUserListNotInByRoleId(data: AssignUserReqBo) {
  return request.post<Api.SystemManage.UserList>({
    url: '/api/sys/role/unallocatedList',
    data
  })
}
type UserRoleDTO = {
  roleId: string | number
  userIds: string[] | number[]
}
/**
 * 分配用户
 */
export function fetchAssignUser(data: UserRoleDTO) {
  return request.post({
    url: '/api/sys/role/assignUser',
    data,
    showSuccessMessage: true
  })
}

type UnassignUserDTO = {
  userId: number
  roleId: string | number
}

/**
 * 取消用户授权
 */
export function fetchCancelUser(data: UnassignUserDTO) {
  return request.del({
    url: '/api/sys/role/cancelUser',
    data,
    showSuccessMessage: true
  })
}
