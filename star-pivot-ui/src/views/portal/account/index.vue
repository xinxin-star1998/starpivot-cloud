<!-- C 端会员中心 -->
<template>
  <div v-loading="loading" class="portal-account">
    <PortalPageHeader title="会员中心" subtitle="管理账户、订单与收藏" />

    <template v-if="center">
      <section class="profile-card">
        <div class="profile-card__avatar">
          <img v-if="avatarUrl" :src="avatarUrl" alt="" />
          <ArtSvgIcon v-else icon="ri:user-3-line" />
        </div>
        <div class="profile-card__info">
          <h2>{{ center.member.nickname || center.member.username }}</h2>
          <p v-if="center.levelName" class="level-tag">{{ center.levelName }}</p>
          <p v-if="center.member.sign" class="sign">{{ center.member.sign }}</p>
          <div class="points">
            <span>积分 {{ center.member.integration ?? 0 }}</span>
            <span>成长值 {{ center.member.growth ?? 0 }}</span>
          </div>
        </div>
        <ElButton type="primary" plain round @click="openProfileDialog">编辑资料</ElButton>
      </section>

      <section class="stats-grid">
        <button type="button" class="stat-item" @click="router.push('/portal/orders')">
          <strong>{{ center.orderCount ?? 0 }}</strong>
          <span>我的订单</span>
        </button>
        <button type="button" class="stat-item" @click="router.push('/portal/coupons')">
          <strong>{{ center.couponCount ?? 0 }}</strong>
          <span>优惠券</span>
        </button>
        <button type="button" class="stat-item" @click="router.push('/portal/account/favorites')">
          <strong>{{ center.collectCount ?? 0 }}</strong>
          <span>我的收藏</span>
        </button>
        <button type="button" class="stat-item" @click="router.push('/portal/account/pending-reviews')">
          <strong>{{ center.pendingReviewCount ?? 0 }}</strong>
          <span>待评价</span>
        </button>
        <button type="button" class="stat-item" @click="router.push('/portal/account/reviews')">
          <strong>{{ center.commentCount ?? 0 }}</strong>
          <span>已评价</span>
        </button>
      </section>

      <section class="menu-grid">
        <button type="button" class="menu-item" @click="router.push('/portal/orders')">
          <ArtSvgIcon icon="ri:file-list-3-line" />
          <span>全部订单</span>
        </button>
        <button type="button" class="menu-item" @click="router.push('/portal/account/favorites')">
          <ArtSvgIcon icon="ri:heart-line" />
          <span>我的收藏</span>
        </button>
        <button type="button" class="menu-item" @click="router.push('/portal/account/history')">
          <ArtSvgIcon icon="ri:history-line" />
          <span>浏览足迹</span>
        </button>
        <button type="button" class="menu-item" @click="router.push('/portal/account/pending-reviews')">
          <ArtSvgIcon icon="ri:edit-box-line" />
          <span>待评价</span>
          <span v-if="(center.pendingReviewCount ?? 0) > 0" class="menu-badge">
            {{ center.pendingReviewCount }}
          </span>
        </button>
        <button type="button" class="menu-item" @click="router.push('/portal/account/reviews')">
          <ArtSvgIcon icon="ri:chat-smile-2-line" />
          <span>我的评价</span>
        </button>
        <button type="button" class="menu-item" @click="router.push('/portal/account/addresses')">
          <ArtSvgIcon icon="ri:map-pin-line" />
          <span>收货地址</span>
        </button>
        <button type="button" class="menu-item" @click="router.push('/portal/coupons')">
          <ArtSvgIcon icon="ri:coupon-3-line" />
          <span>优惠券</span>
        </button>
        <button type="button" class="menu-item" @click="router.push('/portal/account/security')">
          <ArtSvgIcon icon="ri:shield-keyhole-line" />
          <span>账号安全</span>
        </button>
      </section>
    </template>

    <ElDialog v-model="profileVisible" title="编辑资料" width="420px" destroy-on-close>
      <ElForm label-width="72px">
        <ElFormItem label="昵称">
          <ElInput v-model="profileForm.nickname" maxlength="64" show-word-limit />
        </ElFormItem>
        <ElFormItem label="头像">
          <div class="avatar-upload">
            <div class="avatar-upload__preview">
              <img v-if="profileAvatarPreview" :src="profileAvatarPreview" alt="" />
              <ArtSvgIcon v-else icon="ri:user-3-line" />
            </div>
            <div class="avatar-upload__actions">
              <ElButton size="small" :loading="uploadingAvatar" @click="triggerAvatarUpload">
                上传头像
              </ElButton>
              <input
                ref="avatarInputRef"
                type="file"
                accept="image/*"
                hidden
                @change="handleAvatarSelect"
              />
              <p class="avatar-upload__tip">支持 JPG/PNG，不超过 5MB</p>
            </div>
          </div>
        </ElFormItem>
        <ElFormItem label="性别">
          <ElRadioGroup v-model="profileForm.gender">
            <ElRadio :value="0">未知</ElRadio>
            <ElRadio :value="1">男</ElRadio>
            <ElRadio :value="2">女</ElRadio>
          </ElRadioGroup>
        </ElFormItem>
        <ElFormItem label="签名">
          <ElInput v-model="profileForm.sign" type="textarea" :rows="3" maxlength="200" show-word-limit />
        </ElFormItem>
      </ElForm>
      <template #footer>
        <ElButton @click="profileVisible = false">取消</ElButton>
        <ElButton type="primary" :loading="savingProfile" @click="saveProfile">保存</ElButton>
      </template>
    </ElDialog>
  </div>
