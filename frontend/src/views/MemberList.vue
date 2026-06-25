<template>
  <div class="member-list-page">
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
          placeholder="搜索姓名或手机号"
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
          新增会员
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
      <el-table-column label="会员卡类型" width="110" align="center">
        <template #default="{ row }">
          <el-tag :type="cardTypeTag(row.cardType)" size="small">
            {{ cardTypeLabel(row.cardType) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="cardStartDate" label="开卡日期" width="120" />
      <el-table-column prop="cardEndDate" label="到期日期" width="120" />
      <el-table-column label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '过期' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
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
      :title="isEdit ? '编辑会员' : '新增会员'"
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
        <el-form-item label="会员卡类型" prop="cardType">
          <el-select v-model="form.cardType" placeholder="请选择会员卡类型" style="width: 100%">
            <el-option label="月卡" value="MONTH" />
            <el-option label="季卡" value="QUARTER" />
            <el-option label="年卡" value="YEAR" />
            <el-option label="终身卡" value="LIFETIME" />
          </el-select>
        </el-form-item>
        <el-form-item label="开卡日期" prop="cardStartDate">
          <el-date-picker
            v-model="form.cardStartDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="到期日期" prop="cardEndDate">
          <el-date-picker
            v-model="form.cardEndDate"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">过期</el-radio>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus, Search, Edit, Delete } from '@element-plus/icons-vue'
import { memberApi } from '@/api'
import TableWrapper from '@/components/TableWrapper.vue'
import type { Member } from '@/types'

const loading = ref(false)
const submitting = ref(false)
const list = ref<Member[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const defaultForm = (): Partial<Member> => ({
  name: '',
  phone: '',
  gender: 1,
  cardType: 'MONTH',
  cardStartDate: '',
  cardEndDate: '',
  status: 1,
})

const form = reactive<Partial<Member>>(defaultForm())

const rules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  gender: [{ required: true, message: '请选择性别', trigger: 'change' }],
  cardType: [{ required: true, message: '请选择会员卡类型', trigger: 'change' }],
  cardStartDate: [{ required: true, message: '请选择开卡日期', trigger: 'change' }],
  cardEndDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }],
}

const genderLabel = (val: number) => {
  const map: Record<number, string> = { 0: '未知', 1: '男', 2: '女' }
  return map[val] ?? '未知'
}

const genderTagType = (val: number) => {
  const map: Record<number, string> = { 0: 'info', 1: '', 2: 'danger' }
  return map[val] ?? 'info'
}

const cardTypeLabel = (val: string) => {
  const map: Record<string, string> = {
    MONTH: '月卡', QUARTER: '季卡', YEAR: '年卡', LIFETIME: '终身卡',
  }
  return map[val] ?? val
}

const cardTypeTag = (val: string) => {
  const map: Record<string, string> = {
    MONTH: '', QUARTER: 'success', YEAR: 'warning', LIFETIME: 'danger',
  }
  return map[val] ?? ''
}

const fetchList = async () => {
  loading.value = true
  try {
    const res = await memberApi.list({
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
  dialogVisible.value = true
}

const openEditDialog = (row: Member) => {
  isEdit.value = true
  editingId.value = row.id
  Object.assign(form, {
    name: row.name,
    phone: row.phone,
    gender: row.gender,
    cardType: row.cardType,
    cardStartDate: row.cardStartDate,
    cardEndDate: row.cardEndDate,
    status: row.status,
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
      if (isEdit.value && editingId.value) {
        await memberApi.update(editingId.value, form)
        ElMessage.success('更新成功')
      } else {
        await memberApi.create(form)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchList()
    } finally {
      submitting.value = false
    }
  })
}

const handleDelete = (row: Member) => {
  ElMessageBox.confirm(`确定要删除会员「${row.name}」吗？删除后不可恢复。`, '删除确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(async () => {
    await memberApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchList()
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped lang="scss">
.member-list-page {
  padding: $content-padding;
}
</style>
