export interface PortalMember {
  id?: number
  username?: string
  nickname?: string
  mobile?: string
  header?: string
  integration?: number
  growth?: number
  levelId?: number
  levelName?: string
  gender?: number
  sign?: string
}

export interface PortalLoginResult {
  token: string
  member: PortalMember
}

export interface PortalAuthConfig {
  passwordLogin: boolean
  smsLogin: boolean
  wechatLogin: boolean
  miniProgramLogin: boolean
  qqLogin: boolean
  smsMockEnabled: boolean
}

export interface PortalBanner {
  id?: number
  name?: string
  pic?: string
  url?: string
}

export interface PortalCategory {
  catId?: number
  name?: string
  children?: PortalCategory[]
}

export interface PortalHomeData {
  banners: PortalBanner[]
  categories: PortalCategory[]
  homeBlocks?: PortalHomeBlock[]
}

export interface PortalHomeBlock {
  code: 'new' | 'seckill' | 'budget' | 'subject'
  title?: string
  subTitle?: string
  url?: string
  refId?: number
  coverImg?: string
}

export interface PortalProductListItem {
  id?: number
  spuName?: string
  spuDescription?: string
  catalogId?: number
  brandName?: string
  price?: number
  saleCount?: number
  pic?: string
  coverImg?: string
  commentCount?: number
  avgStar?: number
}

export interface PortalProductSearchParams {
  pageNum: number
  pageSize: number
  keyword?: string
  catalogId?: number
  brandId?: number
  sort?: string
}

export interface PortalProductDetail {
  id?: number
  spuName?: string
  spuDescription?: string
  price?: number
  pic?: string
  images?: string[]
  skus?: Array<{
    skuId?: number
    skuName?: string
    price?: number
    stock?: number
    saleAttrs?: string
  }>
}

export interface PortalCartItem {
  skuId: number
  spuId?: number
  skuTitle?: string
  skuDefaultImg?: string
  price?: number
  quantity?: number
  checked?: boolean
  skuAttr?: string
  stock?: number
  valid?: boolean
}

export interface PortalCartData {
  items: PortalCartItem[]
  checkedCount: number
  checkedAmount: number
}

export interface PortalAddress {
  id?: number
  name?: string
  phone?: string
  postCode?: string
  province?: string
  city?: string
  region?: string
  detailAddress?: string
  defaultStatus?: number
}

export interface PortalAddressSavePayload {
  id?: number
  name: string
  phone: string
  postCode?: string
  province: string
  city: string
  region: string
  detailAddress: string
  defaultStatus?: number
}

export interface PortalRegion {
  id?: number
  code?: string
  parentCode?: string
  name?: string
  level?: number
  hasChildren?: boolean
}

export interface PortalOrderItem {
  skuId?: number
  spuName?: string
  skuName?: string
  skuPic?: string
  spuPic?: string
  skuPrice?: number
  skuQuantity?: number
  realAmount?: number
}

export interface PortalOrder {
  id?: number
  orderSn?: string
  status?: number
  payAmount?: number
  totalAmount?: number
  freightAmount?: number
  createTime?: string
  note?: string
  receiverName?: string
  receiverPhone?: string
  receiverProvince?: string
  receiverCity?: string
  receiverRegion?: string
  receiverDetailAddress?: string
  deliveryCompany?: string
  deliverySn?: string
  orderItemList?: PortalOrderItem[]
}

export interface PageResult<T> {
  rows: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface PortalOrderSubmitToken {
  orderToken: string
}

export interface PortalOrderPriceLine {
  skuId: number
  skuTitle?: string
  quantity: number
  lineAmount: number
  unitPrice?: number
}

export interface PortalOrderPriceTrial {
  payAmount: number
  freightAmount: number
  couponAmount: number
  promotionAmount: number
  integrationAmount: number
  useIntegration: number
  availableIntegration: number
  maxUsableIntegration: number
  freeFreight: boolean
  lines: PortalOrderPriceLine[]
}

export interface PortalOrderSubmitPayload {
  addressId: number
  useCart?: boolean
  cartSkuIds?: number[]
  items?: Array<{ skuId: number; quantity: number }>
  note?: string
  payType?: number
  couponHistoryId?: number
  useIntegration?: number
  orderToken: string
}

export interface PortalOrderSubmitResult {
  orderId: number
  orderSn: string
  status: number
}

export interface WxJsapiPayParams {
  orderSn: string
  mock: boolean
  timeStamp: string
  nonceStr: string
  packageValue: string
  signType: string
  paySign: string
}

export interface PortalHomeProduct {
  spuId?: number
  skuId?: number
  spuName?: string
  coverImg?: string
  price?: number
  promoPrice?: number
  seckillStockRemain?: number
  seckillLimit?: number
}

export interface PortalSeckillSession {
  id?: number
  startLabel?: string
  state?: 'ongoing' | 'upcoming' | 'ended'
}

export interface PortalSeckillPage {
  title?: string
  subTitle?: string
  activeSessionId?: number
  sessions?: PortalSeckillSession[]
  products?: PortalHomeProduct[]
}

export interface PortalSeckillOrderPayload {
  sessionId: number
  skuId: number
  quantity: number
  addressId: number
  orderToken: string
  payType?: number
}

export interface PortalClaimableCoupon {
  couponId: number
  couponName: string
  amount: number
  minPoint?: number
  endTime?: string
  perLimit?: number
  receivedCount?: number
  canReceive?: boolean
}

export interface PortalMyCoupon {
  historyId: number
  couponId: number
  couponName: string
  amount: number
  minPoint?: number
  status?: number
  endTime?: string
}

export interface PortalCollectItem {
  id?: number
  spuId?: number
  spuName?: string
  spuImg?: string
  price?: number
  defaultSkuId?: number
}

export interface PortalCommentSummary {
  spuId?: number
  total?: number
  avgStar?: number
}

export interface PortalComment {
  id?: number
  memberNickName?: string
  star?: number
  content?: string
  createTime?: string
}

export interface PortalPendingReview {
  spuId?: number
  skuId?: number
  spuName?: string
  coverImg?: string
  orderSn?: string
}

export interface PortalMemberCenter {
  member: PortalMember
  levelName?: string
  collectCount?: number
  orderCount?: number
  couponCount?: number
  commentCount?: number
  pendingReviewCount?: number
}

export interface PortalMemberProfilePayload {
  nickname?: string
  header?: string
  gender?: number
  sign?: string
}

export interface PortalCheckoutCoupon {
  historyId: number
  couponId: number
  couponName: string
  amount: number
  minPoint?: number
  usable?: boolean
  unusableReason?: string
  endTime?: string
}

export interface PortalSubjectDetail {
  id?: number
  name?: string
  title?: string
  subTitle?: string
  coverImg?: string
  products?: PageResult<PortalProductListItem>
}

export interface PortalMemberAuthBinding {
  authType: number
  authTypeLabel: string
  identifier: string
  maskedIdentifier: string
  bindTime?: string
}

export interface PortalImageUploadResult {
  objectName: string
  displayUrl: string
  permanentUrl?: string
}

export interface PortalBrowseRecord {
  spuId: number
  spuName?: string
  coverImg?: string
  price?: number
  viewedAt: number
}
