<template>
  <div class="coach-list-page">
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
          placeholder="搜索教练姓名或手机号"
          clearable
          style="width: 260px"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" @click="handleSearch" style="margin-left: 12px">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
      </template>

      <template #toolbar>
        <el-button type="primary" @click="openAddDialog">
          <el-icon><Plus /></el-icon>
          新增教练
        </el-button>
      </template>

      <el-table-column type="index" label="#" width="60" />
      <el-table-column prop="name" label="姓名" min-width="100" />
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column label="性别" width="80" align="center">
        <template #default="{ row }">
          <el-tag :type="genderTagType(row.gender)" size="small">
            {{ genderLabel(row.gender) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="擅长领域" min-width="200">
        <template #default="{ row }">
          <el-tag
            v-for="(tag, i) in parseSpecialties(row.specialty)"
            :key="i"
            size="small"
            style="margin: 2px"
            type="warning"
          >
            {{ tag }}
          </el-tag>
          <span v-if="!row.specialty" class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column label="简介" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          {{ truncateText(row.description, 40) }}
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
            {{ row.status === 1 ? '在职' : '离职' }}
          </el-tag>
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
      :title="isEdit ? '编辑教练' : '新增教练'"
      width="560px"
      :close-on-click-modal="false"
      @closed="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="form.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="未知" :value="0" />
            <el-option label="男" :value="1" />
            <el-option label="女" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="擅长领域" prop="specialty">
          <el-input
            v-model="specialtyInput"
            placeholder="多个领域用逗号分隔，例如：瑜伽,普拉提,搏击"
            @blur="handleSpecialtyBlur"
          />
          <div v-if="specialtyTags.length" class="specialty-tags">
            <el-tag
              v-for="(tag, i) in specialtyTags"
              :key="i"
              closable
              size="small"
              type="warning"
              @close="removeSpecialtyTag(i)"
            >
              {{ tag }}
            </el-tag>
          </div>
        </el-form-item>
        <el-form-item label="简介" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="请输入教练简介"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">在职</el-radio>
            <el-radio :value="0">离职</el-radio>
          </el-radio-group>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { coachApi } from '@/api'
import TableWrapper from '@/components/TableWrapper.vue'
import type { Coach } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const list = ref<Coach[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const specialtyInput = ref('')

const defaultForm = (): Partial<Coach> => ({
  name: '',
  phone: '',
  gender: 1,
  specialty: '',
  description: '',
  status: 1,
})

const form = reactive<Partial<Coach>>(defaultForm())

const specialtyTags = computed(() => {
  return form.specialty
    ? form.specialty.split(',').map((s) => s.trim()).filter(Boolean)
    : []
})

const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
}

const genderLabel = (val: number) => {
  const map: Record<number, string> = { 0: '未知', 1: '男', 2: '女' }
  return map[val] ?? '未知'
}

const genderTagType = (val: number) => {
  const map: Record<number, string> = { 0: 'info', 1: '', 2: 'danger' }
  return map[val] ?? 'info'
}

const parseSpecialties = (val: string | undefined): string[] => {
  if (!val) return []
  return val.split(',').map((s) => s.trim()).filter(Boolean)
}

const truncateText = (text: string | undefined, max: number): string => {
  if (!text) return '-'
  return text.length > max ? text.slice(0, max) + '...' : text
}

const handleSpecialtyBlur = () => {
  if (specialtyInput.value) {
    const current = form.specialty ? form.specialty.split(',').map((s) => s.trim()).filter(Boolean) : []
    const newTags = specialtyInput.value.split(',').map((s) => s.trim()).filter(Boolean)
    const merged = [...new Set([...current, ...newTags])]
    form.specialty = merged.join(',')
    specialtyInput.value = ''
  }
}

const removeSpecialtyTag = (index: number) => {
  const tags = specialtyTags.value.filter((_, i) => i !== index)
  form.specialty = tags.join(',')
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await coachApi.list({
      pageNum: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
    })
    list.value = res.data.records ?? []
    total.value = res.data.total ?? 0
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
  specialtyInput.value = ''
  dialogVisible.value = true
}

const openEditDialog = (row: Coach) => {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    phone: row.phone,
    gender: row.gender,
    specialty: row.specialty,
    description: row.description,
    status: row.status,
  })
  specialtyInput.value = ''
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
      if (isEdit.value && editingId.value) {
        await coachApi.update(editingId.value, form)
        ElMessage.success('更新成功')
      } else {
        await coachApi.create(form)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchList()
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = (row: Coach) => {
  ElMessageBox.confirm(`确定要删除教练「${row.name}」吗？删除后不可恢复。`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await coachApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchList()
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.coach-list-page {
  padding: $content-padding;
}

.specialty-tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.text-muted {
  color: #c0c4cc;
}
</style>