</template>

<script setup lang="ts">
import {fetchPortalMemberCenter, fetchPortalMemberProfileUpdate} from '@/api/portal/member'
import {uploadPortalCommentImage} from '@/api/portal/image'
import type {PortalMemberCenter} from '@/api/portal/types'
import PortalPageHeader from '@/views/portal/components/portal-page-header.vue'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import {usePortalAuth} from '@/hooks/portal/usePortalAuth'
import {ElMessage} from 'element-plus'
import {resolveGoodsImageDisplayUrls} from '@/utils/mall/goods-image-url'

defineOptions({ name: 'PortalAccount' })

  const router = useRouter()
  const { requireLogin, portalStore } = usePortalAuth()

  const loading = ref(true)
  const center = ref<PortalMemberCenter | null>(null)
  const avatarUrl = ref('')
  const profileVisible = ref(false)
  const savingProfile = ref(false)
  const uploadingAvatar = ref(false)
  const avatarInputRef = ref<HTMLInputElement>()
  const profileAvatarPreview = ref('')
  const profileForm = reactive({
    nickname: '',
    header: '',
    gender: 0,
    sign: ''
  })

  async function loadCenter() {
    if (!requireLogin()) return
    center.value = await fetchPortalMemberCenter()
    portalStore.setMember(center.value.member)
    if (center.value.member.header) {
      const map = await resolveGoodsImageDisplayUrls([center.value.member.header])
      avatarUrl.value = map.get(center.value.member.header) || center.value.member.header
    } else {
      avatarUrl.value = ''
    }
  }

  async function resolveProfileAvatarPreview(header?: string) {
    if (!header) {
      profileAvatarPreview.value = ''
      return
    }
    const map = await resolveGoodsImageDisplayUrls([header])
    profileAvatarPreview.value = map.get(header) || header
  }

  function openProfileDialog() {
    if (!center.value) return
    profileForm.nickname = center.value.member.nickname || ''
    profileForm.header = center.value.member.header || ''
    profileForm.gender = center.value.member.gender ?? 0
    profileForm.sign = center.value.member.sign || ''
    resolveProfileAvatarPreview(profileForm.header)
    profileVisible.value = true
  }

  function triggerAvatarUpload() {
    avatarInputRef.value?.click()
  }

  async function handleAvatarSelect(event: Event) {
    const input = event.target as HTMLInputElement
    const file = input.files?.[0]
    input.value = ''
    if (!file) return
    if (!file.type.startsWith('image/')) {
      ElMessage.warning('请选择图片文件')
      return
    }
    uploadingAvatar.value = true
    try {
      const res = await uploadPortalCommentImage(file)
      profileForm.header = res.objectName
      profileAvatarPreview.value = res.displayUrl || res.presignedUrl || res.objectName
    } finally {
      uploadingAvatar.value = false
    }
  }

  watch(profileVisible, (visible) => {
    if (visible) openProfileDialog()
  })

  async function saveProfile() {
    savingProfile.value = true
    try {
      const member = await fetchPortalMemberProfileUpdate({ ...profileForm })
      portalStore.setMember(member)
      if (center.value) center.value.member = member
      profileVisible.value = false
      await loadCenter()
    } finally {
      savingProfile.value = false
    }
  }

  onMounted(async () => {
    try {
      await loadCenter()
    } finally {
      loading.value = false
    }
    window.addEventListener('portal-review-changed', loadCenter)
  })

  onUnmounted(() => {
    window.removeEventListener('portal-review-changed', loadCenter)
  })
