import request from '@/utils/http'

/**
 * 系统用户实体类型
 */
export interface User {
  id?: number
  username?: String
  password?: String
  realName?: String
  headUrl?: String
  gender?: Integer
  email?: String
  mobile?: String
  deptId?: Long
  superAdmin?: Integer
  status?: Integer
  creator?: Long
  createDate?: LocalDateTime
  updater?: Long
  updateDate?: LocalDateTime
}

/**
 * 系统用户搜索参数
 */
export interface UserSearchParams {
  pageNum?: number
  pageSize?: number
  username?: String
  password?: String
  realName?: String
  headUrl?: String
  gender?: Integer
  email?: String
  mobile?: String
  deptId?: Long
  superAdmin?: Integer
  status?: Integer
  creator?: Long
  createDate?: LocalDateTime
  updater?: Long
  updateDate?: LocalDateTime
}

/**
 * 获取系统用户列表（分页）
 */
export function fetchGetUserList(params: UserSearchParams) {
  return request.post<Api.Common.PageResponse<User>>({
    url: '/api/demo/user/userPageList',
    data: params
  })
}

/**
 * 根据ID获取系统用户详情
 */
export function fetchGetUserById(id: number) {
  return request.get<User>({
    url: `/api/demo/user/\${id}`
  })
}

/**
 * 新增系统用户
 */
export function fetchAddUser(data: User) {
  return request.post({
    url: '/api/demo/user',
    data
  })
}

/**
 * 修改系统用户
 */
export function fetchUpdateUser(data: User) {
  return request.put({
    url: '/api/demo/user',
    data
  })
}

/**
 * 删除系统用户（支持单删和批量删除，请求体 ids）
 */
export function fetchDeleteUser(ids: number[]) {
  return request.del({
    url: '/api/demo/user/removeUser',
    data: { ids: ids }
  })
}
