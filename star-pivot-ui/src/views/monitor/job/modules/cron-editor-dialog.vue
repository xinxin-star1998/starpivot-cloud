<template>
  <ElDialog
    v-model="visible"
    width="1080px"
    append-to-body
    destroy-on-close
    align-center
    class="cron-editor-dialog"
    header-class="cron-editor-dialog__header"
    body-class="cron-editor-dialog__body"
    footer-class="cron-editor-dialog__footer"
  >
    <template #header>
      <div class="cron-head">
        <div class="cron-head__brand">
          <span class="cron-head__icon">
            <ArtSvgIcon icon="ri:calendar-event-line" />
          </span>
          <div>
            <div class="cron-head__title">{{ t('monitor.job.cronEditor') }}</div>
            <div class="cron-head__desc">{{ t('monitor.job.cron.hint') }}</div>
          </div>
        </div>
        <div class="mode-switch" role="tablist">
          <button
            type="button"
            class="mode-switch__btn"
            :class="{ 'is-active': mode === 'visual' }"
            @click="mode = 'visual'"
          >
            <ArtSvgIcon icon="ri:layout-grid-line" />
            {{ t('monitor.job.cron.visual') }}
          </button>
          <button
            type="button"
            class="mode-switch__btn"
            :class="{ 'is-active': mode === 'advanced' }"
            @click="mode = 'advanced'"
          >
            <ArtSvgIcon icon="ri:code-s-slash-line" />
            {{ t('monitor.job.cron.advanced') }}
          </button>
        </div>
      </div>
    </template>

    <div class="cron-layout">
      <div class="cron-main">
        <section class="cron-panel">
          <div class="cron-panel__label">{{ t('monitor.job.cron.presets') }}</div>
          <div class="preset-grid">
            <button
              v-for="p in presets"
              :key="p.expression + p.label"
              type="button"
              class="preset-chip"
              :class="{ 'is-active': draft === p.expression }"
              @click="applyPreset(p.expression)"
            >
              <span class="preset-chip__icon">
                <ArtSvgIcon :icon="p.icon" />
              </span>
              <span class="preset-chip__meta">
                <span class="preset-chip__name">{{ p.label }}</span>
                <span class="preset-chip__desc">{{ p.desc }}</span>
              </span>
            </button>
          </div>
        </section>

        <section v-if="mode === 'visual'" class="cron-panel cron-panel--editor">
          <div class="freq-tabs">
            <button
              v-for="tab in freqTabs"
              :key="tab.name"
              type="button"
              class="freq-tab"
              :class="{ 'is-active': visualTab === tab.name }"
              @click="visualTab = tab.name"
            >
              <ArtSvgIcon :icon="tab.icon" />
              <span>{{ tab.label }}</span>
            </button>
          </div>

          <div class="cron-sentence">
            <template v-if="visualTab === 'minute'">
              <span>{{ t('monitor.job.cron.every') }}</span>
              <ElInputNumber
                v-model="minute.every"
                :min="1"
                :max="59"
                controls-position="right"
                size="large"
              />
              <span>{{ t('monitor.job.cron.minutesOnce') }}</span>
            </template>

            <template v-else-if="visualTab === 'hour'">
              <span>{{ t('monitor.job.cron.every') }}</span>
              <ElInputNumber
                v-model="hour.every"
                :min="1"
                :max="23"
                controls-position="right"
                size="large"
              />
              <span>{{ t('monitor.job.cron.hoursOnce') }}</span>
              <ElInputNumber
                v-model="hour.minute"
                :min="0"
                :max="59"
                controls-position="right"
                size="large"
              />
              <span>{{ t('monitor.job.cron.minuteExec') }}</span>
            </template>

            <template v-else-if="visualTab === 'day'">
              <span>{{ t('monitor.job.cron.dailyAt') }}</span>
              <ElTimePicker
                v-model="dayClock"
                format="HH:mm"
                value-format="HH:mm"
                :clearable="false"
                :placeholder="t('monitor.job.cron.pickTime')"
                size="large"
              />
              <span>{{ t('monitor.job.cron.execAt') }}</span>
            </template>

            <template v-else-if="visualTab === 'week'">
              <div class="sentence-stack">
                <div class="sentence-line">
                  <span>{{ t('monitor.job.cron.weeklyOn') }}</span>
                  <div class="dow-pills">
                    <button
                      v-for="d in dowOptions"
                      :key="d.value"
                      type="button"
                      class="dow-pill"
                      :class="{ 'is-active': week.dow === d.value }"
                      @click="week.dow = d.value"
                    >
                      {{ d.label }}
                    </button>
                  </div>
                </div>
                <div class="sentence-line">
                  <ElTimePicker
                    v-model="weekClock"
                    format="HH:mm"
                    value-format="HH:mm"
                    :clearable="false"
                    :placeholder="t('monitor.job.cron.pickTime')"
                    size="large"
                  />
                  <span>{{ t('monitor.job.cron.execAt') }}</span>
                </div>
              </div>
            </template>

            <template v-else>
              <div class="sentence-stack">
                <div class="sentence-line">
                  <span>{{ t('monitor.job.cron.monthlyOn') }}</span>
                </div>
                <div class="dom-grid">
                  <button
                    v-for="n in 31"
                    :key="n"
                    type="button"
                    class="dom-cell"
                    :class="{ 'is-active': month.dom === n }"
                    @click="month.dom = n"
                  >
                    {{ n }}
                  </button>
                </div>
                <div class="sentence-line">
                  <ElTimePicker
                    v-model="monthClock"
                    format="HH:mm"
                    value-format="HH:mm"
                    :clearable="false"
                    :placeholder="t('monitor.job.cron.pickTime')"
                    size="large"
                  />
                  <span>{{ t('monitor.job.cron.execAt') }}</span>
                </div>
              </div>
            </template>
          </div>
        </section>

        <section v-else class="cron-panel cron-panel--editor">
          <div class="advanced-head">
            <div class="advanced-title">{{ t('monitor.job.cron.advanced') }}</div>
            <div class="advanced-tip">{{ t('monitor.job.cron.advancedTip') }}</div>
          </div>
          <ElInput
            v-model="draft"
            type="textarea"
            :rows="5"
            class="cron-textarea"
            :placeholder="t('monitor.job.cron.placeholder')"
            maxlength="100"
            show-word-limit
          />
          <div class="advanced-hint">
            {{ t('monitor.job.cron.fieldOrder') }}
          </div>
        </section>
      </div>

      <aside class="cron-side">
        <div class="preview-hero">
          <div class="preview-hero__top">
            <span>{{ t('monitor.job.cronExpression') }}</span>
            <ElButton text class="copy-btn" :disabled="!draft" @click="copyExpression">
              <ArtSvgIcon icon="ri:file-copy-line" />
              {{ t('monitor.job.cron.copy') }}
            </ElButton>
          </div>
          <div class="preview-hero__cron">{{ draft || '—' }}</div>
          <div class="preview-hero__plain">{{ scheduleText || '—' }}</div>
          <div class="chips">
            <div v-for="c in chips" :key="c.k" class="chip">
              <div class="chip__k">{{ c.k }}</div>
              <div class="chip__v">{{ c.v }}</div>
            </div>
          </div>
        </div>

        <div class="side-card">
          <div class="side-row">
            <div class="side-title">{{ t('monitor.job.status') }}</div>
            <span class="status-pill" :class="validation.ok ? 'is-ok' : 'is-bad'">
              <ArtSvgIcon
                :icon="validation.ok ? 'ri:checkbox-circle-fill' : 'ri:close-circle-fill'"
              />
              {{ validation.ok ? t('monitor.job.cron.valid') : t('monitor.job.cron.invalid') }}
            </span>
          </div>
          <div class="side-desc" :class="{ 'is-bad': !validation.ok }">
            {{ validation.ok ? scheduleText : validation.message }}
          </div>
        </div>

        <div class="side-card side-card--runs">
          <div class="side-row">
            <div class="side-title">{{ t('monitor.job.cron.nextRuns') }}</div>
            <div class="side-tz">{{ t('monitor.job.cron.timezone') }}</div>
          </div>
          <div class="next-list">
            <div v-for="(item, i) in nextRuns" :key="item.time + i" class="next-item">
              <span class="next-item__idx">{{ String(i + 1).padStart(2, '0') }}</span>
              <span class="next-item__time">{{ item.time }}</span>
              <span class="next-item__rel">{{ item.relative }}</span>
            </div>
            <div v-if="!nextRuns.length" class="next-empty">
              {{ t('monitor.job.cron.emptyNext') }}
            </div>
          </div>
        </div>
      </aside>
    </div>

    <template #footer>
      <ElButton @click="visible = false">{{ t('common.cancel') }}</ElButton>
      <ElButton type="primary" :disabled="!validation.ok" @click="confirmUse">
        {{ t('monitor.job.cron.confirm') }}
      </ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import dayjs from 'dayjs'
  import { ElMessage } from 'element-plus'
  import { CronExpressionParser } from 'cron-parser'
  import { useI18n } from 'vue-i18n'
  import ArtSvgIcon from '@/components/core/base/art-svg-icon/index.vue'

  interface Props {
    modelValue: boolean
    value?: string
  }

  interface Emits {
    (e: 'update:modelValue', v: boolean): void
    (e: 'confirm', expression: string): void
  }

  type Mode = 'visual' | 'advanced'
  type VisualTab = 'minute' | 'hour' | 'day' | 'week' | 'month'

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()
  const { t } = useI18n()

  const visible = computed({
    get: () => props.modelValue,
    set: (v) => emit('update:modelValue', v)
  })

  const mode = ref<Mode>('visual')
  const syncing = ref(false)
  const draft = ref('')
  const visualTab = ref<VisualTab>('day')
  const minute = reactive({ every: 5 })
  const hour = reactive({ every: 1, minute: 0 })
  const day = reactive({ hour: 3, minute: 0 })
  const week = reactive({ dow: 'MON', hour: 3, minute: 0 })
  const month = reactive({ dom: 1, hour: 9, minute: 0 })

  const presets = computed(() => [
    {
      label: t('monitor.job.cron.preset5'),
      expression: '0 0/5 * * * ?',
      desc: t('monitor.job.cron.preset5Desc'),
      icon: 'ri:timer-line'
    },
    {
      label: t('monitor.job.cron.preset15'),
      expression: '0 0/15 * * * ?',
      desc: t('monitor.job.cron.preset15Desc'),
      icon: 'ri:timer-flash-line'
    },
    {
      label: t('monitor.job.cron.presetHour'),
      expression: '0 0 0/1 * * ?',
      desc: t('monitor.job.cron.presetHourDesc'),
      icon: 'ri:time-line'
    },
    {
      label: t('monitor.job.cron.presetDaily'),
      expression: '0 0 3 * * ?',
      desc: t('monitor.job.cron.presetDailyDesc'),
      icon: 'ri:moon-clear-line'
    },
    {
      label: t('monitor.job.cron.presetWeekday'),
      expression: '0 0 9 ? * MON-FRI',
      desc: t('monitor.job.cron.presetWeekdayDesc'),
      icon: 'ri:briefcase-line'
    },
    {
      label: t('monitor.job.cron.presetMonday'),
      expression: '0 0 2 ? * MON',
      desc: t('monitor.job.cron.presetMondayDesc'),
      icon: 'ri:calendar-check-line'
    }
  ])

  const freqTabs = computed(() => [
    { name: 'minute' as const, label: t('monitor.job.cron.tabMinute'), icon: 'ri:timer-line' },
    { name: 'hour' as const, label: t('monitor.job.cron.tabHour'), icon: 'ri:time-line' },
    { name: 'day' as const, label: t('monitor.job.cron.tabDay'), icon: 'ri:sun-line' },
    { name: 'week' as const, label: t('monitor.job.cron.tabWeek'), icon: 'ri:calendar-2-line' },
    { name: 'month' as const, label: t('monitor.job.cron.tabMonth'), icon: 'ri:calendar-todo-line' }
  ])

  const dowOptions = computed(() => [
    { label: t('monitor.job.cron.mon'), value: 'MON' },
    { label: t('monitor.job.cron.tue'), value: 'TUE' },
    { label: t('monitor.job.cron.wed'), value: 'WED' },
    { label: t('monitor.job.cron.thu'), value: 'THU' },
    { label: t('monitor.job.cron.fri'), value: 'FRI' },
    { label: t('monitor.job.cron.sat'), value: 'SAT' },
    { label: t('monitor.job.cron.sun'), value: 'SUN' }
  ])

  const dowMap = computed<Record<string, string>>(() => ({
    MON: t('monitor.job.cron.mon'),
    TUE: t('monitor.job.cron.tue'),
    WED: t('monitor.job.cron.wed'),
    THU: t('monitor.job.cron.thu'),
    FRI: t('monitor.job.cron.fri'),
    SAT: t('monitor.job.cron.sat'),
    SUN: t('monitor.job.cron.sun')
  }))

  const bindClock = (state: { hour: number; minute: number }) =>
    computed({
      get: () => `${String(state.hour).padStart(2, '0')}:${String(state.minute).padStart(2, '0')}`,
      set: (v: string) => {
        const [hh, mm] = (v || '00:00').split(':')
        state.hour = Number(hh) || 0
        state.minute = Number(mm) || 0
      }
    })

  const dayClock = bindClock(day)
  const weekClock = bindClock(week)
  const monthClock = bindClock(month)

  const clamp = (n: number, min: number, max: number) => Math.min(max, Math.max(min, n))

  const buildVisualCron = () => {
    switch (visualTab.value) {
      case 'minute':
        return `0 0/${minute.every} * * * ?`
      case 'hour':
        return `0 ${hour.minute} 0/${hour.every} * * ?`
      case 'day':
        return `0 ${day.minute} ${day.hour} * * ?`
      case 'week':
        return `0 ${week.minute} ${week.hour} ? * ${week.dow}`
      case 'month':
        return `0 ${month.minute} ${month.hour} ${month.dom} * ?`
      default:
        return draft.value
    }
  }

  const parseToVisual = (exp: string) => {
    const parts = exp.trim().split(/\s+/)
    if (parts.length < 6) return false
    const [s, m, h, dom, mon, dow] = parts
    if (s !== '0') return false

    const minuteEvery = m?.match(/^0\/(\d{1,2})$/)
    if (minuteEvery && h === '*' && dom === '*' && mon === '*' && (dow === '?' || dow === '*')) {
      visualTab.value = 'minute'
      minute.every = clamp(Number(minuteEvery[1]), 1, 59)
      return true
    }

    const hourEvery = h?.match(/^0\/(\d{1,2})$/)
    if (
      hourEvery &&
      /^\d{1,2}$/.test(m) &&
      dom === '*' &&
      mon === '*' &&
      (dow === '?' || dow === '*')
    ) {
      visualTab.value = 'hour'
      hour.every = clamp(Number(hourEvery[1]), 1, 23)
      hour.minute = clamp(Number(m), 0, 59)
      return true
    }

    if (
      /^\d{1,2}$/.test(m) &&
      /^\d{1,2}$/.test(h) &&
      dom === '*' &&
      mon === '*' &&
      (dow === '?' || dow === '*')
    ) {
      visualTab.value = 'day'
      day.hour = clamp(Number(h), 0, 23)
      day.minute = clamp(Number(m), 0, 59)
      return true
    }

    if (
      /^\d{1,2}$/.test(m) &&
      /^\d{1,2}$/.test(h) &&
      dom === '?' &&
      mon === '*' &&
      /^(MON|TUE|WED|THU|FRI|SAT|SUN)$/.test(dow)
    ) {
      visualTab.value = 'week'
      week.dow = dow
      week.hour = clamp(Number(h), 0, 23)
      week.minute = clamp(Number(m), 0, 59)
      return true
    }

    if (
      /^\d{1,2}$/.test(m) &&
      /^\d{1,2}$/.test(h) &&
      /^\d{1,2}$/.test(dom) &&
      mon === '*' &&
      dow === '?'
    ) {
      visualTab.value = 'month'
      month.dom = clamp(Number(dom), 1, 31)
      month.hour = clamp(Number(h), 0, 23)
      month.minute = clamp(Number(m), 0, 59)
      return true
    }

    return false
  }

  const applyExpression = (expression: string) => {
    syncing.value = true
    if (parseToVisual(expression)) {
      mode.value = 'visual'
      draft.value = buildVisualCron()
    } else {
      mode.value = 'advanced'
      draft.value = expression
    }
    nextTick(() => {
      syncing.value = false
    })
  }

  const applyPreset = (expression: string) => {
    applyExpression(expression)
  }

  watch(
    [
      mode,
      visualTab,
      () => minute.every,
      () => hour.every,
      () => hour.minute,
      () => day.hour,
      () => day.minute,
      () => week.dow,
      () => week.hour,
      () => week.minute,
      () => month.dom,
      () => month.hour,
      () => month.minute
    ],
    () => {
      if (mode.value === 'visual' && !syncing.value) draft.value = buildVisualCron()
    }
  )

  watch(
    () => visible.value,
    (v) => {
      if (!v) return
      const current = (props.value || '').trim()
      if (current) applyExpression(current)
      else {
        mode.value = 'visual'
        visualTab.value = 'day'
        draft.value = buildVisualCron()
      }
    }
  )

  const pad2 = (n: string | number) => String(n).padStart(2, '0')
  const timeText = (hh: string, mm: string) => `${pad2(hh)}:${pad2(mm)}`

  const validation = computed(() => {
    const exp = (draft.value || '').trim()
    if (!exp) return { ok: false, message: t('monitor.job.cronPlaceholder') }
    try {
      CronExpressionParser.parse(exp)
      return { ok: true, message: t('monitor.job.cron.valid') }
    } catch (e: any) {
      return { ok: false, message: e?.message || t('monitor.job.cron.invalid') }
    }
  })

  const relativeLabel = (date: Date) => {
    const mins = Math.max(0, Math.round((date.getTime() - Date.now()) / 60000))
    if (mins < 1) return t('monitor.job.cron.soon')
    if (mins < 60) return t('monitor.job.cron.inMinutes', { n: mins })
    const hours = Math.round(mins / 60)
    if (hours < 24) return t('monitor.job.cron.inHours', { n: hours })
    return t('monitor.job.cron.inDays', { n: Math.round(hours / 24) })
  }

  const nextRuns = computed(() => {
    if (!validation.value.ok) return []
    try {
      const it = CronExpressionParser.parse(draft.value.trim(), { currentDate: new Date() })
      const arr: { time: string; relative: string }[] = []
      for (let i = 0; i < 5; i++) {
        const d = it.next().toDate()
        arr.push({
          time: dayjs(d).format('MM/DD HH:mm:ss'),
          relative: relativeLabel(d)
        })
      }
      return arr
    } catch {
      return []
    }
  })

  const chips = computed(() => {
    const parts = (draft.value || '').trim().split(/\s+/)
    const fill = (i: number) => parts[i] ?? '—'
    return [
      { k: t('monitor.job.cron.second'), v: fill(0) },
      { k: t('monitor.job.cron.minute'), v: fill(1) },
      { k: t('monitor.job.cron.hour'), v: fill(2) },
      { k: t('monitor.job.cron.day'), v: fill(3) },
      { k: t('monitor.job.cron.month'), v: fill(4) },
      { k: t('monitor.job.cron.week'), v: fill(5) }
    ]
  })

  const scheduleText = computed(() => {
    if (!validation.value.ok) return ''
    const exp = draft.value.trim()
    const parts = exp.split(/\s+/)
    const s = parts[0]
    const m = parts[1]
    const h = parts[2]
    const dom = parts[3]
    const mon = parts[4]
    const dow = parts[5]
    const map = dowMap.value

    const minuteEvery = m?.match(/^0\/(\d{1,2})$/)
    if (
      s === '0' &&
      minuteEvery &&
      h === '*' &&
      dom === '*' &&
      mon === '*' &&
      (dow === '?' || dow === '*')
    ) {
      return t('monitor.job.cron.everyNMinutes', { n: minuteEvery[1] })
    }

    const hourEvery = h?.match(/^0\/(\d{1,2})$/)
    if (s === '0' && hourEvery && dom === '*' && mon === '*' && (dow === '?' || dow === '*')) {
      return t('monitor.job.cron.everyNHours', {
        n: hourEvery[1],
        time: timeText('00', m)
      })
    }

    if (
      s === '0' &&
      /^\d{1,2}$/.test(m) &&
      /^\d{1,2}$/.test(h) &&
      dom === '*' &&
      mon === '*' &&
      (dow === '?' || dow === '*')
    ) {
      return t('monitor.job.cron.everyDayAt', { time: timeText(h, m) })
    }

    if (
      s === '0' &&
      /^\d{1,2}$/.test(m) &&
      /^\d{1,2}$/.test(h) &&
      dom === '?' &&
      mon === '*' &&
      /^[A-Z]{3}(-[A-Z]{3})?$/.test(dow)
    ) {
      if (dow.includes('-')) {
        const [a, b] = dow.split('-')
        return t('monitor.job.cron.everyWeekRange', {
          from: map[a] ?? a,
          to: map[b] ?? b,
          time: timeText(h, m)
        })
      }
      return t('monitor.job.cron.everyWeekAt', {
        day: map[dow] ?? dow,
        time: timeText(h, m)
      })
    }

    if (
      s === '0' &&
      /^\d{1,2}$/.test(m) &&
      /^\d{1,2}$/.test(h) &&
      /^\d{1,2}$/.test(dom) &&
      mon === '*' &&
      dow === '?'
    ) {
      return t('monitor.job.cron.everyMonthAt', { day: dom, time: timeText(h, m) })
    }

    return t('monitor.job.cron.customExpr')
  })

  const copyExpression = async () => {
    const text = draft.value.trim()
    if (!text) return
    try {
      await navigator.clipboard.writeText(text)
      ElMessage.success(t('monitor.job.cron.copied'))
    } catch {
      ElMessage.error(t('monitor.job.cron.invalid'))
    }
  }

  const confirmUse = () => {
    if (!validation.value.ok) return
    emit('confirm', draft.value.trim())
    visible.value = false
  }
