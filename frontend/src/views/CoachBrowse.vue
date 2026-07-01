<template>
  <div class="coach-browse">
    <el-input v-model="keyword" placeholder="搜索教练" clearable class="search-bar" @input="search">
      <template #prefix><el-icon><Search /></el-icon></template>
    </el-input>

    <!-- 分类标签 -->
    <div class="filter-tabs">
      <button v-for="t in tabs" :key="t.key" class="filter-tab" :class="{ active: activeTab === t.key }"
              @click="switchTab(t.key)">{{ t.label }}</button>
    </div>

    <CoachCard v-for="c in filteredList" :key="c.id" :coach="c" @click="$router.push(`/coaches/${c.id}`)" />

    <el-empty v-if="!loading && !filteredList.length" description="暂无教练" />

    <div v-if="total > list.length" class="load-more">
      <el-button :loading="loading" @click="loadMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { Search } from '@element-plus/icons-vue'
import { coachApi, favoriteApi } from '@/api'
import type { Coach } from '@/types'
import CoachCard from '@/components/CoachCard.vue'

const keyword = ref('')
const list = ref<Coach[]>([])
const favoritedIds = ref<number[]>([])
const total = ref(0)
const pageNum = ref(1)
const loading = ref(false)
const activeTab = ref('all')

const tabs = [
  { key: 'all', label: '全部' },
  { key: 'favorites', label: '已收藏' },
  { key: 'yoga', label: '瑜伽' },
  { key: 'strength', label: '增肌' },
  { key: 'boxing', label: '搏击' },
  { key: 'dance', label: '舞蹈' },
]

const specialtyMap: Record<string, string[]> = {
  yoga: ['瑜伽', '普拉提', '冥想'],
  strength: ['增肌', '减脂', '塑形', 'CrossFit', '体能'],
  boxing: ['拳击', '搏击', '散打', '格斗'],
  dance: ['舞蹈', '街舞', '霹雳舞', 'Hip-Hop', 'Zumba', '有氧操', '动感单车'],
}

const filteredList = computed(() => {
  let result = list.value
  if (activeTab.value === 'favorites') {
    result = result.filter(c => favoritedIds.value.includes(c.id!))
  } else if (activeTab.value !== 'all') {
    const keywords = specialtyMap[activeTab.value] || []
    result = result.filter(c => {
      const spec = c.specialty || ''
      return keywords.some(k => spec.includes(k))
    })
  }
  return result
})

const switchTab = async (key: string) => {
  activeTab.value = key
  if (key === 'favorites') {
    // 收藏列表：拉全量教练然后本地筛选
    try {
      const [coachRes, favRes] = await Promise.all([
        coachApi.list({ pageNum: 1, pageSize: 100 }),
        favoriteApi.ids(),
      ])
      list.value = coachRes.data.records
      favoritedIds.value = favRes.data
      total.value = coachRes.data.total
    } catch { /* handled */ }
  } else {
    fetchList(true)
  }
}

const fetchList = async (reset = false) => {
  loading.value = true
  if (reset) { pageNum.value = 1; list.value = [] }
  try {
    const res = await coachApi.list({ pageNum: pageNum.value, pageSize: 20, keyword: keyword.value || undefined })
    if (reset) list.value = res.data.records
    else list.value.push(...res.data.records)
    total.value = res.data.total
  } catch { /* handled */ }
  finally { loading.value = false }
}

let timer: ReturnType<typeof setTimeout>
const search = () => { clearTimeout(timer); timer = setTimeout(() => fetchList(true), 300) }
const loadMore = () => { pageNum.value++; fetchList() }

onMounted(async () => {
  await fetchList()
  // 加载收藏列表
  try { favoritedIds.value = (await favoriteApi.ids()).data } catch { /* handled */ }
})
</script>

<style scoped lang="scss">
.coach-browse { padding: 16px; }
.search-bar { margin-bottom: 12px;
  :deep(.el-input__wrapper) { border-radius: 20px; }
}

.filter-tabs { display: flex; gap: 8px; margin-bottom: 16px; overflow-x: auto; padding-bottom: 4px; }

.filter-tab { padding: 6px 14px; border: 1px solid $color-steel; border-radius: 20px; background: $color-sheet;
  font-size: 13px; color: $color-ash; cursor: pointer; white-space: nowrap; transition: all 0.2s;
  &:hover { border-color: $color-cobalt; color: $color-cobalt; }
  &.active { background: $color-cobalt; color: #fff; border-color: $color-cobalt; }
}

.load-more { text-align: center; padding: 16px; }

html.dark {
  .filter-tab { background: $dark-bg-card; border-color: $dark-border; color: $dark-text-secondary;
    &.active { background: $color-cobalt; color: #fff; }
  }
}
</style>
