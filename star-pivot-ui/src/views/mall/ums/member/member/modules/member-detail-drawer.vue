<template>
  <ElDrawer v-model="drawerVisible" title="会员详情" size="520px" destroy-on-close>
    <ElSkeleton v-if="loading" :rows="8" animated />
    <template v-else-if="detail">
      <ElDescriptions :column="1" border size="small">
        <ElDescriptionsItem label="会员 ID">{{ detail.id }}</ElDescriptionsItem>
        <ElDescriptionsItem label="用户名">{{ detail.username || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="昵称">{{ detail.nickname || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="手机号">{{ detail.mobile || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="邮箱">{{ detail.email || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="性别">
          {{ MEMBER_GENDER_MAP[detail.gender ?? 0] ?? detail.gender ?? '-' }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="积分">{{ detail.integration ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="成长值">{{ detail.growth ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="等级 ID">{{ detail.levelId ?? '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="状态">
          {{ MEMBER_STATUS_MAP[detail.status ?? 0] ?? detail.status }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="注册时间">{{ formatDateTime(detail.createTime) }}</ElDescriptionsItem>
        <ElDescriptionsItem label="城市">{{ detail.city || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="签名">{{ detail.sign || '-' }}</ElDescriptionsItem>
      </ElDescriptions>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
import {fetchMemberById, MEMBER_GENDER_MAP, MEMBER_STATUS_MAP, type MemberVo} from '@/api/mall/member'
import {formatDateTime} from '@/utils/common/datetime'

interface Props {
    visible: boolean
    memberId?: number
  }
  interface Emits {
    (e: 'update:visible', value: boolean): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const loading = ref(false)
  const detail = ref<MemberVo | null>(null)

  watch(
    () => [props.visible, props.memberId] as const,
    async ([visible, id]) => {
      if (!visible || id == null) {
        detail.value = null
        return
      }
      loading.value = true
      try {
        detail.value = await fetchMemberById(id)
      } finally {
        loading.value = false
      }
    }
  )
</script>
