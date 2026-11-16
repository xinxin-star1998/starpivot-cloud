<!-- C 端砍价页 -->
<template>
  <div v-loading="loading" class="portal-bargain">
    <PortalPageHeader title="砍价免费拿" subtitle="邀请好友砍一刀 · 砍到底价即可下单" />

    <ElAlert v-if="sharedRecord" class="bargain-share" type="success" :closable="false" show-icon>
      <template #title>
        好友正在砍价：{{ sharedRecord.title || sharedRecord.skuName }}，当前价
        <b>¥{{ formatPrice(sharedRecord.currentPrice) }}</b
        >，已助力 {{ sharedRecord.helpCount }}/{{ sharedRecord.bargainTimes }} 次
      </template>
      <ElButton type="primary" size="small" :loading="helping" @click="handleHelp(sharedRecord)">
        帮 TA 砍一刀
      </ElButton>
    </ElAlert>

    <ElTabs v-model="activeTab">
      <ElTabPane label="砍价活动" name="activities">
        <div v-if="activities.length" class="activity-grid">
          <div v-for="item in activities" :key="item.id" class="activity-card">
            <div class="activity-card__cover">
              <img :src="coverUrls.get(item.skuImg || '') || placeholderImg" :alt="item.title" />
            </div>
            <div class="activity-card__body">
              <p class="activity-card__title">{{ item.title || item.skuName }}</p>
              <p class="activity-card__price">
                <span class="price-floor">底价 ¥{{ formatPrice(item.floorPrice) }}</span>
                <span class="price-origin">原价 ¥{{ formatPrice(item.originalPrice) }}</span>
              </p>
              <p class="activity-card__meta">
                {{ item.bargainTimes }} 次助力 · 剩余 {{ item.remainStock ?? 0 }} 件
              </p>
              <ElButton
                type="primary"
                class="activity-card__btn"
                :disabled="item.state !== 'ongoing' || (item.remainStock ?? 0) <= 0"
                @click="handleStart(item)"
              >
                {{
                  (item.remainStock ?? 0) <= 0
                    ? '已抢完'
                    : item.state === 'upcoming'
                      ? '未开始'
                      : '发起砍价'
                }}
              </ElButton>
            </div>
          </div>
        </div>
        <ElEmpty v-else description="暂无进行中的砍价活动" />
      </ElTabPane>

      <ElTabPane label="我的砍价" name="mine">
        <div v-if="myRecords.length" class="record-list">
          <div v-for="record in myRecords" :key="record.id" class="record-item">
            <img
              :src="coverUrls.get(record.skuImg || '') || placeholderImg"
              :alt="record.skuName"
            />
            <div class="record-item__main">
              <p class="record-item__title">{{ record.title || record.skuName }}</p>
              <p class="record-item__meta">
                当前价 ¥{{ formatPrice(record.currentPrice) }} · 已砍 {{ record.helpCount }}/{{
                  record.bargainTimes
                }}
                次 · {{ record.statusText }}
              </p>
            </div>
            <ElButton v-if="record.status === 0" size="small" @click="copyShare(record)">
              邀请助力
            </ElButton>
            <ElButton
              v-else-if="record.status === 1"
              type="primary"
              size="small"
              @click="openOrderDialog(record)"
            >
              立即下单
            </ElButton>
            <ElButton
              v-else-if="record.orderSn"
              size="small"
              @click="router.push('/portal/orders')"
            >
              查看订单
            </ElButton>
          </div>
        </div>
        <ElEmpty v-else description="还没有发起过砍价" />
      </ElTabPane>
    </ElTabs>

    <ElDialog v-model="progressVisible" title="砍价进度" width="480px" destroy-on-close>
      <div v-if="activeRecord" class="progress">
        <p class="progress__name">{{ activeRecord.title || activeRecord.skuName }}</p>
        <p class="progress__price">
          <span class="price-now">¥{{ formatPrice(activeRecord.currentPrice) }}</span>
          <span class="price-origin">原价 ¥{{ formatPrice(activeRecord.originalPrice) }}</span>
        </p>
        <ElProgress
          :percentage="progressPercent"
          :show-text="false"
          :stroke-width="10"
          color="var(--portal-primary)"
        />
        <p class="progress__meta">
          已砍 {{ activeRecord.helpCount }}/{{ activeRecord.bargainTimes }} 次 · 剩余可助力
          {{ activeRecord.remainHelps }} 次 · 底价 ¥{{ formatPrice(activeRecord.floorPrice) }}
        </p>
        <div class="progress__actions">
          <ElButton @click="copyShare(activeRecord)">邀请好友助力</ElButton>
          <ElButton
            v-if="activeRecord.status === 1"
            type="primary"
            @click="openOrderDialog(activeRecord)"
          >
            立即下单
          </ElButton>
        </div>
      </div>
    </ElDialog>

    <ElDialog v-model="orderVisible" title="确认下单" width="480px" destroy-on-close>
      <ElForm label-width="80px">
        <ElFormItem label="数量">
          <ElInputNumber
            v-model="orderQuantity"
            :min="1"
            :max="maxQuantity"
            :disabled="submitting"
          />
        </ElFormItem>
        <ElFormItem label="收货地址">
          <ElSelect v-model="selectedAddressId" placeholder="选择地址" style="width: 100%">
            <ElOption
              v-for="addr in addresses"
              :key="addr.id"
              :label="`${addr.name} ${addr.phone} · ${addr.province}${addr.city}${addr.region}${addr.detailAddress}`"
              :value="addr.id!"
            />
          </ElSelect>
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="orderVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="submitting" @click="submitOrder">提交订单</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
  import { fetchPortalAddressList } from '@/api/portal/address'
  import {
    fetchPortalBargainHelp,
    fetchPortalBargainList,
    fetchPortalBargainMy,
    fetchPortalBargainOrder,
    fetchPortalBargainRecord,
    fetchPortalBargainStart,
    type PortalBargainActivity,
    type PortalBargainRecord
  } from '@/api/portal/bargain'
  import { fetchPortalOrderSubmitToken } from '@/api/portal/order'
  import type { PortalAddress } from '@/api/portal/types'
  import { usePortalMemberStore } from '@/store/modules/portal-member'
  import { resolveGoodsImageDisplayUrls } from '@/utils/mall/goods-image-url'
  import { formatMoney } from '@/utils/mall/money'
  import { PORTAL_PRODUCT_PLACEHOLDER_IMG } from '@/utils/portal/product-placeholder'
  import PortalPageHeader from '@/views/portal/components/portal-page-header.vue'

  defineOptions({ name: 'PortalBargain' })

  const router = useRouter()
  const route = useRoute()
  const portalStore = usePortalMemberStore()
  const loading = ref(true)
  const activeTab = ref(route.query.tab === 'mine' ? 'mine' : 'activities')
  const activities = ref<PortalBargainActivity[]>([])
  const myRecords = ref<PortalBargainRecord[]>([])
  const coverUrls = ref(new Map<string, string>())
  const placeholderImg = PORTAL_PRODUCT_PLACEHOLDER_IMG

  const sharedRecord = ref<PortalBargainRecord>()
  const helping = ref(false)

  const progressVisible = ref(false)
  const activeRecord = ref<PortalBargainRecord>()

  const orderVisible = ref(false)
  const orderQuantity = ref(1)
  const addresses = ref<PortalAddress[]>([])
  const selectedAddressId = ref<number>()
  const orderToken = ref('')
  const submitting = ref(false)

  const maxQuantity = computed(() => {
    const activity = activities.value.find((item) => item.id === activeRecord.value?.activityId)
    const limit = activity?.limitBuy
    return limit && limit > 0 ? limit : 1
  })

  const progressPercent = computed(() => {
    const record = activeRecord.value
    if (!record?.originalPrice || !record.floorPrice) return 0
    const total = record.originalPrice - record.floorPrice
    if (total <= 0) return 100
    const done = record.originalPrice - (record.currentPrice ?? record.originalPrice)
    return Math.min(Math.max(Math.round((done / total) * 100), 0), 100)
  })

  const formatPrice = (p?: number) => formatMoney(p)

  async function resolveCovers(keys: string[]) {
    const filtered = keys.filter(Boolean)
    if (!filtered.length) return
    const map = await resolveGoodsImageDisplayUrls(filtered)
    map.forEach((url, key) => coverUrls.value.set(key, url))
  }

  async function loadActivities() {
    activities.value = await fetchPortalBargainList()
    await resolveCovers(activities.value.map((item) => item.skuImg || ''))
  }

  async function loadMine() {
    myRecords.value = await fetchPortalBargainMy()
    await resolveCovers(myRecords.value.map((item) => item.skuImg || ''))
  }

  async function ensureLogin() {
    if (portalStore.token) return true
    router.push({ path: '/portal/login', query: { redirect: route.fullPath } })
    return false
  }

  async function handleStart(item: PortalBargainActivity) {
    if (!(await ensureLogin()) || !item.id) return
    const record = await fetchPortalBargainStart(item.id)
    activeRecord.value = record
    progressVisible.value = true
    await loadMine()
  }

  async function handleHelp(record: PortalBargainRecord) {
    if (!(await ensureLogin()) || !record.id) return
    helping.value = true
    try {
      const updated = await fetchPortalBargainHelp(record.id)
      const cut = (record.currentPrice ?? 0) - (updated.currentPrice ?? 0)
      sharedRecord.value = updated
      ElMessage.success(cut > 0 ? `助力成功，砍掉了 ¥${formatMoney(cut)}` : '助力成功')
    } catch {
      // 业务异常已由请求层提示
    } finally {
      helping.value = false
    }
  }

  function copyShare(record: PortalBargainRecord) {
    const link = `${location.origin}/portal/bargain?recordId=${record.id}`
    navigator.clipboard
      .writeText(link)
      .then(() => ElMessage.success('助力链接已复制，快分享给好友'))
      .catch(() => ElMessage.info(`请手动复制链接：${link}`))
  }

  async function openOrderDialog(record: PortalBargainRecord) {
    activeRecord.value = record
    orderQuantity.value = 1
    orderVisible.value = true
    const [addrList, tokenResult] = await Promise.all([
      fetchPortalAddressList(),
      fetchPortalOrderSubmitToken()
    ])
    addresses.value = addrList
    orderToken.value = tokenResult.orderToken
    const defaultAddr = addrList.find((a) => a.defaultStatus === 1) || addrList[0]
    selectedAddressId.value = defaultAddr?.id
  }

  async function submitOrder() {
    const record = activeRecord.value
    if (!record?.id || !selectedAddressId.value || !orderToken.value) {
      ElMessage.warning('请完善收货地址')
      return
    }
    submitting.value = true
    try {
      const updated = await fetchPortalBargainOrder({
        recordId: record.id,
        quantity: orderQuantity.value,
        addressId: selectedAddressId.value,
        orderToken: orderToken.value
      })
      orderVisible.value = false
      progressVisible.value = false
      ElMessage.success(`下单成功：${updated.orderSn}`)
      router.push('/portal/orders')
    } catch {
      orderToken.value = (await fetchPortalOrderSubmitToken()).orderToken
    } finally {
      submitting.value = false
    }
  }

  watch(activeTab, async (tab) => {
    if (tab === 'mine') await loadMine()
  })

  onMounted(async () => {
    const recordId = route.query.recordId ? Number(route.query.recordId) : undefined
    try {
      await loadActivities()
      if (recordId) {
        sharedRecord.value = await fetchPortalBargainRecord(recordId)
        await resolveCovers([sharedRecord.value.skuImg || ''])
      }
    } finally {
      loading.value = false
    }
  })
