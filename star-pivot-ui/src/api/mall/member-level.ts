import request from '@/utils/http'

export interface MemberLevelVo {
  id?: number
  name?: string
  growthPoint?: number
  defaultStatus?: number
  freeFreightPoint?: number
  commentGrowthPoint?: number
  privilegeFreeFreight?: number
  privilegeMemberPrice?: number
  privilegeBirthday?: number
  note?: string
}

export interface MemberLevelSavePayload {
  id?: number
  name: string
  growthPoint?: number
  defaultStatus?: number
  freeFreightPoint?: number
  commentGrowthPoint?: number
  privilegeFreeFreight?: number
  privilegeMemberPrice?: number
  privilegeBirthday?: number
  note?: string
}

export function fetchMemberLevelList() {
  return request.get<MemberLevelVo[]>({
    url: '/api/mall/member-level/list'
  })
}

/** 适配 useTable 的分页包装（后端返回全量列表） */
export async function fetchMemberLevelPage(params: Api.Common.CommonSearchParams) {
  const rows = await fetchMemberLevelList()
  return {
    rows,
    total: rows.length,
    pageNum: params.pageNum ?? 1,
    pageSize: params.pageSize ?? (rows.length || 10),
    pageCount: 1
  }
}

export function fetchMemberLevelById(id: number) {
  return request.get<MemberLevelVo>({
    url: `/api/mall/member-level/${id}`
  })
}

export function fetchMemberLevelAdd(data: MemberLevelSavePayload) {
  return request.post<void>({
    url: '/api/mall/member-level',
    data,
    showSuccessMessage: true
  })
}

export function fetchMemberLevelUpdate(data: MemberLevelSavePayload) {
  return request.put<void>({
    url: '/api/mall/member-level',
    data,
    showSuccessMessage: true
  })
}

export function fetchMemberLevelRemove(ids: number[]) {
  return request.del<void>({
    url: '/api/mall/member-level/remove',
    data: { ids },
    showSuccessMessage: true
  })
}

export const PRIVILEGE_MAP: Record<number, string> = {
  0: '否',
  1: '是'
}
