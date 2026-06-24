<template>
  <ElDrawer v-model="drawerVisible" title="评论详情" size="560px" destroy-on-close>
    <ElSkeleton v-if="loading" :rows="8" animated />
    <template v-else-if="detail">
      <ElDescriptions :column="1" border size="small" class="mb-4">
        <ElDescriptionsItem label="评论 ID">{{ detail.id }}</ElDescriptionsItem>
        <ElDescriptionsItem label="SPU">{{ detail.spuName || detail.spuId }}</ElDescriptionsItem>
        <ElDescriptionsItem label="SKU ID">{{ detail.skuId ?? '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="会员">{{ detail.memberNickName || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="评分">
          <ElRate :model-value="detail.star ?? 0" disabled />
        </ElDescriptionsItem>
        <ElDescriptionsItem label="展示状态">
          {{ COMMENT_SHOW_STATUS_MAP[detail.showStatus ?? 0] ?? detail.showStatus }}
        </ElDescriptionsItem>
        <ElDescriptionsItem label="规格">{{ detail.spuAttributes || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="点赞">{{ detail.likesCount ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="回复数">{{ detail.replyCount ?? 0 }}</ElDescriptionsItem>
        <ElDescriptionsItem label="IP">{{ detail.memberIp || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="时间">{{ detail.createTime || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem label="内容">{{ detail.content || '-' }}</ElDescriptionsItem>
        <ElDescriptionsItem v-if="detail.resources" label="图片">{{ detail.resources }}</ElDescriptionsItem>
      </ElDescriptions>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
  import {
    fetchCommentById,
    COMMENT_SHOW_STATUS_MAP,
    type CommentVo
  } from '@/api/mall/comment'

  interface Props {
    visible: boolean
    commentId?: number
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
  const detail = ref<CommentVo | null>(null)

  watch(
    () => [props.visible, props.commentId] as const,
    async ([visible, id]) => {
      if (!visible || id == null) {
        detail.value = null
        return
      }
      loading.value = true
      try {
        detail.value = await fetchCommentById(id)
      } finally {
        loading.value = false
      }
    }
  )
</script>

<style scoped>
  .mb-4 {
    margin-bottom: 16px;
  }
</style>
