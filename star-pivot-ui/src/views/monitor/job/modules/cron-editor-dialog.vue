<template>
  <ElDialog
    v-model="visible"
    title="配置 Cron 表达式"
    width="980px"
    append-to-body
    destroy-on-close
    class="cron-editor-dialog"
  >
    <div class="cron-layout">
      <div class="cron-main">
        <div class="cron-header">
          <div class="cron-title">
            <div class="cron-title__name">调度编辑器</div>
            <div class="cron-title__desc">默认使用可视化模式，高级模式用于兼容复杂 Cron。</div>
          </div>
          <ElRadioGroup v-model="mode" class="mode-toggle">
            <ElRadioButton label="visual">可视化</ElRadioButton>
            <ElRadioButton label="advanced">高级模式</ElRadioButton>
          </ElRadioGroup>
        </div>

        <div class="cron-section">
          <div class="cron-section__title">快速预设</div>
          <ElRow :gutter="12">
            <ElCol v-for="p in presets" :key="p.label" :span="8">
              <ElCard
                shadow="never"
                class="preset-card"
                :class="{ 'preset-card--active': draft === p.expression }"
                @click="applyPreset(p.expression)"
              >
                <div class="preset-card__name">{{ p.label }}</div>
                <div class="preset-card__desc">{{ p.desc }}</div>
              </ElCard>
            </ElCol>
          </ElRow>
        </div>

        <div class="cron-section">
          <div class="cron-section__title">可视化配置</div>
          <ElTabs v-model="visualTab" type="card" class="visual-tabs" :disabled="mode !== 'visual'">
            <ElTabPane label="按分钟" name="minute">
              <div class="form-grid">
                <div class="form-row">
                  <div class="form-row__label">每</div>
                  <ElInputNumber
                    v-model="minute.every"
                    :min="1"
                    :max="59"
                    controls-position="right"
                  />
                  <div class="form-row__label">分钟执行（秒位固定为 0）</div>
                </div>
              </div>
            </ElTabPane>
            <ElTabPane label="按小时" name="hour">
              <div class="form-grid">
                <div class="form-row">
                  <div class="form-row__label">每</div>
                  <ElInputNumber
                    v-model="hour.every"
                    :min="1"
                    :max="23"
                    controls-position="right"
                  />
                  <div class="form-row__label">小时，在第</div>
                  <ElInputNumber
                    v-model="hour.minute"
                    :min="0"
                    :max="59"
                    controls-position="right"
                  />
                  <div class="form-row__label">分钟执行</div>
                </div>
              </div>
            </ElTabPane>
            <ElTabPane label="每日" name="day">
              <div class="form-grid">
                <div class="form-row">
                  <div class="form-row__label">每天</div>
                  <ElInputNumber v-model="day.hour" :min="0" :max="23" controls-position="right" />
                  <div class="form-row__label">时</div>
                  <ElInputNumber
                    v-model="day.minute"
                    :min="0"
                    :max="59"
                    controls-position="right"
                  />
                  <div class="form-row__label">分执行</div>
                </div>
              </div>
            </ElTabPane>
            <ElTabPane label="每周" name="week">
              <div class="form-grid">
                <div class="form-row">
                  <div class="form-row__label">每周</div>
                  <ElSelect v-model="week.dow" style="width: 140px">
                    <ElOption
                      v-for="d in dowOptions"
                      :key="d.value"
                      :label="d.label"
                      :value="d.value"
                    />
                  </ElSelect>
                  <div class="form-row__label">：</div>
                  <ElInputNumber v-model="week.hour" :min="0" :max="23" controls-position="right" />
                  <div class="form-row__label">时</div>
                  <ElInputNumber
                    v-model="week.minute"
                    :min="0"
                    :max="59"
                    controls-position="right"
                  />
                  <div class="form-row__label">分执行</div>
                </div>
              </div>
            </ElTabPane>
            <ElTabPane label="每月" name="month">
              <div class="form-grid">
                <div class="form-row">
                  <div class="form-row__label">每月</div>
                  <ElInputNumber v-model="month.dom" :min="1" :max="31" controls-position="right" />
                  <div class="form-row__label">号</div>
                  <ElInputNumber
                    v-model="month.hour"
                    :min="0"
                    :max="23"
                    controls-position="right"
                  />
                  <div class="form-row__label">时</div>
                  <ElInputNumber
                    v-model="month.minute"
                    :min="0"
                    :max="59"
                    controls-position="right"
                  />
                  <div class="form-row__label">分执行</div>
                </div>
              </div>
            </ElTabPane>
          </ElTabs>

          <div v-if="mode === 'advanced'" class="advanced-section">
            <div class="advanced-header">
              <div class="advanced-title">高级模式</div>
              <div class="advanced-tip">支持直接输入 Cron，适合复杂任务编排</div>
            </div>
            <ElInput
              v-model="draft"
              type="textarea"
              :rows="4"
              placeholder="请输入 Cron 表达式"
              maxlength="100"
              show-word-limit
            />
            <div class="advanced-hint">
              字段顺序：<span class="mono">秒 分 时 日 月 周</span>，兼容 5 位和 6 位，推荐使用 6
              位。
            </div>
          </div>
        </div>

        <div class="cron-section">
          <div class="cron-section__title">当前表达式</div>
          <ElInput v-model="draft" readonly />
        </div>
      </div>

      <div class="cron-side">
        <ElCard shadow="never" class="side-card side-card--summary">
          <div class="side-title">表达式摘要</div>
          <div class="side-cron">{{ draft || '-' }}</div>
          <div class="side-tz">Asia/Shanghai</div>
          <div class="chips">
            <div class="chip" v-for="c in chips" :key="c.k">
              <div class="chip__k">{{ c.k }}</div>
              <div class="chip__v">{{ c.v }}</div>
            </div>
          </div>
        </ElCard>

        <ElCard shadow="never" class="side-card">
          <div class="side-row">
            <div class="side-title">调度校验</div>
            <ElTag :type="validation.ok ? 'success' : 'danger'" effect="light">
              {{ validation.ok ? '有效' : '无效' }}
            </ElTag>
          </div>
          <div class="side-desc">
            <template v-if="validation.ok">
              {{ scheduleText }}
            </template>
            <template v-else>
              {{ validation.message }}
            </template>
          </div>
        </ElCard>

        <ElCard shadow="never" class="side-card">
          <div class="side-row">
            <div class="side-title">未来执行</div>
            <div class="side-tz">Asia/Shanghai</div>
          </div>
          <div class="next-list">
            <div v-for="(t, i) in nextRuns" :key="t + i" class="next-item">
              <div class="next-item__main">{{ t }}</div>
            </div>
            <div v-if="!nextRuns.length" class="next-empty">-</div>
          </div>
        </ElCard>
      </div>
    </div>

    <template #footer>
      <ElButton @click="visible = false">取消</ElButton>
      <ElButton type="primary" :disabled="!validation.ok" @click="confirmUse">确认使用</ElButton>
    </template>
  </ElDialog>