</script>

<style scoped lang="scss">
  .bargain-share {
    margin-bottom: 16px;

    :deep(.el-alert__content) {
      display: flex;
      align-items: center;
      gap: 12px;
      justify-content: space-between;
    }
  }

  .activity-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
    gap: 20px;
  }

  .activity-card {
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius-sm);
    overflow: hidden;
    background: var(--portal-bg-elevated);

    &__cover {
      height: 180px;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    &__body {
      padding: 12px 14px 14px;
    }

    &__title {
      margin: 0;
      font-size: 16px;
      font-weight: 600;
      color: var(--portal-text);
    }

    &__price {
      margin: 8px 0 0;
    }

    &__meta {
      margin: 6px 0 12px;
      font-size: 12px;
      color: var(--portal-text-muted);
    }

    &__btn {
      width: 100%;
    }
  }

  .price-floor {
    color: var(--portal-primary);
    font-size: 18px;
    font-weight: 700;
  }

  .price-origin {
    margin-left: 8px;
    color: var(--portal-text-muted);
    font-size: 12px;
    text-decoration: line-through;
  }

  .price-now {
    color: var(--portal-primary);
    font-size: 22px;
    font-weight: 700;
  }

  .record-list {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }

  .record-item {
    display: flex;
    gap: 12px;
    align-items: center;
    padding: 12px;
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius-sm);
    background: var(--portal-bg-elevated);

    img {
      width: 72px;
      height: 72px;
      border-radius: var(--portal-radius-sm);
      object-fit: cover;
    }

    &__main {
      flex: 1;
      min-width: 0;
    }

    &__title {
      margin: 0;
      font-weight: 600;
      color: var(--portal-text);
    }

    &__meta {
      margin: 4px 0 0;
      font-size: 12px;
      color: var(--portal-text-muted);
    }
  }

  .progress {
    &__name {
      margin: 0;
      font-weight: 600;
    }

    &__price {
      margin: 8px 0 12px;
    }

    &__meta {
      margin: 8px 0 20px;
      font-size: 12px;
      color: var(--portal-text-muted);
    }

    &__actions {
      display: flex;
      gap: 12px;
      justify-content: center;
    }
  }
</style>
