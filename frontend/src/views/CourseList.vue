<template>
  <div class="course-list-page">
    <TableWrapper
      :data="list"
      :loading="loading"
      :total="total"
      v-model:page="page"
      v-model:page-size="pageSize"
      @load="fetchList"
    >
      <template #search>
        <el-input
          v-model="keyword"
          placeholder="搜索课程名称"
          clearable
          style="width: 220px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-select
          v-model="filterStatus"
          placeholder="课程状态"
          clearable
          style="width: 140px; margin-left: 12px"
          @change="handleSearch"
        >
          <el-option label="正常" :value="1" />
          <el-option label="已满" :value="2" />
          <el-option label="已取消" :value="0" />
        </el-select>
        <el-button type="primary" @click="handleSearch" style="margin-left: 12px">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </template>

      <template #toolbar>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          新增课程
        </el-button>
      </template>

      <el-table-column type="index" label="#" width="60" />
      <el-table-column prop="name" label="课程名称" min-width="140" />
      <el-table-column label="授课教练" width="120">
        <template #default="{ row }">
          {{ row.coachName ?? coachNameMap[row.coachId] ?? '-' }}
        </template>
      </el-table-column>
      <el-table-column prop="courseDate" label="课程日期" width="120" />
      <el-table-column label="上课时间" width="160">
        <template #default="{ row }">
          {{ row.startTime }} - {{ row.endTime }}
        </template>
      </el-table-column>
      <el-table-column label="容量" width="100" align="center">
        <template #default="{ row }">
          <span :class="{ 'text-danger': row.currentCount >= row.maxCapacity }">
            {{ row.currentCount }}/{{ row.maxCapacity }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="courseStatusTag(row.status)" size="small">
            {{ courseStatusLabel(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="描述" min-width="160" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.description || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link size="small" @click="openEditDialog(row)">
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
          <el-button type="danger" link size="small" @click="handleDelete(row)">
            <el-icon><Delete /></el-icon>
            删除
          </el-button>
        </template>
      </el-table-column>
    </TableWrapper>

    <!-- Add/Edit Dialog -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑课程' : '新增课程'"
      width="560px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="授课教练" prop="coachId">
          <el-select v-model="form.coachId" placeholder="请选择教练" style="width: 100%">
            <el-option
              v-for="coach in coachOptions"
              :key="coach.id"
              :label="coach.name"
              :value="coach.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="课程日期" prop="courseDate">
          <el-date-picker
            v-model="form.courseDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="上课时间" prop="timeRange">
          <el-time-picker
            v-model="form.timeRange"
            is-range
            range-separator="-"
            start-placeholder="开始时间"
            end-placeholder="结束时间"
            format="HH:mm"
            value-format="HH:mm"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="最大容量" prop="maxCapacity">
          <el-input-number
            v-model="form.maxCapacity"
            :min="1"
            :max="200"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="课程描述" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入课程描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          确 定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { courseApi, coachApi } from '@/api'
import TableWrapper from '@/components/TableWrapper.vue'
import type { Course, Coach } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const list = ref<Course[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const filterStatus = ref<number | undefined>(undefined)

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const coachOptions = ref<Coach[]>([])
const coachNameMap = ref<Record<number, string>>({})

const defaultForm = () => ({
  name: '',
  coachId: undefined as number | undefined,
  courseDate: '',
  timeRange: ['', ''] as [string, string],
  maxCapacity: 30,
  description: '',
})

const form = reactive({ ...defaultForm() })

const rules: FormRules = {
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  coachId: [{ required: true, message: '请选择教练', trigger: 'change' }],
  courseDate: [{ required: true, message: '请选择课程日期', trigger: 'change' }],
  maxCapacity: [{ required: true, message: '请输入最大容量', trigger: 'blur' }],
}

const courseStatusLabel = (status: number): string => {
  const map: Record<number, string> = { 0: '已取消', 1: '正常', 2: '已满' }
  return map[status] ?? '未知'
}

const courseStatusTag = (status: number): string => {
  const map: Record<number, string> = { 0: 'danger', 1: 'success', 2: 'warning' }
  return map[status] ?? 'info'
}

const fetchCoachOptions = async () => {
  try {
    const res = await coachApi.list({ pageNum: 1, pageSize: 100 })
    const coaches = (res.data as any)?.records ?? []
    coachOptions.value = coaches
    coaches.forEach((c: Coach) => {
      coachNameMap.value[c.id] = c.name
    })
  } catch {
    // ignore
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await courseApi.list({
      pageNum: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
    })
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

const openAddDialog = () => {
  isEdit.value = false
  editingId.value = null
  Object.assign(form, defaultForm())
  dialogVisible.value = true
}

const openEditDialog = (row: Course) => {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    coachId: row.coachId,
    courseDate: row.courseDate,
    timeRange: [row.startTime, row.endTime] as [string, string],
    maxCapacity: row.maxCapacity,
    description: row.description,
  })
  dialogVisible.value = true
}

const resetForm = () => {
  formRef.value?.resetFields()
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        name: form.name,
        coachId: form.coachId,
        courseDate: form.courseDate,
        startTime: form.timeRange[0],
        endTime: form.timeRange[1],
        maxCapacity: form.maxCapacity,
        description: form.description,
      }
      if (isEdit.value && editingId.value) {
        await courseApi.update(editingId.value, payload)
        ElMessage.success('更新成功')
      } else {
        await courseApi.create(payload)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchList()
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = (row: Course) => {
  ElMessageBox.confirm(`确定要删除课程「${row.name}」吗？删除后不可恢复。`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await courseApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchList()
  })
}

onMounted(() => {
  fetchCoachOptions()
  fetchList()
})
</script>

<style scoped lang="scss">
.course-list-page {
  padding: $content-padding;
}

.text-danger {
  color: #f56c6c;
  font-weight: 600;
}
</style>