</template>

<script setup lang="ts">
  import dayjs from 'dayjs'
  import { CronExpressionParser } from 'cron-parser'

  interface Props {
    modelValue: boolean
    value?: string
  }

  interface Emits {
    (e: 'update:modelValue', v: boolean): void
    (e: 'confirm', expression: string): void
  }

  const props = defineProps<Props>()
  const emit = defineEmits<Emits>()

  const visible = computed({
    get: () => props.modelValue,
    set: (v) => emit('update:modelValue', v)
  })

  type Mode = 'visual' | 'advanced'
  const mode = ref<Mode>('visual')

  const presets = [
    { label: '每 5 分钟', expression: '0 0/5 * * * ?', desc: '轻量巡检与同步' },
    { label: '每 15 分钟', expression: '0 0/15 * * * ?', desc: '常规增量任务' },
    { label: '每小时整点', expression: '0 0 0/1 * * ?', desc: '小时级统计聚合' },
    { label: '每日凌晨 3 点', expression: '0 0 3 * * ?', desc: '离峰批处理' },
    { label: '工作日 9 点', expression: '0 0 9 ? * MON-FRI', desc: '业务日常任务' },
    { label: '每周一 2 点', expression: '0 0 2 ? * MON', desc: '周度维护窗口' }
  ] as const

  const draft = ref('')

  const visualTab = ref<'minute' | 'hour' | 'day' | 'week' | 'month'>('day')
  const minute = reactive({ every: 5 })
  const hour = reactive({ every: 1, minute: 0 })
  const day = reactive({ hour: 3, minute: 0 })
  const week = reactive({ dow: 'MON', hour: 3, minute: 0 })
  const month = reactive({ dom: 1, hour: 9, minute: 0 })

  const dowOptions = [
    { label: '周一', value: 'MON' },
    { label: '周二', value: 'TUE' },
    { label: '周三', value: 'WED' },
    { label: '周四', value: 'THU' },
    { label: '周五', value: 'FRI' },
    { label: '周六', value: 'SAT' },
    { label: '周日', value: 'SUN' }
  ] as const

  const buildVisualCron = () => {
    // Quartz 6 段：秒 分 时 日 月 周
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

  const applyPreset = (expression: string) => {
    draft.value = expression
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
      if (mode.value === 'visual') draft.value = buildVisualCron()
    },
    { immediate: true }
  )

  watch(
    () => props.value,
    (v) => {
      if (v && !visible.value) draft.value = v
    },
    { immediate: true }
  )

  watch(
    () => visible.value,
    (v) => {
      if (v) {
        draft.value = props.value || buildVisualCron()
      }
    }
  )

  const validation = computed(() => {
    const exp = (draft.value || '').trim()
    if (!exp) return { ok: false, message: '请输入 Cron 表达式' }
    try {
      CronExpressionParser.parse(exp)
      return { ok: true, message: '表达式可解析' }
    } catch (e: any) {
      return { ok: false, message: e?.message || '表达式不可解析' }
    }
  })

  const nextRuns = computed(() => {
    if (!validation.value.ok) return []
    try {
      const it = CronExpressionParser.parse(draft.value.trim(), { currentDate: new Date() })
      const arr: string[] = []
      for (let i = 0; i < 6; i++) {
        const d = it.next().toDate()
        arr.push(dayjs(d).format('YYYY/MM/DD HH:mm:ss'))
      }
      return arr
    } catch {
      return []
    }
  })

  const chips = computed(() => {
    const parts = (draft.value || '').trim().split(/\s+/)
    const fill = (i: number) => parts[i] ?? '-'
    return [
      { k: '秒', v: fill(0) },
      { k: '分', v: fill(1) },
      { k: '时', v: fill(2) },
      { k: '日', v: fill(3) },
      { k: '月', v: fill(4) },
      { k: '周', v: fill(5) }
    ]
  })

  const scheduleText = computed(() => {
    if (!validation.value.ok) return ''
    const exp = draft.value.trim()
    if (!exp) return ''

    // 优先对可视化生成的表达式做自然语言描述
    const parts = exp.split(/\s+/)
    const s = parts[0]
    const m = parts[1]
    const h = parts[2]
    const dom = parts[3]
    const mon = parts[4]
    const dow = parts[5]

    const pad2 = (n: string | number) => String(n).padStart(2, '0')
    const timeText = (hh: string, mm: string) => `${pad2(hh)}:${pad2(mm)}:00`

    // 0 0/5 * * * ?
    const minuteEvery = m?.match(/^0\/(\d{1,2})$/)
    if (
      s === '0' &&
      minuteEvery &&
      h === '*' &&
      dom === '*' &&
      mon === '*' &&
      (dow === '?' || dow === '*')
    ) {
      return `每 ${minuteEvery[1]} 分钟执行`
    }

    // 0 m 0/h * * ?
    const hourEvery = h?.match(/^0\/(\d{1,2})$/)
    if (s === '0' && hourEvery && dom === '*' && mon === '*' && (dow === '?' || dow === '*')) {
      return `每 ${hourEvery[1]} 小时，在 ${timeText('00', m)} 执行`
    }

    // 0 m H * * ?
    if (
      s === '0' &&
      /^\d{1,2}$/.test(m) &&
      /^\d{1,2}$/.test(h) &&
      dom === '*' &&
      mon === '*' &&
      (dow === '?' || dow === '*')
    ) {
      return `每天 ${timeText(h, m)} 执行`
    }

    // 0 m H ? * MON
    if (
      s === '0' &&
      /^\d{1,2}$/.test(m) &&
      /^\d{1,2}$/.test(h) &&
      dom === '?' &&
      mon === '*' &&
      /^[A-Z]{3}(-[A-Z]{3})?$/.test(dow)
    ) {
      const map: Record<string, string> = {
        MON: '周一',
        TUE: '周二',
        WED: '周三',
        THU: '周四',
        FRI: '周五',
        SAT: '周六',
        SUN: '周日'
      }
      if (dow.includes('-')) {
        const [a, b] = dow.split('-')
        return `${map[a] ?? a}至${map[b] ?? b} ${timeText(h, m)} 执行`
      }
      return `每周${map[dow] ?? dow} ${timeText(h, m)} 执行`
    }

    // 0 m H D * ?
    if (
      s === '0' &&
      /^\d{1,2}$/.test(m) &&
      /^\d{1,2}$/.test(h) &&
      /^\d{1,2}$/.test(dom) &&
      mon === '*' &&
      dow === '?'
    ) {
      return `每月 ${dom} 号 ${timeText(h, m)} 执行`
    }

    return `按 Cron 表达式执行：${exp}`
  })

  const confirmUse = () => {
    if (!validation.value.ok) return
    const exp = draft.value.trim()
    emit('confirm', exp)
    visible.value = false
  }
