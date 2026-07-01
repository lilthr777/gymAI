<template>
  <div class="card-page">
    <div class="page-nav">
      <button class="back-btn" @click="router.back()">
        <el-icon :size="18"><ArrowLeft /></el-icon>
        <span>返回</span>
      </button>
      <h2>我的会员卡</h2>
    </div>

    <!-- Membership card -->
    <div class="vip-card" :class="cardClass">
      <div class="card-chip">gymAI</div>
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

    <!-- Plan selection -->
    <section class="section">
      <h3>{{ info.cardType ? '续费 / 升级' : '开通会员卡' }}</h3>
      <div class="plan-grid">
        <div v-for="plan in plans" :key="plan.type" class="plan-card"
             :class="{ selected: selectedPlan === plan.type }"
             @click="selectedPlan = plan.type">
          <div class="plan-name">{{ plan.name }}</div>
          <div class="plan-price"><span class="plan-currency">&yen;</span>{{ plan.price }}<span class="plan-unit">/{{ plan.unit }}</span></div>
          <div class="plan-desc">{{ plan.desc }}</div>
        </div>
      </div>
      <el-button type="primary" size="large" :loading="renewing" class="renew-btn"
                 :disabled="!selectedPlan" @click="handleRenew">
        {{ renewing ? '处理中...' : (info.cardType ? '立即续费' : '立即开通') }}
      </el-button>
    </section>

    <!-- Purchase history -->
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
.card-page {
  padding: 24px 20px;
  max-width: 480px;
  margin: 0 auto;
}

.page-nav {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 28px;

  h2 {
    font-size: $font-size-xl;
    font-weight: 600;
    color: $color-text-primary;
    letter-spacing: -0.01em;
  }
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 0;
  border: none;
  background: none;
  color: $color-accent;
  font-size: $font-size-base;
  font-family: $font-family;
  cursor: pointer;

  &:hover {
    text-decoration: underline;
  }
}

// ── Card — Apple Card style ─────────────────
.vip-card {
  border-radius: $radius-lg;
  padding: 28px 24px 24px;
  margin-bottom: 36px;
  color: #fff;
  background: linear-gradient(135deg, #1c1c1e 0%, #2c2c2e 50%, #1c1c1e 100%);
  border: 1px solid rgba(255, 255, 255, 0.08);

  &.card-lifetime {
    background: linear-gradient(135deg, #1c1c1e 0%, #2d1b69 50%, #8b3a3a 100%);
  }

  &.card-none {
    background: linear-gradient(135deg, #8e8e93 0%, #636366 100%);
  }
}

.card-chip {
  font-size: 14px;
  font-weight: 600;
  opacity: 0.5;
  letter-spacing: 0.04em;
  margin-bottom: 24px;
}

.card-type {
  font-size: 32px;
  font-weight: 700;
  letter-spacing: -0.02em;
  margin-bottom: 6px;
}

.card-holder {
  font-size: $font-size-sm;
  opacity: 0.6;
  margin-bottom: 24px;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
}

.card-dates {
  display: flex;
  gap: 28px;
}

.date-row {
  display: flex;
  flex-direction: column;
  gap: 2px;

  .date-label {
    font-size: 10px;
    opacity: 0.4;
    text-transform: uppercase;
    letter-spacing: 0.05em;
    font-weight: 500;
  }

  span {
    font-size: $font-size-xs;
  }
}

.card-progress {
  text-align: right;

  .progress-bar {
    width: 80px;
    height: 3px;
    background: rgba(255, 255, 255, 0.15);
    border-radius: 2px;
    margin-bottom: 4px;
    overflow: hidden;
  }

  .progress-fill {
    height: 100%;
    background: rgba(255, 255, 255, 0.6);
    border-radius: 2px;
    transition: width 0.5s;
  }

  .progress-text {
    font-size: 11px;
    opacity: 0.5;

    &.text-warn {
      color: $color-warning;
      opacity: 1;
    }
  }
}

// ── Plans ────────────────────────────────────
.section {
  margin-bottom: 36px;

  h3 {
    font-size: $font-size-xl;
    font-weight: 600;
    letter-spacing: -0.01em;
    color: $color-text-primary;
    margin-bottom: 16px;
  }
}

.plan-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 20px;
}

.plan-card {
  border: 2px solid $color-border-light;
  border-radius: $radius-md;
  padding: 20px 16px;
  cursor: pointer;
  text-align: center;
  transition: all $transition-fast;
  background: $color-bg;

  &:hover {
    border-color: $color-accent;
  }

  &.selected {
    border-color: $color-accent;
  }
}

.plan-name {
  font-size: $font-size-base;
  font-weight: 600;
  color: $color-text-primary;
  margin-bottom: 8px;
  letter-spacing: -0.01em;
}

.plan-price {
  font-size: $font-size-2xl;
  font-weight: 700;
  color: $color-text-primary;
  margin-bottom: 4px;
  letter-spacing: -0.02em;
}

.plan-currency {
  font-size: $font-size-base;
  font-weight: 500;
}

.plan-unit {
  font-size: $font-size-sm;
  color: $color-text-secondary;
  font-weight: 400;
}

.plan-desc {
  font-size: $font-size-xs;
  color: $color-text-secondary;
}

.renew-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 500;
  border-radius: $radius-md;
}

// ── Orders ───────────────────────────────────
.order-list {
  background: $color-bg;
  border: 1px solid $color-border-light;
  border-radius: $radius-lg;
  overflow: hidden;
}

.order-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 20px;
  border-bottom: 1px solid $color-border-light;

  &:last-child {
    border-bottom: none;
  }
}

.order-left {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.order-type {
  font-size: $font-size-sm;
  color: $color-text-primary;
  font-weight: 500;
}

.order-time {
  font-size: $font-size-xs;
  color: $color-text-secondary;
}

.order-amount {
  font-weight: 600;
  color: $color-text-primary;
  letter-spacing: -0.01em;
}

html.dark {
  .page-nav h2,
  .section h3,
  .plan-name,
  .plan-price,
  .order-type,
  .order-amount {
    color: $dark-text;
  }

  .plan-card {
    background: $dark-bg-secondary;
    border-color: $dark-border;
  }

  .plan-desc,
  .plan-unit,
  .order-time {
    color: $dark-text-secondary;
  }

  .order-list {
    background: $dark-bg;
    border-color: $dark-border;
  }

  .order-row {
    border-bottom-color: $dark-border;
  }
}
</style>
