<!-- 分类热门：无独立后端表，复用专题活动 -->
<template>
  <div class="cms-category-hot-page art-full-height">
    <ElCard shadow="never">
      <ElResult icon="info" title="分类热门">
        <template #sub-title>
          <p>当前系统无独立的「分类热门」数据表，请使用<strong>优惠营销 → 专题活动</strong>配置首页专题及关联商品。</p>
          <p class="hint">专题活动对应表 <code>sms_home_subject</code> + <code>sms_home_subject_spu</code>。</p>
        </template>
        <template #extra>
          <ElButton type="primary" @click="goSubject">前往专题活动</ElButton>
        </template>
      </ElResult>
    </ElCard>
  </div>
</template>

<script setup lang="ts">
  import { useRouter } from 'vue-router'

  defineOptions({ name: 'CmsCategoryHot' })

  const router = useRouter()

  function goSubject() {
    router.push('/mall/sms/coupon/subject/index')
  }
</script>

<style scoped>
  .hint {
    margin-top: 8px;
    font-size: 13px;
    color: var(--el-text-color-secondary);
  }
  code {
    padding: 2px 6px;
    font-size: 12px;
    background: var(--el-fill-color-light);
    border-radius: 4px;
  }
</style>