</script>

<style scoped lang="scss">
  .cron-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    padding-right: 28px;
  }

  .cron-head__brand {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
  }

  .cron-head__icon {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    color: var(--el-color-primary);
    font-size: 20px;
    background: var(--el-color-primary-light-9);
    border-radius: 12px;
  }

  .cron-head__title {
    font-size: 16px;
    font-weight: 650;
    line-height: 1.2;
    color: var(--art-gray-900);
  }

  .cron-head__desc {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .mode-switch {
    display: inline-flex;
    padding: 3px;
    background: var(--el-fill-color-light);
    border-radius: 12px;
  }

  .mode-switch__btn {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 8px 14px;
    color: var(--el-text-color-regular);
    font-size: 13px;
    font-weight: 600;
    background: transparent;
    border: 0;
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.18s ease;

    &:hover {
      color: var(--el-text-color-primary);
    }

    &.is-active {
      color: var(--el-color-primary);
      background: var(--el-bg-color);
      box-shadow: 0 4px 12px rgb(0 0 0 / 6%);
    }
  }

  .cron-layout {
    display: grid;
    grid-template-columns: minmax(0, 1fr) 340px;
    gap: 18px;
  }

  .cron-main,
  .cron-side {
    display: flex;
    flex-direction: column;
    gap: 14px;
    min-width: 0;
  }

  .cron-panel {
    padding: 14px;
    background: var(--el-bg-color);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 16px;
  }

  .cron-panel--editor {
    flex: 1;
  }

  .cron-panel__label {
    margin-bottom: 10px;
    font-size: 12px;
    font-weight: 650;
    letter-spacing: 0.04em;
    color: var(--el-text-color-secondary);
  }

  .preset-grid {
    display: grid;
    grid-template-columns: repeat(3, minmax(0, 1fr));
    gap: 8px;
  }

  .preset-chip {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
    padding: 10px 10px;
    text-align: left;
    background: var(--el-fill-color-lighter);
    border: 1px solid transparent;
    border-radius: 12px;
    cursor: pointer;
    transition: all 0.18s ease;

    &:hover {
      border-color: var(--el-color-primary-light-5);
      transform: translateY(-1px);
    }

    &.is-active {
      background: var(--el-color-primary-light-9);
      border-color: var(--el-color-primary);
    }
  }

  .preset-chip__icon {
    display: inline-flex;
    flex-shrink: 0;
    align-items: center;
    justify-content: center;
    width: 30px;
    height: 30px;
    color: var(--el-color-primary);
    background: var(--el-bg-color);
    border-radius: 9px;
  }

  .preset-chip__meta {
    display: flex;
    flex-direction: column;
    min-width: 0;
  }

  .preset-chip__name {
    overflow: hidden;
    font-size: 13px;
    font-weight: 650;
    color: var(--art-gray-900);
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .preset-chip__desc {
    overflow: hidden;
    margin-top: 2px;
    font-size: 11px;
    color: var(--el-text-color-secondary);
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .freq-tabs {
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    gap: 6px;
    margin-bottom: 14px;
    padding: 4px;
    background: var(--el-fill-color-light);
    border-radius: 14px;
  }

  .freq-tab {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    padding: 10px 6px;
    color: var(--el-text-color-regular);
    font-size: 12px;
    font-weight: 600;
    background: transparent;
    border: 0;
    border-radius: 11px;
    cursor: pointer;
    transition: all 0.18s ease;

    &.is-active {
      color: var(--el-color-primary);
      background: var(--el-bg-color);
      box-shadow: 0 6px 16px rgb(0 0 0 / 6%);
    }
  }

  .cron-sentence {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px 12px;
    min-height: 168px;
    padding: 22px 18px;
    font-size: 15px;
    font-weight: 550;
    color: var(--art-gray-800);
    background:
      radial-gradient(120% 80% at 100% 0%, var(--el-color-primary-light-9), transparent 46%),
      var(--el-fill-color-lighter);
    border-radius: 14px;
  }

  .sentence-stack {
    display: flex;
    flex-direction: column;
    gap: 14px;
    width: 100%;
  }

  .sentence-line {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 10px;
  }

  .dow-pills {
    display: flex;
    flex-wrap: wrap;
    gap: 6px;
  }

  .dow-pill {
    width: 36px;
    height: 36px;
    color: var(--el-text-color-regular);
    font-size: 13px;
    font-weight: 650;
    background: var(--el-bg-color);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 10px;
    cursor: pointer;
    transition: all 0.16s ease;

    &.is-active {
      color: #fff;
      background: var(--el-color-primary);
      border-color: var(--el-color-primary);
    }
  }

  .dom-grid {
    display: grid;
    grid-template-columns: repeat(7, minmax(0, 1fr));
    gap: 6px;
  }

  .dom-cell {
    height: 34px;
    color: var(--el-text-color-regular);
    font-size: 12px;
    font-weight: 650;
    background: var(--el-bg-color);
    border: 1px solid transparent;
    border-radius: 8px;
    cursor: pointer;
    transition: all 0.16s ease;

    &:hover {
      border-color: var(--el-color-primary-light-5);
    }

    &.is-active {
      color: #fff;
      background: var(--el-color-primary);
    }
  }

  .advanced-head {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 12px;
    margin-bottom: 12px;
  }

  .advanced-title {
    font-weight: 650;
    color: var(--art-gray-800);
  }

  .advanced-tip,
  .advanced-hint {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .advanced-hint {
    margin-top: 10px;
  }

  .cron-textarea :deep(.el-textarea__inner) {
    font-family:
      ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
      monospace;
    font-size: 15px;
    line-height: 1.7;
    letter-spacing: 0.04em;
    border-radius: 12px;
  }

  .preview-hero {
    position: relative;
    overflow: hidden;
    padding: 16px;
    color: #fff;
    background:
      radial-gradient(120% 90% at 100% -10%, rgb(255 255 255 / 18%), transparent 52%),
      linear-gradient(155deg, var(--el-color-primary) 0%, #1e1b4b 100%);
    border-radius: 16px;
  }

  .preview-hero__top {
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 12px;
    opacity: 0.86;
  }

  .copy-btn {
    color: #fff !important;
    font-weight: 600;
  }

  .preview-hero__cron {
    margin-top: 10px;
    overflow: hidden;
    font-family:
      ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
      monospace;
    font-size: 20px;
    font-weight: 700;
    letter-spacing: 0.04em;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .preview-hero__plain {
    margin-top: 6px;
    font-size: 13px;
    opacity: 0.88;
  }

  .chips {
    display: grid;
    grid-template-columns: repeat(6, minmax(0, 1fr));
    gap: 6px;
    margin-top: 14px;
  }

  .chip {
    padding: 8px 4px;
    text-align: center;
    background: rgb(255 255 255 / 12%);
    border-radius: 10px;
  }

  .chip__k {
    font-size: 11px;
    opacity: 0.8;
  }

  .chip__v {
    overflow: hidden;
    margin-top: 4px;
    font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
    font-size: 12px;
    font-weight: 700;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .side-card {
    padding: 14px;
    background: var(--el-bg-color);
    border: 1px solid var(--el-border-color-lighter);
    border-radius: 16px;
  }

  .side-card--runs {
    flex: 1;
  }

  .side-title {
    font-size: 13px;
    font-weight: 650;
  }

  .side-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
  }

  .side-tz {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .side-desc {
    margin-top: 10px;
    font-size: 13px;
    color: var(--el-text-color-regular);

    &.is-bad {
      color: var(--el-color-danger);
    }
  }

  .status-pill {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    padding: 4px 8px;
    font-size: 12px;
    font-weight: 650;
    border-radius: 999px;

    &.is-ok {
      color: var(--el-color-success);
      background: var(--el-color-success-light-9);
    }

    &.is-bad {
      color: var(--el-color-danger);
      background: var(--el-color-danger-light-9);
    }
  }

  .next-list {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-top: 12px;
  }

  .next-item {
    display: grid;
    grid-template-columns: 28px 1fr auto;
    align-items: center;
    gap: 8px;
    padding: 8px 10px;
    background: var(--el-fill-color-lighter);
    border-radius: 10px;
  }

  .next-item__idx {
    color: var(--el-color-primary);
    font-size: 11px;
    font-weight: 700;
  }

  .next-item__time {
    font-size: 13px;
    font-weight: 650;
    font-variant-numeric: tabular-nums;
  }

  .next-item__rel {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .next-empty {
    padding: 18px 0;
    color: var(--el-text-color-secondary);
    text-align: center;
  }

  @media (max-width: 960px) {
    .cron-layout,
    .preset-grid {
      grid-template-columns: 1fr;
    }

    .cron-head {
      flex-direction: column;
      align-items: flex-start;
    }
  }
</style>

<style lang="scss">
  .cron-editor-dialog.el-dialog {
    overflow: hidden;
    border-radius: 20px !important;
  }

  .cron-editor-dialog__header {
    margin-right: 0 !important;
    padding: 18px 20px 14px !important;
    border-bottom: 1px solid var(--el-border-color-lighter);
  }

  .cron-editor-dialog__body {
    padding: 16px 20px 8px !important;
  }

  .cron-editor-dialog__footer {
    padding: 12px 20px 18px !important;
    background: var(--el-fill-color-lighter);
    border-top: 1px solid var(--el-border-color-lighter);
  }
</style>
