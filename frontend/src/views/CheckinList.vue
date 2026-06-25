<template>
  <div class="checkin-list-page">
    <TableWrapper
      :data="list"
      :loading="loading"
      :total="total"
      v-model:page="page"
      v-model:page-size="pageSize"
      @search="handleSearch"
      @reset="handleSearch"
      @load="fetchList"
    >
      <template #search>
        <el-input-number
          v-model="filterMemberId"
          placeholder="会员ID"
          :min="1"
          controls-position="right"
          style="width: 160px"
        />
        <el-input-number
          v-model="filterCourseId"
          placeholder="课程ID"
          :min="1"
          controls-position="right"
          style="width: 160px; margin-left: 12px"
        />
        <el-date-picker
          v-model="filterDate"
          type="date"
          placeholder="签到日期"
          value-format="YYYY-MM-DD"
          style="width: 180px; margin-left: 12px"
        />
      </template>

      <template #toolbar>
        <el-button type="primary" @click="openCheckinDialog">
          <el-icon><Select /></el-icon>
          快速签到
        </el-button>
      </template>

      <el-table-column label="会员" width="140">
        <template #default="{ row }">
          <span>{{ row.memberName ?? `会员 #${row.memberId}` }}</span>
        </template>
      </el-table-column>
      <el-table-column label="课程" width="140">
        <template #default="{ row }">
          <span>{{ row.courseName ?? `课程 #${row.courseId}` }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="checkinTime" label="签到时间" width="170" />
      <el-table-column label="签到状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="checkinStatusTag(row.status)" size="small">
            {{ checkinStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="备注" min-width="140" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.remark || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button type="danger" link size="small" @click="handleDelete(row)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>
    </TableWrapper>

    <!-- Quick Checkin Dialog -->
    <el-dialog
      v-model="dialogVisible"
      title="快速签到"
      width="480px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="选择会员" prop="memberId">
          <el-select
            v-model="form.memberId"
            filterable
            remote
            reserve-keyword
            :remote-method="searchMembers"
            :loading="memberSearchLoading"
            placeholder="请输入会员姓名或手机号搜索"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="m in memberOptions"
              :key="m.id"
              :label="`${m.name} (${m.phone})`"
              :value="m.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="选择课程" prop="courseId">
          <el-select
            v-model="form.courseId"
            filterable
            remote
            reserve-keyword
            :remote-method="searchCourses"
            :loading="courseSearchLoading"
            placeholder="请输入课程名称搜索"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="c in courseOptions"
              :key="c.id"
              :label="`${c.name} (${c.courseDate} ${c.startTime})`"
              :value="c.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="可选备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleCheckin">
          签到
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Select, Delete } from '@element-plus/icons-vue'
import { checkinApi, memberApi, courseApi } from '@/api'
import TableWrapper from '@/components/TableWrapper.vue'
import type { Checkin, Member, Course } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const list = ref<Checkin[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

const filterMemberId = ref<number | undefined>(undefined)
const filterCourseId = ref<number | undefined>(undefined)
const filterDate = ref<string>('')

const dialogVisible = ref(false)
const formRef = ref<FormInstance>()

const memberOptions = ref<Member[]>([])
const courseOptions = ref<Course[]>([])
const memberSearchLoading = ref(false)
const courseSearchLoading = ref(false)

const form = reactive({
  memberId: undefined as number | undefined,
  courseId: undefined as number | undefined,
  remark: '',
})

const rules: FormRules = {
  memberId: [{ required: true, message: '请选择会员', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
}

const checkinStatusLabel = (status: number): string => {
  const map: Record<number, string> = { 0: '已取消', 1: '已签到', 2: '迟到' }
  return map[status] ?? '未知'
}

const checkinStatusTag = (status: number): string => {
  const map: Record<number, string> = { 0: 'info', 1: 'success', 2: 'danger' }
  return map[status] ?? 'info'
}

const fetchList = async () => {
  loading.value = true
  try {
    const params: Record<string, unknown> = {
      pageNum: page.value,
      pageSize: pageSize.value,
    }
    if (filterMemberId.value) params.memberId = filterMemberId.value
    if (filterCourseId.value) params.courseId = filterCourseId.value
    if (filterDate.value) params.date = filterDate.value
    const res = await checkinApi.list(params as any)
    list.value = (res.data as any)?.records ?? []
    total.value = (res.data as any)?.total ?? 0
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  page.value = 1
  fetchList()
}

const searchMembers = async (query: string) => {
  if (!query) {
    memberOptions.value = []
    return
  }
  memberSearchLoading.value = true
  try {
    const res = await memberApi.list({ keyword: query, pageNum: 1, pageSize: 20 })
    memberOptions.value = (res.data as any)?.records ?? []
  } finally {
    memberSearchLoading.value = false
  }
}

const searchCourses = async (query: string) => {
  if (!query) {
    courseOptions.value = []
    return
  }
  courseSearchLoading.value = true
  try {
    const res = await courseApi.list({ keyword: query, pageNum: 1, pageSize: 20 })
    courseOptions.value = (res.data as any)?.records ?? []
  } finally {
    courseSearchLoading.value = false
  }
}

const openCheckinDialog = () => {
  form.memberId = undefined
  form.courseId = undefined
  form.remark = ''
  memberOptions.value = []
  courseOptions.value = []
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
}

const handleCheckin = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await checkinApi.create({
        memberId: form.memberId,
        courseId: form.courseId,
        remark: form.remark,
      })
      ElMessage.success('签到成功')
      dialogVisible.value = false
      fetchList()
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = (row: Checkin) => {
  ElMessageBox.confirm('确定要删除这条签到记录吗？删除后不可恢复。', '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await checkinApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchList()
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.checkin-list-page {
  padding: $content-padding;
}
</style>
