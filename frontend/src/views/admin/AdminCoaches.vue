<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { adminApi, coachApi } from '@/api'
import type { Coach, PageResult } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'

const isMobile = ref(false)
function onResize() { isMobile.value = window.innerWidth < 768 }

const coaches = ref<Coach[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')
const form = ref<Coach>({ name: '', phone: '', gender: 1, specialty: '', description: '', avatar: '', status: 1 })
const editingId = ref<number | null>(null)

function fetchCoaches() {
  coachApi.list({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value || undefined })
    .then(res => {
      if (res.data) { coaches.value = res.data.records; total.value = res.data.total }
    })
}

function openDialog(title: string, coach?: Coach) {
  dialogTitle.value = title
  if (coach) {
    editingId.value = coach.id!
    form.value = { ...coach }
  } else {
    editingId.value = null
    form.value = { name: '', phone: '', gender: 1, specialty: '', description: '', avatar: '', status: 1 }
  }
  dialogVisible.value = true
}

async function submitForm() {
  if (!form.value.name || !form.value.phone) {
    ElMessage.warning('请填写姓名和手机号')
    return
  }
  if (editingId.value) {
    await adminApi.updateCoach(editingId.value, form.value)
    ElMessage.success('教练信息已更新')
  } else {
    await adminApi.createCoach(form.value)
    ElMessage.success('教练已添加')
  }
  dialogVisible.value = false
  fetchCoaches()
}

async function deleteCoach(coach: Coach) {
  try {
    await ElMessageBox.confirm(`确认删除教练「${coach.name}」?`, '删除教练', { type: 'warning' })
  } catch { return }
  await adminApi.deleteCoach(coach.id!)
  ElMessage.success('教练已删除')
  fetchCoaches()
}

function handlePageChange(p: number) { pageNum.value = p; fetchCoaches() }

onMounted(() => {
  onResize()
  window.addEventListener('resize', onResize)
  fetchCoaches()
})
onUnmounted(() => {
  window.removeEventListener('resize', onResize)
})
</script>

<template>
  <div class="admin-coaches">
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索教练姓名或专长" clearable style="width: 260px"
        :prefix-icon="Search" @keyup.enter="fetchCoaches" />
      <el-button type="primary" :icon="Plus" @click="openDialog('新增教练')">新增教练</el-button>
    </div>

    <el-card shadow="never" style="margin-top: 16px" class="table-card">
      <div class="table-scroll">
        <el-table :data="coaches" v-loading="loading" stripe>
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="phone" label="手机号" width="130" />
        <el-table-column label="性别" width="60">
          <template #default="{ row }">{{ row.gender === 1 ? '男' : row.gender === 2 ? '女' : '-' }}</template>
        </el-table-column>
        <el-table-column prop="specialty" label="专长" min-width="180" />
        <el-table-column prop="description" label="简介" min-width="200" show-overflow-tooltip />
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ row.status === 1 ? '在职' : '离职' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" :width="isMobile ? 100 : 150">
          <template #default="{ row }">
            <div class="action-btns">
              <el-button link size="small" :icon="Edit" class="edit-btn" :title="isMobile ? '编辑' : ''" @click="openDialog('编辑教练', row)">
                <span v-if="!isMobile">编辑</span>
              </el-button>
              <el-button type="danger" link size="small" :icon="Delete" :title="isMobile ? '删除' : ''" @click="deleteCoach(row)">
                <span v-if="!isMobile">删除</span>
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

    <!-- 新增/编辑教练对话框 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="480px" destroy-on-close>
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="教练姓名" />
        </el-form-item>
        <el-form-item label="手机号" required>
          <el-input v-model="form.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="专长">
          <el-input v-model="form.specialty" placeholder="多个专长用逗号分隔，如：增肌,减脂" />
        </el-form-item>
        <el-form-item label="简介">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="教练简介" />
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
.admin-coaches {
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
  .admin-coaches {
    .toolbar {
      flex-direction: column;
      align-items: stretch;

      .el-input { width: 100% !important; }
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
