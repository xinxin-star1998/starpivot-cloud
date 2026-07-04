<!-- 秒杀倒计时 -->
<template>
  <span v-if="displayText" class="portal-seckill-countdown" :class="{ pulse: isUrgent }">
    <span class="portal-seckill-countdown__prefix">{{ prefix }}</span>
    <span class="portal-seckill-countdown__time">{{ countdownText }}</span>
  </span>
</template>

<script setup lang="ts">
import {
  formatCountdown,
  getSeckillCountdownPrefix,
  getSeckillCountdownTarget,
  type SeckillCountdownTarget
} from '@/utils/portal/seckill-countdown'

defineOptions({ name: 'PortalSeckillCountdown' })

  const props = defineProps<{
    session: SeckillCountdownTarget
  }>()

  const remainingMs = ref(0)
  let timer: ReturnType<typeof setInterval> | null = null

  const prefix = computed(() => getSeckillCountdownPrefix(props.session.state))

  const countdownText = computed(() => formatCountdown(remainingMs.value))

  const displayText = computed(
    () =>
      !!prefix.value &&
      (props.session.state === 'ongoing' || props.session.state === 'upcoming') &&
      remainingMs.value > 0
  )

  const isUrgent = computed(
    () => props.session.state === 'ongoing' && remainingMs.value > 0 && remainingMs.value <= 300_000
  )

  function tick() {
    const target = getSeckillCountdownTarget(props.session)
    if (target == null) {
      remainingMs.value = 0
      return
    }
    remainingMs.value = Math.max(0, target - Date.now())
  }

  function startTimer() {
    stopTimer()
    tick()
    if (props.session.state === 'ongoing' || props.session.state === 'upcoming') {
      timer = setInterval(tick, 1000)
    }
  }

  function stopTimer() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  watch(() => props.session, startTimer, { deep: true, immediate: true })

  onUnmounted(stopTimer)
</script>

<style scoped lang="scss">
  .portal-seckill-countdown {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 11px;
    color: var(--portal-primary);
    font-weight: 600;

    &__prefix {
      color: var(--portal-text-muted);
      font-weight: 500;
    }

    &__time {
      font-variant-numeric: tabular-nums;
      letter-spacing: 0.02em;
    }

    &.pulse .portal-seckill-countdown__time {
      animation: portal-countdown-pulse 1s ease-in-out infinite;
    }
  }

  @keyframes portal-countdown-pulse {
    0%,
    100% {
      opacity: 1;
    }

    50% {
      opacity: 0.55;
    }
  }
</style>