</script>

<style scoped lang="scss">
  @import '../styles/variables.scss';

  .profile-card {
    display: flex;
    align-items: center;
    gap: 20px;
    padding: 28px;
    margin-bottom: 20px;
    background: var(--portal-bg-elevated);
    border-radius: var(--portal-radius-lg);
    border: 1px solid var(--portal-border);
    box-shadow: var(--portal-shadow-sm);

    &__avatar {
      width: 80px;
      height: 80px;
      border-radius: 50%;
      overflow: hidden;
      background: var(--portal-primary-light);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      svg {
        font-size: 36px;
        color: var(--portal-primary);
      }
    }

    &__info {
      flex: 1;
      min-width: 0;

      h2 {
        margin: 0 0 6px;
        font-size: 22px;
        font-weight: 700;
      }

      .level-tag {
        display: inline-block;
        margin: 0 0 8px;
        padding: 2px 10px;
        border-radius: 12px;
        background: var(--portal-primary-light);
        color: var(--portal-primary);
        font-size: 12px;
        font-weight: 600;
      }

      .sign {
        margin: 0 0 8px;
        font-size: 13px;
        color: var(--portal-text-secondary);
      }

      .points {
        display: flex;
        gap: 16px;
        font-size: 13px;
        color: var(--portal-text-muted);
      }
    }
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 16px;
    margin-bottom: 20px;

    @media (width <= 992px) {
      grid-template-columns: repeat(3, 1fr);
    }

    @media (width <= 768px) {
      grid-template-columns: repeat(2, 1fr);
    }
  }

  .stat-item {
    padding: 20px;
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius);
    background: var(--portal-bg-elevated);
    cursor: pointer;
    transition: all var(--portal-transition);
    text-align: center;

    strong {
      display: block;
      font-size: 28px;
      color: var(--portal-primary);
      margin-bottom: 4px;
    }

    span {
      font-size: 13px;
      color: var(--portal-text-secondary);
    }

    &:hover {
      border-color: var(--portal-primary);
      box-shadow: var(--portal-shadow-sm);
    }
  }

  .menu-grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
    gap: 12px;
  }

  .menu-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    padding: 20px 12px;
    border: 1px solid var(--portal-border);
    border-radius: var(--portal-radius);
    background: var(--portal-bg-elevated);
    cursor: pointer;
    transition: all var(--portal-transition);
    color: var(--portal-text-secondary);
    font-size: 13px;
    position: relative;

    .menu-badge {
      position: absolute;
      top: 8px;
      right: 8px;
      min-width: 18px;
      height: 18px;
      padding: 0 5px;
      border-radius: 9px;
      background: var(--portal-primary);
      color: #fff;
      font-size: 11px;
      line-height: 18px;
      font-weight: 700;
    }

    svg {
      font-size: 26px;
      color: var(--portal-primary);
    }

    &:hover {
      border-color: var(--portal-primary);
      background: var(--portal-primary-light);
      color: var(--portal-primary);
    }
  }

  .avatar-upload {
    display: flex;
    align-items: center;
    gap: 16px;

    &__preview {
      width: 72px;
      height: 72px;
      border-radius: 50%;
      overflow: hidden;
      background: var(--portal-primary-light);
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;

      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }

      svg {
        font-size: 32px;
        color: var(--portal-primary);
      }
    }

    &__tip {
      margin: 6px 0 0;
      font-size: 12px;
      color: var(--portal-text-muted);
    }
  }
</style>
