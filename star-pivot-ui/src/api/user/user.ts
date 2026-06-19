import request from '@/utils/http'
import {
  fetchExcelExport,
  fetchExcelImport,
  fetchExcelTemplate,
  type ExcelImportResultVo
} from '@/api/common/excel'

/**
 * 获取用户列表（分页）
 */
export function fetchGetUserList(params: Api.SystemManage.UserSearchParams) {
  return request.post<Api.SystemManage.UserList>({
    url: '/api/sys/user/pageList',
    params
  })
}

/**
 * 根据ID获取用户详情
 */
export function fetchGetUserById(userId: number) {
  return request.get<Api.SystemManage.UserListItem>({
    url: `/api/sys/user/${userId}`
  })
}

/**
 * 新增用户
 */
export function fetchAddUser(data: Api.SystemManage.UserBo) {
  return request.post({
    url: '/api/sys/user/add',
    data
  })
}

/**
 * 修改用户
 */
export function fetchUpdateUser(data: Api.SystemManage.UserListItem) {
  return request.post({
    url: '/api/sys/user/update',
    data
  })
}

/**
 * 删除用户（支持单删和批量删除）
 */
export function fetchDeleteUser(userIds: number[]) {
  return request.del({
    url: '/api/sys/user/delete',
    data: { ids: userIds }
  })
}

/**
 * 修改用户状态
 */
export function fetchUpdateUserStatus(userId: number, status: number) {
  return request.post({
    url: '/api/sys/user/changeStatus',
    data: {
      userId,
      status
    }
  })
}
/**
 * 重置用户密码
 */
export function fetchResetUserPassword(userId: number, password: string) {
  return request.post({
    url: '/api/sys/user/resetPwd',
    data: {
      userId,
      password
    }
  })
}

/**
 * 当前用户修改密码
 */
export function fetchUpdateUserPassword(data: { oldPassword: string; newPassword: string }) {
  return request.post({
    url: '/api/sys/user/updatePwd',
    data
  })
}

/**
 * 上传头像
 */
export function fetchUploadAvatar(data: FormData) {
  return request.post({
    url: '/api/avatar/upload',
    data,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 删除头像
 */
export function fetchDeleteAvatar(userId: string) {
  return request.del({
    url: '/api/avatar/delete',
    params: {
      userId
    }
  })
}

/**
 * 获取头像临时访问链接
 */
export function fetchGetAvatarPresignedUrl(filePath: string) {
  return request.get({
    url: '/api/avatar/presigned-url',
    params: {
      filePath
    }
  })
}

/**
 * 管理员解锁账户
 * 解除因登录失败次数过多而被锁定的账户
 */
export function fetchUnlockUser(userId: number) {
  return request.post({
    url: `/api/sys/user/unlock/${userId}`
  })
}

/** EasyExcel 导出用户 */
export function fetchExportUser(params: Api.SystemManage.UserSearchParams) {
  return fetchExcelExport({
    url: '/api/sys/user/export',
    data: params as Record<string, unknown>,
    filenameFallback: `user_export_${Date.now()}.xlsx`,
    successMessage: false
  })
}

/** EasyExcel 导入用户 */
export function fetchImportUserExcel(file: File, updateSupport = false) {
  return fetchExcelImport<ExcelImportResultVo>({
    url: '/api/sys/user/import',
    file,
    updateSupport
  })
}

/** EasyExcel 下载用户导入模板 */
export function fetchDownloadUserImportTemplate() {
  return fetchExcelTemplate({
    url: '/api/sys/user/importTemplate',
    filenameFallback: 'user_import_template.xlsx',
    successMessage: false
  })
}
