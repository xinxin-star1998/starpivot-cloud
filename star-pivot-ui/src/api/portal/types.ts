/** C 端会员 */
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
  email?: string
  sign?: string
}

export interface PortalLoginResult {
  token: string
  member: PortalMember
}

export interface PortalRegisterPayload {
  username: string
  password: string
  nickname?: string
  mobile?: string
}

export interface PortalLoginPayload {
  account: string
  password: string
}

export interface PortalAuthConfig {
  passwordLogin: boolean
  smsLogin: boolean
  wechatLogin: boolean
  qqLogin: boolean
  smsMockEnabled: boolean
  captchaRequired: boolean
}

export interface PortalSmsSendPayload {
  mobile: string
  scene: 'login' | 'register' | 'bind' | 'set_password' | 'unbind'
}

export interface PortalSmsLoginPayload {
  mobile: string
  code: string
}

export interface PortalMemberAuthBinding {
  authType: number
  authTypeLabel: string
  identifier: string
  maskedIdentifier: string
  bindTime?: string
}

/** 首页轮播（复用后台字段） */
export interface PortalBanner {
  id?: number
  name?: string
  pic?: string
  url?: string
  sort?: number
  status?: number
}

/** 分类树节点 */
export interface PortalCategory {
  catId?: number
  name?: string
  parentCid?: number
  catLevel?: number
  showStatus?: number
  sort?: number
  icon?: string
  children?: PortalCategory[]
}

/** 首页品牌简要信息 */
export interface PortalBrandBrief {
  brandId?: number
  name?: string
  logo?: string
}

/** 首页分类热门 */
export interface PortalHotCategory {
  id?: number
  catId?: number
  catName?: string
  title?: string
  icon?: string
  url?: string
  sort?: number
}

export interface PortalHomeData {
  banners: PortalBanner[]
  categories: PortalCategory[]
  categoryBrands?: Record<number, PortalBrandBrief[]>
  hotCategories?: PortalHotCategory[]
  homeBlocks?: PortalHomeBlock[]
}

/** 首页营销商品简要 */
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

/** 秒杀场次 */
export interface PortalSeckillSession {
  id?: number
  name?: string
  startLabel?: string
  endLabel?: string
  state?: 'ongoing' | 'upcoming' | 'ended'
  products?: PortalHomeProduct[]
}

/** 秒杀页 */
export interface PortalSeckillPage {
  title?: string
  subTitle?: string
  promotionId?: number
  activeSessionId?: number
  sessions?: PortalSeckillSession[]
  products?: PortalHomeProduct[]
}

/** 秒杀下单 */
export interface PortalSeckillOrderPayload {
  sessionId: number
  skuId: number
  quantity: number
  addressId: number
  orderToken: string
  payType?: number
  note?: string
}

/** 首页营销模块 */
export interface PortalHomeBlock {
  code: 'new' | 'seckill' | 'budget' | 'subject'
  title?: string
  subTitle?: string
  url?: string
  refId?: number
  coverImg?: string
  products?: PortalHomeProduct[]
  sessions?: PortalSeckillSession[]
  activeSessionId?: number
}

/** 商品列表项 */
export interface PortalProductListItem {
  id?: number
  spuName?: string
  spuDescription?: string
  catalogId?: number
  brandId?: number
  brandName?: string
  price?: number
  coverImg?: string
  createTime?: string
  commentCount?: number
  avgStar?: number
}

export interface PortalProductSearchParams extends Api.Common.CommonSearchParams {
  keyword?: string
  catalogId?: number
  brandId?: number
  /** default | priceAsc | priceDesc | newest */
  sort?: string
}

/** 专题活动详情 */
export interface PortalSubjectDetail {
  id?: number
  name?: string
  title?: string
  subTitle?: string
  coverImg?: string
  url?: string
  products?: Api.Common.PaginatedResponse<PortalProductListItem>
}

/** 商品详情（继承 SPU 结构） */
export interface PortalProductDetail extends PortalProductListItem {
  weight?: number
  publishStatus?: number
  decript?: string[]
  images?: string[]
  baseAttrs?: Array<{ attrId: number; attrName?: string; attrValues: string; showDesc: number }>
  skus?: Array<{
    skuId?: number
    attr: Array<{ attrId: number; attrName: string; attrValue: string }>
    skuName: string
    price: number
    skuTitle?: string
    skuSubtitle?: string
    images?: Array<{ imgUrl: string; defaultImg: number }>
    availableStock?: number
  }>
}

/** 购物车 */
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

