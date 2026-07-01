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

      <section v-if="detail.replies?.length" class="reply-section">
        <h4 class="reply-section__title">回复记录</h4>
        <div v-for="reply in detail.replies" :key="reply.id" class="reply-item">
          <p class="reply-item__meta">
            <strong>{{ reply.memberNickName || '客服' }}</strong>
            <span>{{ reply.createTime }}</span>
          </p>
          <p class="reply-item__content">{{ reply.content }}</p>
        </div>
      </section>

      <section v-if="hasAuth('mall:comment:edit')" class="reply-form">
        <h4 class="reply-section__title">商家回复</h4>
        <ElInput
          v-model="replyContent"
          type="textarea"
          :rows="3"
          maxlength="500"
          show-word-limit
          placeholder="输入回复内容"
        />
        <ElButton
          type="primary"
          class="reply-form__btn"
          :loading="replying"
          :disabled="!replyContent.trim()"
          @click="handleReply"
        >
          提交回复
        </ElButton>
      </section>
    </template>
  </ElDrawer>
</template>

<script setup lang="ts">
import {COMMENT_SHOW_STATUS_MAP, type CommentVo, fetchCommentById, fetchCommentReply} from '@/api/mall/comment'
import {useAuth} from '@/hooks/core/useAuth'

interface Props {
    visible: boolean
    commentId?: number
  }
  interface Emits {
    (e: 'update:visible', value: boolean): void
    (e: 'replied'): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()
  const { hasAuth } = useAuth()

  const drawerVisible = computed({
    get: () => props.visible,
    set: (value) => emit('update:visible', value)
  })

  const loading = ref(false)
  const replying = ref(false)
  const detail = ref<CommentVo | null>(null)
  const replyContent = ref('')

  async function loadDetail(id: number) {
    loading.value = true
    try {
      detail.value = await fetchCommentById(id)
    } finally {
      loading.value = false
    }
  }

  async function handleReply() {
    if (!detail.value?.id || !replyContent.value.trim()) return
    replying.value = true
    try {
      await fetchCommentReply({
        commentId: detail.value.id,
        content: replyContent.value.trim()
      })
      replyContent.value = ''
      await loadDetail(detail.value.id)
      emit('replied')
    } finally {
      replying.value = false
    }
  }

  watch(
    () => [props.visible, props.commentId] as const,
    async ([visible, id]) => {
      if (!visible || id == null) {
        detail.value = null
        replyContent.value = ''
        return
      }
      await loadDetail(id)
    }
  )
</script>

<style scoped>
  .mb-4 {
    margin-bottom: 16px;
  }

  .reply-section__title {
    margin: 0 0 12px;
    font-size: 14px;
    font-weight: 600;
  }

  .reply-item {
    padding: 10px 12px;
    margin-bottom: 8px;
    border-radius: 8px;
    background: var(--el-fill-color-light);
  }

  .reply-item__meta {
    display: flex;
    justify-content: space-between;
    gap: 12px;
    margin: 0 0 6px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .reply-item__content {
    margin: 0;
    font-size: 13px;
    line-height: 1.5;
  }

  .reply-form {
    margin-top: 16px;
  }

  .reply-form__btn {
    margin-top: 10px;
  }
</style>
