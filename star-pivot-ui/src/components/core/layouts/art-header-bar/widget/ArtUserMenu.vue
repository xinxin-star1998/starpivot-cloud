<!-- 用户菜单 -->
<template>
  <ElPopover
    ref="userMenuPopover"
    placement="bottom-end"
    :width="240"
    :hide-after="0"
    :offset="10"
    trigger="hover"
    :show-arrow="false"
    popper-class="user-menu-popover"
    popper-style="padding: 5px 16px;"
  >
    <template #reference>
      <ArtAvatarDisplay
        :key="avatarDisplayKey"
        :avatar-url="avatarUrl"
        :size="34"
        avatar-class="mr-5 c-p max-sm:mr-[16px] object-cover"
      />
    </template>
    <template #default>
      <div class="pt-3">
        <div class="flex-c pb-1 px-0">
          <ArtAvatarDisplay
            :key="avatarDisplayKey"
            :avatar-url="avatarUrl"
            :size="40"
            avatar-class="mr-3 ml-0 overflow-hidden float-left object-cover"
          />
          <div class="w-[calc(100%-60px)] h-full">
            <span class="block text-sm font-medium text-g-800 truncate">
              {{ userInfo.user?.username || userInfo.user?.nickName || '' }}
            </span>
            <span class="block mt-0.5 text-xs text-g-500 truncate">
              {{ userInfo.user?.email }}
            </span>
          </div>
        </div>
        <ul class="py-4 mt-3 border-t border-g-300/80">
          <li class="btn-item" @click="goPage('/system/user-center')">
            <ArtSvgIcon icon="ri:user-3-line" />
            <span>{{ t('topBar.user.userCenter') }}</span>
          </li>
          <!--          <li class="btn-item" @click="toDocs()">-->
          <!--            <ArtSvgIcon icon="ri:book-2-line" />-->
          <!--            <span>{{ t('topBar.user.docs') }}</span>-->
          <!--          </li>-->
          <!--          <li class="btn-item" @click="toGithub()">-->
          <!--            <ArtSvgIcon icon="ri:gitee-line" />-->
          <!--            <span>{{ t('topBar.user.github') }}</span>-->
          <!--          </li>-->
          <li class="btn-item" @click="lockScreen()">
            <ArtSvgIcon icon="ri:lock-line" />
            <span>{{ t('topBar.user.lockScreen') }}</span>
          </li>
          <div class="w-full h-px my-2 bg-g-300/80"></div>
          <div class="log-out c-p" @click="loginOut">
            {{ t('topBar.user.logout') }}
          </div>
        </ul>
      </div>
    </template>
  </ElPopover>
</template>

<script setup lang="ts">
import {useI18n} from 'vue-i18n'
import {useRouter} from 'vue-router'
import {ElMessageBox} from 'element-plus'
import {storeToRefs} from 'pinia'
import {useUserStore} from '@/store/modules/user'
import {mittBus} from '@/utils/sys'
import {fetchLogout} from '@/api/auth'
import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'
import ArtAvatarDisplay from '@/components/core/media/art-avatar-display/index.vue'
import defaultAvatarImg from '@imgs/user/avatar.webp'

defineOptions({ name: 'ArtUserMenu' })

  const router = useRouter()
  const { t } = useI18n()
  const userStore = useUserStore()

  const { getUserInfo: userInfo } = storeToRefs(userStore)
  const userMenuPopover = ref()

  /** 用户头像：优先顶层 avatar（本地更新后），否则 user.avatar，无则用默认图；展示由 ArtAvatarDisplay 内部处理 OSS 私有桶 presigned */
  const avatarUrl = computed(() => {
    const u = userInfo.value as any
    // 优先使用顶层 avatar（更新后的新值），其次使用 user.avatar（登录时的旧值）
    const topAvatar = u?.avatar
    const userAvatar = u?.user?.avatar
    // 如果顶层 avatar 存在且不为空，使用它；否则使用 user.avatar
    const url = topAvatar && String(topAvatar).trim() ? topAvatar : userAvatar
    return url && String(url).trim() ? url : defaultAvatarImg
  })

  /** 用于 :key，头像或版本变化时强制重新渲染（修复 ElPopover 下拉内头像不更新） */
  const avatarDisplayKey = computed(() => {
    const u = userInfo.value as any
    const v = u?.avatarUpdatedAt ?? u?.user?.avatarUpdatedAt ?? ''
    return `${String(avatarUrl.value)}|${v}`
  })

  /**
   * 页面跳转
   * @param {string} path - 目标路径
   */
  const goPage = (path: string): void => {
    router.push(path)
  }

  /**
   * 打开锁屏功能
   */
  const lockScreen = (): void => {
    mittBus.emit('openLockScreen')
  }

  /**
   * 用户登出确认
   */
  const loginOut = (): void => {
    closeUserMenu()
    setTimeout(() => {
      ElMessageBox.confirm(t('common.logOutTips'), t('common.tips'), {
        confirmButtonText: t('common.confirm'),
        cancelButtonText: t('common.cancel'),
        customClass: 'login-out-dialog'
      }).then(() => {
        // 手动登出：尽力通知后端加入 token 黑名单（失败也不影响前端清理）
        fetchLogout()
          .catch(() => void 0)
          .finally(() => userStore.logOut())
      })
    }, 200)
  }

  /**
   * 关闭用户菜单弹出层
   */
  const closeUserMenu = (): void => {
    setTimeout(() => {
      userMenuPopover.value.hide()
    }, 100)
  }
</script>

<style scoped>
  @reference '@styles/core/tailwind.css';

  @layer components {
    .btn-item {
      @apply flex items-center p-2 mb-3 select-none rounded-md cursor-pointer last:mb-0;

      span {
        @apply text-sm;
      }

      .art-svg-icon {
        @apply mr-2 text-base;
      }

      &:hover {
        background-color: var(--art-gray-200);
      }
    }
  }

  .log-out {
    @apply py-1.5
    mt-5
    text-xs
    text-center
    border
    border-g-400
    rounded-md
    transition-all
    duration-200
    hover:shadow-xl;
  }
</style>
