<template>
  <ElDrawer v-model="drawerVisible" destroy-on-close size="680px" title="优惠券详情">
    <ElSkeleton v-if="loading" :rows="10" animated />
    <template v-else-if="detail">
      <ElDescriptions :column="2" border class="detail-block" size="small">
        <ElDescriptionsItem :span="2" label="名称">{{
          detail.couponName || '-'
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="券类型">{{
          couponTypeLabel(detail.couponType)
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="适用范围">{{ useTypeLabel(detail.useType) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="面额">{{ formatCouponMoney(detail.amount) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="使用门槛">{{
          formatCouponMoney(detail.minPoint)
        }}</ElDescriptionsItem>
        <ElDescriptionsItem label="发行数量">{{ detail.publishCount ?? '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="每人限领">{{ detail.perLimit ?? '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="已领取">{{ detail.receiveCount ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="已使用">{{ detail.useCount ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem :span="2" label="有效期">
          {{ formatCouponDateRange(detail.startTime, detail.endTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem :span="2" label="领取时间">
          {{ formatCouponDateRange(detail.enableStartTime, detail.enableEndTime) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="会员等级">
          {{ memberLevelLabel(detail.memberLevel, detail.couponType) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="领取状态">
          <ElTag :type="claimStatusMeta.tagType" size="small">{{ claimStatusMeta.label }}</ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="使用状态">
          <ElTag :type="useStatusMeta.tagType" size="small">{{ useStatusMeta.label }}</ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="综合状态">
          <ElTag :type="runStatusMeta.tagType" size="small">{{ runStatusMeta.label }}</ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="发布状态">
          <ElTag :type="detail.publish === 1 ? 'success' : 'info'" size="small">
            {{ detail.publish === 1 ? '已发布' : '未发布' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem label="审批状态">
          <ElTag :type="auditTagType(detail.auditStatus)" size="small">
            {{ MALL_AUDIT_STATUS_MAP[detail.auditStatus || ''] || detail.auditStatus || '-' }}
          </ElTag>
        </ElDescriptionsItem>
        <ElDescriptionsItem :span="2" label="备注">{{ detail.note || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem v-if="detail.couponImg" :span="2" label="券图片">
          <ElImage
            v-if="imageUrl"
            :preview-src-list="[imageUrl]"
            :src="imageUrl"
            class="coupon-image"
            fit="cover"
            preview-teleported
          />
          <span v-else class="image-loading">加载中...</span>
        </ElDescriptionsItem>
      </ElDescriptions>

      <ApprovalTimeline
        v-if="detail.approvalInstanceId"
        :instance-id="detail.approvalInstanceId"
        class="mb-4"
      />

      <template v-if="detail.useType === 2 && detail.spuList?.length">
        <h4 class="section-title">关联商品</h4>
        <ElTable :data="detail.spuList" border size="small">
          <ElTableColumn label="ID" prop="spuId" width="80" />
          <ElTableColumn label="商品名称" min-width="200" prop="spuName" show-overflow-tooltip />
        </ElTable>
      </template>

      <template v-if="detail.useType === 1 && detail.categoryList?.length">
        <h4 class="section-title">关联分类</h4>
        <ElTable :data="detail.categoryList" border size="small">
          <ElTableColumn label="ID" prop="categoryId" width="80" />
          <ElTableColumn
            label="分类名称"
            min-width="200"
            prop="categoryName"
            show-overflow-tooltip
          />
        </ElTable>
      </template>

      <div v-if="showActions" class="drawer-actions">
        <ElButton
          v-if="hasAuth('mall:coupon:edit')"
          @click="emit('edit', detail.id)"
        >
          编辑
        </ElButton>
        <ElButton
          v-if="hasAuth('mall:coupon:edit') && detail.publish !== 1 && canSubmitCouponAudit(detail)"
          :loading="submittingApproval"
          type="primary"
          @click="handleSubmitApproval"
        >
          提交审批
        </ElButton>
        <ElButton
          v-if="hasAuth('mall:coupon:edit') && detail.publish === 1"
          type="warning"
          @click="handleTogglePublish"
        >
          下架
        </ElButton>
      </div>
    </template>
  </ElDrawer>
</template>

<script lang="ts" setup>
import {ElMessageBox} from 'element-plus'
import {
  canSubmitCouponAudit,
  COUPON_TYPE_OPTIONS,
  COUPON_USE_TYPE_OPTIONS,
  type CouponVo,
  fetchCouponById,
  fetchCouponPublishStatus,
  fetchCouponSubmitApproval
} from '@/api/mall/coupon'
import ApprovalTimeline from '@/views/approval/components/ApprovalTimeline.vue'
import {MALL_AUDIT_STATUS_MAP} from '@/utils/mall/audit-status'
import {fetchMemberLevelList, type MemberLevelVo} from '@/api/mall/member-level'
import {useAuth} from '@/hooks/core/useAuth'
import {
  COUPON_PHASE_STATUS_MAP,
  COUPON_RUN_STATUS_MAP,
  formatCouponDateRange,
  formatCouponMoney,
  getCouponClaimStatus,
  getCouponRunStatus,
  getCouponUseStatus
} from '@/utils/mall/coupon'
import {resolveGoodsImageDisplayUrl} from '@/utils/mall/goods-image-url'

interface Props {
    visible: boolean
    couponId?: number
    showActions?: boolean
  }

  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'edit', couponId?: number): void
    (e: 'submit'): void
  }

  const props = withDefaults(defineProps<Props>(), {
    showActions: true
  })
  const emit = defineEmits<Emits>()

  const { hasAuth } = useAuth()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const loading = ref(false)
  const submittingApproval = ref(false)
  const detail = ref<CouponVo | null>(null)
  const imageUrl = ref('')
  const levelOptions = ref<MemberLevelVo[]>([])

  const couponTypeLabel = (v?: number) =>
    COUPON_TYPE_OPTIONS.find((o) => o.value === v)?.label || '-'

  const useTypeLabel = (v?: number) =>
    COUPON_USE_TYPE_OPTIONS.find((o) => o.value === v)?.label || '-'

  const memberLevelLabel = (id?: number, couponType?: number) => {
    if (couponType !== 1 || id == null || id <= 0) return '-'
    return levelOptions.value.find((lv) => lv.id === id)?.name || String(id)
  }

  const runStatusMeta = computed(() => {
    if (!detail.value) return COUPON_RUN_STATUS_MAP.running
    return COUPON_RUN_STATUS_MAP[getCouponRunStatus(detail.value)]
  })

  const claimStatusMeta = computed(() => {
    if (!detail.value) return COUPON_PHASE_STATUS_MAP.active
    return COUPON_PHASE_STATUS_MAP[getCouponClaimStatus(detail.value)]
  })

  const useStatusMeta = computed(() => {
    if (!detail.value) return COUPON_PHASE_STATUS_MAP.active
    return COUPON_PHASE_STATUS_MAP[getCouponUseStatus(detail.value)]
  })

  const loadImage = async (raw?: string) => {
    imageUrl.value = ''
    if (!raw) return
    imageUrl.value = await resolveGoodsImageDisplayUrl(raw)
  }

  watch(
    () => [props.visible, props.couponId] as const,
    async ([visible, id]) => {
      if (!visible || id == null) {
        detail.value = null
        imageUrl.value = ''
        return
      }
      loading.value = true
      try {
        const [coupon, levels] = await Promise.all([fetchCouponById(id), fetchMemberLevelList()])
        detail.value = coupon
        levelOptions.value = levels
        await loadImage(coupon.couponImg)
      } finally {
        loading.value = false
      }
    }
  )

  const handleTogglePublish = async () => {
    if (!detail.value?.id) return
    await ElMessageBox.confirm(
      `确定下架优惠券「${detail.value.couponName}」吗？下架后用户将无法继续领取。`,
      '下架优惠券',
      { type: 'warning' }
    )
    await fetchCouponPublishStatus(detail.value.id, 0)
    detail.value = { ...detail.value, publish: 0, auditStatus: 'DRAFT' }
    emit('submit')
  }

  const handleSubmitApproval = async () => {
    if (!detail.value?.id) return
    await ElMessageBox.confirm(
      `确定提交优惠券「${detail.value.couponName}」发布审批吗？`,
      '提交审批',
      { type: 'info' }
    )
    submittingApproval.value = true
    try {
      await fetchCouponSubmitApproval(detail.value.id)
      const refreshed = await fetchCouponById(detail.value.id)
      detail.value = refreshed
      emit('submit')
    } finally {
      submittingApproval.value = false
    }
  }
</script>

<style lang="scss" scoped>
  .detail-block {
    margin-bottom: 16px;
  }

  .section-title {
    margin: 0 0 8px;
    font-size: 14px;
    font-weight: 600;
  }

  .coupon-image {
    width: 120px;
    height: 120px;
    border-radius: 6px;
  }

  .image-loading {
    font-size: 13px;
    color: var(--art-gray-500);
  }

  .drawer-actions {
    display: flex;
    gap: 8px;
    margin-top: 20px;
  }
</style>
