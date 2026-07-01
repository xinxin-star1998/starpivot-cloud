<!-- C 端结算页 -->
<template>
  <div v-loading="loading" class="portal-checkout">
    <PortalPageHeader title="确认订单" subtitle="请核对收货信息与商品清单" />

    <section class="panel">
      <div class="panel__title">
        <ArtSvgIcon icon="ri:map-pin-line" />
        <h3>收货地址</h3>
      </div>
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
      <div class="panel__title">
        <ArtSvgIcon icon="ri:coupon-3-line" />
        <h3>优惠券</h3>
      </div>
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
      <div class="panel__title">
        <ArtSvgIcon icon="ri:shopping-bag-3-line" />
        <h3>商品清单</h3>
      </div>
      <div v-for="item in displayItems" :key="item.skuId" class="checkout-item">
        <span class="checkout-item__name">{{ item.skuTitle }}</span>
        <span class="checkout-item__qty">x{{ item.quantity }}</span>
        <span class="checkout-item__price"
          >¥{{ formatPrice(item.lineAmount ?? (item.price || 0) * (item.quantity || 0)) }}</span
        >
      </div>
      <div class="checkout-total">
        <p v-if="priceTrial && priceTrial.promotionAmount > 0" class="checkout-discount">
          促销优惠：-¥{{ formatPrice(priceTrial.promotionAmount) }}
        </p>
        <p v-if="couponDiscount > 0" class="checkout-discount"
          >优惠券：-¥{{ formatPrice(couponDiscount) }}</p
        >
        <p v-if="integrationDiscount > 0" class="checkout-discount"
          >积分抵扣：-¥{{ formatPrice(integrationDiscount) }}（{{ useIntegrationInput }} 积分）</p
        >
        <p v-if="freightAmount > 0" class="checkout-freight">运费：¥{{ formatPrice(freightAmount) }}</p>
        <p v-else-if="priceTrial?.freeFreight" class="checkout-freight checkout-freight--free">免运费</p>
        应付：<strong>¥{{ formatPrice(payAmount) }}</strong>
      </div>
    </section>

    <section v-if="maxUsableIntegration > 0" class="panel">
      <div class="panel__title">
        <ArtSvgIcon icon="ri:coin-line" />
        <h3>积分抵扣</h3>
      </div>
      <p class="integration-hint">
        可用 {{ availableIntegration }} 积分，本单最多可用 {{ maxUsableIntegration }} 积分
      </p>
      <ElInputNumber
        v-model="useIntegrationInput"
        :min="0"
        :max="maxUsableIntegration"
        :step="100"
        controls-position="right"
        @change="onIntegrationChange"
      />
    </section>

    <section class="panel">
      <div class="panel__title">
        <ArtSvgIcon icon="ri:wallet-3-line" />
        <h3>支付方式</h3>
      </div>
      <ElRadioGroup v-model="selectedPayType">
        <ElRadio v-if="alipayEnabled" :value="1">支付宝</ElRadio>
        <ElRadio v-if="wxPayEnabled" :value="2">微信支付</ElRadio>
        <ElRadio v-if="!alipayEnabled && !wxPayEnabled" :value="1">在线支付（Mock）</ElRadio>
      </ElRadioGroup>
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
        <PortalRegionFields ref="regionFieldsRef" :form="addressForm" />
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
      <p v-if="alipayEnabled && selectedPayType === 1">请使用支付宝完成支付</p>
      <p v-else-if="wxPayEnabled && selectedPayType === 2">请使用微信扫码完成支付</p>
      <p v-else>请完成支付（开发环境可使用 Mock 支付）</p>
      <template #footer>
        <ElButton @click="router.push('/portal')">返回首页</ElButton>
        <ElButton
          v-if="alipayEnabled && selectedPayType === 1"
          :loading="paying"
          type="primary"
          @click="handleAlipayPay"
        >
          支付宝支付
        </ElButton>
        <ElButton
          v-else-if="wxPayEnabled && selectedPayType === 2"
          :loading="paying"
          type="primary"
          @click="handleWxPay"
        >
          微信扫码支付
        </ElButton>
        <ElButton v-else :loading="paying" type="primary" @click="handleMockPay">Mock 支付</ElButton>
      </template>
    </ElDialog>

    <ElDialog
      v-model="wxPayDialogVisible"
      title="微信扫码支付"
      width="360px"
      :close-on-click-modal="false"
      @closed="wxCodeUrl = ''"
    >
      <p class="wx-pay-sn">订单号：{{ lastOrderSn }}</p>
      <div v-if="wxCodeUrl" class="wx-pay-qr">
        <QrcodeVue :value="wxCodeUrl" :size="220" level="M" />
      </div>
      <p v-if="wxPayMock" class="wx-pay-mock-hint">开发 Mock 模式，点击下方按钮模拟支付成功</p>
      <template #footer>
        <ElButton @click="wxPayDialogVisible = false">关闭</ElButton>
        <ElButton v-if="wxPayMock" :loading="paying" type="primary" @click="handleWxMockConfirm">
          Mock 确认支付
        </ElButton>
        <ElButton v-else type="primary" @click="router.push('/portal/orders')">我已完成支付</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import type {FormInstance, FormRules} from 'element-plus'
