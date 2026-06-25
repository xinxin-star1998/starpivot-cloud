<!-- C 端结算页 -->
<template>
  <div v-loading="loading" class="portal-checkout">
    <h1 class="page-title">确认订单</h1>

    <section class="panel">
      <h3>收货地址</h3>
      <div v-if="addresses.length" class="address-list">
        <div
          v-for="addr in addresses"
          :key="addr.id"
          class="address-card"
          :class="{ active: selectedAddressId === addr.id }"
          @click="selectedAddressId = addr.id"
        >
          <p class="address-card__user">{{ addr.name }} {{ addr.phone }}</p>
          <p class="address-card__detail">
            {{ addr.province }}{{ addr.city }}{{ addr.region }}{{ addr.detailAddress }}
          </p>
          <ElTag v-if="addr.defaultStatus === 1" size="small" type="danger">默认</ElTag>
        </div>
      </div>
      <ElEmpty v-else description="暂无收货地址" :image-size="80" />
      <ElButton type="primary" link @click="addressDialogVisible = true">+ 新增地址</ElButton>
    </section>

    <section class="panel">
      <h3>优惠券</h3>
      <div v-if="checkoutCoupons.length" class="coupon-row">
        <ElSelect
          v-model="selectedCouponHistoryId"
          clearable
          placeholder="选择优惠券（可不选）"
          style="width: 100%"
          @change="onCouponChange"
        >
          <ElOption
            v-for="c in checkoutCoupons"
            :key="c.historyId"
            :disabled="!c.usable"
            :label="couponOptionLabel(c)"
            :value="c.historyId"
          />
        </ElSelect>
        <p v-if="pendingCouponHint" class="coupon-hint">{{ pendingCouponHint }}</p>
      </div>
      <p v-else class="coupon-empty">暂无可用优惠券</p>
    </section>

    <section class="panel">
      <h3>商品清单</h3>
      <div v-for="item in checkoutItems" :key="item.skuId" class="checkout-item">
        <span class="checkout-item__name">{{ item.skuTitle }}</span>
        <span class="checkout-item__qty">x{{ item.quantity }}</span>
        <span class="checkout-item__price"
          >¥{{ formatPrice((item.price || 0) * (item.quantity || 0)) }}</span
        >
      </div>
      <div class="checkout-total">
        <p v-if="couponDiscount > 0" class="checkout-discount"
          >优惠券：-¥{{ formatPrice(couponDiscount) }}</p
        >
        应付：<strong>¥{{ formatPrice(payAmount) }}</strong>
      </div>
    </section>

    <div class="submit-bar">
      <ElButton
        type="danger"
        size="large"
        :loading="submitting"
        :disabled="!canSubmit"
        @click="handleSubmit"
      >
        提交订单
      </ElButton>
    </div>

    <ElDialog v-model="addressDialogVisible" title="新增收货地址" width="520px" destroy-on-close>
      <ElForm ref="formRef" :model="addressForm" :rules="addressRules" label-width="88px">
        <ElFormItem label="收货人" prop="name">
          <ElInput v-model="addressForm.name" />
        </ElFormItem>
        <ElFormItem label="手机号" prop="phone">
          <ElInput v-model="addressForm.phone" />
        </ElFormItem>
        <ElFormItem label="省份" prop="province">
          <ElSelect v-model="regionCodes[0]" placeholder="省" @change="onProvinceChange">
            <ElOption v-for="p in provinces" :key="p.code" :label="p.name" :value="p.code!" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="城市" prop="city">
          <ElSelect
            v-model="regionCodes[1]"
            placeholder="市"
            :disabled="!cities.length"
            @change="onCityChange"
          >
            <ElOption v-for="c in cities" :key="c.code" :label="c.name" :value="c.code!" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="区/县" prop="region">
          <ElSelect
            v-model="regionCodes[2]"
            placeholder="区/县"
            :disabled="!districts.length"
            @change="onDistrictChange"
          >
            <ElOption v-for="d in districts" :key="d.code" :label="d.name" :value="d.code!" />
          </ElSelect>
        </ElFormItem>
        <ElFormItem label="详细地址" prop="detailAddress">
          <ElInput v-model="addressForm.detailAddress" type="textarea" :rows="2" />
        </ElFormItem>
        <ElFormItem label="默认地址">
          <ElSwitch v-model="addressForm.defaultStatus" :active-value="1" :inactive-value="0" />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="addressDialogVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="savingAddress" @click="saveAddress">保存</ElButton>
      </template>
    </ElDialog>

    <ElDialog
      v-model="payDialogVisible"
      title="订单提交成功"
      width="400px"
      :close-on-click-modal="false"
    >
      <p>订单号：{{ lastOrderSn }}</p>
      <p v-if="alipayEnabled">请使用支付宝完成支付</p>
      <p v-else>请完成支付（开发环境可使用 Mock 支付）</p>
      <template #footer>
        <ElButton @click="router.push('/portal')">返回首页</ElButton>
        <ElButton v-if="alipayEnabled" :loading="paying" type="primary" @click="handleAlipayPay">
          支付宝支付
        </ElButton>
        <ElButton v-else :loading="paying" type="primary" @click="handleMockPay"
          >Mock 支付</ElButton
        >
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import type { FormInstance, FormRules } from 'element-plus'
  import { ElMessage } from 'element-plus'
  import { fetchPortalAddressList, fetchPortalAddressSave } from '@/api/portal/address'
  import { fetchPortalCart } from '@/api/portal/cart'
  import { fetchPortalCouponCheckout } from '@/api/portal/coupon'
  import { fetchPortalOrderMockPay, fetchPortalOrderSubmit } from '@/api/portal/order'
  import { fetchPortalAlipayEnabled, fetchPortalAlipayPay } from '@/api/portal/pay'
  import { fetchPortalRegionChildren } from '@/api/portal/region'
  import type {
    PortalAddress,
    PortalCartItem,
    PortalCheckoutCoupon,
    PortalRegion
  } from '@/api/portal/types'
  import { usePortalAuth } from '@/hooks/portal/usePortalAuth'
  import { notifyPortalCartChanged } from '@/utils/portal/cart-event'
  import { submitAlipayPayForm } from '@/utils/portal/alipay-pay'

  defineOptions({ name: 'PortalCheckout' })

  const route = useRoute()
  const router = useRouter()
  const { requireLogin } = usePortalAuth()

  const loading = ref(true)
  const submitting = ref(false)
  const paying = ref(false)
  const savingAddress = ref(false)
  const addresses = ref<PortalAddress[]>([])
  const checkoutItems = ref<PortalCartItem[]>([])
  const selectedAddressId = ref<number>()
  const addressDialogVisible = ref(false)
  const payDialogVisible = ref(false)
  const lastOrderId = ref<number>()
  const lastOrderSn = ref('')
  const alipayEnabled = ref(false)
  const checkoutCoupons = ref<PortalCheckoutCoupon[]>([])
  const selectedCouponHistoryId = ref<number>()

  const formRef = ref<FormInstance>()
  const addressForm = reactive({
    name: '',
    phone: '',
    province: '',
    city: '',
    region: '',
    detailAddress: '',
    defaultStatus: 0 as 0 | 1
  })
  const addressRules: FormRules = {
    name: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
    province: [{ required: true, message: '请选择省份', trigger: 'change' }],
    city: [{ required: true, message: '请选择城市', trigger: 'change' }],
    region: [{ required: true, message: '请选择区县', trigger: 'change' }],
    detailAddress: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
  }

  const regionCodes = ref<[string, string, string]>(['', '', ''])
  const provinces = ref<PortalRegion[]>([])
  const cities = ref<PortalRegion[]>([])
  const districts = ref<PortalRegion[]>([])

  const totalAmount = computed(() =>
    checkoutItems.value.reduce((sum, item) => sum + (item.price || 0) * (item.quantity || 0), 0)
  )

  const selectedCoupon = computed(() =>
    checkoutCoupons.value.find((c) => c.historyId === selectedCouponHistoryId.value)
  )

  const pendingCouponHint = computed(() => {
    const pending = checkoutCoupons.value.find((c) => !c.usable && c.unusableReason)
    return pending?.unusableReason || ''
  })

  const couponOptionLabel = (coupon: PortalCheckoutCoupon) => {
    const base = `${coupon.couponName} -¥${formatPrice(coupon.amount)}（满${formatPrice(coupon.minPoint || 0)}可用）`
    if (coupon.usable) return base
    return `${base} · ${coupon.unusableReason || '不可用'}`
  }

  const couponDiscount = computed(() => {
    const coupon = selectedCoupon.value
    if (!coupon || !coupon.usable) return 0
    const min = coupon.minPoint || 0
    if (totalAmount.value < min) return 0
    return Math.min(coupon.amount || 0, totalAmount.value)
  })

  const payAmount = computed(() => Math.max(totalAmount.value - couponDiscount.value, 0))
  const canSubmit = computed(
    () => !!selectedAddressId.value && checkoutItems.value.length > 0 && !submitting.value
  )

  const formatPrice = (p: number) => p.toFixed(2)

  async function loadCoupons() {
    if (!checkoutItems.value.length) {
      checkoutCoupons.value = []
      selectedCouponHistoryId.value = undefined
      return
    }
    checkoutCoupons.value = await fetchPortalCouponCheckout({
      useCart: false,
      items: checkoutItems.value
        .filter((i) => i.skuId != null)
        .map((i) => ({
          skuId: Number(i.skuId),
          quantity: i.quantity || 1
        }))
    })
    if (
      selectedCouponHistoryId.value &&
      !checkoutCoupons.value.some((c) => c.historyId === selectedCouponHistoryId.value && c.usable)
    ) {
      selectedCouponHistoryId.value = undefined
    }
  }

  function onCouponChange() {
    const coupon = selectedCoupon.value
    if (!coupon) return
    if (!coupon.usable) {
      ElMessage.warning(coupon.unusableReason || '当前优惠券不可使用')
      selectedCouponHistoryId.value = undefined
      return
    }
    if (couponDiscount.value <= 0) {
      ElMessage.warning('当前订单不满足该优惠券使用条件')
      selectedCouponHistoryId.value = undefined
    }
  }

  async function loadData() {
    const [addrList, cart] = await Promise.all([fetchPortalAddressList(), fetchPortalCart()])
    addresses.value = addrList
    const checked = (cart.items || []).filter((i) => i.checked && i.valid)
    checkoutItems.value = checked
    const defaultAddr = addrList.find((a) => a.defaultStatus === 1) || addrList[0]
    selectedAddressId.value = defaultAddr?.id
    await loadCoupons()
  }

  async function loadProvinces() {
    provinces.value = await fetchPortalRegionChildren('0')
  }

  async function onProvinceChange(code: string) {
    const p = provinces.value.find((x) => x.code === code)
    addressForm.province = p?.name || ''
    addressForm.city = ''
    addressForm.region = ''
    regionCodes.value[1] = ''
    regionCodes.value[2] = ''
    cities.value = code ? await fetchPortalRegionChildren(code) : []
    districts.value = []
  }

  async function onCityChange(code: string) {
    const c = cities.value.find((x) => x.code === code)
    addressForm.city = c?.name || ''
    addressForm.region = ''
    regionCodes.value[2] = ''
    districts.value = code ? await fetchPortalRegionChildren(code) : []
  }

  function onDistrictChange(code: string) {
    const d = districts.value.find((x) => x.code === code)
    addressForm.region = d?.name || ''
  }

  async function saveAddress() {
    await formRef.value?.validate()
    savingAddress.value = true
    try {
      await fetchPortalAddressSave({ ...addressForm })
      addressDialogVisible.value = false
      await loadData()
      ElMessage.success('地址已保存')
    } finally {
      savingAddress.value = false
    }
  }

  async function handleSubmit() {
    if (!selectedAddressId.value) return
    submitting.value = true
    try {
      const result = await fetchPortalOrderSubmit({
        addressId: selectedAddressId.value,
        useCart: true,
        couponHistoryId: selectedCouponHistoryId.value
      })
      lastOrderId.value = result.orderId
      lastOrderSn.value = result.orderSn
      payDialogVisible.value = true
      notifyPortalCartChanged()
    } finally {
      submitting.value = false
    }
  }

  async function handleAlipayPay() {
    if (!lastOrderId.value) return
    paying.value = true
    try {
      const result = await fetchPortalAlipayPay(lastOrderId.value)
      payDialogVisible.value = false
      submitAlipayPayForm(result.payForm)
      ElMessage.info('正在跳转支付宝，支付完成后可在「我的订单」查看状态')
      router.push('/portal/orders')
    } catch (err) {
      ElMessage.error(err instanceof Error ? err.message : '支付宝下单失败')
    } finally {
      paying.value = false
    }
  }

  async function handleMockPay() {
    if (!lastOrderId.value) return
    paying.value = true
    try {
      await fetchPortalOrderMockPay(lastOrderId.value)
      payDialogVisible.value = false
      ElMessage.success('支付成功')
      router.push('/portal/orders')
    } finally {
      paying.value = false
    }
  }

  watch(addressDialogVisible, (visible) => {
    if (visible) loadProvinces()
  })

  onMounted(async () => {
    if (!requireLogin()) {
      loading.value = false
      return
    }
    try {
      await loadData()
      alipayEnabled.value = await fetchPortalAlipayEnabled()
      if (!checkoutItems.value.length && route.query.direct !== '1') {
        ElMessage.warning('请先勾选要结算的商品')
        router.replace('/portal/cart')
      }
    } finally {
      loading.value = false
    }
  })
