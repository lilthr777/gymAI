<template>
  <div class="table-wrapper">
    <!-- Search -->
    <div v-if="$slots.search || showSearch" class="search-bar">
      <el-form :inline="true" :model="searchParams" @submit.prevent="handleSearch">
        <slot name="search" :params="searchParams" :update="updateSearchParams" />
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            搜索
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- Toolbar -->
    <div v-if="$slots.toolbar" class="toolbar">
      <slot name="toolbar" :selectedRows="selectedRows" />
    </div>

    <!-- Table -->
    <div class="table-shell">
      <el-table
        ref="tableRef"
        v-loading="loading"
        :data="data"
        :border="border"
        :stripe="stripe"
        @selection-change="handleSelectionChange"
        @sort-change="handleSortChange"
      >
        <el-table-column
          v-if="showSelection"
          type="selection"
          width="50"
          align="center"
        />
        <el-table-column
          v-if="showIndex"
          type="index"
          label="#"
          width="60"
          align="center"
        />
        <slot />
      </el-table>

      <!-- Pagination -->
      <div v-if="showPagination" class="pagination-bar">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="pageSizes"
          :total="total"
          :layout="paginationLayout"
          background
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts" generic="T extends Record<string, any>">
import { ref, reactive } from 'vue'
import type { TableInstance } from 'element-plus'

interface Props {
  loading?: boolean
  data?: T[]
  total?: number
  border?: boolean
  stripe?: boolean
  showSearch?: boolean
  showSelection?: boolean
  showIndex?: boolean
  showPagination?: boolean
  pageSizes?: number[]
  paginationLayout?: string
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  data: () => [],
  total: 0,
  border: true,
  stripe: true,
  showSearch: false,
  showSelection: false,
  showIndex: true,
  showPagination: true,
  pageSizes: () => [10, 20, 50, 100],
  paginationLayout: 'total, sizes, prev, pager, next, jumper',
})

const emit = defineEmits<{
  search: [params: Record<string, any>]
  reset: []
  'update:page': [page: number, size: number]
  'sort-change': [sort: { prop: string; order: string }]
  'selection-change': [rows: T[]]
}>()

const tableRef = ref<TableInstance>()
const currentPage = ref(1)
const pageSize = ref(10)
const selectedRows = ref<T[]>([])
const searchParams = reactive<Record<string, any>>({})

const updateSearchParams = (key: string, value: any) => {
  searchParams[key] = value
}

const handleSearch = () => {
  currentPage.value = 1
  emit('search', { ...searchParams, pageNum: 1, pageSize: pageSize.value })
}

const handleReset = () => {
  Object.keys(searchParams).forEach((key) => delete searchParams[key])
  currentPage.value = 1
  emit('reset')
}

const handlePageChange = (page: number) => {
  currentPage.value = page
  emit('update:page', page, pageSize.value)
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
  emit('update:page', 1, size)
}

const handleSelectionChange = (rows: T[]) => {
  selectedRows.value = rows
  emit('selection-change', rows)
}

const handleSortChange = (sort: any) => {
  emit('sort-change', sort)
}

defineExpose({ tableRef, currentPage, pageSize, searchParams })
</script>

<style scoped lang="scss">
.table-wrapper {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.search-bar {
  padding: 16px 20px;
  background: $color-sheet;
  border: 1px solid $color-steel;
  border-radius: $radius-lg;

  :deep(.el-form) {
    display: flex;
    flex-wrap: wrap;
    align-items: center;
    gap: 12px;
  }

  :deep(.el-form-item) {
    margin: 0;
  }
}

.toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}

.table-shell {
  background: $color-sheet;
  border: 1px solid $color-steel;
  border-radius: $radius-lg;
  overflow: hidden;
}

.pagination-bar {
  display: flex;
  justify-content: flex-end;
  padding: 14px 20px;
  border-top: 1px solid $color-steel;
}

// ── Dark Mode ────────────────────────────────
html.dark {
  .search-bar {
    background: $dark-bg-secondary;
    border-color: $dark-border;
  }

  .table-shell {
    background: $dark-bg-secondary;
    border-color: $dark-border;
  }

  .pagination-bar {
    border-top-color: $dark-border;
  }
}

// ── Responsive ───────────────────────────────
@media (max-width: 768px) {
  .search-bar {
    padding: 12px 14px;
  }

  .pagination-bar {
    padding: 12px 14px;
  }
}
</style>