import {ElMessage} from 'element-plus'
import {fetchPortalAddressList, fetchPortalAddressSave} from '@/api/portal/address'
import {fetchPortalCart} from '@/api/portal/cart'
import {fetchPortalCouponCheckout} from '@/api/portal/coupon'
import {
  fetchPortalOrderMockPay,
  fetchPortalOrderPriceTrial,
  fetchPortalOrderSubmit,
  fetchPortalOrderSubmitToken
} from '@/api/portal/order'
import {
  fetchPortalAlipayEnabled,
  fetchPortalAlipayPay,
  fetchPortalWxMockPay,
  fetchPortalWxNativePay,
  fetchPortalWxPayEnabled
} from '@/api/portal/pay'
import type {PortalAddress, PortalCartItem, PortalCheckoutCoupon, PortalOrderPriceTrial} from '@/api/portal/types'
import {usePortalAuth} from '@/hooks/portal/usePortalAuth'
import {notifyPortalCartChanged} from '@/utils/portal/cart-event'
import {submitAlipayPayForm} from '@/utils/portal/alipay-pay'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import PortalPageHeader from '../components/portal-page-header.vue'
import PortalRegionFields from '../components/portal-region-fields.vue'
import QrcodeVue from 'qrcode.vue'

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
  const wxPayEnabled = ref(false)
  const wxPayMock = ref(false)
  const selectedPayType = ref(1)
  const orderToken = ref('')
  const wxPayDialogVisible = ref(false)
  const wxCodeUrl = ref('')
  const checkoutCoupons = ref<PortalCheckoutCoupon[]>([])
  const selectedCouponHistoryId = ref<number>()
  const priceTrial = ref<PortalOrderPriceTrial | null>(null)
  const useIntegrationInput = ref(0)
  const priceTrialLoading = ref(false)

  const formRef = ref<FormInstance>()
  const regionFieldsRef = ref<InstanceType<typeof PortalRegionFields>>()
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

  const displayItems = computed(() => {
    if (priceTrial.value?.lines?.length) {
      return priceTrial.value.lines.map((line) => ({
        skuId: line.skuId,
        skuTitle: line.skuTitle || `SKU ${line.skuId}`,
        quantity: line.quantity,
        lineAmount: line.lineAmount,
        price: line.unitPrice
      }))
    }
    return checkoutItems.value
  })

  const couponDiscount = computed(() => priceTrial.value?.couponAmount ?? 0)
  const integrationDiscount = computed(() => priceTrial.value?.integrationAmount ?? 0)
  const freightAmount = computed(() => priceTrial.value?.freightAmount ?? 0)
  const payAmount = computed(() => priceTrial.value?.payAmount ?? 0)
  const availableIntegration = computed(() => priceTrial.value?.availableIntegration ?? 0)
  const maxUsableIntegration = computed(() => priceTrial.value?.maxUsableIntegration ?? 0)
  const canSubmit = computed(
    () =>
      !!selectedAddressId.value &&
      checkoutItems.value.length > 0 &&
      !!orderToken.value &&
      !submitting.value
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

  const formatPrice = (p: number) => p.toFixed(2)

  async function refreshPriceTrial() {
    if (!checkoutItems.value.length) {
      priceTrial.value = null
      return
    }
    priceTrialLoading.value = true
    try {
      priceTrial.value = await fetchPortalOrderPriceTrial({
        useCart: true,
        couponHistoryId: selectedCouponHistoryId.value,
        useIntegration: useIntegrationInput.value || undefined
      })
      if (priceTrial.value.useIntegration != null) {
        useIntegrationInput.value = priceTrial.value.useIntegration
      }
    } finally {
      priceTrialLoading.value = false
    }
  }

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
    if (coupon && !coupon.usable) {
      ElMessage.warning(coupon.unusableReason || '当前优惠券不可使用')
      selectedCouponHistoryId.value = undefined
    }
    refreshPriceTrial()
  }

  function onIntegrationChange() {
    refreshPriceTrial()
  }

  async function loadData() {
    const [addrList, cart] = await Promise.all([fetchPortalAddressList(), fetchPortalCart()])
    addresses.value = addrList
    const checked = (cart.items || []).filter((i) => i.checked && i.valid)
    checkoutItems.value = checked
    const defaultAddr = addrList.find((a) => a.defaultStatus === 1) || addrList[0]
    selectedAddressId.value = defaultAddr?.id
    await loadCoupons()
    await refreshPriceTrial()
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

  async function refreshSubmitToken() {
    const tokenResult = await fetchPortalOrderSubmitToken()
    orderToken.value = tokenResult.orderToken
  }

  async function handleSubmit() {
    if (!selectedAddressId.value || !orderToken.value) return
    submitting.value = true
    try {
      const result = await fetchPortalOrderSubmit({
        addressId: selectedAddressId.value,
        useCart: true,
        couponHistoryId: selectedCouponHistoryId.value,
        useIntegration: useIntegrationInput.value || undefined,
        payType: selectedPayType.value,
        orderToken: orderToken.value
      })
      lastOrderId.value = result.orderId
      lastOrderSn.value = result.orderSn
      payDialogVisible.value = true
      notifyPortalCartChanged()
      await refreshSubmitToken()
    } catch {
      await refreshSubmitToken()
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

  async function handleWxPay() {
    if (!lastOrderId.value) return
    paying.value = true
    try {
      const result = await fetchPortalWxNativePay(lastOrderId.value)
      wxCodeUrl.value = result.codeUrl
      wxPayMock.value = !!result.mock
      payDialogVisible.value = false
      wxPayDialogVisible.value = true
    } catch (err) {
      ElMessage.error(err instanceof Error ? err.message : '微信下单失败')
    } finally {
      paying.value = false
    }
  }

  async function handleWxMockConfirm() {
    if (!lastOrderId.value) return
    paying.value = true
    try {
      await fetchPortalWxMockPay(lastOrderId.value)
      wxPayDialogVisible.value = false
      ElMessage.success('支付成功')
      router.push('/portal/orders')
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
    if (visible) {
      regionFieldsRef.value?.resetRegionPicker()
    }
  })

  onMounted(async () => {
    if (!requireLogin()) {
      loading.value = false
      return
    }
    try {
      await loadData()
      const [alipay, wx] = await Promise.all([fetchPortalAlipayEnabled(), fetchPortalWxPayEnabled()])
      alipayEnabled.value = alipay
      wxPayEnabled.value = wx.enabled
      wxPayMock.value = wx.mock
      if (alipay) {
        selectedPayType.value = 1
      } else if (wx.enabled) {
        selectedPayType.value = 2
      }
      await refreshSubmitToken()
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
  @import '../styles/variables.scss';

  .panel {
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius-lg);
    padding: 24px;
    margin-bottom: 16px;
    box-shadow: var(--portal-shadow-sm);
    border: 1px solid var(--portal-border);

    &__title {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;

      svg {
        font-size: 20px;
        color: var(--portal-primary);
      }

      h3 {
        margin: 0;
        font-size: 16px;
        font-weight: 700;
        color: var(--portal-text);
      }
    }
  }

  .address-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
    gap: 12px;
    margin-bottom: 12px;
  }

  .address-card {
    border: 2px solid var(--portal-border);
    border-radius: var(--portal-radius);
    padding: 14px 16px;
    cursor: pointer;
    transition: all var(--portal-transition);
    position: relative;

    &:hover {
      border-color: #ffb8b8;
    }

    &.active {
      border-color: var(--portal-primary);
      background: var(--portal-primary-light);
      box-shadow: 0 0 0 1px var(--portal-primary) inset;
    }

    &__user {
      margin: 0 0 6px;
      font-weight: 600;
      color: var(--portal-text);
    }

    &__detail {
      margin: 0 0 8px;
      font-size: 13px;
      color: var(--portal-text-secondary);
      line-height: 1.5;
    }
  }

  .checkout-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 0;
    border-bottom: 1px solid var(--portal-border);
    font-size: 14px;

    &:last-of-type {
      border-bottom: none;
    }

    &__name {
      flex: 1;
      color: var(--portal-text);
    }

    &__qty {
      color: var(--portal-text-muted);
    }

    &__price {
      color: var(--portal-primary);
      font-weight: 600;
      min-width: 80px;
      text-align: right;
    }
  }

  .checkout-total {
    text-align: right;
    padding-top: 16px;
    margin-top: 8px;
    border-top: 1px dashed var(--portal-border);
    font-size: 16px;

    .checkout-discount {
      margin: 0 0 8px;
      font-size: 14px;
      color: var(--portal-accent-green);
    }

    .checkout-freight {
      margin: 0 0 8px;
      font-size: 14px;
      color: var(--portal-text-secondary);

      &--free {
        color: var(--portal-accent-green);
      }
    }

    strong {
      color: var(--portal-primary);
      font-size: 24px;
      font-weight: 800;
    }
  }

  .integration-hint {
    margin: 0 0 12px;
    font-size: 13px;
    color: var(--portal-text-muted);
  }

  .wx-pay-sn {
    margin: 0 0 12px;
    font-size: 14px;
    color: var(--portal-text-secondary);
    text-align: center;
  }

  .wx-pay-qr {
    display: flex;
    justify-content: center;
    padding: 12px 0;
  }

  .wx-pay-mock-hint {
    margin: 0;
    font-size: 13px;
    color: var(--portal-accent-orange);
    text-align: center;
  }

  .coupon-row {
    max-width: 480px;
  }

  .coupon-hint {
    margin: 8px 0 0;
    font-size: 13px;
    color: var(--portal-accent-orange);
  }

  .coupon-empty {
    margin: 0;
    font-size: 14px;
    color: var(--portal-text-muted);
  }

  .submit-bar {
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius-lg);
    padding: 20px 24px;
    display: flex;
    justify-content: flex-end;
    box-shadow: var(--portal-shadow-sm);
    border: 1px solid var(--portal-border);
    position: sticky;
    bottom: 16px;

    :deep(.el-button) {
      min-width: 160px;
      border-radius: 24px;
      font-weight: 600;
      background: var(--portal-primary-gradient);
      border: none;
    }
  }
</style>
