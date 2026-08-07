<script setup lang="ts">
import { nextTick, onMounted, reactive, ref } from 'vue'
import {
  ElMessage,
  ElMessageBox,
  type FormInstance,
  type FormRules
} from 'element-plus'
import { getDepartmentTree } from '../../api/department'
import {
  createUser,
  getUser,
  getUsers,
  resetUserPassword,
  updateUser,
  updateUserStatus
} from '../../api/user'
import type { DepartmentTreeNode } from '../../types/department'
import type {
  CreateUserReq,
  RoleCode,
  UpdateUserReq,
  UserItem,
  UserQuery,
  UserStatus
} from '../../types/user'

interface UserForm {
  username: string
  password: string
  nickname: string
  departmentId?: number
  roleCodes: RoleCode[]
}

const roles: { value: RoleCode; label: string }[] = [
  { value: 'USER', label: '普通员工' },
  { value: 'ADMIN', label: '管理员' }
]

const loading = ref(false)
const saving = ref(false)
const open = ref(false)
const editId = ref<number>()
const formRef = ref<FormInstance>()
const records = ref<UserItem[]>([])
const total = ref(0)
const departments = ref<DepartmentTreeNode[]>([])

const query = reactive<UserQuery>({
  page: 1,
  size: 10,
  keyword: '',
  departmentId: undefined,
  status: undefined
})

const form = reactive<UserForm>({
  username: '',
  password: '',
  nickname: '',
  departmentId: undefined,
  roleCodes: ['USER']
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { max: 50, message: '账号不能超过50个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 50, message: '密码长度必须为6到50个字符', trigger: 'blur' }
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  departmentId: [{ required: true, message: '请选择部门', trigger: 'change' }],
  roleCodes: [{ type: 'array', required: true, min: 1, message: '至少选择一个角色', trigger: 'change' }]
}

function resetForm(): void {
  Object.assign(form, {
    username: '',
    password: '',
    nickname: '',
    departmentId: undefined,
    roleCodes: ['USER']
  })
}

async function loadUsers(): Promise<void> {
  loading.value = true
  try {
    const data = await getUsers(query)
    records.value = data.records
    total.value = data.total
  } finally {
    loading.value = false
  }
}

async function loadDepartments(): Promise<void> {
  departments.value = await getDepartmentTree()
}

function search(): void {
  query.page = 1
  void loadUsers()
}

function resetQuery(): void {
  Object.assign(query, {
    page: 1,
    size: 10,
    keyword: '',
    departmentId: undefined,
    status: undefined
  })
  void loadUsers()
}

async function openCreate(): Promise<void> {
  editId.value = undefined
  resetForm()
  open.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function openEdit(row: UserItem): Promise<void> {
  const user = await getUser(row.id)
  editId.value = user.id
  Object.assign(form, {
    username: user.username,
    password: '',
    nickname: user.nickname,
    departmentId: user.departmentId,
    roleCodes: user.roleCodes
  })
  open.value = true
  await nextTick()
  formRef.value?.clearValidate()
}

async function save(): Promise<void> {
  if (!formRef.value) {
    return
  }

  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || form.departmentId === undefined) {
    return
  }

  saving.value = true
  try {
    if (editId.value === undefined) {
      const data: CreateUserReq = {
        username: form.username.trim(),
        password: form.password,
        nickname: form.nickname.trim(),
        departmentId: form.departmentId,
        roleCodes: form.roleCodes
      }
      await createUser(data)
      ElMessage.success('新增成功')
    } else {
      const data: UpdateUserReq = {
        nickname: form.nickname.trim(),
        departmentId: form.departmentId,
        roleCodes: form.roleCodes
      }
      await updateUser(editId.value, data)
      ElMessage.success('修改成功')
    }

    open.value = false
    await loadUsers()
  } finally {
    saving.value = false
  }
}

async function changeStatus(row: UserItem): Promise<void> {
  const status: UserStatus = row.status === 'ENABLED' ? 'DISABLED' : 'ENABLED'
  const action = status === 'ENABLED' ? '启用' : '禁用'

  try {
    await ElMessageBox.confirm(`确定${action}账号“${row.username}”吗？`, `${action}确认`, {
      confirmButtonText: action,
      cancelButtonText: '取消',
      type: 'warning'
    })
    await updateUserStatus(row.id, status)
    ElMessage.success(`${action}成功`)
    await loadUsers()
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
  }
}

async function resetPassword(row: UserItem): Promise<void> {
  try {
    const { value } = await ElMessageBox.prompt(
      `为账号“${row.username}”设置新密码`,
      '重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputType: 'password',
        inputValidator: (value) =>
          value.length >= 6 && value.length <= 50 || '密码长度必须为6到50个字符'
      }
    )
    await resetUserPassword(row.id, value)
    ElMessage.success('密码已重置')
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return
    }
  }
}