export interface PortalCart {
  items: PortalCartItem[]
  checkedCount?: number
  checkedAmount?: number
}

export interface PortalCartAddPayload {
  skuId: number
  quantity: number
}

export interface PortalCartUpdatePayload {
  skuId: number
  quantity: number
  checked?: boolean
}

/** 收货地址 */
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

/** 省市区 */
export interface PortalRegion {
  id?: number
  code?: string
  parentCode?: string
  name?: string
  level?: number
  hasChildren?: boolean
}

/** 订单 */
export interface PortalOrderItem {
  id?: number
  orderId?: number
  spuId?: number
  spuName?: string
  spuPic?: string
  skuId?: number
  skuName?: string
  skuPic?: string
  skuPrice?: number
  skuQuantity?: number
  skuAttrsVals?: string
  realAmount?: number
}

export interface PortalOrder {
  id?: number
  orderSn?: string
  status?: number
  totalAmount?: number
  payAmount?: number
  freightAmount?: number
  receiverName?: string
  receiverPhone?: string
  receiverProvince?: string
  receiverCity?: string
  receiverRegion?: string
  receiverDetailAddress?: string
  note?: string
  deliveryCompany?: string
  deliverySn?: string
  createTime?: string
  orderItemList?: PortalOrderItem[]
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

export interface PortalOrderSubmitToken {
  orderToken: string
}

export interface PortalOrderPriceTrialPayload {
  useCart?: boolean
  cartSkuIds?: number[]
  items?: Array<{ skuId: number; quantity: number }>
  couponHistoryId?: number
  useIntegration?: number
}

export interface PortalOrderPriceLine {
  skuId: number
  skuTitle?: string
  quantity: number
  originalUnitPrice: number
  unitPrice: number
  lineOriginalAmount: number
  promotionAmount: number
  lineAmount: number
}

export interface PortalOrderPriceTrial {
  originalAmount: number
  promotionAmount: number
  merchandiseAmount: number
  couponAmount: number
  integrationAmount: number
  useIntegration: number
  availableIntegration: number
  maxUsableIntegration: number
  freightAmount: number
  freeFreight: boolean
  payAmount: number
  lines: PortalOrderPriceLine[]
}

export interface PortalMemberCoupon {
  historyId: number
  couponId: number
  couponName: string
  amount: number
  minPoint?: number
  useType?: number
  endTime?: string
}

export interface PortalCheckoutCoupon extends PortalMemberCoupon {
  startTime?: string
  usable?: boolean
  unusableReason?: string
}

export interface PortalClaimableCoupon {
  couponId: number
  couponName: string
  amount: number
  minPoint?: number
  useType?: number
  startTime?: string
  endTime?: string
  enableEndTime?: string
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
  useType?: number
  status?: number
  endTime?: string
  createTime?: string
  useTime?: string
}

export interface PortalOrderSubmitResult {
  orderId: number
  orderSn: string
  status: number
}

export interface PortalOrderQueryParams extends Api.Common.CommonSearchParams {
  status?: number
}

/** 会员中心概览 */
export interface PortalMemberCenter {
  member: PortalMember
  levelName?: string
  collectCount?: number
  orderCount?: number
  couponCount?: number
  commentCount?: number
  pendingReviewCount?: number
}

export interface PortalCommentSummary {
  spuId?: number
  total?: number
  avgStar?: number
}

export interface PortalPendingReview {
  spuId?: number
  skuId?: number
  spuName?: string
  coverImg?: string
  orderSn?: string
}

export interface PortalMemberProfilePayload {
  nickname?: string
  header?: string
  gender?: number
  sign?: string
}

/** 收藏 */
export interface PortalCollectItem {
  id?: number
  spuId?: number
  spuName?: string
  spuImg?: string
  price?: number
  publishStatus?: number
  defaultSkuId?: number
  createTime?: string
}

/** 商品评价 */
export interface PortalCommentReply {
  id?: number
  memberNickName?: string
  memberIcon?: string
  content?: string
  createTime?: string
}

export interface PortalComment {
  id?: number
  spuId?: number
  spuName?: string
  skuId?: number
  memberNickName?: string
  memberIcon?: string
  star?: number
  content?: string
  resources?: string
  spuAttributes?: string
  likesCount?: number
  replyCount?: number
  createTime?: string
  replies?: PortalCommentReply[]
}

export interface PortalCommentSubmitPayload {
  spuId: number
  skuId: number
  star: number
  content: string
  resources?: string
}

export interface PortalCommentQueryParams extends Api.Common.CommonSearchParams {
  spuId: number
}
