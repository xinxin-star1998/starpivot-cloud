<!-- C 端拼团页 -->
<template>
  <div v-loading="loading" class="portal-groupbuy">
    <PortalPageHeader title="拼团优惠" subtitle="邀好友一起拼 · 人数到齐即享拼团价" />

    <ElTabs v-model="activeTab" class="groupbuy-tabs">
      <ElTabPane label="可拼团" name="activities">
        <div v-if="activities.length" class="activity-grid">
          <div v-for="item in activities" :key="item.id" class="activity-card">
            <div class="activity-card__cover">
              <img :src="coverUrls.get(item.skuImg || '') || placeholderImg" :alt="item.title" />
              <span v-if="item.state === 'upcoming'" class="activity-card__badge">即将开始</span>
              <span v-else-if="item.state === 'ended'" class="activity-card__badge ended">已结束</span>
            </div>
            <div class="activity-card__body">
              <p class="activity-card__title">{{ item.title || item.skuName }}</p>
              <p class="activity-card__sku">{{ item.skuName }}</p>
              <p class="activity-card__price">
                <span class="price-now">¥{{ formatPrice(item.groupPrice) }}</span>
                <span class="price-origin">¥{{ formatPrice(item.originalPrice) }}</span>
              </p>
              <p class="activity-card__meta">
                {{ item.groupSize }} 人成团 · 剩余 {{ item.remainStock ?? 0 }} 件
              </p>
              <ElButton
                type="primary"
                class="activity-card__btn"
                :disabled="item.state !== 'ongoing' || (item.remainStock ?? 0) <= 0"
                @click="openDetail(item)"
              >
                {{ (item.remainStock ?? 0) <= 0 ? '已拼完' : item.state === 'upcoming' ? '未开始' : '去拼团' }}
              </ElButton>
            </div>
          </div>
        </div>
        <ElEmpty v-else description="暂无进行中的拼团活动" />
      </ElTabPane>

      <ElTabPane label="我的拼团" name="mine">
        <div v-if="myGroups.length" class="record-list">
          <div v-for="record in myGroups" :key="record.id" class="record-item">
            <img :src="coverUrls.get(record.skuImg || '') || placeholderImg" :alt="record.skuName" />
            <div class="record-item__main">
              <p class="record-item__title">{{ record.title || record.skuName }}</p>
              <p class="record-item__meta">
                团号 {{ record.groupNo }} · {{ record.joinedCount }}/{{ record.groupSize }} 人 ·
                {{ record.statusText }}
              </p>
              <p class="record-item__price">拼团价 ¥{{ formatPrice(record.groupPrice) }}</p>
            </div>
            <ElButton
              v-if="record.status === 1"
              type="primary"
              plain
              size="small"
              @click="copyInvite(record)"
            >
              邀请好友
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
        <ElEmpty v-else description="还没有参与过拼团" />
      </ElTabPane>
    </ElTabs>

    <ElDialog v-model="detailVisible" :title="detail?.activity?.title || '拼团详情'" width="640px" destroy-on-close>
      <div v-if="detail?.activity" class="detail">
        <div class="detail__head">
          <img :src="coverUrls.get(detail.activity.skuImg || '') || placeholderImg" :alt="detail.activity.skuName" />
          <div class="detail__info">
            <p class="detail__name">{{ detail.activity.skuName }}</p>
            <p class="detail__price">拼团价 ¥{{ formatPrice(detail.activity.groupPrice) }}</p>
            <p class="detail__meta">
              {{ detail.activity.groupSize }} 人成团 · {{ detail.activity.validHours }} 小时内成团
            </p>
          </div>
        </div>

        <div v-if="detail.groups?.length" class="detail__groups">
          <p class="detail__section-title">正在进行的团</p>
          <div v-for="group in detail.groups" :key="group.groupNo" class="group-row">
            <div>
              <p class="group-row__no">团号 {{ group.groupNo }}</p>
              <p class="group-row__meta">
                还差 {{ group.remainCount }} 人成团 · 剩余 {{ formatRemain(group.remainSeconds) }}
              </p>
            </div>
            <ElButton type="primary" size="small" @click="openOrderDialog(group.groupNo)">去参团</ElButton>
          </div>
        </div>
        <ElEmpty v-else description="暂无进行中的团，快来开团吧" :image-size="60" />

        <div class="detail__footer">
          <ElButton type="primary" size="large" @click="openOrderDialog()">我要开团</ElButton>
        </div>
      </div>
    </ElDialog>

    <ElDialog v-model="orderVisible" title="确认拼团" width="480px" destroy-on-close>
      <ElForm label-width="80px">
        <ElFormItem label="数量">
          <ElInputNumber v-model="orderQuantity" :min="1" :max="maxQuantity" :disabled="submitting" />
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
    fetchPortalGroupBuyDetail,
    fetchPortalGroupBuyJoin,
    fetchPortalGroupBuyList,
    fetchPortalGroupBuyMy,
    fetchPortalGroupBuyStart,
    type PortalGroupBuyActivity,
    type PortalGroupBuyDetail,
    type PortalGroupBuyRecord
  } from '@/api/portal/groupbuy'
  import { fetchPortalOrderSubmitToken } from '@/api/portal/order'
  import type { PortalAddress } from '@/api/portal/types'
  import { usePortalMemberStore } from '@/store/modules/portal-member'
  import { resolveGoodsImageDisplayUrls } from '@/utils/mall/goods-image-url'
  import { formatMoney } from '@/utils/mall/money'
  import { PORTAL_PRODUCT_PLACEHOLDER_IMG } from '@/utils/portal/product-placeholder'
  import PortalPageHeader from '@/views/portal/components/portal-page-header.vue'

  defineOptions({ name: 'PortalGroupBuy' })

  const router = useRouter()
  const route = useRoute()
  const portalStore = usePortalMemberStore()
  const loading = ref(true)
  const activeTab = ref(route.query.tab === 'mine' ? 'mine' : 'activities')
  const activities = ref<PortalGroupBuyActivity[]>([])
  const myGroups = ref<PortalGroupBuyRecord[]>([])
  const coverUrls = ref(new Map<string, string>())
  const placeholderImg = PORTAL_PRODUCT_PLACEHOLDER_IMG

  const detailVisible = ref(false)
  const detail = ref<PortalGroupBuyDetail>()
  const currentGroupNo = ref<string>()

  const orderVisible = ref(false)
  const orderQuantity = ref(1)
  const addresses = ref<PortalAddress[]>([])
  const selectedAddressId = ref<number>()
  const orderToken = ref('')
  const submitting = ref(false)

  const maxQuantity = computed(() => {
    const limit = detail.value?.activity?.limitBuy
    return limit && limit > 0 ? limit : 1
  })

  const formatPrice = (p?: number) => formatMoney(p)

  function formatRemain(seconds?: number) {
    if (seconds == null || seconds <= 0) return '00:00:00'
    const h = Math.floor(seconds / 3600)
    const m = Math.floor((seconds % 3600) / 60)
    const s = seconds % 60
    return [h, m, s].map((v) => String(v).padStart(2, '0')).join(':')
  }

  async function resolveCovers(keys: string[]) {
    const filtered = keys.filter(Boolean)
    if (!filtered.length) return
    const map = await resolveGoodsImageDisplayUrls(filtered)
    map.forEach((url, key) => coverUrls.value.set(key, url))
  }

  async function loadActivities() {
    activities.value = await fetchPortalGroupBuyList()
    await resolveCovers(activities.value.map((item) => item.skuImg || ''))
  }

  async function loadMine() {
    myGroups.value = await fetchPortalGroupBuyMy()
    await resolveCovers(myGroups.value.map((item) => item.skuImg || ''))
  }

  async function ensureLogin() {
    if (portalStore.token) return true
    router.push({ path: '/portal/login', query: { redirect: route.fullPath } })
    return false
  }

  async function openDetail(item: PortalGroupBuyActivity) {
    if (!(await ensureLogin()) || !item.id) return
    detail.value = await fetchPortalGroupBuyDetail(item.id)
    await resolveCovers([detail.value.activity?.skuImg || ''])
    detailVisible.value = true
  }

  async function openOrderDialog(groupNo?: string) {
    currentGroupNo.value = groupNo
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
    const activityId = detail.value?.activity?.id
    if (!activityId || !selectedAddressId.value || !orderToken.value) {
      ElMessage.warning('请完善收货地址')
      return
    }
    submitting.value = true
    try {
      const payload = {
        activityId,
        groupNo: currentGroupNo.value,
        quantity: orderQuantity.value,
        addressId: selectedAddressId.value,
        orderToken: orderToken.value
      }
      const record = currentGroupNo.value
        ? await fetchPortalGroupBuyJoin(payload)
        : await fetchPortalGroupBuyStart(payload)
      orderVisible.value = false
      detailVisible.value = false
      ElMessage.success(`拼团订单已创建：${record.orderSn}`)
      router.push('/portal/orders')
    } catch {
      orderToken.value = (await fetchPortalOrderSubmitToken()).orderToken
    } finally {
      submitting.value = false
    }
  }

  async function copyInvite(record: PortalGroupBuyRecord) {
    const link = `${location.origin}/portal/groupbuy?groupNo=${record.groupNo}`
    try {
      await navigator.clipboard.writeText(link)
      ElMessage.success('邀请链接已复制，快分享给好友')
    } catch {
      ElMessage.info(`请手动复制链接：${link}`)
    }
  }

  watch(activeTab, async (tab) => {
    if (tab === 'mine') await loadMine()
  })

  onMounted(async () => {
    try {
      await loadActivities()
      if (activeTab.value === 'mine') await loadMine()
    } finally {
      loading.value = false
    }
  })
