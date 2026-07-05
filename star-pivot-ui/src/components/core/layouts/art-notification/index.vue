<!-- 全平台站内消息面板（顶栏铃铛） -->
<template>
  <div
    class="art-notification-panel art-card-sm !shadow-xl"
    :style="{
      transform: show ? 'scaleY(1)' : 'scaleY(0.9)',
      opacity: show ? 1 : 0
    }"
    v-show="visible"
    @click.stop
  >
    <div class="flex-cb px-3.5 mt-3.5">
      <span class="text-base font-medium text-g-800">{{ $t('notice.title') }}</span>
      <span
        class="text-xs text-g-800 px-1.5 py-1 c-p select-none rounded hover:bg-g-200"
        @click="handleReadAll"
      >
        {{ $t('notice.btnRead') }}
      </span>
    </div>

    <div class="px-3.5 py-2 text-xs text-g-500">未读 {{ unreadCount }} 条</div>

    <div class="w-full h-[calc(100%-95px)]">
      <div class="h-[calc(100%-60px)] overflow-y-scroll scrollbar-thin">
        <ElSkeleton v-if="loading" :rows="5" animated class="px-3.5" />
        <ul v-else-if="items.length">
          <li
            v-for="item in items"
            :key="item.messageId"
            class="box-border px-3.5 py-3.5 c-p last:border-b-0 hover:bg-g-200/60"
            :class="{ 'bg-theme/5': item.readFlag === '0' }"
            @click="handleClick(item)"
          >
            <div class="flex-c">
              <div
                class="size-9 leading-9 text-center rounded-lg flex-cc bg-theme/12 text-theme shrink-0"
              >
                <ArtSvgIcon class="text-lg !bg-transparent" icon="ri:notification-3-line" />
              </div>
              <div class="w-[calc(100%-45px)] ml-3.5 min-w-0">
                <h4 class="text-sm font-normal leading-5.5 text-g-900 truncate">{{ item.title }}</h4>
                <p class="mt-1 text-xs text-g-500 line-clamp-2">{{ item.content }}</p>
                <p class="mt-1 text-xs text-g-400">{{ formatDateTime(item.createTime) }}</p>
              </div>
            </div>
          </li>
        </ul>
        <div
          v-else
          class="relative top-25 h-full text-g-500 text-center !bg-transparent"
        >
          <ArtSvgIcon icon="system-uicons:inbox" class="text-5xl" />
          <p class="mt-3.5 text-xs !bg-transparent">{{ $t('notice.text[0]') }}{{ $t('notice.bar[0]') }}</p>
        </div>
      </div>

      <div class="relative box-border w-full px-3.5">
        <ElButton class="w-full mt-3" @click="handleViewAll" v-ripple>
          {{ $t('notice.viewAll') }}
        </ElButton>
      </div>
    </div>

    <div class="h-25"></div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import {
  fetchUserMessageList,
  fetchUserMessageRead,
  fetchUserMessageReadAll,
  fetchUserMessageUnreadCount,
  type UserMessageVo
} from '@/api/system/message'
import { formatDateTime } from '@/utils/common/datetime'
import { resolveMessageBizNav } from '@/utils/system/message-nav'

defineOptions({ name: 'ArtNotification' })

const props = defineProps<{
  value: boolean
}>()

const emit = defineEmits<{
  'update:value': [value: boolean]
  'unread-change': [count: number]
}>()

const router = useRouter()
const show = ref(false)
const visible = ref(false)
const loading = ref(false)
const items = ref<UserMessageVo[]>([])
const unreadCount = ref(0)

async function loadData() {
  loading.value = true
  try {
    const [count, page] = await Promise.all([
      fetchUserMessageUnreadCount(),
      fetchUserMessageList({ pageNum: 1, pageSize: 10 })
    ])
    unreadCount.value = Number(count) || 0
    items.value = page.rows || []
    emit('unread-change', unreadCount.value)
  } catch {
    items.value = []
  } finally {
    loading.value = false
  }
}

async function refreshUnreadCount() {
  try {
    unreadCount.value = Number(await fetchUserMessageUnreadCount()) || 0
    emit('unread-change', unreadCount.value)
  } catch {
    unreadCount.value = 0
    emit('unread-change', 0)
  }
}

function showNotice(open: boolean) {
  if (open) {
    visible.value = true
    loadData()
    setTimeout(() => {
      show.value = true
    }, 5)
  } else {
    show.value = false
    setTimeout(() => {
      visible.value = false
    }, 350)
  }
}

async function handleClick(item: UserMessageVo) {
  if (item.messageId && item.readFlag === '0') {
    await fetchUserMessageRead(item.messageId)
    item.readFlag = '1'
    unreadCount.value = Math.max(0, unreadCount.value - 1)
    emit('unread-change', unreadCount.value)
  }
  emit('update:value', false)
  const nav = resolveMessageBizNav(item.bizModule, item.bizType, item.bizKey, item.linkPath)
  if (nav) {
    await router.push({ path: nav.path, query: nav.query })
  }
}

async function handleReadAll() {
  if (unreadCount.value <= 0) return
  await fetchUserMessageReadAll()
  unreadCount.value = 0
  items.value = items.value.map((item) => ({ ...item, readFlag: '1' }))
  emit('unread-change', 0)
}

function handleViewAll() {
  emit('update:value', false)
  router.push('/system/message')
}

watch(
  () => props.value,
  (newValue) => {
    showNotice(newValue)
  }
)

onMounted(() => {
  refreshUnreadCount()
})

defineExpose({
  refreshUnreadCount
})
</script>

<style scoped>
@reference '@styles/core/tailwind.css';

.art-notification-panel {
  @apply absolute top-14.5 right-5 w-90 h-125 origin-top-right transition-all duration-300 z-1000;
}
</style>
