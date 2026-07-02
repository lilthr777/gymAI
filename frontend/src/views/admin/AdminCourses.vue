<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { adminApi, coachApi } from '@/api'
import type { Course, Coach } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'

const isMobile = ref(false)
function onResize() { isMobile.value = window.innerWidth < 768 }

const courses = ref<Course[]>([])
const coaches = ref<Coach[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref<Course>({
  name: '', coachId: 0, courseDate: '', startTime: '', endTime: '',
  maxCapacity: 20, price: 0, description: '', location: '', status: 1,
  currentCount: 0,
})
const editingId = ref<number | null>(null)

function fetchCourses() {
  loading.value = true
  adminApi.adminCourses({ pageNum: pageNum.value, pageSize: pageSize.value })
    .then(res => {
      if (res.data) { courses.value = res.data.records; total.value = res.data.total }
    })
    .finally(() => loading.value = false)
}

function fetchCoaches() {
  coachApi.list({ pageNum: 1, pageSize: 100 }).then(res => {
    if (res.data) coaches.value = res.data.records
  })
}

function openDialog(title: string, course?: Course) {
  dialogTitle.value = title
  if (course) {
    editingId.value = course.id!
    form.value = { ...course }
  } else {
    editingId.value = null
    form.value = {
      name: '', coachId: 0, courseDate: '', startTime: '', endTime: '',
      maxCapacity: 20, price: 0, description: '', location: '', status: 1,
      currentCount: 0,
    }
  }
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.value.name || !form.value.coachId || !form.value.courseDate) {
    ElMessage.warning('请填写课程名称、教练和日期')
    return
  }
  if (editingId.value) {
    await adminApi.updateCourse(editingId.value, form.value)
    ElMessage.success('课程信息已更新')
  } else {
    await adminApi.createCourse(form.value)
    ElMessage.success('课程已创建')
  }
  dialogVisible.value = false
  fetchCourses()
}

async function deleteCourse(course: Course) {
  try {
    await ElMessageBox.confirm(`确认取消课程「${course.name}」?`, '取消课程', { type: 'warning' })
  } catch { return }
  await adminApi.deleteCourse(course.id!)
  ElMessage.success('课程已取消')
  fetchCourses()
}

function handlePageChange(p: number) { pageNum.value = p; fetchCourses() }

const statusTagType = (s: number) => s === 1 ? 'success' : s === 2 ? 'warning' : 'info'
const statusLabel = (s: number) => s === 1 ? '正常' : s === 2 ? '满员' : '已取消'

onMounted(() => {
  onResize()
  window.addEventListener('resize', onResize)
  fetchCourses()
  fetchCoaches()
})
onUnmounted(() => {
  window.removeEventListener('resize', onResize)
})
</script>

<template>
  <div class="admin-courses">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-button type="primary" :icon="Plus" @click="openDialog('新增课程')">新增课程</el-button>
      </div>
    </div>

    <el-card shadow="never" style="margin-top: 16px" class="table-card">
      <div class="table-scroll">
        <el-table :data="courses" v-loading="loading" stripe>
        <el-table-column prop="name" label="课程名称" min-width="130" />
        <el-table-column prop="coachName" label="教练" width="100" />
        <el-table-column prop="courseDate" label="日期" width="110" />
        <el-table-column label="时间" width="130">
          <template #default="{ row }">{{ row.startTime }} - {{ row.endTime }}</template>
        </el-table-column>
        <el-table-column label="容量" width="100">
          <template #default="{ row }">{{ row.currentCount }}/{{ row.maxCapacity }}</template>
        </el-table-column>
        <el-table-column label="价格" width="80">
          <template #default="{ row }">¥{{ row.price || 0 }}</template>
        </el-table-column>
        <el-table-column prop="location" label="地点" min-width="150" />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="isMobile ? 100 : 150">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button link size="small" :icon="Edit" class="edit-btn" :title="isMobile ? '编辑' : ''" @click="openDialog('编辑课程', row)">
                <span v-if="!isMobile">编辑</span>
              </el-button>
              <el-button type="danger" link size="small" :icon="Delete" :title="isMobile ? '取消' : ''" @click="deleteCourse(row)">
                <span v-if="!isMobile">取消</span>
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      </div>
      <div style="margin-top: 16px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="pageNum" :page-size="pageSize" :total="total"
          layout="total, prev, pager, next" @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 新增/编辑课程对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="课程名称" required>
          <el-input v-model="form.name" placeholder="课程名称" />
        </el-form-item>
        <el-form-item label="教练" required>
          <el-select v-model="form.coachId" placeholder="选择教练" style="width: 100%">
            <el-option v-for="c in coaches" :key="c.id" :label="c.name" :value="c.id!" />
          </el-select>
        </el-form-item>
        <el-form-item label="日期" required>
          <el-date-picker v-model="form.courseDate" type="date" placeholder="选择日期"
            value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="时间" required>
          <el-time-picker v-model="form.startTime" placeholder="开始时间" format="HH:mm" value-format="HH:mm"
            style="width: 48%" />
          <span style="margin: 0 2%">-</span>
          <el-time-picker v-model="form.endTime" placeholder="结束时间" format="HH:mm" value-format="HH:mm"
            style="width: 48%" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="最大人数">
              <el-input-number v-model="form.maxCapacity" :min="1" :max="200" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="价格(元)">
              <el-input-number v-model="form.price" :min="0" :precision="2" :step="10" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="上课地点">
          <el-input v-model="form.location" placeholder="如：A区力量训练房" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" :rows="2" placeholder="课程描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.admin-courses {
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
  }

  .table-card {
    .table-scroll {
      overflow-x: auto;
      -webkit-overflow-scrolling: touch;
    }
  }

  .action-btns {
    display: flex;
    align-items: center;
    gap: 4px;
    white-space: nowrap;
  }

  .edit-btn {
    color: var(--el-text-color-regular);
    &:hover { color: var(--el-color-primary); }
  }
}

@media (max-width: 767px) {
  .admin-courses {
    .toolbar {
      flex-direction: column;
      align-items: stretch;

      > .el-button { width: 100%; }
    }

    .table-card :deep(.el-card__body) { padding: 12px; }

    .action-btns {
      gap: 2px;
      :deep(.el-button) { padding: 4px 6px; }
    }
  }
}
</style>