</script>

<style scoped lang="scss">
  .portal-groupbuy {
    padding: 0 0 40px;
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
      position: relative;
      height: 180px;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
    }

    &__badge {
      position: absolute;
      top: 8px;
      left: 8px;
      padding: 2px 8px;
      border-radius: 10px;
      background: var(--portal-primary);
      color: #fff;
      font-size: 12px;

      &.ended {
        background: var(--portal-text-muted);
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

    &__sku {
      margin: 4px 0 0;
      font-size: 12px;
      color: var(--portal-text-muted);
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

  .price-now {
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

    &__price {
      margin: 4px 0 0;
      color: var(--portal-primary);
      font-weight: 600;
    }
  }

  .detail {
    &__head {
      display: flex;
      gap: 14px;

      img {
        width: 110px;
        height: 110px;
        border-radius: var(--portal-radius-sm);
        object-fit: cover;
      }
    }

    &__name {
      margin: 0;
      font-weight: 600;
    }

    &__price {
      margin: 8px 0 4px;
      color: var(--portal-primary);
      font-size: 18px;
      font-weight: 700;
    }

    &__meta {
      margin: 0;
      color: var(--portal-text-muted);
      font-size: 12px;
    }

    &__groups {
      margin-top: 20px;
    }

    &__section-title {
      margin: 0 0 10px;
      font-weight: 600;
    }

    &__footer {
      margin-top: 20px;
      text-align: center;
    }
  }

  .group-row {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 0;
    border-top: 1px dashed var(--portal-border);

    &__no {
      margin: 0;
      font-size: 13px;
      color: var(--portal-text);
    }

    &__meta {
      margin: 2px 0 0;
      font-size: 12px;
      color: var(--portal-text-muted);
    }
  }
</style>
