import request from '@/utils/http'

/**
 * 岗位实体类型
 */
export interface SysPost {
  postId?: number
  postCode: string
  postName: string
  postSort?: number
  status?: string // 0正常 1停用
  createTime?: string
  updateTime?: string
  remark?: string
  label?: string
  value?: number
}

/**
 * 获取岗位列表（分页）
 */
export function fetchGetPostList(params: Api.Post.PostSearchParams) {
  return request.post<Api.Post.PostList>({
    url: '/api/sys/post/postPageList',
    data: params
  })
}

/**
 * 获取所有岗位列表（不分页）
 */
export function fetchGetAllPosts() {
  return request.get<SysPost[]>({
    url: '/api/sys/post/all'
  })
}

/**
 * 根据ID获取岗位详情
 */
export function fetchGetPostById(postId: number) {
  return request.get<SysPost>({
    url: `/api/sys/post/${postId}`
  })
}

/**
 * 新增岗位
 */
export function fetchAddPost(data: SysPost) {
  return request.post({
    url: '/api/sys/post',
    data
  })
}

/**
 * 修改岗位
 */
export function fetchUpdatePost(data: SysPost) {
  return request.put({
    url: '/api/sys/post',
    data
  })
}

/**
 * 删除岗位（支持单删和批量删除）
 */
export function fetchDeletePost(postIds: number[]) {
  return request.del({
    url: '/api/sys/post/removePost',
    data: { ids: postIds }
  })
}

/**
 * 下拉岗位列表
 */
export function fetchGetPostSelect() {
  return request.get<Api.Post.PostBo>({
    url: '/api/sys/post/simpleList'
  })
}
