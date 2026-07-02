<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { adminApi } from '@/api'
import type { User } from '@/types'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Delete, Edit } from '@element-plus/icons-vue'

const isMobile = ref(false)
function onResize() { isMobile.value = window.innerWidth < 768 }

const users = ref<User[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')
const roleFilter = ref('')
const loading = ref(false)

// 编辑弹窗
const editDialogVisible = ref(false)
const editingUser = ref<User>({} as User)
const editForm = ref({ nickname: '', phone: '' })

function openEditDialog(user: User) {
  editingUser.value = user
  editForm.value = { nickname: user.nickname || '', phone: user.phone || '' }
  editDialogVisible.value = true
}

async function saveEdit() {
  await adminApi.updateUser(editingUser.value.id!, editForm.value)
  ElMessage.success('用户信息已更新')
  editDialogVisible.value = false
  fetchUsers()
}

function fetchUsers() {
  loading.value = true
  adminApi.users({
    pageNum: pageNum.value,
    pageSize: pageSize.value,
    keyword: keyword.value || undefined,
    role: roleFilter.value || undefined,
  }).then(res => {
    if (res.data) { users.value = res.data.records; total.value = res.data.total }
  }).finally(() => loading.value = false)
}

function onSearch() {
  pageNum.value = 1
  fetchUsers()
}

async function updateRole(user: User, newRole: string) {
  try {
    await ElMessageBox.confirm(
      `确认将用户「${user.nickname || user.username}」的角色改为「${newRole === 'ADMIN' ? '管理员' : newRole === 'COACH' ? '教练' : '会员'}」?`,
      '修改角色',
      { type: 'warning' }
    )
  } catch { return }
  await adminApi.updateUser(user.id!, { role: newRole } as any)
  ElMessage.success('角色已更新')
  fetchUsers()
}

async function deleteUser(user: User) {
  try {
    await ElMessageBox.confirm(
      `确认删除用户「${user.nickname || user.username}」（ID: ${user.id}）吗？此操作不可撤销。`,
      '删除用户',
      { type: 'error', confirmButtonText: '确认删除', cancelButtonText: '取消' }
    )
  } catch { return }
  await adminApi.deleteUser(user.id!)
  ElMessage.success('用户已删除')
  fetchUsers()
}

function handlePageChange(p: number) { pageNum.value = p; fetchUsers() }

const roleTagType = (role: string) => role === 'ADMIN' ? 'danger' : role === 'COACH' ? 'warning' : 'info'
const roleLabel = (role: string) => role === 'ADMIN' ? '管理员' : role === 'COACH' ? '教练' : '会员'
const cardLabel = (type: string) => ({ MONTH: '月卡', QUARTER: '季卡', YEAR: '年卡', LIFETIME: '终身卡' } as any)[type] || type || '-'

onMounted(() => {
  onResize()
  window.addEventListener('resize', onResize)
  fetchUsers()
})
onUnmounted(() => {
  window.removeEventListener('resize', onResize)
})
</script>

<template>
  <div class="admin-users">
    <!-- 搜索栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="keyword" placeholder="搜索用户ID或用户名" clearable :prefix-icon="Search"
          class="search-input" @keyup.enter="onSearch" />
        <el-select v-model="roleFilter" placeholder="角色筛选" clearable class="role-select" @change="onSearch">
          <el-option label="管理员" value="ADMIN" />
          <el-option label="教练" value="COACH" />
          <el-option label="会员" value="MEMBER" />
        </el-select>
      </div>
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
    </div>

    <!-- 表格 -->
    <el-card shadow="never" class="table-card">
      <div class="table-scroll">
        <el-table :data="users" v-loading="loading" stripe>
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column label="用户名" width="140">
            <template #default="{ row }">
              <span>{{ row.username }}</span>
              <el-tag :type="roleTagType(row.role)" size="small" style="margin-left: 6px">
                {{ roleLabel(row.role) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="nickname" label="昵称" width="100" />
          <el-table-column prop="phone" label="手机号" width="130" />
          <el-table-column label="角色" width="120">
            <template #default="{ row }">
              <el-select :model-value="row.role" size="small" style="width: 90px"
                @change="(val: string) => updateRole(row, val)">
                <el-option label="管理员" value="ADMIN" />
                <el-option label="教练" value="COACH" />
                <el-option label="会员" value="MEMBER" />
              </el-select>
            </template>
          </el-table-column>
          <el-table-column label="会员卡" width="100">
            <template #default="{ row }">
              {{ cardLabel(row.cardType || '') }}
            </template>
          </el-table-column>
          <el-table-column label="操作" :width="isMobile ? 100 : 140" fixed="right">
            <template #default="{ row }">
              <div class="action-btns">
                <el-button link size="small" :icon="Edit" class="edit-btn" :title="isMobile ? '编辑' : ''" @click="openEditDialog(row)">
                  <span v-if="!isMobile">编辑</span>
                </el-button>
                <el-button type="danger" link size="small" :icon="Delete" :title="isMobile ? '删除' : ''" @click="deleteUser(row)">
                  <span v-if="!isMobile">删除</span>
                </el-button>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="pageNum" :page-size="pageSize" :total="total"
          layout="total, prev, pager, next" :small="true"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑用户" width="460px" class="edit-dialog" destroy-on-close>
      <el-form :model="editForm" label-width="70px">
        <el-form-item label="用户名">
          <el-input :model-value="editingUser.username" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="editForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveEdit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped lang="scss">
.admin-users {
  .toolbar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;

    .toolbar-left {
      display: flex;
      gap: 12px;
      align-items: center;
      flex-wrap: wrap;
    }

    .search-input { width: 240px; }
    .role-select { width: 130px; }
  }

  .table-card {
    margin-top: 16px;

    .table-scroll {
      overflow-x: auto;
      -webkit-overflow-scrolling: touch;
    }

    .pagination-wrap {
      margin-top: 16px;
      display: flex;
      justify-content: flex-end;
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

  .edit-dialog {
    :deep(.el-dialog) { max-width: 95vw; }
  }
}

// 手机端适配
@media (max-width: 767px) {
  .admin-users {
    .toolbar {
      flex-direction: column;
      align-items: stretch;

      .toolbar-left {
        flex-direction: column;
        .search-input, .role-select { width: 100%; }
      }

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
