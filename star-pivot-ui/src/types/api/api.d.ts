/**
 * API 接口类型定义模块
 *
 * 提供所有后端接口的类型定义
 *
 * ## 主要功能
 *
 * - 通用类型（分页参数、响应结构等）
 * - 认证类型（登录、用户信息等）
 * - 系统管理类型（用户、角色等）
 * - 全局命名空间声明
 *
 * ## 使用场景
 *
 * - API 请求参数类型约束
 * - API 响应数据类型定义
 * - 接口文档类型同步
 *
 * ## 注意事项
 *
 * - 在 .vue 文件使用需要在 eslint.config.mjs 中配置 globals: { Api: 'readonly' }
 * - 使用全局命名空间，无需导入即可使用
 *
 * ## 使用方式
 *
 * ```typescript
 * const params: Api.Auth.LoginParams = { userName: 'admin', password: '123456' }
 * const response: Api.Auth.UserInfo = await fetchUserInfo()
 * ```
 *
 * @module types/api/api
 * @author Art Design Pro Team
 */

declare namespace Api {
  /** 通用类型 */
  namespace Common {
    /** 分页参数（前端内部使用，Element Plus 标准字段） */
    interface PaginationParams {
      /** 当前页码 */
      current: number
      /** 每页条数 */
      size: number
      /** 总条数 */
      total: number
    }
    /** 通用搜索参数（发送给后端的参数，使用 pageNum 和 pageSize） */
    interface CommonSearchParams {
      /** 当前页码，默认值为 1 */
      pageNum?: number
      /** 每页条数，默认值为 10 */
      pageSize?: number
    }
    /** 后端 PageResponse 标准结构（列表字段为 rows） */
    interface PageResponse<T = any> {
      rows: T[]
      total: number
      pageNum?: number
      pageSize?: number
      pageCount?: number
    }
    /**
     * 分页响应（兼容 records / rows，优先 rows）
     * @deprecated 新接口请使用 PageResponse
     */
    interface PaginatedResponse<T = any> {
      records?: T[]
      rows?: T[]
      pageNum?: number
      pageSize?: number
      pageCount?: number
      total: number
    }
    /** 启用状态 */
    type EnableStatus = '1' | '2'
  }

  /** 认证类型 */
  namespace Auth {
    /** 登录请求参数 */
    interface LoginParams {
      username: string
      password: string
      rememberPassword: boolean
      /** 验证码通过凭证（一次性 proof） */
      captchaProof: string
    }
    /** 登录响应 */
    interface LoginResponse {
      token: string
      username: string
      nickname: string
      /** 刷新令牌，用于在访问令牌过期后无感刷新 */
      refreshToken?: string
      /** 当前设备会话ID，用于会话管理 */
      deviceSessionId?: string
    }

    /** 用户信息 */
    interface UserInfo {
      user?: {
        userId: number
        username: string
        nickName: string
        avatar: string
        email: string
        phoneNumber: string
        sex: number
        status: string
        createTime: string
      }
      roles?: Array<{
        roleId: number
        roleName: string
        roleKey: string
        roleSort: number
        status: string
        createTime: string
      }>
      permissions?: Array<{
        menuId: number
        menuName: string
        parentId: number
        orderNum: number
        path: string
        component: string
        query: string
        isFrame: number
        isCache: number
        menuType: string
        visible: string
        status: string
        perms: string
        icon: string
        createTime: string
        children?: Array<any>
      }>
      /** 前端扩展：头像 URL 更新后覆盖 user.avatar，便于顶部栏立即展示 */
      avatar?: string
      /** 前端扩展：头像更新时间戳 */
      avatarUpdatedAt?: number
    }
    /** 验证码响应 */
    interface CaptchaResponse {
      /** 服务端生成的验证码 token */
      captchaToken: string
      captchaImage: string
    }

    /** 验证码校验响应 */
    interface CaptchaVerifyResponse {
      captchaProof: string
    }

    /** 注册请求参数 */
    interface RegisterParams {
      /** 用户名 */
      username: string
      /** 密码 */
      password: string
    }

    /** 注册响应结果 */
    interface RegisterResponse {
      /** 用户ID（可选，视后端实现而定） */
      userId?: number
      /** 用户名 */
      username: string
      /** 昵称（可选） */
      nickName?: string
    }

    /** 注册开关配置 */
    interface RegisterConfigResponse {
      registerEnabled: boolean
    }
    /** 设备会话信息 */
    interface DeviceSession {
      /** 设备会话ID */
      deviceSessionId: string
      /** IP地址 */
      ipaddr?: string
      /** 浏览器 */
      browser?: string
      /** 操作系统 */
      os?: string
      /** 创建时间 */
      createdAt?: string
      /** 最后访问时间 */
      lastAccessTime?: string
      /** 会话持续时间（格式化后的字符串，如"2小时30分钟"） */
      sessionDuration?: string
      /** 是否为当前会话 */
      isCurrent?: boolean
    }
  }

  /** 系统管理类型 */
  namespace SystemManage {
    /** 用户列表 */
    type UserList = Api.Common.PaginatedResponse<UserListItem>

