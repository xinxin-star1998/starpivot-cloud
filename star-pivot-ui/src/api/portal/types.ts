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

export interface PortalHomeData {
  banners: PortalBanner[]
  categories: PortalCategory[]
  categoryBrands?: Record<number, PortalBrandBrief[]>
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

/** 首页营销模块 */
export interface PortalHomeBlock {
  code: 'new' | 'seckill' | 'budget' | 'subject'
  title?: string
  subTitle?: string
  url?: string
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
}

export interface PortalProductSearchParams extends Api.Common.CommonSearchParams {
  keyword?: string
  catalogId?: number
  brandId?: number
  /** default | priceAsc | priceDesc | newest */
  sort?: string
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
