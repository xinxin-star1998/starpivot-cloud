<template>
  <div class="art-card p-5 h-128 overflow-hidden mb-5 max-sm:mb-4">
    <div class="art-card-header">
      <div class="title">
        <h4>{{ t('dashboard.newUser.title') }}</h4>
        <p>{{ t('dashboard.newUser.growth') }}<span class="text-success">+20%</span></p>
      </div>
      <ElRadioGroup v-model="radioPeriod">
        <ElRadioButton value="thisMonth" :label="t('dashboard.newUser.thisMonth')"></ElRadioButton>
        <ElRadioButton value="lastMonth" :label="t('dashboard.newUser.lastMonth')"></ElRadioButton>
        <ElRadioButton value="thisYear" :label="t('dashboard.newUser.thisYear')"></ElRadioButton>
      </ElRadioGroup>
    </div>
    <ArtTable
      class="w-full"
      :data="tableData"
      style="width: 100%"
      size="large"
      :border="false"
      :stripe="false"
      :header-cell-style="{ background: 'transparent' }"
    >
      <template #default>
        <ElTableColumn :label="t('dashboard.newUser.avatar')" prop="avatar" width="150px">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <img class="size-9 rounded-lg" :src="scope.row.avatar" alt="avatar" />
              <span class="ml-2">{{ scope.row.username }}</span>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="t('dashboard.newUser.region')" prop="province" />
        <ElTableColumn :label="t('dashboard.newUser.gender')" prop="avatar">
          <template #default="scope">
            <div style="display: flex; align-items: center">
              <span style="margin-left: 10px">{{
                scope.row.sex === 1 ? t('dashboard.newUser.male') : t('dashboard.newUser.female')
              }}</span>
            </div>
          </template>
        </ElTableColumn>
        <ElTableColumn :label="t('dashboard.newUser.progress')" width="240">
          <template #default="scope">
            <ElProgress
              :percentage="scope.row.pro"
              :color="scope.row.color"
              :stroke-width="4"
              :aria-label="`${scope.row.username} ${t('dashboard.newUser.progress')}: ${scope.row.pro}%`"
            />
          </template>
        </ElTableColumn>
      </template>
    </ArtTable>
  </div>
</template>

<script setup lang="ts">
import {useI18n} from 'vue-i18n'
import avatar1 from '@/assets/images/avatar/avatar1.webp'
import avatar2 from '@/assets/images/avatar/avatar2.webp'
import avatar3 from '@/assets/images/avatar/avatar3.webp'
import avatar4 from '@/assets/images/avatar/avatar4.webp'
import avatar5 from '@/assets/images/avatar/avatar5.webp'
import avatar6 from '@/assets/images/avatar/avatar6.webp'

const { t } = useI18n()

  interface UserTableItem {
    username: string
    province: string
    sex: 0 | 1
    age: number
    percentage: number
    pro: number
    color: string
    avatar: string
  }

  interface NewUserRawItem {
    username: string
    province: string
    sex: number
    percentage: number
  }

  const ANIMATION_DELAY = 100

  const radioPeriod = ref('thisMonth')

  const props = withDefaults(
    defineProps<{
      userList: NewUserRawItem[]
    }>(),
    {
      userList: () => []
    }
  )

  const avatars = [avatar1, avatar2, avatar3, avatar4, avatar5, avatar6]
  const colors = [
    'var(--art-primary)',
    'var(--art-secondary)',
    'var(--art-warning)',
    'var(--art-info)',
    'var(--art-error)',
    'var(--art-success)'
  ]

  const tableData = reactive<UserTableItem[]>([])

  const rebuildTableData = () => {
    tableData.splice(0, tableData.length)
    props.userList.forEach((item, index) => {
      tableData.push({
        username: item.username,
        province: item.province || '--',
        sex: item.sex === 1 ? 1 : 0,
        age: 0,
        percentage: item.percentage ?? 0,
        pro: 0,
        color: colors[index % colors.length],
        avatar: avatars[index % avatars.length]
      })
    })
  }

  /**
   * 添加进度条动画效果
   * 延迟后将进度值从 0 更新到目标百分比，触发动画
   */
  const addAnimation = (): void => {
    setTimeout(() => {
      tableData.forEach((item) => {
        item.pro = item.percentage
      })
    }, ANIMATION_DELAY)
  }

  onMounted(() => {
    addAnimation()
  })

  watch(
    () => props.userList,
    () => {
      rebuildTableData()
      addAnimation()
    },
    { immediate: true }
  )
</script>

<style lang="scss" scoped>
  .art-card {
    :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
      color: var(--el-color-primary) !important;
      background: transparent !important;
    }
  }
</style>