</script>

<style scoped lang="scss">
  .page-title {
    margin: 0 0 16px;
    font-size: 22px;
  }

  .panel {
    background: #fff;
    border-radius: 8px;
    padding: 20px;
    margin-bottom: 16px;

    h3 {
      margin: 0 0 16px;
      font-size: 16px;
    }
  }

  .address-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 12px;
    margin-bottom: 12px;
  }

  .address-card {
    border: 2px solid #eee;
    border-radius: 8px;
    padding: 12px 16px;
    cursor: pointer;
    transition: border-color 0.2s;

    &.active {
      border-color: #e1251b;
      background: #fffafa;
    }

    &__user {
      margin: 0 0 6px;
      font-weight: 600;
    }

    &__detail {
      margin: 0 0 8px;
      font-size: 13px;
      color: #666;
      line-height: 1.5;
    }
  }

  .checkout-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 10px 0;
    border-bottom: 1px solid #f5f5f5;
    font-size: 14px;

    &__name {
      flex: 1;
    }

    &__qty {
      color: #999;
    }

    &__price {
      color: #e1251b;
      min-width: 80px;
      text-align: right;
    }
  }

  .checkout-total {
    text-align: right;
    padding-top: 16px;
    font-size: 16px;

    .checkout-discount {
      margin: 0 0 8px;
      font-size: 14px;
      color: #67c23a;
    }

    strong {
      color: #e1251b;
      font-size: 22px;
    }
  }

  .coupon-row {
    max-width: 480px;
  }

  .coupon-hint {
    margin: 8px 0 0;
    font-size: 13px;
    color: #e6a23c;
  }

  .coupon-empty {
    margin: 0;
    font-size: 14px;
    color: #999;
  }

  .submit-bar {
    background: #fff;
    border-radius: 8px;
    padding: 16px 20px;
    display: flex;
    justify-content: flex-end;
  }
</style>