    /** 用户列表项 */
    interface UserListItem {
      userId: number
      deptId?: number
      /** 部门名称（后端 UserVO.deptName） */
      deptName?: string
      /** 展示用：所属部门（为空时为 '-'） */
      deptNameText?: string
      avatar?: string
      status: string
      userName: string
      nickName?: string
      userType?: string
      sex: string
      phonenumber?: string
      email?: string
      password?: string
      loginIp?: string
      loginDate?: string
      pwdUpdateDate?: string
      userRoles?: string[]
      roleName?: string
      /** 角色名称列表（后端 UserVO.roleNames） */
      roleNames?: string[]
      /** 岗位名称列表（后端 UserVO.postNames） */
      postNames?: string[]
      /** 展示用：关联角色（数组拼接后字符串，空为 '-'） */
      roleNamesText?: string
      /** 展示用：所属岗位（数组拼接后字符串，空为 '-'） */
      postNamesText?: string
      createBy?: string
      createTime?: string
      updateBy?: string
      updateTime?: string
      delFlag?: number
      remark?: string
      /** 账户是否被锁定（true=已锁定，false=未锁定） */
      isLocked?: boolean
    }
    interface UserBo {
      userId: number
      deptId: number
      userName: string
      nickName: string
      email: string
      avatar: string
      password: string
      phonenumber: string
      status: string
      sex: string
      remark: string
      roleIds: string[]
      postIds: string[]
    }

    /** 用户搜索参数 */
    type UserSearchParams = Partial<
      Pick<
        UserListItem,
        'userId' | 'userName' | 'nickName' | 'sex' | 'phonenumber' | 'email' | 'status' | 'deptId'
      >
    > &
      Api.Common.CommonSearchParams

    /** 角色列表 */
    type RoleList = Api.Common.PaginatedResponse<RoleListItem>

    /** 角色列表项 */
    interface RoleListItem {
      roleId: number
      roleName: string
      roleKey: string
      roleSort: number
      dataScope: string
      menuCheckStrictly: number
      deptCheckStrictly: number
      remark: string
      status: number
      createTime: string
      delFlag: number
      createBy: string
      updateTime: string
      updateBy: string
    }
    interface RolePermissionAssignDTO {
      roleId: number
      menuIds?: number[]
      deptIds: number[]
      /** 数据范围（1全部 2自定 3本部门 4本部门及以下 5仅本人） */
      dataScope?: string
    }

    /** 角色搜索参数 */
    type RoleSearchParams = Partial<
      Pick<RoleListItem, 'roleId' | 'roleName' | 'roleKey' | 'remark' | 'status'>
    > &
      Api.Common.CommonSearchParams
  }

  namespace Post {
    /** 岗位列表 */
    type PostList = Api.Common.PaginatedResponse<PostListItem>

    interface PostListItem {
      postId: number
      postName: string
      postCode: string
      postSort: number
      status: number
      remark: string
      createTime: string
      updateTime: string
      createBy: string
      updateBy: string
    }
    type PostSearchParams = Partial<
      Pick<PostListItem, 'postId' | 'postName' | 'postCode' | 'status'>
    > &
      Api.Common.CommonSearchParams

    interface PostBo {
      postId: number
      postCode: string
      postName: string
    }
  }

  /** 代码生成类型 */
  namespace Generator {
    /** 代码生成表列表响应 */
    interface GenTableList {
      total: number
      rows: GenTableListItem[]
      pageNum: number
      pageSize: number
      pageCount: number
    }

    /** 代码生成表列表项 */
    interface GenTableListItem {
      tableId: number
      tableName: string
      tableComment: string
      subTableName?: string
      subTableFkName?: string
      className: string
      tplCategory?: string
      tplWebType?: string
      packageName?: string
      moduleName?: string
      businessName?: string
      functionName?: string
      functionAuthor?: string
      genType?: string
      genPath?: string
      options?: string
      createTime?: string
      updateTime?: string
      createBy?: string
      updateBy?: string
      remark?: string
    }

    /** 代码生成表搜索参数 */
    type GenTableSearchParams = Partial<
      Pick<GenTableListItem, 'tableName' | 'tableComment' | 'className'>
    > &
      Api.Common.CommonSearchParams

    /** 操作日志列表 */
    type OperLogList = Api.Common.PaginatedResponse<OperLogListItem>

    /** 操作日志列表项 */
    interface OperLogListItem {
      operId: number
      title?: string
      businessType?: number
      method?: string
      requestMethod?: string
      operatorType?: number
      operName?: string
      deptName?: string
      operUrl?: string
      operIp?: string
      operLocation?: string
      operParam?: string
      jsonResult?: string
      status?: number
      errorMsg?: string
      operTime?: string
      costTime?: number
    }

    /** 操作日志搜索参数 */
    type OperLogSearchParams = Partial<
      Pick<OperLogListItem, 'title' | 'businessType' | 'operName' | 'status'>
    > &
      Api.Common.CommonSearchParams & {
        startTime?: string
        endTime?: string
      }
  }

}
