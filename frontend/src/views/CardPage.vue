<template>
  <div class="card-page">
    <div class="page-nav">
      <el-button text @click="router.back()"><el-icon :size="18"><ArrowLeft /></el-icon></el-button>
      <h2>我的会员卡</h2>
    </div>

    <!-- 实体卡 -->
    <div class="vip-card" :class="cardClass">
      <div class="card-chip">gymAI</div>
      <div class="card-body">
        <div class="card-type">{{ typeLabel }}</div>
        <div class="card-holder">{{ userStore.nickname }}</div>
        <div class="card-footer">
          <div class="card-dates">
            <div class="date-row">
              <span class="date-label">开卡</span>
              <span>{{ info.cardStartDate || '--' }}</span>
            </div>
            <div class="date-row">
              <span class="date-label">到期</span>
              <span>{{ info.cardEndDate || '终身' }}</span>
            </div>
          </div>
          <div class="card-progress" v-if="remaining != null">
            <div class="progress-bar">
              <div class="progress-fill" :style="{ width: progressPercent + '%' }"></div>
            </div>
            <span class="progress-text" :class="{ 'text-warn': remaining <= 7 }">
              {{ remaining <= 0 ? '已过期' : '剩余 ' + remaining + ' 天' }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 续费 -->
    <section class="section">
      <h3>{{ info.cardType ? '续费 / 升级' : '开通会员卡' }}</h3>
      <div class="plan-grid">
        <div v-for="plan in plans" :key="plan.type" class="plan-card"
             :class="{ selected: selectedPlan === plan.type }"
             @click="selectedPlan = plan.type">
          <div class="plan-name">{{ plan.name }}</div>
          <div class="plan-price">&yen;{{ plan.price }}<span class="plan-unit">/{{ plan.unit }}</span></div>
          <div class="plan-desc">{{ plan.desc }}</div>
        </div>
      </div>
      <el-button type="primary" size="large" :loading="renewing" class="renew-btn"
                 :disabled="!selectedPlan" @click="handleRenew">
        {{ renewing ? '处理中...' : (info.cardType ? '立即续费' : '立即开通') }}
      </el-button>
    </section>

    <!-- 购买记录 -->
    <section v-if="info.orders?.length" class="section">
      <h3>购买记录</h3>
      <div class="order-list">
        <div v-for="o in info.orders" :key="o.id" class="order-row">
          <div class="order-left">
            <span class="order-type">{{ typeMap[o.cardType] || o.cardType }}</span>
            <span class="order-time">{{ o.createdAt?.slice(0, 10) }}</span>
          </div>
          <span class="order-amount">&yen;{{ o.amount }}</span>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { cardApi } from '@/api'

const router = useRouter()
const userStore = useUserStore()
const renewing = ref(false)
const selectedPlan = ref('')

const info = ref<any>({})
const plans = [
  { type: 'MONTH', name: '月卡', price: 299, unit: '月', desc: '30天无限次使用' },
  { type: 'QUARTER', name: '季卡', price: 799, unit: '季', desc: '90天，省98元' },
  { type: 'YEAR', name: '年卡', price: 2599, unit: '年', desc: '365天，省989元' },
  { type: 'LIFETIME', name: '终身卡', price: 0, unit: '', desc: '一次开通，永久有效' },
]

const typeMap: Record<string, string> = { MONTH: '月卡', QUARTER: '季卡', YEAR: '年卡', LIFETIME: '终身卡' }

const typeLabel = computed(() => info.value.cardType ? typeMap[info.value.cardType] || '' : '未开通')

const cardClass = computed(() => {
  if (!info.value.cardType) return 'card-none'
  if (info.value.cardType === 'LIFETIME') return 'card-lifetime'
  return ''
})

const remaining = computed(() => {
  if (!info.value.cardEndDate) return null
  const end = new Date(info.value.cardEndDate)
  const today = new Date()
  return Math.ceil((end.getTime() - today.getTime()) / (1000 * 60 * 60 * 24))
})

const progressPercent = computed(() => {
  if (remaining.value == null || info.value.cardType === 'LIFETIME') return 100
  if (!info.value.cardStartDate) return 0
  const start = new Date(info.value.cardStartDate)
  const end = new Date(info.value.cardEndDate)
  const total = end.getTime() - start.getTime()
  if (total <= 0) return 0
  const used = Date.now() - start.getTime()
  return Math.min(100, Math.max(0, Math.round((used / total) * 100)))
})

const handleRenew = async () => {
  if (!selectedPlan.value) return
  renewing.value = true
  try {
    const res = await cardApi.renew(selectedPlan.value)
    info.value.cardType = selectedPlan.value
    info.value.cardStartDate = res.data.startDate
    info.value.cardEndDate = res.data.endDate
    selectedPlan.value = ''
    ElMessage.success('开通成功')
  } catch { /* handled */ }
  finally { renewing.value = false; fetchInfo() }
}

const fetchInfo = async () => {
  try { const res = await cardApi.info(); info.value = res.data } catch { /* handled */ }
}

onMounted(fetchInfo)
</script>

<style scoped lang="scss">
.card-page { padding: 14px; }

.page-nav {
  display: flex; align-items: center; gap: 8px; margin-bottom: 16px;
  h2 { font-family: $font-display; font-size: $font-size-lg; font-weight: 600; color: $color-carbon; text-transform: uppercase; letter-spacing: 0.03em; }
}

// ── 实体卡片 ────────────────────────────────
.vip-card {
  border-radius: $radius-lg; padding: 24px; margin-bottom: 28px; color: #fff;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 50%, #0f3460 100%);

  &.card-lifetime { background: linear-gradient(135deg, #1a1a2e 0%, #2d1b69 50%, #c83a24 100%); }
  &.card-none { background: linear-gradient(135deg, #555 0%, #333 100%); }
}

.card-chip { font-family: $font-display; font-size: 18px; opacity: 0.6; letter-spacing: 0.06em; margin-bottom: 20px; }

.card-type { font-family: $font-display; font-size: 28px; font-weight: 700; letter-spacing: 0.04em; margin-bottom: 12px; }

.card-holder { font-size: $font-size-base; opacity: 0.8; margin-bottom: 20px; }

.card-dates { display: flex; gap: 32px; margin-bottom: 12px; }

.date-row {
  display: flex; flex-direction: column; gap: 2px;
  .date-label { font-size: 10px; opacity: 0.5; text-transform: uppercase; letter-spacing: 0.05em; }
  span { font-size: $font-size-sm; }
}

.card-progress {
  .progress-bar { height: 3px; background: rgba(255,255,255,0.2); border-radius: 2px; margin-bottom: 6px; }
  .progress-fill { height: 100%; background: #fff; border-radius: 2px; transition: width 0.5s; }
  .progress-text { font-size: $font-size-sm; opacity: 0.7;
    &.text-warn { color: #ff9f43; opacity: 1; }
  }
}

// ── 套餐选择 ────────────────────────────────
.section { margin-bottom: 28px;
  h3 { font-family: $font-display; font-size: $font-size-lg; font-weight: 600; color: $color-carbon; margin-bottom: 12px; }
}

.plan-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 16px; }

.plan-card {
  border: 2px solid $color-steel; border-radius: $radius-md; padding: 16px; cursor: pointer; text-align: center; transition: all $transition-fast;
  &:hover { border-color: $color-cobalt; }
  &.selected { border-color: $color-cobalt; background: rgba($color-cobalt, 0.05); }
}

.plan-name { font-family: $font-display; font-size: $font-size-lg; font-weight: 600; color: $color-carbon; margin-bottom: 6px; text-transform: uppercase; }
.plan-price { font-size: $font-size-xl; font-weight: 700; color: $color-cobalt; margin-bottom: 4px; }
.plan-unit { font-size: $font-size-sm; color: $color-lead; font-weight: 400; }
.plan-desc { font-size: $font-size-sm; color: $color-lead; }

.renew-btn { width: 100%; height: 48px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.04em; }

// ── 购买记录 ────────────────────────────────
.order-row {
  display: flex; justify-content: space-between; align-items: center; padding: 12px 14px; background: $color-sheet; border-radius: $radius-md; margin-bottom: 4px;
}
.order-left { display: flex; flex-direction: column; gap: 2px; }
.order-type { font-size: $font-size-sm; color: $color-carbon; font-weight: 500; }
.order-time { font-size: $font-size-sm; color: $color-lead; }
.order-amount { font-weight: 600; color: $color-cobalt; }

html.dark {
  .page-nav h2, .section h3, .plan-name, .order-type { color: $dark-text; }
  .plan-card { border-color: $dark-border; }
  .order-row { background: $dark-bg-card; }
}
</style>