</script>

<style scoped lang="scss">
  .cron-layout {
    display: grid;
    grid-template-columns: 1fr 320px;
    gap: 16px;
  }

  .cron-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 12px 16px;
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
    background: linear-gradient(
      135deg,
      var(--el-color-primary-light-9) 0%,
      var(--el-color-primary-light-8) 100%
    );
  }

  .mode-toggle :deep(.el-radio-button__inner) {
    border-radius: 10px;
    padding: 8px 14px;
    font-weight: 600;
  }
  .mode-toggle :deep(.el-radio-button:first-child .el-radio-button__inner) {
    border-top-left-radius: 10px;
    border-bottom-left-radius: 10px;
  }
  .mode-toggle :deep(.el-radio-button:last-child .el-radio-button__inner) {
    border-top-right-radius: 10px;
    border-bottom-right-radius: 10px;
  }

  .cron-title__name {
    font-weight: 600;
    color: var(--art-gray-900);
    line-height: 1.2;
  }
  .cron-title__desc {
    margin-top: 4px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .cron-section {
    margin-top: 14px;
  }
  .cron-section__title {
    margin-bottom: 10px;
    font-weight: 600;
    color: var(--art-gray-800);
  }

  .preset-card {
    cursor: pointer;
    border-radius: 12px;
    transition: all 0.2s ease;
    border: 1px solid var(--art-card-border);
  }
  .preset-card:hover {
    transform: translateY(-1px);
    box-shadow: 0 6px 18px rgb(0 0 0 / 8%);
  }
  .preset-card--active {
    border-color: var(--el-color-primary);
    background: var(--el-color-primary-light-9);
  }
  .preset-card__name {
    font-weight: 600;
    color: var(--art-gray-900);
  }
  .preset-card__desc {
    margin-top: 6px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }

  .visual-tabs :deep(.el-tabs__content) {
    padding: 12px;
    border: 1px solid var(--art-card-border);
    border-top: 0;
    border-radius: 0 0 12px 12px;
  }

  .form-grid {
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  .form-row {
    display: flex;
    align-items: center;
    gap: 10px;
    flex-wrap: wrap;
  }
  .form-row__label {
    color: var(--art-gray-700);
  }

  .advanced-section {
    margin-top: 14px;
    padding-top: 10px;
    border-top: 1px solid var(--art-card-border);
  }
  .advanced-header {
    display: flex;
    align-items: baseline;
    justify-content: space-between;
    gap: 10px;
    margin-bottom: 10px;
  }
  .advanced-title {
    font-weight: 600;
    color: var(--art-gray-800);
  }
  .advanced-tip {
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
  .advanced-hint {
    margin-top: 10px;
    font-size: 12px;
    color: var(--el-text-color-secondary);
  }
  .mono {
    font-family:
      ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, 'Liberation Mono', 'Courier New',
      monospace;
  }

  .cron-side {
    display: flex;
    flex-direction: column;
    gap: 12px;
  }
  .side-card {
    border-radius: 12px;
    border: 1px solid var(--art-card-border);
  }
  .side-card--summary {
    color: #fff;
    background: linear-gradient(135deg, #2d3a57 0%, #1f2b44 100%);
    border: 0;
  }

  .side-title {
    font-weight: 600;
  }
  .side-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
  }
  .side-cron {
    margin-top: 10px;
    font-weight: 700;
    letter-spacing: 1px;
  }
  .side-tz {
    margin-top: 8px;
    font-size: 12px;
    opacity: 0.8;
  }
  .side-desc {
    margin-top: 10px;
    font-size: 13px;
    color: var(--el-text-color-regular);
  }
  .side-desc--ok {
    color: var(--el-color-success);
  }

  .chips {
    margin-top: 12px;
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 8px;
  }
  .chip {
    padding: 8px 10px;
    background: rgb(255 255 255 / 10%);
    border-radius: 12px;
    text-align: center;
  }
  .chip__k {
    font-size: 12px;
    opacity: 0.9;
  }
  .chip__v {
    margin-top: 6px;
    font-weight: 700;
  }

  .next-list {
    margin-top: 10px;
    display: flex;
    flex-direction: column;
    gap: 10px;
  }
  .next-item {
    padding: 10px 12px;
    border: 1px solid var(--art-card-border);
    border-radius: 12px;
  }
  .next-item__main {
    font-weight: 600;
  }
  .next-empty {
    color: var(--el-text-color-secondary);
  }
</style>
