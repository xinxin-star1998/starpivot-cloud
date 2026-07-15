<!-- 个人中心页面 -->
<template>
  <div class="w-full h-full p-0 bg-transparent border-none shadow-none">
    <div v-loading="loading" class="relative flex-b mt-2.5 max-md:block max-md:mt-1">
      <div class="w-112 mr-5 max-md:w-full max-md:mr-0">
        <div class="art-card-sm relative p-0 overflow-hidden">
          <div class="relative h-48 bg-gradient-to-r from-blue-500 to-purple-600">
            <img
              class="absolute top-0 left-0 w-full h-full object-cover opacity-80"
              :src="bgImage"
            />
            <div class="absolute inset-0 bg-gradient-to-b from-transparent to-black/30"></div>
          </div>
          <div class="relative px-6 pb-8 -mt-16 text-center">
            <div class="relative inline-block">
              <ArtAvatarUpload
                class="user-center-top-avatar transition-all duration-300 hover:scale-105"
                :model-value="topAvatarDisplayUrl"
                :user-id="form.userId"
                :size="112"
                :auto-upload="true"
                use-presigned-url
                @update:model-value="(val) => (topAvatarDisplayUrl = val)"
                @success="handleAvatarUploadSuccess"
                @error="handleAvatarUploadError"
              />
              <div
                class="absolute bottom-1 right-1 w-6 h-6 rounded-full border-2 transition-colors"
                :class="isDark ? 'bg-green-500 border-g-700' : 'bg-green-500 border-white'"
              ></div>
            </div>
            <h2
              class="mt-4 text-2xl font-semibold transition-colors"
              :class="isDark ? 'text-g-100' : 'text-g-900'"
            >
              {{ userDetail.userName || userInfo.user?.username }}
            </h2>
            <p class="mt-2 text-sm transition-colors" :class="isDark ? 'text-g-400' : 'text-g-600'">
              {{ userDetail.remark || '专注于用户体验跟视觉设计' }}
            </p>

            <div class="mt-6 space-y-3">
              <div
                class="flex items-center justify-center p-3 rounded-lg transition-colors"
                v-if="userDetail.email"
                :class="isDark ? 'bg-g-800/50 hover:bg-g-800/70' : 'bg-g-100 hover:bg-g-200'"
              >
                <ArtSvgIcon
                  icon="ri:mail-line"
                  class="text-lg transition-colors"
                  :class="isDark ? 'text-blue-400' : 'text-blue-600'"
                />
                <span
                  class="ml-3 text-sm font-medium transition-colors"
                  :class="isDark ? 'text-g-200' : 'text-g-700'"
                >
                  {{ userDetail.email }}
                </span>
              </div>
              <div
                class="flex items-center justify-center p-3 rounded-lg transition-colors"
                v-if="userDetail.nickName"
                :class="isDark ? 'bg-g-800/50 hover:bg-g-800/70' : 'bg-g-100 hover:bg-g-200'"
              >
                <ArtSvgIcon
                  icon="ri:user-3-line"
                  class="text-lg transition-colors"
                  :class="isDark ? 'text-purple-400' : 'text-purple-600'"
                />
                <span
                  class="ml-3 text-sm font-medium transition-colors"
                  :class="isDark ? 'text-g-200' : 'text-g-700'"
                >
                  {{ userDetail.nickName }}
                </span>
              </div>
              <div
                class="flex items-center justify-center p-3 rounded-lg transition-colors"
                v-if="userDetail.phonenumber"
                :class="isDark ? 'bg-g-800/50 hover:bg-g-800/70' : 'bg-g-100 hover:bg-g-200'"
              >
                <ArtSvgIcon
                  icon="ri:phone-line"
                  class="text-lg transition-colors"
                  :class="isDark ? 'text-green-400' : 'text-green-600'"
                />
                <span
                  class="ml-3 text-sm font-medium transition-colors"
                  :class="isDark ? 'text-g-200' : 'text-g-700'"
                >
                  {{ userDetail.phonenumber }}
                </span>
              </div>
              <div
                class="flex items-center justify-center p-3 rounded-lg transition-colors"
                v-if="userDetail.roleName"
                :class="isDark ? 'bg-g-800/50 hover:bg-g-800/70' : 'bg-g-100 hover:bg-g-200'"
              >
                <ArtSvgIcon
                  icon="ri:dribbble-fill"
                  class="text-lg transition-colors"
                  :class="isDark ? 'text-orange-400' : 'text-orange-600'"
                />
                <span
                  class="ml-3 text-sm font-medium transition-colors"
                  :class="isDark ? 'text-g-200' : 'text-g-700'"
                >
                  {{ userDetail.roleName }}
                </span>
              </div>
            </div>

            <div class="mt-6" v-if="userDetail.userRoles && userDetail.userRoles.length > 0">
              <h3
                class="mb-3 text-sm font-semibold transition-colors"
                :class="isDark ? 'text-g-200' : 'text-g-800'"
              >
                角色
              </h3>
              <div class="flex flex-wrap justify-center gap-2">
                <div
                  v-for="role in userDetail.userRoles"
                  :key="role"
                  class="px-3 py-1.5 text-sm font-medium rounded-full transition-all duration-300 hover:scale-105"
                  :class="
                    isDark
                      ? 'bg-gradient-to-r from-blue-600 to-purple-600 text-white shadow-lg'
                      : 'bg-gradient-to-r from-blue-500 to-purple-500 text-white shadow-md'
                  "
                >
                  {{ role }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="flex-1 overflow-hidden max-md:w-full max-md:mt-3.5">
        <div class="art-card-sm">
          <div class="px-6 pt-4 pb-2">
            <h1
              class="text-xl font-semibold transition-colors"
              :class="isDark ? 'text-g-100' : 'text-g-900'"
            >
              基本设置
            </h1>
            <p class="mt-1 text-sm transition-colors" :class="isDark ? 'text-g-400' : 'text-g-500'">
              查看您的基本信息或修改登录密码
            </p>
          </div>

          <ElTabs v-model="activeTab" class="px-6 pb-6 user-center-tabs">
            <ElTabPane label="基本资料" name="basic">
              <ElForm
                :model="form"
                class="box-border pt-4 space-y-6"
                label-width="86px"
                label-position="top"
              >
                <ElRow class="gap-6">
                  <ElFormItem label="用户名" class="flex-1">
                    <ElInput v-model="form.userName" class="transition-all duration-300">
                      <template #prefix>
                        <ArtSvgIcon
                          icon="ri:user-line"
                          class="transition-colors"
                          :class="isDark ? 'text-g-500' : 'text-g-400'"
                        />
                      </template>
                    </ElInput>
                  </ElFormItem>
                  <ElFormItem label="性别" class="flex-1">
                    <ElSelect v-model="form.sex" class="w-full transition-all duration-300">
                      <ElOption
                        v-for="item in options"
                        :key="item.value"
                        :label="item.label"
                        :value="item.value"
                      />
                    </ElSelect>
                  </ElFormItem>
                </ElRow>

                <ElRow class="gap-6">
                  <ElFormItem label="昵称" class="flex-1">
                    <ElInput v-model="form.nickName" class="transition-all duration-300">
                      <template #prefix>
                        <ArtSvgIcon
                          icon="ri:user-smile-line"
                          class="transition-colors"
                          :class="isDark ? 'text-g-500' : 'text-g-400'"
                        />
                      </template>
                    </ElInput>
                  </ElFormItem>
                  <ElFormItem label="邮箱" class="flex-1">
                    <ElInput v-model="form.email" class="transition-all duration-300">
                      <template #prefix>
                        <ArtSvgIcon
                          icon="ri:mail-line"
                          class="transition-colors"
                          :class="isDark ? 'text-g-500' : 'text-g-400'"
                        />
                      </template>
                    </ElInput>
                  </ElFormItem>
                </ElRow>

                <ElRow class="gap-6">
                  <ElFormItem label="手机" class="flex-1">
                    <ElInput v-model="form.phonenumber" class="transition-all duration-300">
                      <template #prefix>
                        <ArtSvgIcon
                          icon="ri:phone-line"
                          class="transition-colors"
                          :class="isDark ? 'text-g-500' : 'text-g-400'"
                        />
                      </template>
                    </ElInput>
                  </ElFormItem>
                </ElRow>

                <ElFormItem label="个人介绍" class="h-32">
                  <ElInput
                    type="textarea"
                    :rows="4"
                    v-model="form.remark"
                    class="transition-all duration-300"
                  >
                    <template #prefix>
                      <ArtSvgIcon
                        icon="ri:file-text-line"
                        class="transition-colors"
                        :class="isDark ? 'text-g-500' : 'text-g-400'"
                      />
                    </template>
                  </ElInput>
                </ElFormItem>
                <ElFormItem>
                  <ElButton type="primary" @click="submitBasicProfile">保存</ElButton>
                  <ElButton plain @click="resetBasicProfile">重置</ElButton>
                </ElFormItem>
              </ElForm>
            </ElTabPane>

            <ElTabPane label="修改密码" name="password">
              <div class="password-panel pt-4">
                <ElForm
                  ref="passwordFormRef"
                  :model="passwordForm"
                  :rules="passwordRules"
                  class="password-panel__form"
                  label-position="top"
                >
                  <ElFormItem label="当前密码" prop="oldPassword">
                    <ElInput
                      v-model="passwordForm.oldPassword"
                      type="password"
                      show-password
                      autocomplete="current-password"
                      placeholder="请输入当前密码"
                    />
                  </ElFormItem>
                  <div class="password-panel__row">
                    <ElFormItem label="新密码" prop="newPassword" class="password-panel__half">
                      <ElInput
                        v-model="passwordForm.newPassword"
                        type="password"
                        show-password
                        autocomplete="new-password"
                        placeholder="请输入新密码"
                      />
                    </ElFormItem>
                    <ElFormItem label="确认新密码" prop="confirmPassword" class="password-panel__half">
                      <ElInput
                        v-model="passwordForm.confirmPassword"
                        type="password"
                        show-password
                        autocomplete="new-password"
                        placeholder="请再次输入新密码"
                      />
                    </ElFormItem>
                  </div>
                  <ElFormItem>
                    <ElButton type="primary" @click="submitPassword">保存</ElButton>
                  </ElFormItem>
                </ElForm>

                <aside class="password-tips" aria-label="密码安全建议">
                  <div class="password-tips__title">安全建议</div>
                  <ul class="password-tips__list">
                    <li>新密码须为 6–20 位，且同时包含字母和数字（仅允许字母与数字）；与用户管理新增、重置密码规则一致。</li>
                    <li>避免使用生日、手机号、工号等容易被猜测的信息。</li>
                    <li>修改成功后请使用新密码重新登录系统。</li>
                  </ul>
                </aside>
              </div>
            </ElTabPane>
            <ElTabPane label="会话管理" name="session">
              <div class="session-management pt-4">
                <!-- 操作按钮 -->
                <div class="flex items-center gap-3 mb-4">
                  <el-button
                    type="danger"
                    :loading="logoutAllLoading"
                    :disabled="sessionList.length === 0"
                    @click="handleLogoutAll"
                  >
                    <el-icon><SwitchButton /></el-icon>
                    强制下线所有会话
                  </el-button>
                  <el-button :loading="sessionLoading" @click="loadSessions">
                    <el-icon><Refresh /></el-icon>
                    刷新
                  </el-button>
                  <el-tag v-if="sessionList.length > 0" type="info" size="small" class="ml-2">
                    共 {{ sessionList.length }} 个活跃会话
                  </el-tag>
                </div>

                <!-- 会话列表 -->
                <el-table
                  v-loading="sessionLoading"
                  :data="sessionList"
                  style="width: 100%"
                  empty-text="暂无活跃会话"
                  :row-class-name="sessionRowClassName"
                >
                  <el-table-column type="index" label="序号" width="60" align="center" />
                  <el-table-column label="设备信息" min-width="200">
                    <template #default="{ row }">
                      <div class="device-info">
                        <span class="font-medium">{{ getDeviceText(row) }}</span>
                        <div class="text-xs text-g-500">
                          <el-icon><Location /></el-icon>
                          {{ row.ipaddr || '未知IP' }}
                        </div>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column
                    label="浏览器"
                    prop="browser"
                    min-width="120"
                    show-overflow-tooltip
                  />
                  <el-table-column
                    label="操作系统"
                    prop="os"
                    min-width="100"
                    show-overflow-tooltip
                  />
                  <el-table-column label="登录时间" min-width="160">
                    <template #default="{ row }">
                      {{ formatDateTime(row.createdAt) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="最后访问" min-width="160">
                    <template #default="{ row }">
                      {{ formatDateTime(row.lastAccessTime) }}
                    </template>
                  </el-table-column>
                  <el-table-column label="会话时长" width="110" align="center">
                    <template #default="{ row }">
                      <el-tag type="info" size="small">{{ row.sessionDuration || '-' }}</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column label="当前会话" width="90" align="center">
                    <template #default="{ row }">
                      <el-tag v-if="isCurrentSession(row)" type="success" size="small">当前</el-tag>
                      <el-tag v-else type="info" size="small">其他</el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column
                    v-if="hasOtherSessions"
                    label="操作"
                    width="90"
                    fixed="right"
                    align="center"
                  >
                    <template #default="{ row }">
                      <el-button
                        v-if="!isCurrentSession(row)"
                        type="danger"
                        size="small"
                        link
                        :loading="row.logoutLoading"
                        @click="handleLogoutSession(row)"
                      >
                        下线
                      </el-button>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
            </ElTabPane>
          </ElTabs>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import {useUserStore} from '@/store/modules/user'
import {fetchGetAvatarPresignedUrl, fetchGetUserById, fetchUpdateUser, fetchUpdateUserPassword} from '@/api/user/user'
import {fetchGetUserInfo, fetchLogout, fetchUserSessions, forceLogoutAllSessions, forceLogoutSession} from '@/api/auth'
import {ElMessage, ElMessageBox, type FormInstance, type FormRules} from 'element-plus'
import {Location, Refresh, SwitchButton} from '@element-plus/icons-vue'
import ArtAvatarUpload from '@/components/core/media/art-avatar-upload/index.vue'
import defaultAvatarImg from '@imgs/user/avatar.webp'
import bgImageImg from '@imgs/user/bg.webp'
import {useSettingStore} from '@/store/modules/setting'
import {extractOssObjectPath, needsOssPresignedDisplay} from '@/utils/storage/oss-object-path'
import dayjs from 'dayjs'
import {logger} from '@/utils/sys/logger'
import {
  ADMIN_PASSWORD_PATTERN,
  ADMIN_PASSWORD_RULE_MESSAGE,
  clearSavedLoginPassword
} from '@/utils/sys/password-prompt-guard'

defineOptions({ name: 'UserCenter' })

  // 主题状态
  const settingStore = useSettingStore()
  const { isDark } = storeToRefs(settingStore)

  // 默认头像和背景图片
  const defaultAvatar = defaultAvatarImg
  const bgImage = bgImageImg

  const userStore = useUserStore()
  const userInfo = computed(() => userStore.getUserInfo)

  const loading = ref(false)
  const activeTab = ref('basic')

  /**
   * 用户详情数据
   */
  const userDetail = ref<Partial<Api.SystemManage.UserListItem>>({})

  /**
   * 顶部卡片头像展示 URL：若存的是 OSS 私有桶永久地址则用 presigned URL 展示，避免 403
   */
  const topAvatarDisplayUrl = ref(defaultAvatarImg)

  /**
   * 用户信息表单
   */
  const form = reactive({
    userId: 0,
    userName: '',
    nickName: '',
    email: '',
    phonenumber: '',
    avatar: '',
    sex: '0',
    remark: ''
  })
  const originalForm = reactive({
    userId: 0,
    userName: '',
    nickName: '',
    email: '',
    phonenumber: '',
    avatar: '',
    sex: '0',
    remark: ''
  })

  const passwordFormRef = ref<FormInstance>()
  const passwordForm = reactive({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  })

  const passwordRules: FormRules<typeof passwordForm> = {
    oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
    newPassword: [
      { required: true, message: '请输入新密码', trigger: 'blur' },
      {
        pattern: ADMIN_PASSWORD_PATTERN,
        message: ADMIN_PASSWORD_RULE_MESSAGE,
        trigger: 'blur'
      },
      {
        validator: (_rule, value, callback) => {
          if (!value) {
            callback()
            return
          }
          if (value === passwordForm.oldPassword) {
            callback(new Error('新密码不能与当前密码相同'))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ],
    confirmPassword: [
      { required: true, message: '请再次输入新密码', trigger: 'blur' },
      {
        validator: (_rule, value, callback) => {
          if (!value) {
            callback(new Error('请再次输入新密码'))
            return
          }
          if (value !== passwordForm.newPassword) {
            callback(new Error('两次输入的新密码不一致'))
            return
          }
          callback()
        },
        trigger: 'blur'
      }
    ]
  }

  /**
   * 性别选项
   */
  const options = [
    { value: '0', label: '男' },
    { value: '1', label: '女' },
    { value: '2', label: '未知' }
  ]

  // ==================== 会话管理 ====================
  const sessionLoading = ref(false)
  const logoutAllLoading = ref(false)
  const sessionList = ref<Array<Api.Auth.DeviceSession & { logoutLoading?: boolean }>>([])

  const loadSessions = async () => {
    sessionLoading.value = true
    try {
      const userId = userStore.getUserInfo?.user?.userId
      if (!userId) {
        ElMessage.warning('用户未登录')
        return
      }
      const res = await fetchUserSessions(userId)
      sessionList.value = (res || []).map((session) => ({
        ...session,
        logoutLoading: false
      }))
    } catch (error: any) {
      ElMessage.error(error.message || '获取会话列表失败')
    } finally {
      sessionLoading.value = false
    }
  }

  const isCurrentSession = (session: Api.Auth.DeviceSession) => {
    return session.isCurrent === true
  }

  /** 存在其他设备会话时才展示操作列（当前会话不可下线） */
  const hasOtherSessions = computed(() =>
    sessionList.value.some((session) => !isCurrentSession(session))
  )

  const sessionRowClassName = ({ row }: { row: Api.Auth.DeviceSession }) => {
    return isCurrentSession(row) ? 'current-session-row' : ''
  }

  const getDeviceText = (session: Api.Auth.DeviceSession) => {
    const parts: string[] = []
    if (session.browser) parts.push(session.browser)
    if (session.os) parts.push(session.os)
    return parts.length > 0 ? parts.join(' / ') : '未知设备'
  }

  const formatDateTime = (dateStr?: string) => {
    if (!dateStr) return '-'
    return dayjs(dateStr).format('YYYY-MM-DD HH:mm:ss')
  }

  const handleLogoutSession = async (
    session: Api.Auth.DeviceSession & { logoutLoading?: boolean }
  ) => {
    try {
      await ElMessageBox.confirm(
        `确定要强制下线该会话吗？\n设备：${getDeviceText(session)}\nIP：${session.ipaddr || '未知'}`,
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      const index = sessionList.value.findIndex(
        (s) => s.deviceSessionId === session.deviceSessionId
      )
      if (index > -1) {
        sessionList.value[index].logoutLoading = true
      }

      const userId = userStore.getUserInfo?.user?.userId
      if (!userId) return

      await forceLogoutSession(userId, session.deviceSessionId)
      ElMessage.success('会话已强制下线')

      if (index > -1) {
        sessionList.value.splice(index, 1)
      }
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || '强制下线失败')
      }
    } finally {
      const idx = sessionList.value.findIndex((s) => s.deviceSessionId === session.deviceSessionId)
      if (idx > -1) {
        sessionList.value[idx].logoutLoading = false
      }
    }
  }

  const handleLogoutAll = async () => {
    try {
      await ElMessageBox.confirm(
        '确定要强制下线所有会话吗？这将导致所有设备上的账户退出登录。',
        '警告',
        {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }
      )

      logoutAllLoading.value = true
      const userId = userStore.getUserInfo?.user?.userId
      if (!userId) return

      await forceLogoutAllSessions(userId)
      ElMessage.success('所有会话已强制下线')

      userStore.logOut()
    } catch (error: any) {
      if (error !== 'cancel') {
        ElMessage.error(error.message || '操作失败')
      }
    } finally {
      logoutAllLoading.value = false
    }
  }
  // ==================== 会话管理 END ====================

  /** 根据 userDetail.avatar 更新顶部头像展示 URL（私有桶时用 presigned 避免 403） */
  const updateTopAvatarDisplayUrl = async () => {
    const avatar = userDetail.value.avatar
    if (!avatar || avatar === '') {
      topAvatarDisplayUrl.value = defaultAvatar
      return
    }
    const path = extractOssObjectPath(avatar)
    if (path && needsOssPresignedDisplay(avatar)) {
      try {
        const res = (await fetchGetAvatarPresignedUrl(path)) as any
        const presigned = res?.presignedUrl ?? res?.data?.presignedUrl
        if (presigned) {
          topAvatarDisplayUrl.value = presigned
        } else {
          topAvatarDisplayUrl.value = defaultAvatar
        }
      } catch {
        topAvatarDisplayUrl.value = defaultAvatar
      }
    } else {
      topAvatarDisplayUrl.value = avatar
    }
  }

  /**
   * 获取用户详情
   */
  const getUserDetail = async () => {
    let currentUserId: number | undefined = userInfo.value?.user?.userId

    // 如果用户信息中没有 userId，尝试重新获取用户信息
    if (!currentUserId) {
      loading.value = true
      try {
        const responseData = await fetchGetUserInfo()

        // 后端返回的数据结构是 { user: {...}, roles: [...], permissions: [...] }
        // 用户信息在 user 字段中
        const userInfoData = (responseData as any)?.user || responseData

        if (userInfoData?.userId) {
          const permissions = (responseData as any)?.permissions || []
          const roles = (responseData as any)?.roles || []
          const formattedUserInfo: Partial<Api.Auth.UserInfo> = {
            user: {
              userId: userInfoData.userId,
              username: userInfoData.userName || '',
              nickName: userInfoData.nickName || '',
              avatar: userInfoData.avatar || '',
              email: userInfoData.email || '',
              phoneNumber: userInfoData.phonenumber || '',
              sex: userInfoData.sex || 0,
              status: userInfoData.status || '0',
              createTime: userInfoData.createTime || ''
            },
            roles: roles.map((r: any) => ({
              roleId: r.roleId,
              roleName: r.roleName,
              roleKey: r.roleKey,
              roleSort: r.roleSort,
              status: r.status,
              createTime: r.createTime
            }))
          }
          userStore.setUserInfo(formattedUserInfo)
          currentUserId = userInfoData.userId
        } else {
          logger.error('重新获取的用户信息中没有 userId:', userInfoData)
          logger.error('完整响应数据:', responseData)
          loading.value = false
          return
        }
      } catch (error) {
        logger.error('获取用户信息失败:', error)
        loading.value = false
        return
      }
    }

    if (!currentUserId) {
      logger.error('最终检查：仍然没有 userId')
      loading.value = false
      return
    }

    loading.value = true
    try {
      const res = await fetchGetUserById(currentUserId)
      if (res) {
        userDetail.value = res
        Object.assign(form, {
          userId: res.userId || 0,
          userName: res.userName || '',
          nickName: res.nickName || '',
          email: res.email || '',
          phonenumber: res.phonenumber || '',
          avatar: res.avatar || '',
          sex: res.sex || '0',
          remark: res.remark || ''
        })
        Object.assign(originalForm, form)
        await updateTopAvatarDisplayUrl()
      }
    } catch (error) {
      logger.error('获取用户详情失败:', error)
    } finally {
      loading.value = false
    }
  }

  onMounted(() => {
    getUserDetail()
  })

  // 切换到会话管理标签页时自动加载会话列表
  watch(activeTab, (val) => {
    if (val === 'session') {
      loadSessions()
    }
  })

  const submitPassword = async () => {
    if (!passwordFormRef.value) return
    await passwordFormRef.value.validate(async (valid) => {
      if (!valid) return
      try {
        await fetchUpdateUserPassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        clearSavedLoginPassword(form.userName || userInfo.value?.user?.username || '')
        ElMessage.success('密码修改成功，请重新登录')
        fetchLogout()
          .catch(() => void 0)
          .finally(() => {
            resetPasswordForm()
            userStore.logOut()
          })
      } catch (error) {
        ElMessage.error((error as any)?.message || '密码修改失败，请检查旧密码或稍后重试')
        if (import.meta.env.DEV) {
          console.error('修改密码失败:', error)
        }
      }
    })
  }

  const resetPasswordForm = () => {
    passwordForm.oldPassword = ''
    passwordForm.newPassword = ''
    passwordForm.confirmPassword = ''
    passwordFormRef.value?.resetFields()
  }

  const resetBasicProfile = () => {
    Object.assign(form, originalForm)
  }

  const submitBasicProfile = async () => {
    if (!form.userId) {
      ElMessage.error('用户信息未加载完成，请稍后重试')
      return
    }
    try {
      await fetchUpdateUser({
        userId: form.userId,
        userName: form.userName,
        nickName: form.nickName,
        email: form.email,
        phonenumber: form.phonenumber,
        avatar: form.avatar,
        sex: form.sex,
        remark: form.remark
      } as Api.SystemManage.UserListItem)
      Object.assign(originalForm, form)
      Object.assign(userDetail.value, form)
      userStore.setUserInfo({
        userName: form.userName,
        nickName: form.nickName,
        email: form.email,
        avatar: form.avatar
      } as any)
      ElMessage.success('基本资料保存成功')
    } catch (error) {
      ElMessage.error((error as any)?.message || '基本资料保存失败')
      if (import.meta.env.DEV) {
        console.error('保存基本资料失败:', error)
      }
    }
  }

  const handleAvatarUploadSuccess = async (avatarUrl: string) => {
    form.avatar = avatarUrl || ''
    userDetail.value.avatar = form.avatar
    await updateTopAvatarDisplayUrl()
    userStore.setUserInfo({
      avatar: form.avatar,
      avatarUpdatedAt: Date.now()
    } as any)
  }

  const handleAvatarUploadError = (error: any) => {
    ElMessage.error(error?.message || '头像上传失败，请稍后重试')
  }
</script>

<style scoped lang="scss">
  :deep(.art-card-sm) {
    overflow: hidden;
    border: 1px solid var(--art-card-border);
    border-radius: 16px;
    box-shadow: var(--art-shadow-card);
    transition: all 0.3s ease;

    &:hover {
      box-shadow: var(--art-shadow-card-hover);
    }
  }

  :deep(.el-form-item__label) {
    font-weight: 500;
    color: var(--art-gray-700);
  }

  :deep(.el-input__wrapper),
  :deep(.el-textarea__inner) {
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      box-shadow: var(--art-shadow-sm);
    }
  }

  :deep(.el-select) {
    .el-select__wrapper {
      border-radius: 8px;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: var(--art-shadow-sm);
      }
    }
  }

  :deep(.el-button) {
    font-weight: 500;
    border-radius: 8px;
    transition: all 0.3s ease;

    &:hover {
      transform: translateY(-1px);
    }
  }

  :deep(.user-center-top-avatar .avatar-preview) {
    border: 4px solid var(--default-box-color);
    box-shadow: 0 10px 20px rgb(0 0 0 / 20%);
  }

  .bg-gradient-to-r {
    background-size: 200% 200%;
    animation: gradientShift 8s ease infinite;
  }

  @keyframes gradientShift {
    0%,
    100% {
      background-position: 0% 50%;
    }

    50% {
      background-position: 100% 50%;
    }
  }

  .hover\:scale-105:hover {
    transform: scale(1.05);
  }

  .password-panel {
    display: flex;
    gap: 28px;
    align-items: flex-start;

    @media (max-width: 900px) {
      flex-direction: column;
    }
  }

  .password-panel__form {
    flex: 1;
    min-width: 0;
    max-width: 560px;
  }

  .password-panel__row {
    display: flex;
    gap: 16px;

    @media (max-width: 640px) {
      flex-direction: column;
      gap: 0;
    }
  }

  .password-panel__half {
    flex: 1;
    min-width: 0;
  }

  .password-tips {
    flex-shrink: 0;
    width: 300px;
    margin-top: 4px;
    padding: 16px 18px;
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 8px;
    background: var(--el-fill-color-light);

    @media (max-width: 900px) {
      width: 100%;
    }
  }

  .password-tips__title {
    margin-bottom: 10px;
    font-size: 14px;
    font-weight: 600;
    color: var(--el-text-color-primary);
  }

  .password-tips__list {
    padding-left: 18px;
    margin: 0;
    font-size: 13px;
    line-height: 1.8;
    color: var(--el-text-color-regular);

    li + li {
      margin-top: 6px;
    }

    li::marker {
      color: var(--el-color-primary-light-3);
    }
  }

  .session-management {
    .device-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    :deep(.el-table) {
      border-radius: 8px;
      overflow: hidden;
    }

    :deep(.current-session-row) {
      background-color: var(--el-color-success-light-9) !important;

      td {
        background-color: transparent !important;
      }
    }
  }
</style>
