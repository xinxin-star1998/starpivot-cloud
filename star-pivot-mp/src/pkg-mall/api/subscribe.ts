import {request} from '@/utils/request'

export interface PortalSubscribeTemplates {
  enabled: boolean
  templateIds: string[]
  paySuccessTemplateId?: string
  deliverTemplateId?: string
  pendingReviewTemplateId?: string
}

export function fetchSubscribeTemplates() {
  return request<PortalSubscribeTemplates>({
    url: '/portal/subscribe/templates'
  })
}