function formatTime(value: string): string {
  return value ? value.replace('T', ' ') : '-'
}

onMounted(() => {
  void loadUsers()
  void loadDepartments()
})
</script>

<template>
  <section class="page">
    <div class="page-header">
      <div>
        <h1>用户管理</h1>
        <p>维护员工账号、部门和角色</p>
      </div>
      <el-button type="primary" @click="openCreate">新增用户</el-button>
    </div>

    <div class="filters">
      <el-input
        v-model="query.keyword"
        clearable
        placeholder="账号或昵称"
        @keyup.enter="search"
      />
      <el-tree-select
        v-model="query.departmentId"
        clearable
        :data="departments"
        :props="{ value: 'id', label: 'name', children: 'children' }"
        placeholder="全部部门"
      />
      <el-select v-model="query.status" clearable placeholder="全部状态">
        <el-option label="启用" value="ENABLED" />
        <el-option label="禁用" value="DISABLED" />
      </el-select>
      <el-button type="primary" @click="search">查询</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <el-table v-loading="loading" :data="records">
      <el-table-column prop="username" label="账号" min-width="130" />
      <el-table-column prop="nickname" label="昵称" min-width="120" />
      <el-table-column prop="departmentName" label="部门" min-width="150" />
      <el-table-column label="角色" min-width="150">
        <template #default="{ row }">
          <el-tag v-for="role in row.roleCodes" :key="role" class="role-tag">
            {{ role }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ENABLED' ? 'success' : 'info'">
            {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="创建时间" min-width="170">
        <template #default="{ row }">{{ formatTime(row.createdAt) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="210" fixed="right">
        <template #default="{ row }">
          <el-button link type="primary" @click="openEdit(row)">编辑</el-button>
          <el-button link type="warning" @click="changeStatus(row)">
            {{ row.status === 'ENABLED' ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="danger" @click="resetPassword(row)">重置密码</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pager">
      <el-pagination
        v-model:current-page="query.page"
        v-model:page-size="query.size"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @current-change="loadUsers"
        @size-change="search"
      />
    </div>

    <el-dialog
      v-model="open"
      :title="editId === undefined ? '新增用户' : '编辑用户'"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px">
        <el-form-item label="账号" prop="username">
          <el-input v-model="form.username" :disabled="editId !== undefined" />
        </el-form-item>
        <el-form-item v-if="editId === undefined" label="密码" prop="password">
          <el-input v-model="form.password" show-password type="password" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="部门" prop="departmentId">
          <el-tree-select
            v-model="form.departmentId"
            :data="departments"
            :props="{ value: 'id', label: 'name', children: 'children' }"
            check-strictly
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleCodes">
          <el-checkbox-group v-model="form.roleCodes">
            <el-checkbox v-for="role in roles" :key="role.value" :value="role.value">
              {{ role.label }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button :disabled="saving" @click="open = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">确定</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<style scoped>
.page {
  padding: 24px;
}

.page-header,
.filters,
.pager {
  display: flex;
  align-items: center;
}

.page-header {
  justify-content: space-between;
  margin-bottom: 24px;
}

.filters {
  gap: 12px;
  margin-bottom: 16px;
}

.filters :deep(.el-input),
.filters :deep(.el-tree-select),
.filters :deep(.el-select) {
  width: 200px;
}

.pager {
  justify-content: flex-end;
  margin-top: 16px;
}

h1,
p {
  margin: 0;
}

h1 {
  color: var(--ink-1);
  font-size: 24px;
}

p {
  margin-top: 8px;
  color: var(--ink-3);
  font-size: 14px;
}

.role-tag + .role-tag {
  margin-left: 6px;
}
</style>
