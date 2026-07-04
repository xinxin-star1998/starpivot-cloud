<template>
  <ElDrawer v-model="drawerVisible" title="会员统计详情" size="520px" destroy-on-close>
    <ElSkeleton v-if="loading" :rows="8" animated />
    <template v-else-if="detail">
      <ElDescriptions :column="1" border size="small" class="mb-4">
        <ElDescriptionsItem label="会员 ID">{{ detail.memberId ?? '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="用户名">{{ detail.username || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="昵称">{{ detail.nickname || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="手机号">{{ detail.mobile || '-' }}</ElDescriptionsItem>
      </ElDescriptions>

      <ElDivider content-position="left">消费与订单</ElDivider>
      <ElDescriptions :column="2" border size="small" class="mb-4">
        <ElDescriptionsItem label="累计消费">
          {{ formatAmount(detail.consumeAmount) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="累计优惠">
          {{ formatAmount(detail.couponAmount) }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="订单数">{{ detail.orderCount ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="退货数">{{ detail.returnOrderCount ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="优惠券数">{{ detail.couponCount ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="评价数">{{ detail.commentCount ?? 0 }}</ElDescriptionsItem>
      </ElDescriptions>

      <ElDivider content-position="left">行为与收藏</ElDivider>
      <ElDescriptions :column="2" border size="small" class="mb-4">
        <ElDescriptionsItem label="登录次数">{{ detail.loginCount ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="关注数">{{ detail.attendCount ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="粉丝数">{{ detail.fansCount ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="邀请好友">{{ detail.inviteFriendCount ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="收藏商品">
          {{ detail.collectProductCount ?? 0 }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="收藏专题">
          {{ detail.collectSubjectCount ?? 0 }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="收藏评论">
          {{ detail.collectCommentCount ?? 0 }}
        </ElDescriptionsItem>
      </ElDescriptions>

      <ElSpace v-if="hasAuth('mall:member:statistics')">
        <ElButton type="primary" :loading="refreshLoading" @click="doRefresh">刷新统计</ElButton>
      </ElSpace>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
import {
  fetchMemberStatisticsByMemberId,
  fetchMemberStatisticsRefresh,
  type MemberStatisticsVo
} from '@/api/mall/member-statistics'
import {useAuth} from '@/hooks/core/useAuth'
import {ElMessage} from 'element-plus'

interface Props {
    visible: boolean
    memberId?: number
  }
  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'submit'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const { hasAuth } = useAuth()

  const loading = ref(false)
  const refreshLoading = ref(false)
  const detail = ref<MemberStatisticsVo>()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (val) => emit('update:visible', val)
  })

  watch(
    () => [props.visible, props.memberId] as const,
    ([visible, memberId]) => {
      if (visible && memberId) {
        loadDetail(memberId)
      } else {
        detail.value = undefined
      }
    },
    { immediate: true }
  )

  function formatAmount(value?: number) {
    if (value == null) return '0.00'
    return Number(value).toFixed(2)
  }

  async function loadDetail(memberId: number) {
    loading.value = true
    try {
      detail.value = await fetchMemberStatisticsByMemberId(memberId)
    } finally {
      loading.value = false
    }
  }

  async function doRefresh() {
    if (!props.memberId) return
    refreshLoading.value = true
    try {
      await fetchMemberStatisticsRefresh(props.memberId)
      ElMessage.success('刷新成功')
      await loadDetail(props.memberId)
      emit('submit')
    } finally {
      refreshLoading.value = false
    }
  }
</script>
